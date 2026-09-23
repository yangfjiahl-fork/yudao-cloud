package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 旅行追问卡片工厂。
 *
 * <p>所有卡片都使用稳定信封，特有配置放在 props，提交行为放在 submit。新增卡片类型时只需新增构造方法，
 * 无需修改 AG-UI 事件结构。</p>
 */
final class TripInputCardFactory {

    private static final int SCHEMA_VERSION = 1;

    private TripInputCardFactory() {
    }

    static List<Map<String, Object>> build(List<String> missingRequired, String question,
                                           List<Map<String, String>> suggestions) {
        List<Map<String, Object>> cards = new ArrayList<>();
        if (missingRequired.contains("days")) {
            cards.add(durationSelectCard());
            cards.add(dateRangeCard());
        }
        List<Map<String, Object>> scenicOptions = scenicSelectionOptions(question, suggestions);
        Set<String> consumedContents = new HashSet<>();
        if (scenicOptions.size() >= 2) {
            cards.add(mustVisitCard(scenicOptions));
            scenicOptions.stream().map(option -> String.valueOf(option.get("content")))
                    .forEach(consumedContents::add);
        }
        List<Map<String, Object>> quickOptions = suggestions.stream()
                .filter(suggestion -> !consumedContents.contains(suggestion.get("content")))
                .filter(suggestion -> !missingRequired.contains("days")
                        || !suggestion.get("content").matches("(?s).*\\d+\\s*[天日].*"))
                .map(TripInputCardFactory::option)
                .toList();
        if (!quickOptions.isEmpty()) {
            cards.add(quickOptionsCard(quickOptions));
        }
        return List.copyOf(cards);
    }

    private static Map<String, Object> durationSelectCard() {
        List<Map<String, Object>> options = List.of(2, 3, 4, 5, 6, 7).stream()
                .map(days -> option(days + "天", days, "计划玩" + days + "天"))
                .toList();
        return card("trip-duration-days", "SINGLE_SELECT", "days", "选择出行天数", "trip-duration",
                Map.of("selectionMode", "SINGLE", "options", options, "allowCustom", true),
                Map.of("mode", "OPTION_CONTENT"));
    }

    private static Map<String, Object> dateRangeCard() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("fields", List.of(
                Map.of("name", "startDate", "label", "开始日期"),
                Map.of("name", "endDate", "label", "结束日期", "globallyRequired", false)));
        props.put("completeRangeRequired", true);
        return card("trip-duration-date-range", "DATE_RANGE", "dateRange", "选择出行日期", "trip-duration",
                props, Map.of("mode", "TEMPLATE",
                        "contentTemplate", "我计划{{startDate}}出发，{{endDate}}返程"));
    }

    private static Map<String, Object> mustVisitCard(List<Map<String, Object>> options) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("selectionMode", "MULTIPLE");
        props.put("minSelections", 0);
        props.put("maxSelections", options.size());
        props.put("options", options);
        return card("trip-must-visit", "MULTI_SELECT", "mustVisit", "选择想去的景点", null, props,
                Map.of("mode", "TEMPLATE", "label", "确认选择",
                        "contentTemplate", "我想去：{{contents}}", "emptyContent", "这些景点都不去"));
    }

    private static Map<String, Object> quickOptionsCard(List<Map<String, Object>> options) {
        return card("trip-quick-options", "SINGLE_SELECT", "userMessage",
                options.size() == 1 ? "下一步" : "快捷选择", null,
                Map.of("selectionMode", "SINGLE", "options", options),
                Map.of("mode", "OPTION_CONTENT"));
    }

    private static Map<String, Object> card(String id, String type, String field, String title,
                                             String requiredGroup, Map<String, Object> props,
                                             Map<String, Object> submit) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("id", id);
        card.put("type", type);
        card.put("schemaVersion", SCHEMA_VERSION);
        card.put("field", field);
        card.put("title", title);
        if (StrUtil.isNotBlank(requiredGroup)) {
            card.put("requiredGroup", requiredGroup);
        }
        card.put("props", props);
        card.put("submit", submit);
        return card;
    }

    private static List<Map<String, Object>> scenicSelectionOptions(String question,
                                                                     List<Map<String, String>> suggestions) {
        String normalizedQuestion = StrUtil.blankToDefault(question, "");
        boolean scenicQuestion = List.of("景点", "地点", "打卡", "必去").stream().anyMatch(normalizedQuestion::contains)
                && List.of("哪些", "哪几个", "是否", "想去", "要去").stream().anyMatch(normalizedQuestion::contains);
        if (!scenicQuestion) {
            return List.of();
        }
        return suggestions.stream()
                .filter(suggestion -> !"请立即生成行程".equals(suggestion.get("content")))
                .map(TripInputCardFactory::option)
                .toList();
    }

    private static Map<String, Object> option(Map<String, String> suggestion) {
        return option(suggestion.get("label"), suggestion.get("content"), suggestion.get("content"));
    }

    private static Map<String, Object> option(String label, Object value, String content) {
        return Map.of("label", label, "value", value, "content", content);
    }

}
