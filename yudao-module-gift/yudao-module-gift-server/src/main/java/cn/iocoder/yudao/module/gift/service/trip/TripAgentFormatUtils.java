package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.jsonrepairj.JsonRepair;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 旅行 Agent 的 JSON 输入输出协议。
 */
public final class TripAgentFormatUtils {

    private TripAgentFormatUtils() {
    }

    /**
     * 提取模型回答中的 JSON 对象，使用 JSON Repair 修复语法，再由 Jackson 严格解析。
     */
    public static Map<String, Object> parseMap(String content) {
        String json = extractJsonObject(content);
        if (StrUtil.isBlank(json)) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> parsed = JsonUtils.parseMap(JsonRepair.repairJson(json));
        return parsed != null ? new LinkedHashMap<>(parsed) : new LinkedHashMap<>();
    }

    private static String extractJsonObject(String content) {
        String value = StrUtil.trim(content);
        if (StrUtil.isBlank(value)) {
            return value;
        }
        if (StrUtil.startWith(value, "```")) {
            int firstLineEnd = value.indexOf('\n');
            int lastFence = value.lastIndexOf("```");
            if (firstLineEnd >= 0 && lastFence > firstLineEnd) {
                value = value.substring(firstLineEnd + 1, lastFence).trim();
            }
        }
        int start = value.indexOf('{');
        int end = value.lastIndexOf('}');
        return start >= 0 && end > start ? value.substring(start, end + 1) : value;
    }

}
