package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversation.UserItineraryConversationDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversationevent.UserItineraryConversationEventDO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** 统一保存当前行程，并更新会话标题。 */
@Service
public class TripItinerarySaveService {

    @Resource
    private TripStructuredItineraryPersistenceService structuredItineraryPersistenceService;
    @Resource
    private UserItineraryConversationService userItineraryConversationService;

    @Transactional(rollbackFor = Exception.class)
    public SavedItinerary saveGeneratedItinerary(UserItineraryConversationDO conversation, Long memberId,
                                                  Map<String, Object> state,
                                                  Map<String, Object> itinerary) {
        return saveGeneratedItinerary(conversation, memberId, null, null, state, itinerary);
    }

    @Transactional(rollbackFor = Exception.class)
    public SavedItinerary saveGeneratedItinerary(UserItineraryConversationDO conversation, Long memberId,
                                                  String runId, Long requestEventId,
                                                  Map<String, Object> state, Map<String, Object> itinerary) {
        normalizeItineraryItems(itinerary);
        String displayText = StrUtil.blankToDefault(text(itinerary.get("summary")), "已为你生成旅行方案。");
        UserItineraryConversationEventDO assistant = userItineraryConversationService.createEvent(conversation.getId(), runId, requestEventId,
                "ITINERARY", "assistant", "ASSEMBLE", displayText);
        Long itineraryId = structuredItineraryPersistenceService.persist(
                conversation, requestEventId, assistant.getId(), memberId, state, itinerary);
        userItineraryConversationService.linkItinerary(assistant.getId(), itineraryId);
        userItineraryConversationService.updateTitle(conversation.getId(), buildConversationTitle(state));
        return new SavedItinerary(itineraryId, assistant.getId(), displayText);
    }

    private static String buildConversationTitle(Map<String, Object> state) {
        String startDate = text(state.get("startDate"));
        String destination = text(state.get("destination"));
        if (StrUtil.isBlank(startDate) || StrUtil.isBlank(destination)) {
            return "新旅行计划";
        }
        return startDate + " " + destination;
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

    private static String text(Object value) {
        String result = value == null ? "" : String.valueOf(value).trim();
        return "null".equalsIgnoreCase(result) ? "" : result;
    }

    public record SavedItinerary(Long itineraryId, Long messageId, String displayText) {
    }

}
