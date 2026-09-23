package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripItineraryDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripItineraryMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripPlanMapper;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripChangeCommand;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripMacroSkeleton;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TripPlanEditorServiceTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TripPlanEditorService editorService;

    @Mock
    private TripPlanMapper tripPlanMapper;
    @Mock
    private TripItineraryMapper tripItineraryMapper;
    @Mock
    private TripItineraryVersionService versionService;
    @Mock
    private TripItineraryAssembler itineraryAssembler;

    @Test
    @SuppressWarnings("unchecked")
    void apply_shouldMoveItemAndSaveNewImmutableVersion() {
        TripPlanDO trip = trip();
        when(tripPlanMapper.selectByConversationIdAndMemberId(2L, 3L)).thenReturn(trip);
        when(tripItineraryMapper.selectById(10L)).thenReturn(itinerary(2));
        when(versionService.saveGeneratedItinerary(eq(trip), eq(3L), anyMap(), anyMap()))
                .thenReturn(new TripItineraryVersionService.SavedItinerary(11L, 12L, 3, "已更新行程"));
        TripChangeCommand command = new TripChangeCommand(TripChangeCommand.Operation.MOVE_ITEM, 2,
                "item-a", 2, "AFTERNOON", 0, Map.of());

        TripPlanEditorService.EditResult result = editorService.apply(2L, 3L, command);

        assertEquals(List.of(1, 2), result.affectedDays());
        assertEquals(3, result.saved().version());
        ArgumentCaptor<Map<String, Object>> itineraryCaptor = ArgumentCaptor.forClass(Map.class);
        verify(versionService).saveGeneratedItinerary(eq(trip), eq(3L), anyMap(), itineraryCaptor.capture());
        Map<String, Object> edited = itineraryCaptor.getValue();
        List<Map<String, Object>> days = (List<Map<String, Object>>) edited.get("daily_itinerary");
        assertEquals(0, ((List<?>) days.get(0).get("slots")).size());
        List<Map<String, Object>> secondDaySlots = (List<Map<String, Object>>) days.get(1).get("slots");
        assertEquals("item-a", secondDaySlots.get(0).get("itemId"));
        assertEquals(2, secondDaySlots.get(0).get("day"));
        assertEquals("AFTERNOON", secondDaySlots.get(0).get("timePeriod"));
        assertEquals("item-b", secondDaySlots.get(1).get("itemId"));
        assertEquals(List.of(1, 2), ((Map<?, ?>) edited.get("last_change")).get("affectedDays"));
    }

    @Test
    void apply_shouldRejectStaleBaseVersion() {
        when(tripPlanMapper.selectByConversationIdAndMemberId(2L, 3L)).thenReturn(trip());
        when(tripItineraryMapper.selectById(10L)).thenReturn(itinerary(3));
        TripChangeCommand command = new TripChangeCommand(TripChangeCommand.Operation.LOCK_ITEM, 2,
                "item-a", null, null, null, Map.of());

        assertThrows(IllegalStateException.class, () -> editorService.apply(2L, 3L, command));

        verifyNoInteractions(versionService);
    }

    @Test
    @SuppressWarnings("unchecked")
    void apply_shouldReplanOnlyAffectedDayAndPreserveLockedItem() {
        TripPlanDO trip = trip();
        when(tripPlanMapper.selectByConversationIdAndMemberId(2L, 3L)).thenReturn(trip);
        when(tripItineraryMapper.selectById(10L)).thenReturn(replanItinerary());
        when(itineraryAssembler.replanDays(anyMap(), any(), eq(Set.of(1)), any(Consumer.class)))
                .thenReturn(List.of(replannedDay()));
        when(versionService.saveGeneratedItinerary(eq(trip), eq(3L), anyMap(), anyMap()))
                .thenReturn(new TripItineraryVersionService.SavedItinerary(11L, 12L, 3, "已更新行程"));
        TripChangeCommand command = new TripChangeCommand(TripChangeCommand.Operation.REPLAN_DAY, 2,
                null, 1, null, null, Map.of("instruction", "换成亲子乐园"));

        TripPlanEditorService.EditResult result = editorService.apply(2L, 3L, command);

        assertEquals(List.of(1), result.affectedDays());
        List<Map<String, Object>> days = (List<Map<String, Object>>) result.itinerary().get("daily_itinerary");
        List<Map<String, Object>> firstDaySlots = (List<Map<String, Object>>) days.get(0).get("slots");
        assertEquals(List.of("item-a", "item-new"), firstDaySlots.stream().map(item -> item.get("itemId")).toList());
        assertEquals(true, firstDaySlots.get(0).get("locked"));
        assertEquals("PENDING", ((Map<?, ?>) days.get(0).get("planning")).get("status"));
        assertEquals("item-b", ((List<Map<String, Object>>) days.get(1).get("slots")).get(0).get("itemId"));
        ArgumentCaptor<TripMacroSkeleton> macroCaptor = ArgumentCaptor.forClass(TripMacroSkeleton.class);
        verify(itineraryAssembler).replanDays(anyMap(), macroCaptor.capture(), eq(Set.of(1)), any(Consumer.class));
        assertEquals(List.of("换成亲子乐园", "滇池"), macroCaptor.getValue().days().get(0).anchorPoiNames());
    }

    private static TripPlanDO trip() {
        return new TripPlanDO().setId(1L).setConversationId(2L).setMemberId(3L)
                .setCurrentItineraryId(10L).setStateJson("{}");
    }

    private static TripItineraryDO itinerary(int version) {
        Map<String, Object> first = item("item-a", 1, "MORNING", 0);
        Map<String, Object> second = item("item-b", 2, "MORNING", 0);
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("summary", "云南行程");
        content.put("citation_ids", List.of());
        content.put("daily_itinerary", List.of(
                Map.of("day", 1, "slots", List.of(first)),
                Map.of("day", 2, "slots", List.of(second))));
        return new TripItineraryDO().setId(10L).setTripId(1L).setVersion(version)
                .setContentJson(JsonUtils.toJsonString(content));
    }

    private static TripItineraryDO replanItinerary() {
        Map<String, Object> first = item("item-a", 1, "MORNING", 0);
        first.put("poiId", "poi-a");
        first.put("locked", true);
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("daily_itinerary", List.of(
                Map.of("day", 1, "slots", List.of(first)),
                Map.of("day", 2, "slots", List.of(item("item-b", 2, "MORNING", 0)))));
        content.put("macro_skeleton", Map.of("days", List.of(
                Map.of("day", 1, "city", "昆明", "area", "滇池", "theme", "亲子",
                        "anchorPoiNames", List.of("滇池")),
                Map.of("day", 2, "city", "大理", "area", "古城", "theme", "人文",
                        "anchorPoiNames", List.of("大理古城")))));
        return new TripItineraryDO().setId(10L).setTripId(1L).setVersion(2)
                .setContentJson(JsonUtils.toJsonString(content));
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

}
