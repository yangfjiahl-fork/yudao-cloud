package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripPlanMapper;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentClient;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentSessionCreateRequest;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedTripAgentProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ManagedTripSessionServiceTest {

    @Test
    void getOrCreateSession_shouldReusePersistedSession() {
        ManagedAgentClient agentClient = mock(ManagedAgentClient.class);
        TripPlanMapper tripPlanMapper = mock(TripPlanMapper.class);
        ManagedTripSessionService service = createService(agentClient, tripPlanMapper);
        when(tripPlanMapper.selectById(1L)).thenReturn(
                new TripPlanDO().setId(1L)
                        .setIntakeAgentSessionId("session-intake")
                        .setPlanAgentSessionId("session-plan"));

        String intakeSessionId = service.getOrCreateSession(
                1L, 2L, Map.of("destination", "云南"), ManagedTripAgentStage.INTAKE);
        String planSessionId = service.getOrCreateSession(
                1L, 2L, Map.of("destination", "云南"), ManagedTripAgentStage.PLAN);

        assertEquals("session-intake", intakeSessionId);
        assertEquals("session-plan", planSessionId);
        verifyNoInteractions(agentClient);
        verify(tripPlanMapper, never()).updateById(any(TripPlanDO.class));
    }

    @Test
    void getOrCreateSession_shouldCreateAndPersistIndependentIntakeSession() {
        ManagedAgentClient agentClient = mock(ManagedAgentClient.class);
        TripPlanMapper tripPlanMapper = mock(TripPlanMapper.class);
        ManagedTripSessionService service = createService(agentClient, tripPlanMapper);
        when(tripPlanMapper.selectById(1L)).thenReturn(new TripPlanDO().setId(1L));
        when(agentClient.createSession(any())).thenReturn("session-new");

        String result = service.getOrCreateSession(1L, 2L,
                Map.of("departure", "上海", "destination", "云南", "days", 6), ManagedTripAgentStage.INTAKE);

        assertEquals("session-new", result);
        ArgumentCaptor<TripPlanDO> updateCaptor = ArgumentCaptor.forClass(TripPlanDO.class);
        verify(tripPlanMapper).updateById(updateCaptor.capture());
        assertEquals(1L, updateCaptor.getValue().getId());
        assertEquals("session-new", updateCaptor.getValue().getIntakeAgentSessionId());
        ArgumentCaptor<ManagedAgentSessionCreateRequest> requestCaptor =
                ArgumentCaptor.forClass(ManagedAgentSessionCreateRequest.class);
        verify(agentClient).createSession(requestCaptor.capture());
        assertEquals("agent-intake", requestCaptor.getValue().agentId());
        assertEquals("environment-intake", requestCaptor.getValue().environmentId());
        assertEquals("上海-云南6日游-需求收集", requestCaptor.getValue().title());
        assertEquals("1", requestCaptor.getValue().metadata().get("trip_id"));
        assertEquals("INTAKE", requestCaptor.getValue().metadata().get("agent_stage"));
    }

    @Test
    void getOrCreateSession_shouldCreateAndPersistIndependentPlanSession() {
        ManagedAgentClient agentClient = mock(ManagedAgentClient.class);
        TripPlanMapper tripPlanMapper = mock(TripPlanMapper.class);
        ManagedTripSessionService service = createService(agentClient, tripPlanMapper);
        when(tripPlanMapper.selectById(1L)).thenReturn(
                new TripPlanDO().setId(1L).setIntakeAgentSessionId("session-intake"));
        when(agentClient.createSession(any())).thenReturn("session-plan");

        String result = service.getOrCreateSession(1L, 2L,
                Map.of("destination", "云南", "days", 6), ManagedTripAgentStage.PLAN);

        assertEquals("session-plan", result);
        ArgumentCaptor<TripPlanDO> updateCaptor = ArgumentCaptor.forClass(TripPlanDO.class);
        verify(tripPlanMapper).updateById(updateCaptor.capture());
        assertEquals("session-plan", updateCaptor.getValue().getPlanAgentSessionId());
        ArgumentCaptor<ManagedAgentSessionCreateRequest> requestCaptor =
                ArgumentCaptor.forClass(ManagedAgentSessionCreateRequest.class);
        verify(agentClient).createSession(requestCaptor.capture());
        assertEquals("agent-plan", requestCaptor.getValue().agentId());
        assertEquals("environment-plan", requestCaptor.getValue().environmentId());
        assertEquals("云南6日游-行程生成", requestCaptor.getValue().title());
        assertEquals("PLAN", requestCaptor.getValue().metadata().get("agent_stage"));
    }

    @Test
    void getOrCreateSession_shouldRejectSameAgentForBothStages() {
        ManagedAgentClient agentClient = mock(ManagedAgentClient.class);
        TripPlanMapper tripPlanMapper = mock(TripPlanMapper.class);
        ManagedTripSessionService service = createService(agentClient, tripPlanMapper);
        ReflectionTestUtils.setField(service, "properties", new ManagedTripAgentProperties()
                .setIntakeAgentId("agent-shared").setIntakeEnvironmentId("environment-intake")
                .setPlanAgentId("agent-shared").setPlanEnvironmentId("environment-plan"));
        when(tripPlanMapper.selectById(1L)).thenReturn(new TripPlanDO().setId(1L));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.getOrCreateSession(1L, 2L, Map.of(), ManagedTripAgentStage.INTAKE));

        assertEquals("需求收集与行程生成必须配置不同的 Managed Agent ID", exception.getMessage());
        verifyNoInteractions(agentClient);
    }

    private static ManagedTripSessionService createService(ManagedAgentClient agentClient,
                                                           TripPlanMapper tripPlanMapper) {
        ManagedTripSessionService service = new ManagedTripSessionService();
        ReflectionTestUtils.setField(service, "managedAgentClient", agentClient);
        ReflectionTestUtils.setField(service, "properties", new ManagedTripAgentProperties()
                .setIntakeAgentId("agent-intake").setIntakeEnvironmentId("environment-intake")
                .setPlanAgentId("agent-plan").setPlanEnvironmentId("environment-plan"));
        ReflectionTestUtils.setField(service, "tripPlanMapper", tripPlanMapper);
        return service;
    }

}
