package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentExecutionBudgetDecider;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentExecutionResult;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentSessionClient;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManagedTripAgentExecutorTest {

    @Test
    void execute_shouldReuseTripSessionAndRunTask() {
        ManagedTripSessionService sessionService = mock(ManagedTripSessionService.class);
        ManagedAgentSessionClient sessionClient = mock(ManagedAgentSessionClient.class);
        ManagedTripAgentExecutor executor = new ManagedTripAgentExecutor();
        ReflectionTestUtils.setField(executor, "managedTripSessionService", sessionService);
        ReflectionTestUtils.setField(executor, "managedAgentSessionClient", sessionClient);
        TripPlanDO trip = new TripPlanDO().setId(1L).setConversationId(2L);
        Map<String, Object> state = Map.of("destination", "云南");
        ManagedAgentExecutionResult agentResult = new ManagedAgentExecutionResult("{\"topic\":\"TRAVEL\"}",
                new ManagedAgentExecutionBudgetDecider.Snapshot(1, 0, 200, 50, 250, 100, 300));
        when(sessionService.getOrCreateSession(1L, 2L, state)).thenReturn("session-trip");
        when(sessionClient.execute("session-trip", "task-json")).thenReturn(agentResult);

        ManagedTripAgentExecutor.Execution result = executor.execute(trip, state, "task-json");

        assertEquals("session-trip", result.sessionId());
        assertEquals(agentResult, result.result());
        assertEquals("session-trip", trip.getManagedAgentSessionId());
        verify(sessionClient).execute("session-trip", "task-json");
    }

}
