package cn.iocoder.yudao.module.gift.service.trip;

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
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** 统一保存不可变行程版本，并更新当前行程指针。 */
@Service
public class TripItineraryVersionService {

    private static final int STATUS_GENERATED = 1;
    private static final int SLOT_RESOLVE_STATUS_PENDING = 0;
    private static final int SLOT_RESOLVE_STATUS_COMPLETED = 2;
    private static final Set<String> ITINERARY_SLOTS = Set.of("MORNING", "LUNCH", "AFTERNOON", "DINNER",
            "EVENING", "ACCOMMODATION", "ARRIVAL", "DEPARTURE", "TRIP_OVERVIEW", "DAY_OVERVIEW");

    @Resource
    private AiChatApi aiChatApi;
    @Resource
    private TripPlanMapper tripPlanMapper;
    @Resource
    private TripItineraryMapper tripItineraryMapper;
    @Resource
    private TripItinerarySlotMapper tripItinerarySlotMapper;
    @Resource
    private TripStructuredItineraryPersistenceService structuredItineraryPersistenceService;

    @Transactional(rollbackFor = Exception.class)
    public SavedItinerary saveGeneratedItinerary(TripPlanDO trip, Long memberId, Map<String, Object> state,
                                                  Map<String, Object> itinerary) {
        Integer maxVersion = tripItineraryMapper.selectMaxVersionByTripId(trip.getId());
        int version = (maxVersion == null ? 0 : maxVersion) + 1;
        normalizeItineraryItems(itinerary);
        itinerary.put("version", version);
        String displayText = StrUtil.blankToDefault(text(itinerary.get("summary")), "已为你生成旅行方案。");
        AiChatMessageRespDTO assistant = createAssistantMessage(trip.getConversationId(), memberId, displayText);

        TripItineraryDO itineraryDO = new TripItineraryDO();
        itineraryDO.setTripId(trip.getId());
        itineraryDO.setVersion(version);
        itineraryDO.setMessageId(assistant.getId());
        itineraryDO.setContentJson(JsonUtils.toJsonString(itinerary));
        itineraryDO.setCitationIdsJson(JsonUtils.toJsonString(itinerary.get("citation_ids")));
        itineraryDO.setStatus(STATUS_GENERATED);
        tripItineraryMapper.insert(itineraryDO);
        initializeItinerarySlots(itineraryDO, itinerary);
        structuredItineraryPersistenceService.persist(trip, itineraryDO, memberId, state, itinerary);

        trip.setCurrentItineraryId(itineraryDO.getId());
        tripPlanMapper.updateById(trip);
        updateConversationTitle(trip.getConversationId(), memberId, state);
        return new SavedItinerary(itineraryDO.getId(), assistant.getId(), version, displayText);
    }

    private AiChatMessageRespDTO createAssistantMessage(Long conversationId, Long memberId, String content) {
        AiChatMessageCreateAssistantReqDTO req = new AiChatMessageCreateAssistantReqDTO();
        req.setConversationId(conversationId);
        req.setUserId(memberId);
        req.setUserType(UserTypeEnum.MEMBER.getValue());
        req.setContent(content);
        return aiChatApi.createAssistantMessage(req);
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
    private static void normalizeItineraryItems(Map<String, Object> itinerary) {
        if (!(itinerary.get("daily_itinerary") instanceof List<?> rawDays)) {
            return;
        }
        String source = "JAVA_PLANNER";
        if (itinerary.get("planner") instanceof Map<?, ?> planner) {
            source = StrUtil.blankToDefault(text(planner.get("type")), source);
        }
        List<Map<String, Object>> days = new ArrayList<>();
        for (Object rawDay : rawDays) {
            if (!(rawDay instanceof Map<?, ?> dayMap)) {
                continue;
            }
            Map<String, Object> day = new LinkedHashMap<>((Map<String, Object>) dayMap);
            Integer dayNumber = MapUtil.getInt(day, "day");
            List<Map<String, Object>> slots = new ArrayList<>();
            if (day.get("slots") instanceof List<?> rawSlots) {
                for (int index = 0; index < rawSlots.size(); index++) {
                    Object rawSlot = rawSlots.get(index);
                    if (!(rawSlot instanceof Map<?, ?> slotMap)) {
                        continue;
                    }
                    Map<String, Object> slot = new LinkedHashMap<>((Map<String, Object>) slotMap);
                    String timePeriod = text(slot.get("slot")).toUpperCase(Locale.ROOT);
                    slot.put("itemId", StrUtil.blankToDefault(text(slot.get("itemId")), UUID.randomUUID().toString()));
                    slot.put("day", dayNumber);
                    slot.put("type", StrUtil.blankToDefault(text(slot.get("type")), itemType(timePeriod)));
                    slot.put("timePeriod", StrUtil.blankToDefault(text(slot.get("timePeriod")), timePeriod));
                    slot.put("sort", MapUtil.getInt(slot, "sort", index));
                    slot.put("startTime", StrUtil.blankToDefault(text(slot.get("startTime")),
                            text(slot.get("plannedStartTime"))));
                    slot.put("durationMinutes", MapUtil.getInt(slot, "durationMinutes",
                            MapUtil.getInt(slot, "suggestedStayMinutes", defaultDuration(timePeriod))));
                    slot.put("poiId", text(slot.get("poiId")));
                    slot.put("locked", MapUtil.getBool(slot, "locked", false));
                    slot.put("source", StrUtil.blankToDefault(text(slot.get("source")), source));
                    slots.add(slot);
                }
            }
            day.put("slots", slots);
            days.add(day);
        }
        itinerary.put("daily_itinerary", days);
    }

    private static String itemType(String timePeriod) {
        return switch (timePeriod) {
            case "LUNCH", "DINNER" -> "MEAL";
            case "ACCOMMODATION" -> "LODGING";
            default -> "ACTIVITY";
        };
    }

    private static int defaultDuration(String timePeriod) {
        return switch (timePeriod) {
            case "LUNCH", "DINNER" -> 60;
            case "ACCOMMODATION" -> 0;
            default -> 150;
        };
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

    public record SavedItinerary(Long itineraryId, Long messageId, int version, String displayText) {
    }

}
