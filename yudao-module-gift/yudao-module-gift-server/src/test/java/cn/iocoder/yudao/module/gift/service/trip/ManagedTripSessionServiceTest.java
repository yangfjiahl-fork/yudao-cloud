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
                new TripPlanDO().setId(1L).setManagedAgentSessionId("session-trip"));

        String result = service.getOrCreateSession(1L, 2L, Map.of("destination", "云南"));

        assertEquals("session-trip", result);
        verifyNoInteractions(agentClient);
        verify(tripPlanMapper, never()).updateById(any());
    }

    @Test
    void getOrCreateSession_shouldCreateAndPersistWhenMissing() {
        ManagedAgentClient agentClient = mock(ManagedAgentClient.class);
        TripPlanMapper tripPlanMapper = mock(TripPlanMapper.class);
        ManagedTripSessionService service = createService(agentClient, tripPlanMapper);
        when(tripPlanMapper.selectById(1L)).thenReturn(new TripPlanDO().setId(1L));
        when(agentClient.createSession(any())).thenReturn("session-new");

        String result = service.getOrCreateSession(1L, 2L,
                Map.of("departure", "上海", "destination", "云南", "days", 6));

        assertEquals("session-new", result);
        ArgumentCaptor<TripPlanDO> updateCaptor = ArgumentCaptor.forClass(TripPlanDO.class);
        verify(tripPlanMapper).updateById(updateCaptor.capture());
        assertEquals(1L, updateCaptor.getValue().getId());
        assertEquals("session-new", updateCaptor.getValue().getManagedAgentSessionId());
        ArgumentCaptor<ManagedAgentSessionCreateRequest> requestCaptor =
                ArgumentCaptor.forClass(ManagedAgentSessionCreateRequest.class);
        verify(agentClient).createSession(requestCaptor.capture());
        assertEquals("agent-trip", requestCaptor.getValue().agentId());
        assertEquals("environment-trip", requestCaptor.getValue().environmentId());
        assertEquals("上海-云南6日游", requestCaptor.getValue().title());
        assertEquals("1", requestCaptor.getValue().metadata().get("trip_id"));
    }

    private static ManagedTripSessionService createService(ManagedAgentClient agentClient,
                                                           TripPlanMapper tripPlanMapper) {
        ManagedTripSessionService service = new ManagedTripSessionService();
        ReflectionTestUtils.setField(service, "managedAgentClient", agentClient);
        ReflectionTestUtils.setField(service, "properties", new ManagedTripAgentProperties()
                .setAgentId("agent-trip").setEnvironmentId("environment-trip"));
        ReflectionTestUtils.setField(service, "tripPlanMapper", tripPlanMapper);
        return service;
    }

}
