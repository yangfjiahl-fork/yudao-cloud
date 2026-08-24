package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 将 Composer 的严格 JSON 流转换为前端可逐步展示的摘要文本与完整日程卡片。
 * 最终 JSON 仍由调用方完整校验并持久化；此类不把未闭合的对象作为事实输出。
 */
final class TripAgentStreamParser {

    private final StringBuilder content = new StringBuilder();
    private final Consumer<TripAgentEvent> eventConsumer;
    private String emittedSummaryRaw = "";
    private int emittedDailyCount;
    private int sequence;

    TripAgentStreamParser(Consumer<TripAgentEvent> eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    void append(String chunk) {
        if (StrUtil.isEmpty(chunk)) {
            return;
        }
        content.append(chunk);
        emitSummaryDelta();
        emitCompletedDailyItems();
    }

    private void emitSummaryDelta() {
        String summaryRaw = findStringValue("summary");
        if (summaryRaw == null || summaryRaw.length() <= emittedSummaryRaw.length()) {
            return;
        }
        String delta = summaryRaw.substring(emittedSummaryRaw.length());
        emittedSummaryRaw = summaryRaw;
        if (StrUtil.isNotEmpty(delta)) {
            eventConsumer.accept(TripAgentEvent.of("assistant_delta", "COMPOSER", unescape(delta))
                    .setSequence(++sequence));
        }
    }

    private void emitCompletedDailyItems() {
        List<Map<String, Object>> dailyItems = findCompletedDailyItems();
        while (emittedDailyCount < dailyItems.size()) {
            Map<String, Object> item = dailyItems.get(emittedDailyCount++);
            eventConsumer.accept(TripAgentEvent.of("itinerary_item", "COMPOSER", null)
                    .setSequence(++sequence).setItemType("daily_itinerary").setItem(item));
        }
    }

    private String findStringValue(String key) {
        String value = content.toString();
        int keyIndex = value.indexOf('"' + key + '"');
        if (keyIndex < 0) {
            return null;
        }
        int colonIndex = value.indexOf(':', keyIndex + key.length() + 2);
        if (colonIndex < 0) {
            return null;
        }
        int start = colonIndex + 1;
        while (start < value.length() && Character.isWhitespace(value.charAt(start))) {
            start++;
        }
        if (start >= value.length() || value.charAt(start) != '"') {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean escaped = false;
        for (int i = start + 1; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!escaped && c == '"') {
                break;
            }
            result.append(c);
            escaped = !escaped && c == '\\';
            if (c != '\\') {
                escaped = false;
            }
        }
        return result.toString();
    }

    private List<Map<String, Object>> findCompletedDailyItems() {
        String value = content.toString();
        int keyIndex = value.indexOf("\"daily_itinerary\"");
        if (keyIndex < 0) {
            return List.of();
        }
        int arrayStart = value.indexOf('[', keyIndex);
        if (arrayStart < 0) {
            return List.of();
        }
        List<Map<String, Object>> items = new ArrayList<>();
        int objectStart = -1;
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = arrayStart + 1; i < value.length(); i++) {
            char c = value.charAt(i);
            if (inString) {
                if (!escaped && c == '"') {
                    inString = false;
                }
                escaped = !escaped && c == '\\';
                if (c != '\\') {
                    escaped = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                if (depth++ == 0) {
                    objectStart = i;
                }
            } else if (c == '}' && depth > 0 && --depth == 0) {
                Map<String, Object> item = TripAgentFormatUtils.parseMap(value.substring(objectStart, i + 1));
                if (!item.isEmpty()) {
                    items.add(item);
                }
            } else if (c == ']' && depth == 0) {
                break;
            }
        }
        return items;
    }

    private static String unescape(String value) {
        return value.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }

}
