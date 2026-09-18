package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentSessionClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** 将完整旅行规划委派给百炼 Managed Agent，Java 只负责 Session、契约校验与审计。 */
@Service
public class ManagedTripPlannerService {

    private static final String STAGE = "MANAGED_PLAN";
    private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");

    @Resource
    private ManagedAgentSessionClient managedAgentSessionClient;
    @Resource
    private ManagedTripSessionService managedTripSessionService;
    @Resource
    private TripRunLogService tripRunLogService;

    public Map<String, Object> plan(TripPlanDO trip, Map<String, Object> state, String latestUserMessage,
                                    Consumer<String> progressConsumer) {
        String sessionId = managedTripSessionService.ensureSession(trip.getId(), trip.getConversationId(), state);
        trip.setManagedAgentSessionId(sessionId);
        String task = buildTask(state, latestUserMessage);
        long start = System.currentTimeMillis();
        Long runId = tripRunLogService.create(trip.getId(), STAGE, JsonUtils.toJsonString(Map.of(
                "sessionId", sessionId, "tripState", state)));
        try {
            progressConsumer.accept("托管旅行 Agent 正在规划跨城路线与每日主题…");
            String response = managedAgentSessionClient.execute(sessionId, task);
            progressConsumer.accept("已完成高德 POI 与路线核验，正在校验行程结构…");
            Map<String, Object> itinerary = ManagedTripPlanValidator.validateAndNormalize(
                    TripAgentFormatUtils.parseMap(response), state);
            tripRunLogService.complete(runId, "managed-agent", null, null, null,
                    System.currentTimeMillis() - start, JsonUtils.toJsonString(Map.of(
                            "sessionId", sessionId, "itinerary", itinerary)));
            progressConsumer.accept("行程约束校验通过，正在生成行程卡片…");
            return itinerary;
        } catch (RuntimeException e) {
            tripRunLogService.fail(runId, System.currentTimeMillis() - start, e.getMessage());
            throw e;
        }
    }

    private static String buildTask(Map<String, Object> state, String latestUserMessage) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("task", "GENERATE_TRAVEL_PLAN");
        request.put("schemaVersion", "1.0");
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
                "先规划跨城/跨区域主路线，再规划每日 POI",
                "POI 必须经高德 MCP 核验并包含 poiId、poiName、longitude、latitude",
                "使用托管沙箱批量检查路线时间、营业时间、重复 POI 与每日可行性",
                "daily_itinerary 数量必须与 days 完全一致",
                "每天至少包含一个游览节点与一个 ACCOMMODATION 节点"));
        return JsonUtils.toJsonString(request);
    }

}
