package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripItineraryDO;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripChangeCommand;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TripAgentServiceImplTest {

    @Test
    void mergeInformationState_shouldRejectNumbersNotMentionedByUser() {
        Map<String, Object> state = new LinkedHashMap<>();
        Map<String, Object> extracted = Map.of(
                "destination", "云南",
                "days", 7,
                "travelerCount", 1,
                "budget", 1500);

        TripAgentServiceImpl.mergeInformationState(state, extracted, "国庆节从上海出发去云南。");

        assertEquals("云南", state.get("destination"));
        assertFalse(state.containsKey("days"));
        assertFalse(state.containsKey("travelerCount"));
        assertFalse(state.containsKey("budget"));
    }

    @Test
    void mergeInformationState_shouldAcceptExplicitNumbersFromChineseMessage() {
        Map<String, Object> state = new LinkedHashMap<>();
        Map<String, Object> extracted = Map.of(
                "days", 6,
                "travelerCount", 4,
                "budget", 6000,
                "hotelBudget", 500,
                "travelerProfile", Map.of("adultCount", 2, "childCount", 2));

        TripAgentServiceImpl.mergeInformationState(state, extracted,
                "2大2小共4人，6天5晚，总预算6000元，住宿每晚500元。");

        assertEquals(6, state.get("days"));
        assertEquals(4, state.get("travelerCount"));
        assertEquals("6000", state.get("budget"));
        assertEquals(500, state.get("hotelBudget"));
        assertEquals(Map.of("adultCount", 2, "childCount", 2), state.get("travelerProfile"));
    }

    @Test
    void mergeInformationState_shouldNormalizeAmountSuggestionContent() {
        Map<String, Object> state = new LinkedHashMap<>();

        TripAgentServiceImpl.mergeInformationState(state, Map.of(
                "budget", "人均预算1,500元",
                "hotelBudget", "每晚住宿预算500元"), "人均预算1,500元，每晚住宿预算500元。");

        assertEquals("1500", state.get("budget"));
        assertEquals(500, state.get("hotelBudget"));
    }

    @Test
    void shortenTripOverview_shouldKeepCompleteSentenceWithinTargetLength() {
        String detail = "甲".repeat(49) + "。" + "乙".repeat(20);

        String shortened = TripAgentServiceImpl.shortenTripOverview(detail, "TRIP_OVERVIEW");

        assertEquals(50, shortened.length());
        assertEquals("。", shortened.substring(shortened.length() - 1));
    }

    @Test
    void shortenTripOverview_shouldCapLongTripOverviewAndLeaveDayOverviewUnchanged() {
        String detail = "甲".repeat(70);

        assertEquals(60, TripAgentServiceImpl.shortenTripOverview(detail, "TRIP_OVERVIEW").length());
        assertEquals("…", TripAgentServiceImpl.shortenTripOverview(detail, "TRIP_OVERVIEW").substring(59));
        assertEquals(detail, TripAgentServiceImpl.shortenTripOverview(detail, "DAY_OVERVIEW"));
    }

    @Test
    void determineAction_shouldUseStateAndIntentInsteadOfFixedUserText() {
        assertEquals(TripAgentServiceImpl.TripOrchestrationAction.INTAKE,
                TripAgentServiceImpl.determineAction(java.util.List.of("budget"), true, true, false));
        assertEquals(TripAgentServiceImpl.TripOrchestrationAction.UPDATE_STATE,
                TripAgentServiceImpl.determineAction(java.util.List.of(), false, true, false));
        assertEquals(TripAgentServiceImpl.TripOrchestrationAction.GENERATE_PLAN,
                TripAgentServiceImpl.determineAction(java.util.List.of(), true, false, false));
        assertEquals(TripAgentServiceImpl.TripOrchestrationAction.EDIT_PLAN,
                TripAgentServiceImpl.determineAction(java.util.List.of(), true, false, true));
        assertEquals(TripAgentServiceImpl.TripOrchestrationAction.CHAT,
                TripAgentServiceImpl.determineAction(java.util.List.of(), false, false, true));
    }

    @Test
    void extractAgentChangeCommand_shouldUseCurrentServerVersion() {
        Map<String, Object> intake = Map.of("change_command", Map.of(
                "operation", "MOVE_ITEM",
                "baseVersion", 99,
                "itemId", "item-1",
                "day", 2,
                "timePeriod", "AFTERNOON",
                "sort", 1,
                "values", Map.of("note", "靠近酒店")));

        TripChangeCommand command = TripAgentServiceImpl.extractAgentChangeCommand(intake, 3);

        assertEquals(TripChangeCommand.Operation.MOVE_ITEM, command.operation());
        assertEquals(3, command.baseVersion());
        assertEquals("item-1", command.itemId());
        assertEquals(2, command.day());
        assertEquals("靠近酒店", command.values().get("note"));
    }

    @Test
    void extractAgentChangeCommand_shouldConvertLegacySingleDayPatchToReplan() {
        Map<String, Object> intake = Map.of("itinerary_patch", Map.of("operations", List.of(
                Map.of("op", "SET", "day", 2, "slot", "AFTERNOON", "instruction", "换成亲子乐园"))));

        TripChangeCommand command = TripAgentServiceImpl.extractAgentChangeCommand(intake, 4);

        assertEquals(TripChangeCommand.Operation.REPLAN_DAY, command.operation());
        assertEquals(4, command.baseVersion());
        assertEquals(2, command.day());
        assertEquals("换成亲子乐园", command.values().get("instruction"));
    }

    @Test
    void extractAgentChangeCommand_shouldRejectMultipleCommandsInOneTurn() {
        Map<String, Object> intake = Map.of("change_commands", List.of(
                Map.of("operation", "LOCK_ITEM", "itemId", "item-1"),
                Map.of("operation", "MOVE_ITEM", "itemId", "item-2", "day", 2)));

        assertThrows(IllegalArgumentException.class,
                () -> TripAgentServiceImpl.extractAgentChangeCommand(intake, 3));
    }

    @Test
    @SuppressWarnings("unchecked")
    void editableItineraryContext_shouldExposeOnlyFieldsNeededToResolveItemIdentity() {
        TripItineraryDO itinerary = new TripItineraryDO().setVersion(3).setContentJson(JsonUtils.toJsonString(Map.of(
                "daily_itinerary", List.of(Map.of("day", 2, "slots", List.of(Map.of(
                        "itemId", "item-1", "day", 2, "type", "SCENIC", "timePeriod", "AFTERNOON",
                        "sort", 1, "poiId", "poi-1", "poiName", "滇池", "locked", false,
                        "longitude", "102.7", "latitude", "25.0")))))));

        Map<String, Object> context = TripAgentServiceImpl.editableItineraryContext(itinerary);

        assertEquals(3, context.get("version"));
        Map<String, Object> item = ((List<Map<String, Object>>) context.get("items")).get(0);
        assertEquals("item-1", item.get("itemId"));
        assertEquals("滇池", item.get("poiName"));
        assertFalse(item.containsKey("longitude"));
        assertFalse(item.containsKey("latitude"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildManagedIntakeTask_shouldProvideExtractionContextWithoutPoiFacts() {
        String task = TripAgentServiceImpl.buildManagedIntakeTask(
                Map.of("departure", "上海"), null, "国庆去云南玩6天");

        Map<String, Object> payload = JsonUtils.parseMap(task);
        assertEquals("EXTRACT_TRIP_REQUIREMENTS", payload.get("task"));
        assertEquals(Map.of("departure", "上海"), payload.get("currentTripState"));
        assertEquals("国庆去云南玩6天", payload.get("userMessage"));
        assertFalse(((List<Map<String, Object>>) payload.get("informationFields")).isEmpty());
        assertEquals(Map.of(), payload.get("currentEditableItinerary"));
        assertFalse(task.contains("longitude"));
        assertFalse(task.contains("latitude"));
    }
}
