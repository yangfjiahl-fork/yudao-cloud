package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentExecutionResult;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentExecutionStage;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentSessionClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

/** Managed Agent 运行入口，统一复用旅行会话 Session 并执行任务。 */
@Service
public class ManagedTripAgentExecutor {

    @Resource
    private ManagedTripSessionService managedTripSessionService;
    @Resource
    private ManagedAgentSessionClient managedAgentSessionClient;

    public Execution execute(TripPlanDO trip, Map<String, Object> state, String task,
                             ManagedAgentExecutionStage stage) {
        String sessionId = managedTripSessionService.getOrCreateSession(trip.getId(), trip.getConversationId(), state);
        trip.setManagedAgentSessionId(sessionId);
        return new Execution(sessionId, managedAgentSessionClient.execute(sessionId, task, stage));
    }

    public record Execution(String sessionId, ManagedAgentExecutionResult result) {
    }

}
