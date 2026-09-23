package cn.iocoder.yudao.module.gift.service.trip.bo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Managed Agent 产出的宏观行程，仅描述每天的检索范围与主题，不携带未经后端核验的 POI 事实。 */
public record TripMacroSkeleton(List<Day> days) {

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("days", days.stream().map(Day::toMap).toList());
        return result;
    }

    public record Day(int day, String city, String area, String theme, List<String> anchorPoiNames) {

        public Map<String, Object> toMap() {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("day", day);
            result.put("city", city);
            result.put("area", area);
            result.put("theme", theme);
            result.put("anchorPoiNames", anchorPoiNames);
            return result;
        }
    }

}
