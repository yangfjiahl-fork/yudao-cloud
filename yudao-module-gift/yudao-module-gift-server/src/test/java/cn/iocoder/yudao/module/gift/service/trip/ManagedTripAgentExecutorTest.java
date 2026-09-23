package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentClient;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentExecutionMetrics;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentExecutionOptions;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentExecutionResult;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedTripAgentProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManagedTripAgentExecutorTest {

    @Test
    void execute_shouldReuseTripSessionAndRunTask() {
        ManagedTripSessionService sessionService = mock(ManagedTripSessionService.class);
        ManagedAgentClient agentClient = mock(ManagedAgentClient.class);
        ManagedTripAgentExecutor executor = new ManagedTripAgentExecutor();
        ReflectionTestUtils.setField(executor, "managedTripSessionService", sessionService);
        ReflectionTestUtils.setField(executor, "managedAgentClient", agentClient);
        ReflectionTestUtils.setField(executor, "properties", new ManagedTripAgentProperties());
        TripPlanDO trip = new TripPlanDO().setId(1L).setConversationId(2L);
        Map<String, Object> state = Map.of("destination", "云南");
        ManagedAgentExecutionResult agentResult = new ManagedAgentExecutionResult("{\"topic\":\"TRAVEL\"}",
                new ManagedAgentExecutionMetrics(1, 0, 200, 50, 250, 100, 300));
        when(sessionService.getOrCreateSession(1L, 2L, state, ManagedTripAgentStage.INTAKE))
                .thenReturn("session-intake");
        when(agentClient.execute(eq("session-intake"), eq("task-json"), any()))
                .thenReturn(agentResult);

        ManagedTripAgentExecutor.Execution result = executor.execute(
                trip, state, "task-json", ManagedTripAgentStage.INTAKE);

        assertEquals("session-intake", result.sessionId());
        assertEquals(agentResult, result.result());
        ArgumentCaptor<ManagedAgentExecutionOptions> optionsCaptor =
                ArgumentCaptor.forClass(ManagedAgentExecutionOptions.class);
        verify(agentClient).execute(eq("session-intake"), eq("task-json"), optionsCaptor.capture());
        assertEquals("INTAKE", optionsCaptor.getValue().operation());
        assertEquals(1, optionsCaptor.getValue().maxModelRequests());
        assertEquals(0, optionsCaptor.getValue().maxToolCalls());
    }

    @Test
    void execute_shouldUseIndependentPlanSession() {
        ManagedTripSessionService sessionService = mock(ManagedTripSessionService.class);
        ManagedAgentClient agentClient = mock(ManagedAgentClient.class);
        ManagedTripAgentExecutor executor = new ManagedTripAgentExecutor();
        ReflectionTestUtils.setField(executor, "managedTripSessionService", sessionService);
        ReflectionTestUtils.setField(executor, "managedAgentClient", agentClient);
        ReflectionTestUtils.setField(executor, "properties", new ManagedTripAgentProperties());
        TripPlanDO trip = new TripPlanDO().setId(1L).setConversationId(2L);
        Map<String, Object> state = Map.of("destination", "云南");
        ManagedAgentExecutionResult agentResult = new ManagedAgentExecutionResult("{\"macro_skeleton\":{}}",
                new ManagedAgentExecutionMetrics(1, 0, 200, 50, 250, 100, 300));
        when(sessionService.getOrCreateSession(1L, 2L, state, ManagedTripAgentStage.PLAN))
                .thenReturn("session-plan");
        when(agentClient.execute(eq("session-plan"), eq("task-json"), any())).thenReturn(agentResult);

        ManagedTripAgentExecutor.Execution result = executor.execute(
                trip, state, "task-json", ManagedTripAgentStage.PLAN);

        assertEquals("session-plan", result.sessionId());
    }

}
