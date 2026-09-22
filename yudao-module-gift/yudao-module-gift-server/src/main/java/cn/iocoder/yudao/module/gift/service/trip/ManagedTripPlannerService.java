package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentBudgetExceededException;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentExecutionStage;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripMacroSkeleton;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** 百炼 Managed Agent 负责宏观路线，Java 负责 POI 事实查询、日内排程与硬约束。 */
@Service
public class ManagedTripPlannerService {

    private static final String STAGE = "MANAGED_PLAN";
    private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");

    @Resource
    private ManagedTripAgentExecutor managedTripAgentExecutor;
    @Resource
    private TripRunLogService tripRunLogService;
    @Resource
    private TripItineraryAssembler tripItineraryAssembler;

    public Map<String, Object> plan(TripPlanDO trip, Map<String, Object> state, String latestUserMessage,
                                    Consumer<String> progressConsumer) {
        long start = System.currentTimeMillis();
        Long runId = tripRunLogService.create(trip.getId(), STAGE, JsonUtils.toJsonString(Map.of(
                "tripState", state)));
        String response = null;
        try {
            String task = buildTask(state, latestUserMessage);
            progressConsumer.accept("托管旅行 Agent 正在规划每日城市、区域与主题…");
            ManagedTripAgentExecutor.Execution execution = managedTripAgentExecutor.execute(
                    trip, state, task, ManagedAgentExecutionStage.PLAN);
            String sessionId = execution.sessionId();
            response = execution.result().response();
            TripMacroSkeleton macroSkeleton = ManagedTripPlanValidator.validateMacroSkeleton(
                    TripAgentFormatUtils.parseMap(response), state);
            progressConsumer.accept("宏观路线已确认，正在按每天的城市与区域查询高德候选…");
            Map<String, Object> itinerary = tripItineraryAssembler.assemble(state, macroSkeleton, progressConsumer);
            tripRunLogService.complete(runId, "managed-agent", execution.result().budget().inputTokens(),
                    execution.result().budget().outputTokens(), execution.result().budget().totalTokens(),
                    System.currentTimeMillis() - start, JsonUtils.toJsonString(Map.of(
                            "sessionId", sessionId, "budget", execution.result().budget(),
                            "macroSkeleton", macroSkeleton.toMap(), "itinerary", itinerary)));
            return itinerary;
        } catch (RuntimeException e) {
            Map<String, Object> failureOutput = new LinkedHashMap<>();
            if (response != null) {
                failureOutput.put("response", response);
            }
            if (e instanceof ManagedAgentBudgetExceededException budgetExceeded) {
                failureOutput.put("budgetReason", budgetExceeded.getReason().name());
                failureOutput.put("budget", budgetExceeded.getSnapshot());
            }
            tripRunLogService.fail(runId, System.currentTimeMillis() - start, e.getMessage(), failureOutput.isEmpty()
                    ? null : JsonUtils.toJsonString(failureOutput));
            throw e;
        }
    }

    private static String buildTask(Map<String, Object> state, String latestUserMessage) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("task", "GENERATE_TRIP_MACRO_SKELETON");
        request.put("schemaVersion", "2.0");
        request.put("requestedAt", OffsetDateTime.now(CHINA_ZONE).toString());
        request.put("tripState", state);
        request.put("latestUserMessage", latestUserMessage);
        request.put("defaults", Map.of(
                "budgetTier", "MEDIUM",
                "pace", "NORMAL",
                "dailyStartTime", "09:00",
                "dailyEndTime", "20:00"));
        request.put("requiredOutput", List.of(
                "只输出一个 JSON 对象，不要 Markdown",
                "输出 macro_skeleton.days，数量必须与 tripState.days 完全一致",
                "每天包含 day、city、area、theme、anchorPoiNames、transferDay",
                "anchorPoiNames 每天 1～2 个，仅作为后端高德检索锚点，不输出坐标、酒店、餐厅或日内时刻",
                "城市变化当天必须设置 transferDay=true，并降低当天游览强度",
                "跨城顺序、每日区域与主题必须符合 tripState 的日期、亲子偏好、预算和节奏约束"));
        return JsonUtils.toJsonString(request);
    }

}
