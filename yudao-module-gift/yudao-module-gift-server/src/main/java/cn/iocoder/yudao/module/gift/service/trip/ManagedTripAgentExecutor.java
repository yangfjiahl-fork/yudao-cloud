package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentExecutionResult;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentSessionClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

/** Managed Agent 单次运行入口，统一创建独立 Session 并执行任务。 */
@Service
public class ManagedTripAgentExecutor {

    @Resource
    private ManagedTripSessionService managedTripSessionService;
    @Resource
    private ManagedAgentSessionClient managedAgentSessionClient;

    public Execution execute(TripPlanDO trip, Map<String, Object> state, String task) {
        String sessionId = managedTripSessionService.createSession(trip.getId(), trip.getConversationId(), state);
        trip.setManagedAgentSessionId(sessionId);
        return new Execution(sessionId, managedAgentSessionClient.execute(sessionId, task));
    }

    public record Execution(String sessionId, ManagedAgentExecutionResult result) {
    }

}
