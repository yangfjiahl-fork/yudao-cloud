package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversation.UserItineraryConversationDO;
import cn.iocoder.yudao.module.gift.dal.mysql.useritineraryconversation.UserItineraryConversationMapper;
import cn.iocoder.yudao.module.gift.service.itinerary.managed.ManagedAgentClient;
import cn.iocoder.yudao.module.gift.service.itinerary.managed.ManagedAgentSessionCreateRequest;
import cn.iocoder.yudao.module.gift.service.itinerary.managed.ManagedItineraryAgentProperties;
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

class ManagedItinerarySessionServiceTest {

    @Test
    void getOrCreateSession_shouldReusePersistedSession() {
        ManagedAgentClient agentClient = mock(ManagedAgentClient.class);
        UserItineraryConversationMapper userItineraryConversationMapper = mock(UserItineraryConversationMapper.class);
        ManagedItinerarySessionService service = createService(agentClient, userItineraryConversationMapper);
        when(userItineraryConversationMapper.selectById(2L)).thenReturn(
                new UserItineraryConversationDO().setId(2L)
                        .setIntakeAgentSessionId("session-intake")
                        .setPlanAgentSessionId("session-plan"));

        String intakeSessionId = service.getOrCreateSession(
                2L, Map.of("destination", "云南"), ManagedItineraryAgentStage.INTAKE);
        String planSessionId = service.getOrCreateSession(
                2L, Map.of("destination", "云南"), ManagedItineraryAgentStage.PLAN);

        assertEquals("session-intake", intakeSessionId);
        assertEquals("session-plan", planSessionId);
        verifyNoInteractions(agentClient);
        verify(userItineraryConversationMapper, never()).updateById(any(UserItineraryConversationDO.class));
    }

    @Test
    void getOrCreateSession_shouldCreateAndPersistIndependentIntakeSession() {
        ManagedAgentClient agentClient = mock(ManagedAgentClient.class);
        UserItineraryConversationMapper userItineraryConversationMapper = mock(UserItineraryConversationMapper.class);
        ManagedItinerarySessionService service = createService(agentClient, userItineraryConversationMapper);
        when(userItineraryConversationMapper.selectById(2L)).thenReturn(new UserItineraryConversationDO().setId(2L));
        when(agentClient.createSession(any())).thenReturn("session-new");

        String result = service.getOrCreateSession(2L,
                Map.of("departure", "上海", "destination", "云南", "days", 6), ManagedItineraryAgentStage.INTAKE);

        assertEquals("session-new", result);
        ArgumentCaptor<UserItineraryConversationDO> updateCaptor = ArgumentCaptor.forClass(UserItineraryConversationDO.class);
        verify(userItineraryConversationMapper).updateById(updateCaptor.capture());
        assertEquals(2L, updateCaptor.getValue().getId());
        assertEquals("session-new", updateCaptor.getValue().getIntakeAgentSessionId());
        ArgumentCaptor<ManagedAgentSessionCreateRequest> requestCaptor =
                ArgumentCaptor.forClass(ManagedAgentSessionCreateRequest.class);
        verify(agentClient).createSession(requestCaptor.capture());
        assertEquals("agent-intake", requestCaptor.getValue().agentId());
        assertEquals("environment-intake", requestCaptor.getValue().environmentId());
        assertEquals("上海-云南6日游-需求收集", requestCaptor.getValue().title());
        assertEquals("2", requestCaptor.getValue().metadata().get("conversation_id"));
        assertEquals("INTAKE", requestCaptor.getValue().metadata().get("agent_stage"));
    }

    @Test
    void getOrCreateSession_shouldCreateAndPersistIndependentPlanSession() {
        ManagedAgentClient agentClient = mock(ManagedAgentClient.class);
        UserItineraryConversationMapper userItineraryConversationMapper = mock(UserItineraryConversationMapper.class);
        ManagedItinerarySessionService service = createService(agentClient, userItineraryConversationMapper);
        when(userItineraryConversationMapper.selectById(2L)).thenReturn(
                new UserItineraryConversationDO().setId(2L).setIntakeAgentSessionId("session-intake"));
        when(agentClient.createSession(any())).thenReturn("session-plan");

        String result = service.getOrCreateSession(2L,
                Map.of("destination", "云南", "days", 6), ManagedItineraryAgentStage.PLAN);

        assertEquals("session-plan", result);
        ArgumentCaptor<UserItineraryConversationDO> updateCaptor = ArgumentCaptor.forClass(UserItineraryConversationDO.class);
        verify(userItineraryConversationMapper).updateById(updateCaptor.capture());
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
        UserItineraryConversationMapper userItineraryConversationMapper = mock(UserItineraryConversationMapper.class);
        ManagedItinerarySessionService service = createService(agentClient, userItineraryConversationMapper);
        ReflectionTestUtils.setField(service, "properties", new ManagedItineraryAgentProperties()
                .setIntakeAgentId("agent-shared").setIntakeEnvironmentId("environment-intake")
                .setPlanAgentId("agent-shared").setPlanEnvironmentId("environment-plan"));
        when(userItineraryConversationMapper.selectById(2L)).thenReturn(new UserItineraryConversationDO().setId(2L));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.getOrCreateSession(2L, Map.of(), ManagedItineraryAgentStage.INTAKE));

        assertEquals("需求收集与行程生成必须配置不同的 Managed Agent ID", exception.getMessage());
        verifyNoInteractions(agentClient);
    }

    private static ManagedItinerarySessionService createService(ManagedAgentClient agentClient,
                                                           UserItineraryConversationMapper userItineraryConversationMapper) {
        ManagedItinerarySessionService service = new ManagedItinerarySessionService();
        ReflectionTestUtils.setField(service, "managedAgentClient", agentClient);
        ReflectionTestUtils.setField(service, "properties", new ManagedItineraryAgentProperties()
                .setIntakeAgentId("agent-intake").setIntakeEnvironmentId("environment-intake")
                .setPlanAgentId("agent-plan").setPlanEnvironmentId("environment-plan"));
        ReflectionTestUtils.setField(service, "userItineraryConversationMapper", userItineraryConversationMapper);
        return service;
    }

}
