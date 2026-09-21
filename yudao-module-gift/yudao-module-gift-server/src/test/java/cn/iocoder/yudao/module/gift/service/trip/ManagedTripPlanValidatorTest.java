package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.module.gift.service.trip.bo.TripMacroSkeleton;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManagedTripPlanValidatorTest {

    @Test
    void shouldNormalizeValidatedMacroSkeleton() {
        Map<String, Object> state = Map.of(
                "destination", "云南", "startDate", "2026-10-01", "days", 2, "travelerCount", 4);
        Map<String, Object> raw = Map.of("macro_skeleton", Map.of("days", List.of(
                macroDay(2, "大理", "大理古城", "换城与人文", List.of("大理古城"), true),
                macroDay(1, "昆明", "滇池周边", "抵达后轻松亲子", List.of("滇池", "海埂大坝"), false))));

        TripMacroSkeleton result = ManagedTripPlanValidator.validateMacroSkeleton(raw, state);

        assertEquals(2, result.days().size());
        assertEquals("昆明", result.days().get(0).city());
        assertEquals(List.of("滇池", "海埂大坝"), result.days().get(0).anchorPoiNames());
        assertEquals("大理", result.days().get(1).city());
        assertEquals(true, result.days().get(1).transferDay());
    }

    @Test
    void shouldRejectCityChangeWithoutTransferDay() {
        Map<String, Object> state = Map.of(
                "destination", "云南", "startDate", "2026-10-01", "days", 2, "travelerCount", 4);
        Map<String, Object> raw = Map.of("days", List.of(
                macroDay(1, "昆明", "滇池周边", "亲子", List.of("滇池"), false),
                macroDay(2, "大理", "大理古城", "人文", List.of("大理古城"), false)));

        assertThrows(IllegalArgumentException.class,
                () -> ManagedTripPlanValidator.validateMacroSkeleton(raw, state));
    }

    private static Map<String, Object> macroDay(int day, String city, String area, String theme,
                                                 List<String> anchors, boolean transferDay) {
        return Map.of("day", day, "city", city, "area", area, "theme", theme,
                "anchorPoiNames", anchors, "transferDay", transferDay);
    }

}
