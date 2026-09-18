package cn.iocoder.yudao.module.gift.service.trip;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManagedTripPlanValidatorTest {

    @Test
    void shouldNormalizeValidatedManagedPlan() {
        Map<String, Object> state = Map.of(
                "destination", "杭州", "startDate", "2026-10-01", "days", 1, "travelerCount", 2);
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("summary", "杭州一日游");
        raw.put("overview", Map.of("detail", "围绕西湖安排轻松的一日行程"));
        raw.put("daily_itinerary", List.of(Map.of(
                "day", 1,
                "overview", Map.of("detail", "西湖与湖滨主题"),
                "slots", List.of(
                        poi("MORNING", "B001", "西湖风景名胜区", "120.148", "30.242", "09:00"),
                        poi("ACCOMMODATION", "B002", "杭州湖滨酒店", "120.160", "30.255", "18:00")))));
        raw.put("transport", Map.of());
        raw.put("citation_ids", List.of("amap:B001", "amap:B001"));

        Map<String, Object> result = ManagedTripPlanValidator.validateAndNormalize(raw, state);

        assertEquals("MANAGED_AGENT", ((Map<?, ?>) result.get("planner")).get("type"));
        assertEquals(List.of("amap:B001"), result.get("citation_ids"));
        Map<?, ?> day = (Map<?, ?>) ((List<?>) result.get("daily_itinerary")).get(0);
        assertEquals("2026-10-01", day.get("date"));
        assertEquals("FEASIBLE", ((Map<?, ?>) day.get("planning")).get("status"));
    }

    @Test
    void shouldRejectPoiWithoutVerifiedCoordinate() {
        Map<String, Object> state = Map.of(
                "destination", "杭州", "startDate", "2026-10-01", "days", 1, "travelerCount", 2);
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("daily_itinerary", List.of(Map.of(
                "day", 1,
                "slots", List.of(
                        poi("MORNING", "B001", "西湖风景名胜区", "", "30.242", "09:00"),
                        poi("ACCOMMODATION", "B002", "杭州湖滨酒店", "120.160", "30.255", "18:00")))));

        assertThrows(IllegalArgumentException.class,
                () -> ManagedTripPlanValidator.validateAndNormalize(raw, state));
    }

    private static Map<String, Object> poi(String slot, String poiId, String poiName,
                                           String longitude, String latitude, String startTime) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("slot", slot);
        result.put("poiId", poiId);
        result.put("poiName", poiName);
        result.put("longitude", longitude);
        result.put("latitude", latitude);
        result.put("plannedStartTime", startTime);
        result.put("detail", poiName + " 已通过高德核验");
        return result;
    }

}
