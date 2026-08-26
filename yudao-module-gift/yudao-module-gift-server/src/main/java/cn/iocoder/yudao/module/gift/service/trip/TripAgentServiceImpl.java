package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tracer.core.util.TracerFrameworkUtils;
import cn.iocoder.yudao.module.ai.api.chat.AiChatApi;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateRespDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateStreamRespDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageCreateAssistantReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageRespDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationRespDTO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripItineraryDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripItinerarySlotDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripItineraryMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripItinerarySlotMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripPlanMapper;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentResult;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentEvent;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripItinerarySlotResult;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.system.api.area.AreaApi;
import com.baomidou.lock.annotation.Lock4j;
import jakarta.annotation.Resource;
import com.fasterxml.jackson.core.type.TypeReference;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 旅行规划的显式编排器。LLM 仅负责需求抽取和行程组织；状态、缺失字段、事实及引用均由服务端控制。
 */
@Service
@Slf4j
public class TripAgentServiceImpl implements TripAgentService {

    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_GENERATED = 1;
    private static final String INTAKE_ROLE_ID_CONFIG_KEY = "trip.agent.intakeRoleId";
    private static final String COMPOSER_ROLE_ID_CONFIG_KEY = "trip.agent.composerRoleId";
    private static final String STATE_ITINERARY_OVERRIDES = "itineraryOverrides";
    private static final String INTAKE_ACTION_GENERATE = "GENERATE";
    private static final String INTAKE_ACTION_CONTINUE = "CONTINUE";
    private static final int SLOT_RESOLVE_STATUS_PENDING = 0;
    private static final int SLOT_RESOLVE_STATUS_PROCESSING = 1;
    private static final int SLOT_RESOLVE_STATUS_COMPLETED = 2;
    private static final int SLOT_RESOLVE_STATUS_FAILED = 3;
    private static final Set<String> STATE_FIELDS = Set.of("destination", "departure", "startDate", "endDate",
            "days", "travelerCount", "travelerProfile", "budget", "interests", "pace", "constraints");
    private static final Set<String> DAILY_ITINERARY_SLOTS = Set.of("MORNING", "LUNCH", "AFTERNOON", "DINNER",
            "EVENING", "ACCOMMODATION");
    private static final Set<String> ITINERARY_SLOTS = Set.of("MORNING", "LUNCH", "AFTERNOON", "DINNER",
            "EVENING", "ACCOMMODATION", "ARRIVAL", "DEPARTURE");

    @Resource
    private AiChatApi aiChatApi;
    @Resource
    private TripPlanMapper tripPlanMapper;
    @Resource
    private TripItineraryMapper tripItineraryMapper;
    @Resource
    private TripItinerarySlotMapper tripItinerarySlotMapper;
    @Resource
    private TripRunLogService tripRunLogService;
    @Resource
    private ConfigApi configApi;
    @Resource
    private AreaApi areaApi;
    @Resource
    private TripResearchExecutor tripResearchExecutor;
    @Resource
    private Tracer tracer;

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
    public TripAgentResult handleMessage(Long conversationId, Long memberId, String content,
                                         Consumer<TripAgentEvent> eventConsumer) {
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
        Map<String, Object> promptVariables = buildPromptVariables(conversationId, memberId);

        Map<String, Object> state = TripAgentFormatUtils.parseMap(trip.getStateJson());
        sanitizeTravelerProfile(state);
        state.remove("destinationEntityId"); // 兼容已存的旧状态，不再持久化外部实体副本
        String stateBeforeIntake = JsonUtils.toJsonString(state);
        eventConsumer.accept(TripAgentEvent.of("stage", "INTAKE", "正在提取本轮出行需求…"));
        AiChatGenerateRespDTO intakeResponse = generateStream(conversationId, memberId,
                "Current TripState:\n" + JsonUtils.toJsonString(state) + "\n\nUser message:\n" + content,
                "INTAKE", trip.getId(), promptVariables);
        Map<String, Object> intake = TripAgentFormatUtils.parseMap(intakeResponse.getContent());
        mergeExplicitPatch(state, intake.get("patch"));
        applyItineraryPatch(state, intake.get("itinerary_patch"));
        List<String> missingRequired = validateState(state);
        boolean stateChanged = !StrUtil.equals(stateBeforeIntake, JsonUtils.toJsonString(state));
        String intakeAction = normalizeIntakeAction(intake.get("action"));
        boolean generateRequested = INTAKE_ACTION_GENERATE.equals(intakeAction);
        trip.setStateJson(JsonUtils.toJsonString(state));
        trip.setMissingRequiredJson(JsonUtils.toJsonString(missingRequired));
        tripPlanMapper.updateById(trip);
        log.info("[handleMessage][tripId({}) 状态字段({}) 缺失字段({})]",
                trip.getId(), state.keySet(), missingRequired);
        eventConsumer.accept(TripAgentEvent.of("intake_completed", "INTAKE", buildIntakeCompletedContent(state, missingRequired))
                .setMissingRequired(missingRequired));

        if (CollUtil.isNotEmpty(missingRequired)) {
            String question = safeQuestion(intake.get("question"), missingRequired);
            AiChatMessageRespDTO assistant = createTranscriptMessage(conversationId, memberId, question, true);
            log.info("[handleMessage][tripId({}) 返回追问 messageId({})]", trip.getId(), assistant.getId());
            TripAgentResult result = new TripAgentResult().setType("QUESTION").setMessageId(assistant.getId()).setContent(question)
                    .setMissingRequired(missingRequired);
            eventConsumer.accept(TripAgentEvent.of("question", "INTAKE", question).setMessageId(assistant.getId())
                    .setMissingRequired(missingRequired).setSuggestions(buildQuestionSuggestions(state, missingRequired)));
            return result;
        }

        boolean hasCurrentItinerary = trip.getCurrentItineraryId() != null;
        if (!generateRequested && (!hasCurrentItinerary || !stateChanged)) {
            String question = readyQuestion(hasCurrentItinerary, INTAKE_ACTION_CONTINUE.equals(intakeAction));
            AiChatMessageRespDTO assistant = createTranscriptMessage(conversationId, memberId, question, true);
            log.info("[handleMessage][tripId({}) 等待生成确认 messageId({}) hasCurrentItinerary({})]",
                    trip.getId(), assistant.getId(), hasCurrentItinerary);
            TripAgentResult result = new TripAgentResult().setType("QUESTION").setMessageId(assistant.getId()).setContent(question)
                    .setMissingRequired(List.of());
            eventConsumer.accept(TripAgentEvent.of("question", "INTAKE", question).setMessageId(assistant.getId())
                    .setMissingRequired(List.of()).setSuggestions(buildGenerationSuggestions(state, hasCurrentItinerary)));
            return result;
        }

        eventConsumer.accept(TripAgentEvent.of("stage", "COMPOSER", "正在生成行程骨架…"));
        eventConsumer.accept(TripAgentEvent.of("assistant_start", "COMPOSER", null));
        TripAgentStreamParser streamParser = new TripAgentStreamParser(eventConsumer);
        AiChatGenerateRespDTO composerResponse = generateStream(conversationId, memberId,
                buildSkeletonInstruction(state, getCurrentItinerary(trip)),
                "COMPOSER", trip.getId(), promptVariables, streamParser::append);
        Map<String, Object> itinerary = parseItinerarySkeleton(composerResponse.getContent());
        Integer maxVersion = tripItineraryMapper.selectMaxVersionByTripId(trip.getId());
        int version = (maxVersion == null ? 0 : maxVersion) + 1;
        itinerary.put("version", version);
        String displayText = StrUtil.blankToDefault(ObjUtil.toString(itinerary.get("summary")), "已为你生成旅行方案。");
        AiChatMessageRespDTO assistant = createTranscriptMessage(conversationId, memberId, displayText, true);
        TripItineraryDO itineraryDO = new TripItineraryDO();
        itineraryDO.setTripId(trip.getId());
        itineraryDO.setVersion(version);
        itineraryDO.setMessageId(assistant.getId());
        itineraryDO.setContentJson(JsonUtils.toJsonString(itinerary));
        itineraryDO.setCitationIdsJson(JsonUtils.toJsonString(itinerary.get("citation_ids")));
        itineraryDO.setStatus(STATUS_GENERATED);
        tripItineraryMapper.insert(itineraryDO);
        initializeItinerarySlots(itineraryDO, itinerary);
        trip.setCurrentItineraryId(itineraryDO.getId());
        tripPlanMapper.updateById(trip);

        log.info("[handleMessage][tripId({}) 行程 itineraryId({}) version({}) messageId({}) 引用数量({}) 已生成]",
                trip.getId(), itineraryDO.getId(), version, assistant.getId(), ((List<?>) itinerary.get("citation_ids")).size());
        TripAgentResult result = new TripAgentResult().setType("ITINERARY_SKELETON").setMessageId(assistant.getId()).setContent(displayText)
                .setItinerary(itinerary).setMissingRequired(List.of());
        eventConsumer.accept(TripAgentEvent.of("assistant_end", "COMPOSER", null));
        eventConsumer.accept(TripAgentEvent.of("itinerary_skeleton", "COMPOSER", displayText).setMessageId(assistant.getId())
                .setItinerary(itinerary).setMissingRequired(List.of()));
        return result;
    }

    @Override
    public Map<Long, Map<String, Object>> getItineraryMapByMessageIds(Collection<Long> messageIds) {
        if (CollUtil.isEmpty(messageIds)) {
            return Map.of();
        }
        List<TripItineraryDO> itineraries = tripItineraryMapper.selectListByMessageIds(messageIds);
        Map<Long, Map<String, Object>> itineraryMap = new LinkedHashMap<>();
        Map<Long, Map<String, Object>> itineraryMapById = new LinkedHashMap<>();
        itineraries.forEach(itinerary -> {
            Map<String, Object> content = TripAgentFormatUtils.parseMap(itinerary.getContentJson());
            itineraryMap.put(itinerary.getMessageId(), content);
            itineraryMapById.put(itinerary.getId(), content);
        });
        tripItinerarySlotMapper.selectListByItineraryIds(itineraryMapById.keySet()).forEach(slot -> {
            if (!ObjUtil.equal(slot.getResolveStatus(), SLOT_RESOLVE_STATUS_COMPLETED)) {
                return;
            }
            Map<String, Object> itinerary = itineraryMapById.get(slot.getItineraryId());
            if (itinerary != null) {
                mergeResolvedSlot(itinerary, slot);
            }
        });
        itineraryMapById.values().forEach(itinerary -> itinerary.put("citation_ids", collectCitationIds(itinerary)));
        return itineraryMap;
    }

    @Override
    public TripItinerarySlotResult resolveItinerarySlot(Long conversationId, Long memberId, Long messageId,
                                                         Integer day, String slot) {
        TripPlanDO trip = tripPlanMapper.selectByConversationIdAndMemberId(conversationId, memberId);
        TripItineraryDO itineraryDO = tripItineraryMapper.selectByMessageId(messageId);
        if (trip == null || itineraryDO == null || !trip.getId().equals(itineraryDO.getTripId())) {
            throw new IllegalArgumentException("行程骨架不存在或不属于当前会话");
        }
        if (!itineraryDO.getId().equals(trip.getCurrentItineraryId())) {
            throw new IllegalArgumentException("只能补充当前生效的行程骨架");
        }
        Map<String, Object> state = TripAgentFormatUtils.parseMap(trip.getStateJson());
        if (StrUtil.isBlank(ObjUtil.toString(state.get("destination")))) {
            throw new IllegalStateException("行程缺少目的地");
        }
        Map<String, Object> itinerary = TripAgentFormatUtils.parseMap(itineraryDO.getContentJson());
        Map<String, Object> skeletonSlot = findSlot(itinerary, day, slot);
        TripItinerarySlotDO itinerarySlot = getOrCreateItinerarySlot(itineraryDO, day, slot, skeletonSlot);
        if (!tripItinerarySlotMapper.claimForResolve(itinerarySlot.getId(), SLOT_RESOLVE_STATUS_PENDING,
                SLOT_RESOLVE_STATUS_FAILED, SLOT_RESOLVE_STATUS_PROCESSING)) {
            TripItinerarySlotDO currentSlot = tripItinerarySlotMapper.selectById(itinerarySlot.getId());
            return toSlotResult(messageId, currentSlot != null ? currentSlot : itinerarySlot);
        }
        long start = System.currentTimeMillis();
        Long runId = tripRunLogService.create(trip.getId(), "SLOT_RESOLVE", JsonUtils.toJsonString(Map.of(
                "messageId", String.valueOf(messageId), "day", day, "slot", slot)));
        try {
            TripResearchExecutor.SlotResearchResult researchResult = tripResearchExecutor.resolveSlot(trip.getId(), state, slot);
            List<Map<String, Object>> candidates = researchResult.candidates();
            itinerarySlot.setStatus(researchResult.status());
            itinerarySlot.setDetail(researchResult.detail());
            itinerarySlot.setCandidatesJson(JsonUtils.toJsonString(candidates));
            itinerarySlot.setCitationIdsJson(JsonUtils.toJsonString(researchResult.citationIds()));
            itinerarySlot.setResolveStatus(SLOT_RESOLVE_STATUS_COMPLETED);
            tripItinerarySlotMapper.updateById(itinerarySlot);
            tripRunLogService.complete(runId, null, 0L, 0L, 0L, System.currentTimeMillis() - start,
                    JsonUtils.toJsonString(Map.of("toolPlans", researchResult.toolPlans(), "status", researchResult.status(),
                            "candidateCount", candidates.size(), "citationIds", researchResult.citationIds())));
            return toSlotResult(messageId, itinerarySlot);
        } catch (RuntimeException e) {
            itinerarySlot.setStatus("PENDING");
            itinerarySlot.setResolveStatus(SLOT_RESOLVE_STATUS_FAILED);
            itinerarySlot.setDetail("节点补充失败，请重试");
            tripItinerarySlotMapper.updateById(itinerarySlot);
            tripRunLogService.fail(runId, System.currentTimeMillis() - start, e.getMessage());
            throw e;
        }
    }

    private AiChatGenerateRespDTO generateStream(Long conversationId, Long memberId, String content,
                                                 String stage, Long tripId) {
        return generateStream(conversationId, memberId, content, stage, tripId, Map.of(), ignored -> { });
    }

    private AiChatGenerateRespDTO generateStream(Long conversationId, Long memberId, String content,
                                                 String stage, Long tripId,
                                                 Consumer<String> contentConsumer) {
        return generateStream(conversationId, memberId, content, stage, tripId, Map.of(), contentConsumer);
    }

    private AiChatGenerateRespDTO generateStream(Long conversationId, Long memberId, String content,
                                                 String stage, Long tripId, Map<String, Object> promptVariables) {
        return generateStream(conversationId, memberId, content, stage, tripId, promptVariables, ignored -> { });
    }

    private AiChatGenerateRespDTO generateStream(Long conversationId, Long memberId, String content,
                                                 String stage, Long tripId, Map<String, Object> promptVariables,
                                                 Consumer<String> contentConsumer) {
        long start = System.currentTimeMillis();
        Long roleId = getRoleId(stage);
        Long runId = tripRunLogService.create(tripId, stage, JsonUtils.toJsonString(Map.of("content", content)));
        log.info("[generateStream][tripId({}) runId({}) stage({}) 开始调用模型]", tripId, runId, stage);
        StringBuilder output = new StringBuilder();
        Span span = tracer.spanBuilder("trip.agent." + stage.toLowerCase(Locale.ROOT))
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute("trip.agent.stage", stage)
                .setAttribute("trip.id", String.valueOf(tripId))
                .setAttribute("trip.chat.role.id", roleId)
                .setAttribute("trip.llm.stream", true)
                .startSpan();
        try (Scope ignored = span.makeCurrent()) {
            AiChatGenerateRespDTO response = aiChatApi.generateStream(new AiChatGenerateReqDTO()
                    .setConversationId(conversationId).setUserId(memberId).setUserType(UserTypeEnum.MEMBER.getValue())
                    .setRoleId(roleId).setContent(content).setPromptVariables(promptVariables), chunk -> {
                        output.append(chunk.getContent());
                        contentConsumer.accept(chunk.getContent());
                    });
            setModelResponseAttributes(span, response);
            tripRunLogService.complete(runId, response.getModel(), response.getPromptTokens(), response.getCompletionTokens(),
                    response.getTotalTokens(), System.currentTimeMillis() - start,
                    JsonUtils.toJsonString(Map.of("response", output.toString())));
            log.info("[generateStream][tripId({}) runId({}) stage({}) model({}) 耗时({} ms) tokens({}) 调用成功]",
                    tripId, runId, stage, response.getModel(), System.currentTimeMillis() - start, response.getTotalTokens());
            return response;
        } catch (RuntimeException e) {
            TracerFrameworkUtils.onError(e, span);
            tripRunLogService.fail(runId, System.currentTimeMillis() - start, e.getMessage());
            log.error("[generate][tripId({}) runId({}) stage({}) 耗时({} ms) 调用失败]",
                    tripId, runId, stage, System.currentTimeMillis() - start, e);
            throw e;
        } finally {
            span.end();
        }
    }

    private static void setModelResponseAttributes(Span span, AiChatGenerateRespDTO response) {
        if (StrUtil.isNotBlank(response.getModel())) {
            span.setAttribute("trip.llm.model", response.getModel());
        }
        if (response.getPromptTokens() != null) {
            span.setAttribute("trip.llm.usage.prompt_tokens", response.getPromptTokens());
        }
        if (response.getCompletionTokens() != null) {
            span.setAttribute("trip.llm.usage.completion_tokens", response.getCompletionTokens());
        }
        if (response.getTotalTokens() != null) {
            span.setAttribute("trip.llm.usage.total_tokens", response.getTotalTokens());
        }
    }

    private Long getRoleId(String stage) {
        String configKey = "INTAKE".equals(stage) ? INTAKE_ROLE_ID_CONFIG_KEY : COMPOSER_ROLE_ID_CONFIG_KEY;
        String configValue = configApi.getConfigValueByKey(configKey).getCheckedData();
        if (StrUtil.isBlank(configValue)) {
            throw new IllegalStateException("系统配置 " + configKey + " 未配置");
        }
        try {
            return Long.parseLong(configValue.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("系统配置 " + configKey + " 必须是角色编号", e);
        }
    }

    private Map<String, Object> buildPromptVariables(Long conversationId, Long memberId) {
        AiChatConversationRespDTO conversation = aiChatApi.getConversation(conversationId, memberId,
                UserTypeEnum.MEMBER.getValue());
        if (conversation == null) {
            throw new IllegalStateException("旅行会话不存在");
        }
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("provinceName", getAreaName(conversation.getProvinceId()));
        variables.put("cityName", getAreaName(conversation.getCityId()));
        variables.put("districtName", getAreaName(conversation.getDistrictId()));
        return variables;
    }

    private String getAreaName(Long areaId) {
        return areaId != null ? areaApi.getAreaName(areaId).getCheckedData() : "";
    }

    private static String buildSkeletonInstruction(Map<String, Object> state, Map<String, Object> currentItinerary) {
        return "TripState:\n" + JsonUtils.toJsonString(state) + "\n\n"
                + "CurrentItinerary（首次生成时为空；有值时仅作为上一版参考）：\n"
                + JsonUtils.toJsonString(currentItinerary) + "\n\n"
                + "若 TripState.itineraryOverrides 非空，必须将其中每条 SET/REMOVE 意图应用到新行程；未涉及的安排保持合理延续。"
                + "只生成可立即展示的 ItinerarySkeleton，不调用工具，不等待或引用天气、景点、酒店、餐饮、交通等外部数据。"
                + "每一天必须有 slots 数组，按 MORNING、LUNCH、AFTERNOON、DINNER、EVENING、ACCOMMODATION 顺序。"
                + "每个 slot 仅含 slot、label、skeleton、status，status 固定 PENDING；skeleton 是简短的高层安排，不得写实时事实或具体供应商结论。"
                + "transport 必须是对象，含 arrival 和 departure；每项仅含 status=PENDING、skeleton。"
                + "输出 JSON：{summary,daily_itinerary:[{day,title,slots:[{slot,label,skeleton,status}]}],"
                + "transport:{arrival:{status,skeleton},departure:{status,skeleton}},booking_actions:[],warnings:[],citation_ids:[]}。"
                + "citation_ids 必须为空数组。";
    }

    private Map<String, Object> getCurrentItinerary(TripPlanDO trip) {
        if (trip.getCurrentItineraryId() == null) {
            return Map.of();
        }
        TripItineraryDO itinerary = tripItineraryMapper.selectById(trip.getCurrentItineraryId());
        if (itinerary == null || !trip.getId().equals(itinerary.getTripId())) {
            log.warn("[getCurrentItinerary][tripId({}) currentItineraryId({}) 不存在或归属不匹配]",
                    trip.getId(), trip.getCurrentItineraryId());
            return Map.of();
        }
        return TripAgentFormatUtils.parseMap(itinerary.getContentJson());
    }

    private AiChatMessageRespDTO createTranscriptMessage(Long conversationId, Long memberId, String content, boolean assistant) {
        AiChatMessageCreateAssistantReqDTO req = new AiChatMessageCreateAssistantReqDTO();
        req.setConversationId(conversationId);
        req.setUserId(memberId);
        req.setUserType(UserTypeEnum.MEMBER.getValue());
        req.setContent(content);
        return assistant ? aiChatApi.createAssistantMessage(req) : aiChatApi.createUserMessage(req);
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
        if ("travelerProfile".equals(field)) {
            return normalizeTravelerProfile(value);
        }
        if ("interests".equals(field) || "constraints".equals(field)) {
            if (value instanceof List<?> list) {
                return list.stream().map(String::valueOf).filter(StrUtil::isNotBlank).toList();
            }
            return List.of(String.valueOf(value));
        }
        return StrUtil.trim(value.toString());
    }

    private static Map<String, Object> normalizeTravelerProfile(Object value) {
        if (!(value instanceof Map<?, ?> profile)) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        addProfileCount(result, profile, "adultCount");
        addProfileCount(result, profile, "childCount");
        addProfileCount(result, profile, "elderlyCount");
        return result;
    }

    private static void addProfileCount(Map<String, Object> result, Map<?, ?> profile, String key) {
        Integer value = MapUtil.getInt(profile, key);
        if (value != null && value >= 0 && value <= 20) {
            result.put(key, value);
        }
    }

    private static List<Map<String, String>> buildQuestionSuggestions(Map<String, Object> state, List<String> missingRequired) {
        List<Map<String, String>> suggestions = new ArrayList<>();
        if (missingRequired.contains("destination")) {
            addSuggestion(suggestions, "去杭州", "目的地是杭州");
            addSuggestion(suggestions, "去成都", "目的地是成都");
        }
        if (missingRequired.contains("date_or_days")) {
            addSuggestion(suggestions, "玩 3 天", "计划玩3天");
            addSuggestion(suggestions, "玩 5 天", "计划玩5天");
        }
        if (MapUtil.getInt(state, "travelerCount") == null) {
            addSuggestion(suggestions, "2 人出行", "总共2人出行");
            addSuggestion(suggestions, "3 人出行", "总共3人出行");
        }
        if (StrUtil.isBlank(ObjUtil.toString(state.get("budget")))) {
            addSuggestion(suggestions, "人均 ¥1,500", "人均预算1500元");
            addSuggestion(suggestions, "人均 ¥3,000", "人均预算3000元");
        }
        if (!hasValues(state.get("interests"))) {
            addSuggestion(suggestions, "美食与人文", "偏好美食和人文");
            addSuggestion(suggestions, "自然风景", "偏好自然风景");
        }
        return suggestions.stream().limit(5).toList();
    }

    private static List<Map<String, String>> buildGenerationSuggestions(Map<String, Object> state,
                                                                          boolean hasCurrentItinerary) {
        List<Map<String, String>> suggestions = new ArrayList<>();
        addSuggestion(suggestions, hasCurrentItinerary ? "重新生成行程" : "直接生成行程",
                hasCurrentItinerary ? "请重新生成当前行程" : "请直接生成行程");
        addSuggestion(suggestions, "继续补充", "我想继续补充旅行偏好");
        buildQuestionSuggestions(state, List.of()).forEach(suggestion ->
                addSuggestion(suggestions, suggestion.get("label"), suggestion.get("content")));
        return suggestions;
    }

    private static String readyQuestion(boolean hasCurrentItinerary, boolean continueRequested) {
        if (hasCurrentItinerary) {
            return "当前行程已经生成。你可以直接告诉我想调整的内容，或选择重新生成。";
        }
        return continueRequested ? "好的，你还可以补充预算、人数、偏好或约束；准备好后可直接生成行程。"
                : "出行的关键信息已经齐全。你想继续补充偏好，还是现在直接生成行程？";
    }

    private static String normalizeIntakeAction(Object action) {
        String value = StrUtil.trim(ObjUtil.toString(action)).toUpperCase(Locale.ROOT);
        return INTAKE_ACTION_GENERATE.equals(value) || INTAKE_ACTION_CONTINUE.equals(value) ? value : "NONE";
    }

    private static boolean hasValues(Object value) {
        return value instanceof List<?> list && CollUtil.isNotEmpty(list);
    }

    private static void sanitizeTravelerProfile(Map<String, Object> state) {
        Object profile = state.get("travelerProfile");
        if (profile == null) {
            return;
        }
        Map<String, Object> sanitized = normalizeTravelerProfile(profile);
        if (sanitized.isEmpty()) {
            state.remove("travelerProfile");
        } else {
            state.put("travelerProfile", sanitized);
        }
    }

    private static void addSuggestion(List<Map<String, String>> suggestions, String label, String content) {
        if (suggestions.size() < 5) {
            suggestions.add(Map.of("label", label, "content", content));
        }
    }

    @SuppressWarnings("unchecked")
    private static void applyItineraryPatch(Map<String, Object> state, Object itineraryPatchValue) {
        if (!(itineraryPatchValue instanceof Map<?, ?> itineraryPatch)
                || !(itineraryPatch.get("operations") instanceof List<?> operations)) {
            return;
        }
        Map<String, Map<String, Object>> overrides = new LinkedHashMap<>();
        Object currentValue = state.get(STATE_ITINERARY_OVERRIDES);
        if (currentValue instanceof List<?> currentOverrides) {
            currentOverrides.forEach(value -> addItineraryOverride(overrides, value));
        }
        operations.forEach(operation -> addItineraryOverride(overrides, operation));
        if (overrides.isEmpty() && !(currentValue instanceof List<?>)) {
            return;
        }
        state.put(STATE_ITINERARY_OVERRIDES, new ArrayList<>(overrides.values()));
    }

    private static void addItineraryOverride(Map<String, Map<String, Object>> overrides, Object value) {
        if (!(value instanceof Map<?, ?> operation)) {
            return;
        }
        String op = StrUtil.trim(ObjUtil.toString(operation.get("op"))).toUpperCase(Locale.ROOT);
        Integer day = MapUtil.getInt(operation, "day");
        String slot = StrUtil.trim(ObjUtil.toString(operation.get("slot"))).toUpperCase(Locale.ROOT);
        if (!ITINERARY_SLOTS.contains(slot) || day == null || !isValidOverrideTarget(day, slot)) {
            return;
        }
        String key = day + ":" + slot;
        if ("REMOVE".equals(op)) {
            overrides.remove(key);
            return;
        }
        String instruction = StrUtil.trim(ObjUtil.toString(operation.get("instruction")));
        if ("SET".equals(op) && StrUtil.isNotBlank(instruction)) {
            Map<String, Object> override = new LinkedHashMap<>();
            override.put("day", day);
            override.put("slot", slot);
            override.put("instruction", instruction);
            overrides.put(key, override);
        }
    }

    private static boolean isValidOverrideTarget(Integer day, String slot) {
        return "ARRIVAL".equals(slot) || "DEPARTURE".equals(slot)
                ? day == 0 : day >= 1 && day <= 30 && DAILY_ITINERARY_SLOTS.contains(slot);
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

    private static String buildIntakeCompletedContent(Map<String, Object> state, List<String> missingRequired) {
        List<String> values = new ArrayList<>();
        String destination = StrUtil.trim(ObjUtil.toString(state.get("destination")));
        if (StrUtil.isNotBlank(destination)) {
            values.add(destination);
        }
        Integer days = MapUtil.getInt(state, "days");
        if (days != null) {
            values.add(days + " 天");
        }
        Integer travelerCount = MapUtil.getInt(state, "travelerCount");
        if (travelerCount != null) {
            values.add(travelerCount + " 人");
        }
        String summary = CollUtil.isEmpty(values) ? "当前需求" : String.join(" · ", values);
        return CollUtil.isEmpty(missingRequired) ? "需求已整理：" + summary : "已整理：" + summary;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseItinerarySkeleton(String content) {
        Map<String, Object> itinerary = TripAgentFormatUtils.parseMap(content);
        if (StrUtil.isBlank(ObjUtil.toString(itinerary.get("summary"))) || !(itinerary.get("daily_itinerary") instanceof List<?>)) {
            throw new IllegalStateException("行程生成结果不符合约定 JSON");
        }
        normalizeSkeleton(itinerary);
        itinerary.put("citation_ids", List.of());
        return itinerary;
    }

    @SuppressWarnings("unchecked")
    private static void normalizeSkeleton(Map<String, Object> itinerary) {
        List<?> days = (List<?>) itinerary.get("daily_itinerary");
        for (Object day : days) {
            if (!(day instanceof Map<?, ?> rawDay)) {
                continue;
            }
            Map<String, Object> dayMap = (Map<String, Object>) rawDay;
            if (!(dayMap.get("slots") instanceof List<?>)) {
                List<Map<String, Object>> slots = new ArrayList<>();
                for (String slot : List.of("MORNING", "LUNCH", "AFTERNOON", "DINNER", "EVENING", "ACCOMMODATION")) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("slot", slot);
                    item.put("label", slotLabel(slot));
                    item.put("skeleton", "待补充");
                    item.put("status", "PENDING");
                    slots.add(item);
                }
                dayMap.put("slots", slots);
            } else {
                ((List<?>) dayMap.get("slots")).forEach(slot -> {
                    if (slot instanceof Map<?, ?> rawSlot) {
                        Map<String, Object> slotMap = (Map<String, Object>) rawSlot;
                        slotMap.putIfAbsent("status", "PENDING");
                        slotMap.putIfAbsent("label", slotLabel(ObjUtil.toString(slotMap.get("slot"))));
                        slotMap.putIfAbsent("skeleton", "待补充");
                    }
                });
            }
        }
        if (!(itinerary.get("transport") instanceof Map<?, ?>)) {
            Map<String, Object> transport = new LinkedHashMap<>();
            transport.put("arrival", Map.of("status", "PENDING", "skeleton", "抵达交通待确认"));
            transport.put("departure", Map.of("status", "PENDING", "skeleton", "返程交通待确认"));
            itinerary.put("transport", transport);
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeItinerarySlots(TripItineraryDO itinerary, Map<String, Object> itineraryContent) {
        Object dailyItinerary = itineraryContent.get("daily_itinerary");
        if (dailyItinerary instanceof List<?> days) {
            for (Object dayItem : days) {
                if (!(dayItem instanceof Map<?, ?> dayMap)) {
                    continue;
                }
                Integer day = MapUtil.getInt(dayMap, "day");
                Object slots = dayMap.get("slots");
                if (day == null || !(slots instanceof List<?> slotList)) {
                    continue;
                }
                for (Object slotItem : slotList) {
                    if (slotItem instanceof Map<?, ?> rawSlot) {
                        createItinerarySlotIfAbsent(itinerary, day, (Map<String, Object>) rawSlot);
                    }
                }
            }
        }
        Object transport = itineraryContent.get("transport");
        if (transport instanceof Map<?, ?> transportMap) {
            createTransportSlotIfAbsent(itinerary, "ARRIVAL", transportMap.get("arrival"));
            createTransportSlotIfAbsent(itinerary, "DEPARTURE", transportMap.get("departure"));
        }
    }

    @SuppressWarnings("unchecked")
    private void createTransportSlotIfAbsent(TripItineraryDO itinerary, String slot, Object value) {
        if (value instanceof Map<?, ?> transportSlot) {
            Map<String, Object> slotData = new LinkedHashMap<>((Map<String, Object>) transportSlot);
            slotData.put("slot", slot);
            createItinerarySlotIfAbsent(itinerary, 0, slotData);
        }
    }

    private TripItinerarySlotDO getOrCreateItinerarySlot(TripItineraryDO itinerary, Integer day, String slot,
                                                           Map<String, Object> skeletonSlot) {
        String normalizedSlot = StrUtil.trim(slot).toUpperCase(Locale.ROOT);
        TripItinerarySlotDO result = tripItinerarySlotMapper.selectByItineraryIdAndDayAndSlot(itinerary.getId(), day, normalizedSlot);
        if (result != null) {
            return result;
        }
        Map<String, Object> slotData = new LinkedHashMap<>(skeletonSlot);
        slotData.put("slot", normalizedSlot);
        createItinerarySlotIfAbsent(itinerary, day, slotData);
        result = tripItinerarySlotMapper.selectByItineraryIdAndDayAndSlot(itinerary.getId(), day, normalizedSlot);
        if (result == null) {
            throw new IllegalStateException("行程节点初始化失败");
        }
        return result;
    }

    private void createItinerarySlotIfAbsent(TripItineraryDO itinerary, Integer day, Map<String, Object> slotData) {
        String slot = StrUtil.trim(ObjUtil.toString(slotData.get("slot"))).toUpperCase(Locale.ROOT);
        if (!ITINERARY_SLOTS.contains(slot)) {
            return;
        }
        TripItinerarySlotDO itinerarySlot = new TripItinerarySlotDO();
        itinerarySlot.setTenantId(itinerary.getTenantId());
        itinerarySlot.setItineraryId(itinerary.getId());
        itinerarySlot.setDay(day);
        itinerarySlot.setSlot(slot);
        itinerarySlot.setSkeleton(StrUtil.blankToDefault(ObjUtil.toString(slotData.get("skeleton")), "待补充"));
        itinerarySlot.setStatus(StrUtil.blankToDefault(ObjUtil.toString(slotData.get("status")), "PENDING"));
        itinerarySlot.setResolveStatus(SLOT_RESOLVE_STATUS_PENDING);
        tripItinerarySlotMapper.insertIgnore(itinerarySlot);
    }

    private static TripItinerarySlotResult toSlotResult(Long messageId, TripItinerarySlotDO slot) {
        String detail = slot.getDetail();
        if (ObjUtil.equal(slot.getResolveStatus(), SLOT_RESOLVE_STATUS_PROCESSING) && StrUtil.isBlank(detail)) {
            detail = "节点正在补充中";
        }
        return new TripItinerarySlotResult().setMessageId(messageId).setDay(slot.getDay()).setSlot(slot.getSlot())
                .setStatus(slot.getStatus()).setDetail(detail).setCandidates(parseCandidates(slot.getCandidatesJson()))
                .setCitationIds(parseCitationIds(slot.getCitationIdsJson()));
    }

    private static void mergeResolvedSlot(Map<String, Object> itinerary, TripItinerarySlotDO slot) {
        try {
            Map<String, Object> target = findSlot(itinerary, slot.getDay(), slot.getSlot());
            target.put("status", slot.getStatus());
            target.put("detail", slot.getDetail());
            target.put("candidates", parseCandidates(slot.getCandidatesJson()));
            target.put("citationIds", parseCitationIds(slot.getCitationIdsJson()));
        } catch (IllegalArgumentException e) {
            log.warn("[mergeResolvedSlot][itineraryId({}) day({}) slot({}) 节点不存在]",
                    slot.getItineraryId(), slot.getDay(), slot.getSlot());
        }
    }

    private static List<Map<String, Object>> parseCandidates(String candidatesJson) {
        if (StrUtil.isBlank(candidatesJson)) {
            return List.of();
        }
        List<Map<String, Object>> candidates = JsonUtils.parseObjectQuietly(candidatesJson,
                new TypeReference<List<Map<String, Object>>>() { });
        return candidates != null ? candidates : List.of();
    }

    private static List<String> parseCitationIds(String citationIdsJson) {
        if (StrUtil.isBlank(citationIdsJson)) {
            return List.of();
        }
        List<String> citationIds = JsonUtils.parseObjectQuietly(citationIdsJson, new TypeReference<List<String>>() { });
        return citationIds != null ? citationIds : List.of();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> findSlot(Map<String, Object> itinerary, Integer day, String slot) {
        if (day == 0 && (StrUtil.equalsIgnoreCase(slot, "ARRIVAL") || StrUtil.equalsIgnoreCase(slot, "DEPARTURE"))) {
            Object transport = itinerary.get("transport");
            if (transport instanceof Map<?, ?> transportMap) {
                Object transportSlot = transportMap.get(StrUtil.equalsIgnoreCase(slot, "ARRIVAL") ? "arrival" : "departure");
                if (transportSlot instanceof Map<?, ?> result) {
                    return (Map<String, Object>) result;
                }
            }
            throw new IllegalArgumentException("交通节点不存在");
        }
        Object dailyItinerary = itinerary.get("daily_itinerary");
        if (!(dailyItinerary instanceof List<?> days)) {
            throw new IllegalArgumentException("行程骨架格式错误");
        }
        for (Object item : days) {
            if (!(item instanceof Map<?, ?> rawDay) || !ObjUtil.equals(day, MapUtil.getInt((Map<?, ?>) rawDay, "day"))) {
                continue;
            }
            Object slots = rawDay.get("slots");
            if (slots instanceof List<?> slotList) {
                for (Object slotItem : slotList) {
                    if (slotItem instanceof Map<?, ?> rawSlot && StrUtil.equalsIgnoreCase(slot, ObjUtil.toString(rawSlot.get("slot")))) {
                        return (Map<String, Object>) rawSlot;
                    }
                }
            }
        }
        throw new IllegalArgumentException("行程节点不存在");
    }

    @SuppressWarnings("unchecked")
    private static List<String> collectCitationIds(Map<String, Object> itinerary) {
        List<String> result = new ArrayList<>();
        Object dailyItinerary = itinerary.get("daily_itinerary");
        if (dailyItinerary instanceof List<?> days) {
            for (Object day : days) {
                if (day instanceof Map<?, ?> dayMap && dayMap.get("slots") instanceof List<?> slots) {
                    for (Object slot : slots) {
                        if (slot instanceof Map<?, ?> slotMap && slotMap.get("citationIds") instanceof List<?> ids) {
                            ids.forEach(id -> result.add(String.valueOf(id)));
                        }
                    }
                }
            }
        }
        return result.stream().distinct().toList();
    }

    private static String slotLabel(String slot) {
        return switch (StrUtil.nullToDefault(slot, "").toUpperCase(java.util.Locale.ROOT)) {
            case "MORNING" -> "上午";
            case "LUNCH" -> "午餐";
            case "AFTERNOON" -> "下午";
            case "DINNER" -> "晚餐";
            case "EVENING" -> "晚上";
            case "ACCOMMODATION" -> "住宿";
            default -> "待补充";
        };
    }

}
