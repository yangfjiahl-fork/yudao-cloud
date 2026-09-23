package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripItinerarySnapshot;
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
    void validateState_shouldAllowMissingStartDateAndBudget() {
        Map<String, Object> state = new LinkedHashMap<>(Map.of(
                "departure", "上海", "destination", "云南", "days", 6, "travelerCount", 4));

        assertEquals(List.of(), TripAgentServiceImpl.validateState(state));
    }

    @Test
    void validateState_shouldDeriveInclusiveDaysFromDateRange() {
        Map<String, Object> state = new LinkedHashMap<>(Map.of(
                "departure", "上海", "destination", "云南", "startDate", "2026-10-01",
                "endDate", "2026-10-06", "travelerCount", 4));

        assertEquals(List.of(), TripAgentServiceImpl.validateState(state));
        assertEquals(6, state.get("days"));
    }

    @Test
    void validateState_shouldRequireDaysWhenDateRangeIsIncomplete() {
        Map<String, Object> state = new LinkedHashMap<>(Map.of(
                "departure", "上海", "destination", "云南", "startDate", "2026-10-01",
                "travelerCount", 4));

        assertEquals(List.of("days"), TripAgentServiceImpl.validateState(state));
    }

    @Test
    void buildInputCards_shouldOfferDurationAndDateRangeAlternatives() {
        List<Map<String, Object>> cards = TripInputCardFactory.build(List.of("days"),
                "请告诉我开始和结束日期，或者这次计划玩几天？", List.of());

        assertEquals(List.of("SINGLE_SELECT", "DATE_RANGE"),
                cards.stream().map(card -> (String) card.get("type")).toList());
        assertEquals("trip-duration", cards.get(0).get("requiredGroup"));
        assertEquals("trip-duration", cards.get(1).get("requiredGroup"));
        assertEquals(1, cards.get(0).get("schemaVersion"));
        Map<?, ?> dateRangeProps = (Map<?, ?>) cards.get(1).get("props");
        assertEquals(false, ((Map<?, ?>) ((List<?>) dateRangeProps.get("fields")).get(1)).get("globallyRequired"));
        assertEquals(true, dateRangeProps.get("completeRangeRequired"));
        assertEquals("TEMPLATE", ((Map<?, ?>) cards.get(1).get("submit")).get("mode"));
    }

    @Test
    void buildInputCards_shouldUseMultiSelectForScenicSuggestions() {
        List<Map<String, String>> suggestions = List.of(
                Map.of("label", "滇池", "content", "我想去滇池"),
                Map.of("label", "石林", "content", "我想去石林"),
                Map.of("label", "立即生成行程", "content", "请立即生成行程"));

        List<Map<String, Object>> cards = TripInputCardFactory.build(List.of(),
                "滇池和石林这些景点有哪些想去？", suggestions);

        assertEquals(2, cards.size());
        assertEquals("MULTI_SELECT", cards.get(0).get("type"));
        assertEquals("mustVisit", cards.get(0).get("field"));
        assertEquals(2, ((List<?>) ((Map<?, ?>) cards.get(0).get("props")).get("options")).size());
        assertEquals("TEMPLATE", ((Map<?, ?>) cards.get(0).get("submit")).get("mode"));
        assertEquals("SINGLE_SELECT", cards.get(1).get("type"));
        assertEquals("请立即生成行程", ((Map<?, ?>) ((List<?>) ((Map<?, ?>) cards.get(1).get("props"))
                .get("options")).get(0)).get("content"));
        assertEquals("OPTION_CONTENT", ((Map<?, ?>) cards.get(1).get("submit")).get("mode"));
    }

    @Test
    void extractAgentChangeCommand_shouldMapSupportedFields() {
        Map<String, Object> intake = Map.of("change_command", Map.of(
                "operation", "MOVE_ITEM",
                "itemId", "item-1",
                "day", 2,
                "timePeriod", "AFTERNOON",
                "sort", 1,
                "values", Map.of("note", "靠近酒店")));

        TripChangeCommand command = TripAgentServiceImpl.extractAgentChangeCommand(intake);

        assertEquals(TripChangeCommand.Operation.MOVE_ITEM, command.operation());
        assertEquals("item-1", command.itemId());
        assertEquals(2, command.day());
        assertEquals("靠近酒店", command.values().get("note"));
    }

    @Test
    void extractAgentChangeCommand_shouldConvertLegacySingleDayPatchToReplan() {
        Map<String, Object> intake = Map.of("itinerary_patch", Map.of("operations", List.of(
                Map.of("op", "SET", "day", 2, "slot", "AFTERNOON", "instruction", "换成亲子乐园"))));

        TripChangeCommand command = TripAgentServiceImpl.extractAgentChangeCommand(intake);

        assertEquals(TripChangeCommand.Operation.REPLAN_DAY, command.operation());
        assertEquals(2, command.day());
        assertEquals("换成亲子乐园", command.values().get("instruction"));
    }

    @Test
    void extractAgentChangeCommand_shouldRejectMultipleCommandsInOneTurn() {
        Map<String, Object> intake = Map.of("change_commands", List.of(
                Map.of("operation", "LOCK_ITEM", "itemId", "item-1"),
                Map.of("operation", "MOVE_ITEM", "itemId", "item-2", "day", 2)));

        assertThrows(IllegalArgumentException.class,
                () -> TripAgentServiceImpl.extractAgentChangeCommand(intake));
    }

    @Test
    @SuppressWarnings("unchecked")
    void editableItineraryContext_shouldExposeOnlyFieldsNeededToResolveItemIdentity() {
        TripItinerarySnapshot itinerary = new TripItinerarySnapshot().setContentJson(JsonUtils.toJsonString(Map.of(
                "daily_itinerary", List.of(Map.of("day", 2, "slots", List.of(Map.of(
                        "itemId", "item-1", "day", 2, "type", "SCENIC", "timePeriod", "AFTERNOON",
                        "sort", 1, "poiId", "poi-1", "poiName", "滇池", "locked", false,
                        "longitude", "102.7", "latitude", "25.0")))))));

        Map<String, Object> context = TripAgentServiceImpl.editableItineraryContext(itinerary);

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

    @Test
    @SuppressWarnings("unchecked")
    void buildManagedFollowUpTask_shouldUseValidatedStateAndPrioritizeMissingFields() {
        String task = TripAgentServiceImpl.buildManagedFollowUpTask(
                Map.of("departure", "上海", "destination", "云南"),
                List.of("days", "traveler_count"), 2);

        Map<String, Object> payload = JsonUtils.parseMap(task);
        assertEquals("GENERATE_TRIP_FOLLOW_UP", payload.get("task"));
        assertEquals(Map.of("departure", "上海", "destination", "云南"), payload.get("currentTripState"));
        assertEquals(List.of("days", "traveler_count"), payload.get("missingRequiredFields"));
        assertEquals(2, payload.get("questionCount"));
        List<Map<String, Object>> candidateFields =
                (List<Map<String, Object>>) payload.get("candidateFields");
        assertEquals("days", candidateFields.get(0).get("stateKey"));
        assertEquals("travelerCount", candidateFields.get(1).get("stateKey"));
    }
}
