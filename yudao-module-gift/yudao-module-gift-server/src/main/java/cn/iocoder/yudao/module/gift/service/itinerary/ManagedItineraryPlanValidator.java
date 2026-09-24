package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItineraryMacroSkeleton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Managed Agent 输出的确定性边界：只接受宏观行程，POI 事实与日内排程仍由 Java 校验和生成。 */
public final class ManagedItineraryPlanValidator {

    private ManagedItineraryPlanValidator() {
    }

    @SuppressWarnings("unchecked")
    public static ItineraryMacroSkeleton validateMacroSkeleton(Map<String, Object> rawPlan,
                                                           Map<String, Object> state) {
        Map<String, Object> macro = rawPlan.get("macro_skeleton") instanceof Map<?, ?> wrapped
                ? new LinkedHashMap<>((Map<String, Object>) wrapped) : new LinkedHashMap<>(rawPlan);
        int expectedDays = MapUtil.getInt(state, "days", 0);
        if (expectedDays <= 0) {
            throw new IllegalArgumentException("旅行状态缺少有效天数");
        }
        Object rawDays = macro.get("days");
        if (!(rawDays instanceof List<?> days) || days.size() != expectedDays) {
            throw new IllegalArgumentException("Managed Agent 返回的宏观行程数量与旅行天数不一致");
        }

        List<ItineraryMacroSkeleton.Day> normalizedDays = new ArrayList<>(expectedDays);
        Set<Integer> seenDays = new LinkedHashSet<>();
        for (Object rawDay : days) {
            if (!(rawDay instanceof Map<?, ?> dayMap)) {
                throw new IllegalArgumentException("Managed Agent 返回了非对象的宏观行程");
            }
            Map<String, Object> day = new LinkedHashMap<>((Map<String, Object>) dayMap);
            Integer dayNumber = MapUtil.getInt(day, "day");
            if (dayNumber == null || dayNumber < 1 || dayNumber > expectedDays || !seenDays.add(dayNumber)) {
                throw new IllegalArgumentException("Managed Agent 返回了无效或重复的 day：" + dayNumber);
            }
            String city = text(day.get("city"));
            String area = text(day.get("area"));
            String theme = text(day.get("theme"));
            List<String> anchors = normalizeStringList(day.get("anchorPoiNames"));
            if (StrUtil.isBlank(city) || StrUtil.isBlank(area) || StrUtil.isBlank(theme)) {
                throw new IllegalArgumentException("第 " + dayNumber + " 天缺少城市、区域或主题");
            }
            if (anchors.isEmpty() || anchors.size() > 2) {
                throw new IllegalArgumentException("第 " + dayNumber + " 天必须包含 1～2 个 anchor POI");
            }
            normalizedDays.add(new ItineraryMacroSkeleton.Day(dayNumber, city, area, theme, anchors));
        }
        normalizedDays.sort(Comparator.comparingInt(ItineraryMacroSkeleton.Day::day));
        return new ItineraryMacroSkeleton(List.copyOf(normalizedDays));
    }

    private static List<String> normalizeStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(ManagedItineraryPlanValidator::text).filter(StrUtil::isNotBlank).distinct().toList();
    }

    private static String text(Object value) {
        if (value == null) {
            return "";
        }
        String result = StrUtil.trim(String.valueOf(value));
        return "null".equalsIgnoreCase(result) ? "" : result;
    }

}
