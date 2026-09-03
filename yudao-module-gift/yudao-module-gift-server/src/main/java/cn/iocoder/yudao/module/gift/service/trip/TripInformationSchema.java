package cn.iocoder.yudao.module.gift.service.trip;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 旅行规划收集信息的唯一字段定义。
 *
 * <p>提取模型、必填校验和模型生成追问时的字段语义都从这里取值，避免三处各自维护字段。</p>
 */
public final class TripInformationSchema {

    public record Suggestion(String label, String content) {
    }

    public record Field(String stateKey, String missingKey, String label, String question,
                        List<Suggestion> suggestions) {
    }

    private static final List<Field> FIELDS = List.of(
            field("departure", "departure", "出发地", "你从哪里出发？", "从上海出发", "我从上海出发"),
            field("destination", "destination", "目的地", "你想去哪里旅行？", "去杭州", "目的地是杭州"),
            field("startDate", "start_date", "出发日期", "你的出发日期是？", "两周后出发", "我计划两周后出发"),
            field("days", "days", "旅行天数", "这次计划玩几天？", "玩 3 天", "计划玩3天"),
            field("travelerCount", "traveler_count", "出行人数", "这次一共几个人出行？", "2 人出行", "总共2人出行"),
            field("budget", "budget", "预算", "你的预算大约是多少？", "人均 ¥1,500", "人均预算1500元"),
            new Field("hotelBudget", "hotel_budget", "住宿预算", "每晚住宿预算大约是多少？", List.of(
                    new Suggestion("¥300/晚", "每晚住宿预算300元"),
                    new Suggestion("¥500/晚", "每晚住宿预算500元"),
                    new Suggestion("¥800/晚", "每晚住宿预算800元"),
                    new Suggestion("¥3,000/晚", "每晚住宿预算3000元")
            )),
            field("endDate", "end_date", "返程日期", "返程日期大约是哪天？", "周末返程", "计划周末返程"),
            field("travelerProfile", "traveler_profile", "同行人构成", "同行人中有儿童或老人吗？", "2 大 1 小", "总共3人出行，其中2位成人、1位儿童"),
            field("interests", "interests", "旅行偏好", "这次更想体验什么？", "美食与人文", "偏好美食和人文"),
            field("pace", "pace", "行程节奏", "希望行程紧凑还是轻松？", "轻松慢游", "希望行程节奏轻松，不要太赶"),
            field("dailyStartTime", "daily_start_time", "每日出发时间", "每天大约几点开始游玩？", "上午 9 点", "每天上午9点开始游玩"),
            field("dailyEndTime", "daily_end_time", "每日结束时间", "每天最晚几点结束行程？", "晚上 8 点", "每天晚上8点结束行程"),
            field("mustVisit", "must_visit", "必去地点", "有没有一定要去的景点或地点？", "一定去西湖", "一定要去西湖"),
            field("constraints", "constraints", "特殊约束", "有需要特别照顾的需求吗？", "亲子友好", "希望行程亲子友好")
    );

    /** 生成首版行程前必须具备的字段；其余字段随时可补充以提升推荐个性化。 */
    private static final Set<String> REQUIRED_STATE_KEYS = Set.of(
            "departure", "destination", "startDate", "days", "travelerCount", "budget");
    private static final Map<String, Field> FIELDS_BY_STATE_KEY = FIELDS.stream()
            .collect(Collectors.toUnmodifiableMap(Field::stateKey, Function.identity()));
    private static final Map<String, Field> FIELDS_BY_MISSING_KEY = FIELDS.stream()
            .collect(Collectors.toUnmodifiableMap(Field::missingKey, Function.identity()));

    private TripInformationSchema() {
    }

    public static List<Field> getFields() {
        return FIELDS;
    }

    public static Set<String> getRequiredStateKeys() {
        return REQUIRED_STATE_KEYS;
    }

    public static boolean supports(String stateKey) {
        return FIELDS_BY_STATE_KEY.containsKey(stateKey);
    }

    public static Field getByStateKey(String stateKey) {
        return FIELDS_BY_STATE_KEY.get(stateKey);
    }

    public static Field getByMissingKey(String missingKey) {
        return FIELDS_BY_MISSING_KEY.get(missingKey);
    }

    private static Field field(String stateKey, String missingKey, String label, String question,
                               String suggestionLabel, String suggestionContent) {
        return new Field(stateKey, missingKey, label, question,
                List.of(new Suggestion(suggestionLabel, suggestionContent)));
    }

}
