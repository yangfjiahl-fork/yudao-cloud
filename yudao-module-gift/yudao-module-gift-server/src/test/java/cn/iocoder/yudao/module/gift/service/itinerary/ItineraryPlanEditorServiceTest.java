package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversation.UserItineraryConversationDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversationevent.UserItineraryConversationEventDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItineraryChangeCommand;
import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItineraryMacroSkeleton;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItineraryPlanEditorServiceTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ItineraryPlanEditorService editorService;

    @Mock
    private UserItineraryConversationService userItineraryConversationService;
    @Mock
    private UserItineraryQueryService userItineraryQueryService;
    @Mock
    private ItinerarySaveService saveService;
    @Mock
    private ItineraryAssembler itineraryAssembler;

    @Test
    @SuppressWarnings("unchecked")
    void apply_shouldMoveItemAndOverwriteCurrentItinerary() {
        UserItineraryConversationDO trip = trip();
        when(userItineraryConversationService.getRequired(2L, 3L)).thenReturn(trip);
        UserItineraryDO current = itinerary();
        when(userItineraryQueryService.getByConversationId(2L)).thenReturn(current);
        when(userItineraryQueryService.toMap(current)).thenReturn(itineraryMap());
        mockManualRequestEvent();
        when(saveService.saveGeneratedItinerary(eq(trip), eq(3L), isNull(), eq(8L), anyMap(), anyMap()))
                .thenReturn(new ItinerarySaveService.SavedItinerary(11L, 12L, "已更新行程"));
        ItineraryChangeCommand command = new ItineraryChangeCommand(ItineraryChangeCommand.Operation.MOVE_ITEM,
                "item-a", 2, "AFTERNOON", 0, Map.of());

        ItineraryPlanEditorService.EditResult result = editorService.apply(2L, 3L, command);

        assertEquals(List.of(1, 2), result.affectedDays());
        ArgumentCaptor<Map<String, Object>> itineraryCaptor = ArgumentCaptor.forClass(Map.class);
        verify(saveService).saveGeneratedItinerary(eq(trip), eq(3L), isNull(), eq(8L), anyMap(),
                itineraryCaptor.capture());
        Map<String, Object> edited = itineraryCaptor.getValue();
        List<Map<String, Object>> days = (List<Map<String, Object>>) edited.get("daily_itinerary");
        assertEquals(0, ((List<?>) days.get(0).get("slots")).size());
        List<Map<String, Object>> secondDaySlots = (List<Map<String, Object>>) days.get(1).get("slots");
        assertEquals("item-a", secondDaySlots.get(0).get("itemId"));
        assertEquals(2, secondDaySlots.get(0).get("day"));
        assertEquals("AFTERNOON", secondDaySlots.get(0).get("timePeriod"));
        assertEquals("item-b", secondDaySlots.get(1).get("itemId"));
        assertEquals(List.of(1, 2), ((Map<?, ?>) edited.get("last_change")).get("affectedDays"));
        assertEquals("MANUAL", ((Map<?, ?>) edited.get("last_change")).get("source"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void apply_shouldReplanOnlyAffectedDayAndPreserveLockedItem() {
        UserItineraryConversationDO trip = trip();
        when(userItineraryConversationService.getRequired(2L, 3L)).thenReturn(trip);
        UserItineraryDO current = itinerary();
        when(userItineraryQueryService.getByConversationId(2L)).thenReturn(current);
        when(userItineraryQueryService.toMap(current)).thenReturn(replanItineraryMap());
        mockManualRequestEvent();
        when(itineraryAssembler.replanDays(anyMap(), any(), eq(Set.of(1)), any(Consumer.class)))
                .thenReturn(List.of(replannedDay()));
        when(saveService.saveGeneratedItinerary(eq(trip), eq(3L), isNull(), eq(8L), anyMap(), anyMap()))
                .thenReturn(new ItinerarySaveService.SavedItinerary(11L, 12L, "已更新行程"));
        ItineraryChangeCommand command = new ItineraryChangeCommand(ItineraryChangeCommand.Operation.REPLAN_DAY,
                null, 1, null, null, Map.of("instruction", "换成亲子乐园"));

        ItineraryPlanEditorService.EditResult result = editorService.apply(2L, 3L, command);

        assertEquals(List.of(1), result.affectedDays());
        List<Map<String, Object>> days = (List<Map<String, Object>>) result.itinerary().get("daily_itinerary");
        List<Map<String, Object>> firstDaySlots = (List<Map<String, Object>>) days.get(0).get("slots");
        assertEquals(List.of("item-a", "item-new"), firstDaySlots.stream().map(item -> item.get("itemId")).toList());
        assertEquals(true, firstDaySlots.get(0).get("locked"));
        assertEquals("PENDING", ((Map<?, ?>) days.get(0).get("planning")).get("status"));
        assertEquals("item-b", ((List<Map<String, Object>>) days.get(1).get("slots")).get(0).get("itemId"));
        ArgumentCaptor<ItineraryMacroSkeleton> macroCaptor = ArgumentCaptor.forClass(ItineraryMacroSkeleton.class);
        verify(itineraryAssembler).replanDays(anyMap(), macroCaptor.capture(), eq(Set.of(1)), any(Consumer.class));
        assertEquals(List.of("换成亲子乐园", "滇池"), macroCaptor.getValue().days().get(0).anchorPoiNames());
    }

    @Test
    void applyWithinExistingLock_shouldReuseAiRequestEvent() {
        UserItineraryConversationDO trip = trip();
        when(userItineraryConversationService.getRequired(2L, 3L)).thenReturn(trip);
        UserItineraryDO current = itinerary();
        when(userItineraryQueryService.getByConversationId(2L)).thenReturn(current);
        when(userItineraryQueryService.toMap(current)).thenReturn(itineraryMap());
        when(saveService.saveGeneratedItinerary(eq(trip), eq(3L), eq("run-1"), eq(7L), anyMap(), anyMap()))
                .thenReturn(new ItinerarySaveService.SavedItinerary(11L, 12L, "已更新行程"));
        ItineraryChangeCommand command = new ItineraryChangeCommand(ItineraryChangeCommand.Operation.LOCK_ITEM,
                "item-a", null, null, null, Map.of());

        ItineraryPlanEditorService.EditResult result = editorService.applyWithinExistingLock(
                2L, 3L, command, "run-1", 7L);

        assertEquals("AI", ((Map<?, ?>) result.itinerary().get("last_change")).get("source"));
        verify(saveService).saveGeneratedItinerary(eq(trip), eq(3L), eq("run-1"), eq(7L), anyMap(), anyMap());
    }

    private void mockManualRequestEvent() {
        when(userItineraryConversationService.createEvent(eq(2L), isNull(), isNull(), eq("USER_ACTION"), eq("user"),
                eq("EDIT"), anyString(), anyString())).thenReturn(new UserItineraryConversationEventDO().setId(8L));
    }

    private static UserItineraryConversationDO trip() {
        return new UserItineraryConversationDO().setId(2L).setMemberId(3L).setStateJson("{}");
    }

    private static UserItineraryDO itinerary() {
        return new UserItineraryDO().setId(10L).setConversationId(2L);
    }

    private static Map<String, Object> itineraryMap() {
        Map<String, Object> first = item("item-a", 1, "MORNING", 0);
        Map<String, Object> second = item("item-b", 2, "MORNING", 0);
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("summary", "云南行程");
        content.put("citation_ids", List.of());
        content.put("daily_itinerary", new ArrayList<>(List.of(
                day(1, first), day(2, second))));
        return content;
    }

    private static Map<String, Object> replanItineraryMap() {
        Map<String, Object> first = item("item-a", 1, "MORNING", 0);
        first.put("poiId", "poi-a");
        first.put("locked", true);
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("daily_itinerary", new ArrayList<>(List.of(
                day(1, first), day(2, item("item-b", 2, "MORNING", 0)))));
        content.put("macro_skeleton", Map.of("days", List.of(
                Map.of("day", 1, "city", "昆明", "area", "滇池", "theme", "亲子",
                        "anchorPoiNames", List.of("滇池")),
                Map.of("day", 2, "city", "大理", "area", "古城", "theme", "人文",
                        "anchorPoiNames", List.of("大理古城")))));
        return content;
    }

    private static Map<String, Object> replannedDay() {
        Map<String, Object> duplicateLockedPoi = item("generated-a", 1, "MORNING", 0);
        duplicateLockedPoi.put("poiId", "poi-a");
        Map<String, Object> newItem = item("item-new", 1, "AFTERNOON", 1);
        newItem.put("poiId", "poi-new");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("day", 1);
        result.put("slots", List.of(duplicateLockedPoi, newItem));
        result.put("planning", Map.of("status", "FEASIBLE"));
        return result;
    }

    private static Map<String, Object> item(String itemId, int day, String timePeriod, int sort) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("itemId", itemId);
        result.put("day", day);
        result.put("slot", timePeriod);
        result.put("timePeriod", timePeriod);
        result.put("sort", sort);
        result.put("locked", false);
        return result;
    }

    private static Map<String, Object> day(int day, Map<String, Object> item) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("day", day);
        result.put("slots", new ArrayList<>(List.of(item)));
        return result;
    }

}
