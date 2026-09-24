package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 旅行会话主题决策器。综合 INTAKE 的结构化输出、当前待补字段和用户原文做决策，
 * 不负责持久化、模型调用或 SSE 输出。
 */
@Component
public class ItineraryTopicGuard {

    private static final String REJECT_REPLY =
            "我目前主要帮你规划和调整旅行行程。你可以告诉我目的地、"
            + "出发时间、人数、预算，或者要修改哪一天的安排。";
    private static final String CLARIFY_REPLY =
            "我还不能确认这是否与当前旅行计划有关。你可以补充目的地、"
            + "日期、预算，或者直接说明想调整哪一天的行程。";
    private static final Set<String> STRUCTURED_STATE_KEYS = Set.of("state", "tripState", "patch");
    private static final Set<String> TRAVEL_TOPICS = Set.of("TRAVEL", "TRIP", "TRIP_PLANNING", "TRIP_EDIT",
            "TRIP_QA", "IN_SCOPE");
    private static final Set<String> OFF_TOPICS = Set.of("OFF_TOPIC", "NON_TRAVEL", "UNRELATED", "OUT_OF_SCOPE");
    private static final Pattern TRAVEL_SIGNAL = Pattern.compile(
            "(?i).*(旅游|旅行|出游|行程|攻略|景点|景区|酒店|住宿|民宿|航班|机票|火车|高铁|"
                    + "签证|护照|目的地|出发|返程|预算|亲子|自由行|跟团|自驾|交通|路线|游玩|"
                    + "度假|入住|退房|"
                    + "必去|餐厅|美食|旅拍|travel|trip|itinerary|hotel|flight|visa).*",
            Pattern.DOTALL);
    private static final Pattern OFF_TOPIC_SIGNAL = Pattern.compile(
            "(?i).*(编程|写代码|算法|java|python|股票|基金|投资建议|写诗|作文|翻译|数学题|"
                    + "方程|"
                    + "电视剧|电影推荐|游戏攻略).*",
            Pattern.DOTALL);
    private static final Pattern CONTROL_SIGNAL = Pattern.compile(
            "(?i)^(好的?|可以|确认|继续|开始|生成|立即生成|重新规划|重做|不用了?|取消|"
                    + "就这样|没问题)[！!。. ]*$");
    private static final Pattern NUMBER_SIGNAL = Pattern.compile(
            ".*[0-9０-９一二两三四五六七八九十百千万]+.*",
            Pattern.DOTALL);

    /** 明显跑题内容在调用 INTAKE 模型前直接拦截，避免无效 Token 消耗。 */
    public Decision precheck(String userMessage, Map<String, Object> currentState,
                             List<String> missingRequired) {
        String message = StrUtil.trim(userMessage);
        if (StrUtil.isBlank(message)) {
            return Decision.clarify("blank_message");
        }
        if (isExpectedShortAnswer(message, missingRequired)
                || hasExistingContext(currentState) && CONTROL_SIGNAL.matcher(message).matches()
                || TRAVEL_SIGNAL.matcher(message).matches()) {
            return Decision.allow("precheck_travel_signal");
        }
        if (OFF_TOPIC_SIGNAL.matcher(message).matches()) {
            return Decision.reject("precheck_off_topic");
        }
        return Decision.allow("requires_intake");
    }

    public Decision decide(String userMessage, Map<String, Object> currentState,
                           List<String> missingRequired, Map<String, Object> intake) {
        String message = StrUtil.trim(userMessage);
        if (StrUtil.isBlank(message)) {
            return Decision.clarify("blank_message");
        }

        Map<String, Object> safeIntake = intake == null ? Map.of() : intake;
        boolean structuredEvidence = hasStructuredTravelEvidence(safeIntake);
        boolean expectedAnswer = isExpectedShortAnswer(message, missingRequired);
        boolean conversationControl = hasExistingContext(currentState) && CONTROL_SIGNAL.matcher(message).matches();
        Topic topic = parseTopic(safeIntake);

        if (structuredEvidence || expectedAnswer || conversationControl) {
            return Decision.allow("travel_evidence");
        }
        if (topic == Topic.OFF_TOPIC || OFF_TOPIC_SIGNAL.matcher(message).matches()) {
            return Decision.reject("intake_off_topic");
        }
        if (topic == Topic.TRAVEL || TRAVEL_SIGNAL.matcher(message).matches()) {
            return Decision.allow("travel_topic");
        }
        if (topic == Topic.UNCERTAIN || message.codePointCount(0, message.length()) <= 12) {
            return Decision.clarify("ambiguous_topic");
        }
        return Decision.reject("no_travel_evidence");
    }

    private static boolean hasStructuredTravelEvidence(Map<String, Object> intake) {
        for (String key : STRUCTURED_STATE_KEYS) {
            if (intake.get(key) instanceof Map<?, ?> patch && patch.entrySet().stream()
                    .anyMatch(entry -> entry.getKey() instanceof String field
                            && ItineraryInformationSchema.supports(field) && hasValue(entry.getValue()))) {
                return true;
            }
        }
        if (hasValue(intake.get("change_command")) || hasValue(intake.get("changeCommand"))
                || hasValue(intake.get("change_commands")) || hasValue(intake.get("itinerary_patch"))) {
            return true;
        }
        String action = normalize(intake.get("action"));
        return "GENERATE".equals(action) || "UPDATE".equals(action) || "EDIT".equals(action)
                || "REPLAN".equals(action);
    }

    private static Topic parseTopic(Map<String, Object> intake) {
        String value = normalize(firstNonNull(intake.get("topic"), intake.get("topicType"), intake.get("domain"),
                intake.get("intent")));
        if (OFF_TOPICS.contains(value)) {
            return Topic.OFF_TOPIC;
        }
        if (TRAVEL_TOPICS.contains(value)) {
            return Topic.TRAVEL;
        }
        if ("UNCERTAIN".equals(value) || "AMBIGUOUS".equals(value)) {
            return Topic.UNCERTAIN;
        }
        return Topic.UNKNOWN;
    }

    private static boolean isExpectedShortAnswer(String message, List<String> missingRequired) {
        if (missingRequired == null || missingRequired.isEmpty() || message.length() > 80) {
            return false;
        }
        boolean hasNumber = NUMBER_SIGNAL.matcher(message).matches();
        for (String missing : missingRequired) {
            if (("days".equals(missing) || "traveler_count".equals(missing) || "budget".equals(missing))
                    && hasNumber) {
                return true;
            }
            if ("start_date".equals(missing)
                    && (hasNumber || message.matches(".*(?:今天|明天|后天|周末|国庆|春节).*"))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasExistingContext(Map<String, Object> state) {
        return state != null && state.values().stream().anyMatch(ItineraryTopicGuard::hasValue);
    }

    private static boolean hasValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return !map.isEmpty();
        }
        if (value instanceof List<?> list) {
            return !list.isEmpty();
        }
        return value != null && StrUtil.isNotBlank(String.valueOf(value));
    }

    private static Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String normalize(Object value) {
        return value == null ? "" : StrUtil.trim(String.valueOf(value)).toUpperCase(Locale.ROOT)
                .replace('-', '_').replace(' ', '_');
    }

    private enum Topic {
        TRAVEL,
        OFF_TOPIC,
        UNCERTAIN,
        UNKNOWN
    }

    public enum Action {
        ALLOW,
        REJECT,
        CLARIFY
    }

    public record Decision(Action action, String reason, String reply) {

        public boolean allowed() {
            return action == Action.ALLOW;
        }

        private static Decision allow(String reason) {
            return new Decision(Action.ALLOW, reason, null);
        }

        private static Decision reject(String reason) {
            return new Decision(Action.REJECT, reason, REJECT_REPLY);
        }

        private static Decision clarify(String reason) {
            return new Decision(Action.CLARIFY, reason, CLARIFY_REPLY);
        }
    }

}
