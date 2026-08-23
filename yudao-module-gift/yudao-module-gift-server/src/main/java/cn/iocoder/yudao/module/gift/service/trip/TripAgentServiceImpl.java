package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.api.chat.AiChatApi;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateRespDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageCreateAssistantReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageRespDTO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripFactDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripItineraryDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripFactMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripItineraryMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripPlanMapper;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentResult;
import com.baomidou.lock.annotation.Lock4j;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 旅行规划的显式编排器。LLM 仅负责需求抽取和行程组织；状态、缺失字段、事实及引用均由服务端控制。
 */
@Service
@Slf4j
public class TripAgentServiceImpl implements TripAgentService {

    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_GENERATED = 1;
    private static final Set<String> STATE_FIELDS = Set.of("destination", "departure", "startDate", "endDate",
            "days", "travelerCount", "budget", "interests", "pace", "constraints");

    private static final String INTAKE_INSTRUCTION = """
            你是旅行需求抽取器。只返回 JSON，不要 Markdown：
            {\"patch\":{...},\"question\":\"...或null\"}。
            patch 只允许 destination, departure, startDate, endDate, days, travelerCount, budget, interests, pace, constraints。
            只能写用户本轮明确说出的事实，禁止猜测或补全。question 最多一个，且只在缺少关键信息时提出。
            日期使用 yyyy-MM-dd，days、travelerCount 使用数字，interests/constraints 使用字符串数组。
            """;
    private static final String COMPOSER_INSTRUCTION = """
            你是旅行行程组织器。仅依据输入中的 TripState 和 VerifiedFacts，返回 JSON，不要 Markdown：
            {\"summary\":\"...\",\"daily_itinerary\":[{\"day\":1,\"title\":\"...\",\"activities\":[\"...\"]}],
            \"transport\":[\"...\"],\"booking_actions\":[\"...\"],\"warnings\":[\"...\"],\"citation_ids\":[\"...\"]}。
            不得编造天气、营业时间、预约、交通班次、价格库存或签证规则；没有 VerifiedFacts 时把它们写为“待确认”。
            citation_ids 只能引用输入 VerifiedFacts 中提供的 id。
            """;

    @Resource
    private AiChatApi aiChatApi;
    @Resource
    private TripPlanMapper tripPlanMapper;
    @Resource
    private TripFactMapper tripFactMapper;
    @Resource
    private TripItineraryMapper tripItineraryMapper;
    @Resource
    private TripRunLogService tripRunLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTrip(Long conversationId, Long memberId) {
        TripPlanDO existing = tripPlanMapper.selectByConversationIdAndMemberId(conversationId, memberId);
        if (existing != null) {
            return;
        }
        TripPlanDO trip = new TripPlanDO();
        trip.setConversationId(conversationId);
        trip.setMemberId(memberId);
        trip.setStateJson(JsonUtils.toJsonString(new LinkedHashMap<>()));
        trip.setMissingRequiredJson(JsonUtils.toJsonString(List.of("destination", "date_or_days")));
        trip.setStatus(STATUS_ACTIVE);
        tripPlanMapper.insert(trip);
        log.info("[createTrip][tripId({}) conversationId({}) memberId({}) 初始化成功]",
                trip.getId(), conversationId, memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = {"#conversationId"}, expire = 30000, acquireTimeout = 3000)
    public TripAgentResult handleMessage(Long conversationId, Long memberId, String content) {
        TripPlanDO trip = tripPlanMapper.selectByConversationIdAndMemberId(conversationId, memberId);
        if (trip == null) {
            log.info("[handleMessage][conversationId({}) memberId({}) 缺少旅行状态，开始兼容初始化]",
                    conversationId, memberId);
            createTrip(conversationId, memberId);
            trip = tripPlanMapper.selectByConversationIdAndMemberId(conversationId, memberId);
        }
        log.info("[handleMessage][tripId({}) conversationId({}) memberId({}) 开始编排]",
                trip.getId(), conversationId, memberId);
        createTranscriptMessage(conversationId, memberId, content, false);

        Map<String, Object> state = parseMap(trip.getStateJson());
        AiChatGenerateRespDTO intakeResponse = generate(conversationId, memberId, INTAKE_INSTRUCTION,
                "Current TripState:\n" + JsonUtils.toJsonString(state) + "\n\nUser message:\n" + content, "INTAKE", trip.getId());
        Map<String, Object> intake = parseMap(intakeResponse.getContent());
        mergeExplicitPatch(state, intake.get("patch"));
        List<String> missingRequired = validateState(state);
        trip.setStateJson(JsonUtils.toJsonString(state));
        trip.setMissingRequiredJson(JsonUtils.toJsonString(missingRequired));
        tripPlanMapper.updateById(trip);
        log.info("[handleMessage][tripId({}) 状态字段({}) 缺失字段({})]",
                trip.getId(), state.keySet(), missingRequired);

        if (CollUtil.isNotEmpty(missingRequired)) {
            String question = safeQuestion(intake.get("question"), missingRequired);
            AiChatMessageRespDTO assistant = createTranscriptMessage(conversationId, memberId, question, true);
            log.info("[handleMessage][tripId({}) 返回追问 messageId({})]", trip.getId(), assistant.getId());
            return new TripAgentResult().setType("QUESTION").setMessageId(assistant.getId()).setContent(question)
                    .setMissingRequired(missingRequired);
        }

        // Provider adapters will write only verified, non-expired facts. An empty list is intentional: the composer
        // must mark volatile information as pending instead of fabricating it.
        List<TripFactDO> facts = tripFactMapper.selectActiveByTripId(trip.getId());
        log.info("[handleMessage][tripId({}) 有效事实数量({})]", trip.getId(), facts.size());
        List<Map<String, Object>> verifiedFacts = facts.stream().map(fact -> Map.<String, Object>of(
                "id", String.valueOf(fact.getId()), "key", fact.getFactKey(), "value", parseMap(fact.getValueJson()),
                "source_id", String.valueOf(fact.getSourceId()))).toList();
        AiChatGenerateRespDTO composerResponse = generate(conversationId, memberId, COMPOSER_INSTRUCTION,
                "TripState:\n" + JsonUtils.toJsonString(state) + "\n\nVerifiedFacts:\n"
                        + JsonUtils.toJsonString(verifiedFacts), "COMPOSER", trip.getId());
        Map<String, Object> itinerary = parseItinerary(composerResponse.getContent(), facts);
        TripItineraryDO itineraryDO = new TripItineraryDO();
        itineraryDO.setTripId(trip.getId());
        itineraryDO.setContentJson(JsonUtils.toJsonString(itinerary));
        itineraryDO.setCitationIdsJson(JsonUtils.toJsonString(itinerary.get("citation_ids")));
        itineraryDO.setStatus(STATUS_GENERATED);
        tripItineraryMapper.insert(itineraryDO);

        String displayText = StrUtil.blankToDefault(ObjUtil.toString(itinerary.get("summary")), "已为你生成旅行方案。");
        AiChatMessageRespDTO assistant = createTranscriptMessage(conversationId, memberId, displayText, true);
        log.info("[handleMessage][tripId({}) 行程 itineraryId({}) messageId({}) 引用数量({}) 已生成]",
                trip.getId(), itineraryDO.getId(), assistant.getId(), ((List<?>) itinerary.get("citation_ids")).size());
        return new TripAgentResult().setType("ITINERARY").setMessageId(assistant.getId()).setContent(displayText)
                .setItinerary(itinerary).setMissingRequired(List.of());
    }

    private AiChatGenerateRespDTO generate(Long conversationId, Long memberId, String instruction, String content,
                                           String stage, Long tripId) {
        long start = System.currentTimeMillis();
        Long runId = tripRunLogService.create(tripId, stage);
        log.info("[generate][tripId({}) runId({}) stage({}) 开始调用模型]", tripId, runId, stage);
        try {
            AiChatGenerateRespDTO response = aiChatApi.generate(new AiChatGenerateReqDTO()
                    .setConversationId(conversationId).setUserId(memberId).setUserType(UserTypeEnum.MEMBER.getValue())
                    .setInstruction(instruction).setContent(content));
            tripRunLogService.complete(runId, response.getModel(), response.getPromptTokens(), response.getCompletionTokens(),
                    response.getTotalTokens(), System.currentTimeMillis() - start);
            log.info("[generate][tripId({}) runId({}) stage({}) model({}) 耗时({} ms) tokens({}) 调用成功]",
                    tripId, runId, stage, response.getModel(), System.currentTimeMillis() - start, response.getTotalTokens());
            return response;
        } catch (RuntimeException e) {
            tripRunLogService.fail(runId, System.currentTimeMillis() - start, e.getMessage());
            log.error("[generate][tripId({}) runId({}) stage({}) 耗时({} ms) 调用失败]",
                    tripId, runId, stage, System.currentTimeMillis() - start, e);
            throw e;
        }
    }

    private AiChatMessageRespDTO createTranscriptMessage(Long conversationId, Long memberId, String content, boolean assistant) {
        AiChatMessageCreateAssistantReqDTO req = new AiChatMessageCreateAssistantReqDTO();
        req.setConversationId(conversationId);
        req.setUserId(memberId);
        req.setUserType(UserTypeEnum.MEMBER.getValue());
        req.setContent(content);
        return assistant ? aiChatApi.createAssistantMessage(req) : aiChatApi.createUserMessage(req);
    }

    private static Map<String, Object> parseMap(String content) {
        Map<String, Object> parsed = JsonUtils.parseMap(content);
        return parsed != null ? new LinkedHashMap<>(parsed) : new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private static void mergeExplicitPatch(Map<String, Object> state, Object patchValue) {
        if (!(patchValue instanceof Map<?, ?> patch)) {
            return;
        }
        patch.forEach((key, value) -> {
            if (key instanceof String field && STATE_FIELDS.contains(field) && value != null) {
                state.put(field, normalizeField(field, value));
            }
        });
    }

    private static Object normalizeField(String field, Object value) {
        if ("days".equals(field) || "travelerCount".equals(field)) {
            return Integer.parseInt(value.toString());
        }
        if ("interests".equals(field) || "constraints".equals(field)) {
            if (value instanceof List<?> list) {
                return list.stream().map(String::valueOf).filter(StrUtil::isNotBlank).toList();
            }
            return List.of(String.valueOf(value));
        }
        return StrUtil.trim(value.toString());
    }

    private static List<String> validateState(Map<String, Object> state) {
        List<String> missing = new ArrayList<>();
        if (StrUtil.isBlank(ObjUtil.toString(state.get("destination")))) {
            missing.add("destination");
        }
        Integer days = MapUtil.getInt(state, "days");
        if (days != null && (days < 1 || days > 30)) {
            state.remove("days");
            days = null;
        }
        String startDate = ObjUtil.toString(state.get("startDate"));
        String endDate = ObjUtil.toString(state.get("endDate"));
        try {
            if (StrUtil.isNotBlank(startDate) && StrUtil.isNotBlank(endDate)
                    && LocalDate.parse(endDate).isBefore(LocalDate.parse(startDate))) {
                state.remove("endDate");
                endDate = null;
            }
        } catch (RuntimeException e) {
            state.remove("startDate");
            state.remove("endDate");
            startDate = null;
            endDate = null;
        }
        if (days == null && StrUtil.isBlank(startDate) && StrUtil.isBlank(endDate)) {
            missing.add("date_or_days");
        }
        Integer travelers = MapUtil.getInt(state, "travelerCount");
        if (travelers != null && (travelers < 1 || travelers > 20)) {
            state.remove("travelerCount");
        }
        return missing;
    }

    private static String safeQuestion(Object question, List<String> missing) {
        String value = question != null ? StrUtil.trim(question.toString()) : null;
        if (StrUtil.isNotBlank(value)) {
            return value;
        }
        if (missing.contains("destination")) {
            return "你想去哪里旅行？";
        }
        return "你的出行日期，或计划玩几天？";
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseItinerary(String content, List<TripFactDO> facts) {
        Map<String, Object> itinerary = parseMap(content);
        if (StrUtil.isBlank(ObjUtil.toString(itinerary.get("summary"))) || !(itinerary.get("daily_itinerary") instanceof List<?>)) {
            throw new IllegalStateException("行程生成结果不符合约定 JSON");
        }
        Set<String> factIds = new LinkedHashSet<>();
        facts.forEach(fact -> factIds.add(String.valueOf(fact.getId())));
        Object citationValue = itinerary.get("citation_ids");
        List<String> citationIds = citationValue instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of();
        if (!factIds.containsAll(citationIds)) {
            throw new IllegalStateException("行程引用了未验证事实");
        }
        itinerary.put("citation_ids", citationIds);
        return itinerary;
    }

}
