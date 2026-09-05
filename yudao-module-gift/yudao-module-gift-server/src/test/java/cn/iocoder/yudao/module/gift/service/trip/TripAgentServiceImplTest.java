package cn.iocoder.yudao.module.gift.service.trip;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
}
