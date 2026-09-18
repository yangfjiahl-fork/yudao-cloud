package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Managed Agent 输出的确定性边界：只校验事实结构与前端契约，不在 Java 中重新规划旅行。 */
public final class ManagedTripPlanValidator {

    private static final Set<String> DAILY_SLOTS = Set.of(
            "MORNING", "LUNCH", "AFTERNOON", "DINNER", "EVENING", "ACCOMMODATION");

    private ManagedTripPlanValidator() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> validateAndNormalize(Map<String, Object> rawPlan,
                                                            Map<String, Object> state) {
        Map<String, Object> plan = rawPlan.get("plan") instanceof Map<?, ?> wrapped
                ? new LinkedHashMap<>((Map<String, Object>) wrapped)
                : new LinkedHashMap<>(rawPlan);
        int expectedDays = MapUtil.getInt(state, "days", 0);
        if (expectedDays <= 0) {
            throw new IllegalArgumentException("旅行状态缺少有效天数");
        }
        Object rawDays = plan.get("daily_itinerary");
        if (!(rawDays instanceof List<?> days) || days.size() != expectedDays) {
            throw new IllegalArgumentException("Managed Agent 返回的每日行程数量与旅行天数不一致");
        }

        List<Map<String, Object>> normalizedDays = new ArrayList<>(expectedDays);
        Set<Integer> seenDays = new LinkedHashSet<>();
        for (Object rawDay : days) {
            if (!(rawDay instanceof Map<?, ?> dayMap)) {
                throw new IllegalArgumentException("Managed Agent 返回了非对象的每日行程");
            }
            Map<String, Object> day = new LinkedHashMap<>((Map<String, Object>) dayMap);
            Integer dayNumber = MapUtil.getInt(day, "day");
            if (dayNumber == null || dayNumber < 1 || dayNumber > expectedDays || !seenDays.add(dayNumber)) {
                throw new IllegalArgumentException("Managed Agent 返回了无效或重复的 day：" + dayNumber);
            }
            normalizeDay(day, state, dayNumber);
            normalizedDays.add(day);
        }
        normalizedDays.sort(Comparator.comparingInt(day -> MapUtil.getInt(day, "day")));

        String destination = text(state.get("destination"));
        plan.put("summary", StrUtil.blankToDefault(text(plan.get("summary")),
                destination + expectedDays + "日旅行方案"));
        plan.put("overview", normalizeOverview(plan.get("overview"), "TRIP_OVERVIEW", "行程总览", destination));
        plan.put("daily_itinerary", normalizedDays);
        plan.put("transport", normalizeTransport(plan.get("transport"), destination));
        plan.put("citation_ids", normalizeStringList(plan.get("citation_ids")));
        plan.put("planner", Map.of("type", "MANAGED_AGENT", "validation", "SERVER_VALIDATED"));
        return plan;
    }

    @SuppressWarnings("unchecked")
    private static void normalizeDay(Map<String, Object> day, Map<String, Object> state, int dayNumber) {
        String defaultCity = text(state.get("destination"));
        Object rawSlots = day.get("slots");
        if (!(rawSlots instanceof List<?> slots) || slots.isEmpty()) {
            throw new IllegalArgumentException("第 " + dayNumber + " 天没有行程节点");
        }
        List<Map<String, Object>> normalizedSlots = new ArrayList<>();
        Set<String> seenSlots = new LinkedHashSet<>();
        boolean hasAccommodation = false;
        boolean hasActivity = false;
        LocalTime previousTime = null;
        for (Object rawSlot : slots) {
            if (!(rawSlot instanceof Map<?, ?> slotMap)) {
                throw new IllegalArgumentException("第 " + dayNumber + " 天包含非对象节点");
            }
            Map<String, Object> slot = new LinkedHashMap<>((Map<String, Object>) slotMap);
            String slotName = text(slot.get("slot")).toUpperCase(Locale.ROOT);
            if (!DAILY_SLOTS.contains(slotName) || !seenSlots.add(slotName)) {
                throw new IllegalArgumentException("第 " + dayNumber + " 天包含无效或重复节点：" + slotName);
            }
            normalizePoiSlot(slot, slotName, defaultCity, dayNumber);
            LocalTime startTime = parseTime(slot.get("plannedStartTime"));
            if (startTime != null && previousTime != null && startTime.isBefore(previousTime)) {
                throw new IllegalArgumentException("第 " + dayNumber + " 天节点时间顺序错误");
            }
            if (startTime != null) {
                previousTime = startTime;
            }
            hasAccommodation |= "ACCOMMODATION".equals(slotName);
            hasActivity |= Set.of("MORNING", "AFTERNOON", "EVENING").contains(slotName);
            normalizedSlots.add(slot);
        }
        if (!hasAccommodation || !hasActivity) {
            throw new IllegalArgumentException("第 " + dayNumber + " 天必须包含游览节点与住宿节点");
        }
        LocalDate startDate = parseDate(state.get("startDate"));
        if (startDate != null) {
            day.put("date", startDate.plusDays(dayNumber - 1L).toString());
        }
        String city = normalizedSlots.stream().map(slot -> text(slot.get("city")))
                .filter(StrUtil::isNotBlank).findFirst().orElse(defaultCity);
        day.put("overview", normalizeOverview(day.get("overview"), "DAY_OVERVIEW", "每日总览", city));
        day.put("slots", normalizedSlots);
        Map<String, Object> planning = day.get("planning") instanceof Map<?, ?> rawPlanning
                ? new LinkedHashMap<>((Map<String, Object>) rawPlanning) : new LinkedHashMap<>();
        planning.put("solver", "MANAGED_AGENT");
        planning.put("status", "FEASIBLE");
        planning.put("validation", "SERVER_VALIDATED");
        day.put("planning", planning);
    }

    private static void normalizePoiSlot(Map<String, Object> slot, String slotName, String defaultCity, int day) {
        String poiId = text(slot.get("poiId"));
        String poiName = text(slot.get("poiName"));
        Double longitude = decimal(slot.get("longitude"));
        Double latitude = decimal(slot.get("latitude"));
        if (StrUtil.isBlank(poiId) || StrUtil.isBlank(poiName) || longitude == null || latitude == null
                || longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("第 " + day + " 天 " + slotName + " 缺少有效高德 POI 或坐标");
        }
        slot.put("slot", slotName);
        slot.put("label", slotLabel(slotName));
        slot.put("city", StrUtil.blankToDefault(text(slot.get("city")), defaultCity));
        slot.put("area", StrUtil.blankToDefault(text(slot.get("area")), text(slot.get("city"))));
        slot.put("poiId", poiId);
        slot.put("poiName", poiName);
        slot.put("longitude", text(slot.get("longitude")));
        slot.put("latitude", text(slot.get("latitude")));
        slot.put("status", "RESOLVED");
        slot.put("planningStatus", "VALIDATED");
        slot.put("skeleton", StrUtil.blankToDefault(text(slot.get("skeleton")),
                "ACCOMMODATION".equals(slotName) ? "入住" + poiName : poiName));
        slot.put("detail", StrUtil.blankToDefault(text(slot.get("detail")), text(slot.get("skeleton"))));
        slot.put("citationIds", normalizeStringList(slot.get("citationIds")));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> normalizeOverview(Object value, String slot, String label, String city) {
        Map<String, Object> result = value instanceof Map<?, ?> map
                ? new LinkedHashMap<>((Map<String, Object>) map) : new LinkedHashMap<>();
        result.put("slot", slot);
        result.put("label", label);
        result.put("city", city);
        String detail = StrUtil.blankToDefault(text(result.get("detail")), text(result.get("skeleton")));
        result.put("status", StrUtil.isBlank(detail) ? "PENDING" : "RESOLVED");
        result.put("skeleton", StrUtil.blankToDefault(text(result.get("skeleton")), label + "待生成"));
        if (StrUtil.isNotBlank(detail)) {
            result.put("detail", detail);
        }
        result.put("citationIds", normalizeStringList(result.get("citationIds")));
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> normalizeTransport(Object value, String destination) {
        Map<String, Object> transport = value instanceof Map<?, ?> map
                ? new LinkedHashMap<>((Map<String, Object>) map) : new LinkedHashMap<>();
        transport.put("arrival", normalizeTransportSlot(transport.get("arrival"), "抵达" + destination, destination));
        transport.put("departure", normalizeTransportSlot(transport.get("departure"), "从" + destination + "返程", destination));
        return transport;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> normalizeTransportSlot(Object value, String fallback, String city) {
        Map<String, Object> slot = value instanceof Map<?, ?> map
                ? new LinkedHashMap<>((Map<String, Object>) map) : new LinkedHashMap<>();
        slot.put("city", StrUtil.blankToDefault(text(slot.get("city")), city));
        slot.put("skeleton", StrUtil.blankToDefault(text(slot.get("skeleton")), fallback));
        String detail = text(slot.get("detail"));
        slot.put("status", StrUtil.isBlank(detail) ? "PENDING" : "RESOLVED");
        if (StrUtil.isNotBlank(detail)) {
            slot.put("detail", detail);
        }
        slot.put("citationIds", normalizeStringList(slot.get("citationIds")));
        return slot;
    }

    private static List<String> normalizeStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(ManagedTripPlanValidator::text).filter(StrUtil::isNotBlank).distinct().toList();
    }

    private static LocalDate parseDate(Object value) {
        try {
            return LocalDate.parse(text(value));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static LocalTime parseTime(Object value) {
        try {
            return LocalTime.parse(text(value));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static Double decimal(Object value) {
        try {
            return Double.valueOf(text(value));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static String slotLabel(String slot) {
        return switch (slot) {
            case "MORNING" -> "上午";
            case "LUNCH" -> "午餐";
            case "AFTERNOON" -> "下午";
            case "DINNER" -> "晚餐";
            case "EVENING" -> "晚上";
            case "ACCOMMODATION" -> "住宿";
            default -> slot;
        };
    }

    private static String text(Object value) {
        if (value == null) {
            return "";
        }
        String result = StrUtil.trim(String.valueOf(value));
        return "null".equalsIgnoreCase(result) ? "" : result;
    }

}
