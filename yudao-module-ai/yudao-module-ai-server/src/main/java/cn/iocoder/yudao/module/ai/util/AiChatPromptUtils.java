package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI Chat Prompt 工具类
 */
public class AiChatPromptUtils {

    private static final ZoneId CHINA_ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter SYSTEM_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z][a-zA-Z0-9_]*)\\s*}}");

    private AiChatPromptUtils() {
    }

    /**
     * 渲染 System Prompt 模板，并在末尾补充当前系统时间。
     * <p>
     * 未传入的变量保留原占位符，方便发现 Prompt 配置遗漏；传入 null 的变量渲染为空字符串。
     */
    public static String buildSystemMessage(String systemMessage, Map<String, Object> variables) {
        String renderedMessage = renderTemplate(StrUtil.blankToDefault(systemMessage, "你是一个严谨的助手。"), variables);
        return StrUtil.format("{}\n\n当前系统时间：{}（Asia/Shanghai）。", renderedMessage,
                LocalDateTime.now(CHINA_ZONE_ID).format(SYSTEM_TIME_FORMATTER));
    }

    public static String renderTemplate(String template, Map<String, Object> variables) {
        if (StrUtil.isBlank(template) || variables == null || variables.isEmpty()) {
            return template;
        }
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            if (!variables.containsKey(key)) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
                continue;
            }
            Object value = variables.get(key);
            matcher.appendReplacement(result, Matcher.quoteReplacement(value == null ? "" : ObjUtil.toString(value)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

}
