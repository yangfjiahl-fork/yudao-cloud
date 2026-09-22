package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentClient;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentExecutionOptions;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentExecutionResult;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedTripAgentProperties;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

/** Managed Agent 运行入口，统一复用旅行会话 Session 并执行任务。 */
@Service
public class ManagedTripAgentExecutor {

    private static final int INTAKE_MAX_MODEL_REQUESTS = 1;
    private static final int INTAKE_MAX_TOOL_CALLS = 0;

    @Resource
    private ManagedTripSessionService managedTripSessionService;
    @Resource
    private ManagedAgentClient managedAgentClient;
    @Resource
    private ManagedTripAgentProperties properties;

    public Execution execute(TripPlanDO trip, Map<String, Object> state, String task,
                             ManagedTripAgentStage stage) {
        String sessionId = managedTripSessionService.getOrCreateSession(trip.getId(), trip.getConversationId(), state);
        trip.setManagedAgentSessionId(sessionId);
        return new Execution(sessionId, managedAgentClient.execute(sessionId, task, executionOptions(stage)));
    }

    private ManagedAgentExecutionOptions executionOptions(ManagedTripAgentStage stage) {
        if (stage == ManagedTripAgentStage.INTAKE) {
            return new ManagedAgentExecutionOptions(stage.name(), properties.getIntakeStreamTimeout(),
                    properties.getIntakeMaxRunDuration(), INTAKE_MAX_MODEL_REQUESTS, INTAKE_MAX_TOOL_CALLS,
                    properties.getIntakeMaxTotalTokens(), properties.getIntakeMaxOutputTokens(),
                    properties.getIntakeMaxOutputCharacters());
        }
        return new ManagedAgentExecutionOptions(stage.name(), properties.getStreamTimeout(),
                properties.getMaxRunDuration(), properties.getMaxModelRequests(), properties.getMaxToolCalls(),
                properties.getMaxTotalTokens(), properties.getMaxOutputTokens(),
                properties.getMaxOutputCharacters());
    }

    public record Execution(String sessionId, ManagedAgentExecutionResult result) {
    }

}
