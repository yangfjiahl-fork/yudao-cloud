package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.ai.api.chat.AiChatApi;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationUpdateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageCreateAssistantReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageRespDTO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripItineraryDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripItinerarySlotDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripItineraryMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripItinerarySlotMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripPlanMapper;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentEvent;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentResult;
import com.baomidou.lock.annotation.Lock4j;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/** Managed Agents 新链路：读取既有 TripState，生成并按原结构保存完整行程。 */
@Service
@Slf4j
public class ManagedTripAgentServiceImpl implements ManagedTripAgentService {

    private static final int STATUS_GENERATED = 1;
    private static final int SLOT_RESOLVE_STATUS_PENDING = 0;
    private static final int SLOT_RESOLVE_STATUS_COMPLETED = 2;
    private static final Set<String> ITINERARY_SLOTS = Set.of("MORNING", "LUNCH", "AFTERNOON", "DINNER",
            "EVENING", "ACCOMMODATION", "ARRIVAL", "DEPARTURE", "TRIP_OVERVIEW", "DAY_OVERVIEW");

    @Resource
    private TripPlanMapper tripPlanMapper;
    @Resource
    private TripItineraryMapper tripItineraryMapper;
    @Resource
    private TripItinerarySlotMapper tripItinerarySlotMapper;
    @Resource
    private ManagedTripPlannerService managedTripPlannerService;
    @Resource
    private AiChatApi aiChatApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = {"#conversationId"}, expire = 360000, acquireTimeout = 3000)
    public TripAgentResult handleMessage(Long conversationId, Long memberId, String content,
                                         Consumer<TripAgentEvent> eventConsumer) {
        TripPlanDO trip = tripPlanMapper.selectByConversationIdAndMemberId(conversationId, memberId);
        if (trip == null) {
            throw new IllegalArgumentException("旅行状态不存在，请先创建旅行会话");
        }
        createTranscriptMessage(conversationId, memberId, content, false);
        Map<String, Object> state = TripAgentFormatUtils.parseMap(trip.getStateJson());
        List<String> missingRequired = TripAgentServiceImpl.validateState(state);
        if (CollUtil.isNotEmpty(missingRequired)) {
            String question = "请先补充" + String.join("、", missingRequired) + "，再生成托管行程。";
            AiChatMessageRespDTO assistant = createTranscriptMessage(conversationId, memberId, question, true);
            eventConsumer.accept(TripAgentEvent.of("intake_completed", "INTAKE", "需求尚未补充完整")
                    .setMissingRequired(missingRequired).setSuggestions(List.of()));
            eventConsumer.accept(TripAgentEvent.of("question", "INTAKE", question)
                    .setMessageId(assistant.getId()).setMissingRequired(missingRequired).setSuggestions(List.of()));
            return new TripAgentResult().setType("QUESTION").setMessageId(assistant.getId()).setContent(question)
                    .setMissingRequired(missingRequired);
        }

        eventConsumer.accept(TripAgentEvent.of("intake_completed", "INTAKE", "需求已整理，开始生成行程。")
                .setMissingRequired(List.of()).setSuggestions(List.of()));
        eventConsumer.accept(TripAgentEvent.of("stage", "ASSEMBLE", "正在启动托管旅行 Agent…"));
        Map<String, Object> itinerary = managedTripPlannerService.plan(trip, state, content,
                progress -> eventConsumer.accept(TripAgentEvent.of("stage", "ASSEMBLE", progress)));
        Integer maxVersion = tripItineraryMapper.selectMaxVersionByTripId(trip.getId());
        int version = (maxVersion == null ? 0 : maxVersion) + 1;
        itinerary.put("version", version);
        String displayText = StrUtil.blankToDefault(text(itinerary.get("summary")), "已为你生成旅行方案。");
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
        updateConversationTitle(conversationId, memberId, state);

        log.info("[handleMessage][Managed Agents tripId({}) itineraryId({}) version({}) 生成完成]",
                trip.getId(), itineraryDO.getId(), version);
        TripAgentResult result = new TripAgentResult().setType("ITINERARY_SKELETON").setMessageId(assistant.getId())
                .setContent(displayText).setItinerary(itinerary).setMissingRequired(List.of());
        eventConsumer.accept(TripAgentEvent.of("itinerary_skeleton", "ASSEMBLE", displayText)
                .setMessageId(assistant.getId()).setItinerary(itinerary).setMissingRequired(List.of())
                .setSuggestions(List.of()));
        return result;
    }

    private AiChatMessageRespDTO createTranscriptMessage(Long conversationId, Long memberId, String content,
                                                          boolean assistant) {
        AiChatMessageCreateAssistantReqDTO req = new AiChatMessageCreateAssistantReqDTO();
        req.setConversationId(conversationId);
        req.setUserId(memberId);
        req.setUserType(UserTypeEnum.MEMBER.getValue());
        req.setContent(content);
        return assistant ? aiChatApi.createAssistantMessage(req) : aiChatApi.createUserMessage(req);
    }

    private void updateConversationTitle(Long conversationId, Long memberId, Map<String, Object> state) {
        String startDate = text(state.get("startDate"));
        String destination = text(state.get("destination"));
        if (StrUtil.isBlank(startDate) || StrUtil.isBlank(destination)) {
            return;
        }
        AiChatConversationUpdateReqDTO req = new AiChatConversationUpdateReqDTO();
        req.setId(conversationId);
        req.setUserId(memberId);
        req.setUserType(UserTypeEnum.MEMBER.getValue());
        req.setTitle(startDate + " " + destination);
        aiChatApi.updateConversation(req);
    }

    @SuppressWarnings("unchecked")
    private void initializeItinerarySlots(TripItineraryDO itinerary, Map<String, Object> content) {
        createSlotIfPresent(itinerary, 0, content.get("overview"));
        if (content.get("daily_itinerary") instanceof List<?> days) {
            for (Object item : days) {
                if (!(item instanceof Map<?, ?> day)) {
                    continue;
                }
                Integer dayNumber = MapUtil.getInt(day, "day");
                createSlotIfPresent(itinerary, dayNumber, day.get("overview"));
                if (day.get("slots") instanceof List<?> slots) {
                    for (Object slot : slots) {
                        createSlotIfPresent(itinerary, dayNumber, slot);
                    }
                }
            }
        }
        if (content.get("transport") instanceof Map<?, ?> transport) {
            createTransportSlot(itinerary, "ARRIVAL", transport.get("arrival"));
            createTransportSlot(itinerary, "DEPARTURE", transport.get("departure"));
        }
    }

    @SuppressWarnings("unchecked")
    private void createSlotIfPresent(TripItineraryDO itinerary, Integer day, Object value) {
        if (day == null || !(value instanceof Map<?, ?> raw)) {
            return;
        }
        Map<String, Object> slot = new LinkedHashMap<>((Map<String, Object>) raw);
        String slotName = text(slot.get("slot")).toUpperCase(Locale.ROOT);
        if (!ITINERARY_SLOTS.contains(slotName)) {
            return;
        }
        String detail = text(slot.get("detail"));
        boolean resolved = "RESOLVED".equalsIgnoreCase(text(slot.get("status"))) && StrUtil.isNotBlank(detail);
        TripItinerarySlotDO entity = new TripItinerarySlotDO();
        entity.setTenantId(TenantContextHolder.getRequiredTenantId());
        entity.setItineraryId(itinerary.getId());
        entity.setDay(day);
        entity.setSlot(slotName);
        entity.setSkeleton(StrUtil.blankToDefault(text(slot.get("skeleton")), "待补充"));
        entity.setPoiId(text(slot.get("poiId")));
        entity.setStatus(resolved ? "RESOLVED" : "PENDING");
        entity.setResolveStatus(resolved ? SLOT_RESOLVE_STATUS_COMPLETED : SLOT_RESOLVE_STATUS_PENDING);
        entity.setDetail(resolved ? detail : null);
        entity.setCandidatesJson(JsonUtils.toJsonString(
                slot.get("candidates") instanceof List<?> candidates ? candidates : List.of()));
        entity.setCitationIdsJson(JsonUtils.toJsonString(
                slot.get("citationIds") instanceof List<?> citations ? citations : List.of()));
        tripItinerarySlotMapper.insert(entity);
    }

    @SuppressWarnings("unchecked")
    private void createTransportSlot(TripItineraryDO itinerary, String slotName, Object value) {
        if (!(value instanceof Map<?, ?> raw)) {
            return;
        }
        Map<String, Object> slot = new LinkedHashMap<>((Map<String, Object>) raw);
        slot.put("slot", slotName);
        createSlotIfPresent(itinerary, 0, slot);
    }

    private static String text(Object value) {
        String result = value == null ? "" : String.valueOf(value).trim();
        return "null".equalsIgnoreCase(result) ? "" : result;
    }

}
