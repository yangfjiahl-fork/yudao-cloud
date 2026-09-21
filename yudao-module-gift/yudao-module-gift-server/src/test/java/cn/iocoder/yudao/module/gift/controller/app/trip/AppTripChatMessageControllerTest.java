package cn.iocoder.yudao.module.gift.controller.app.trip;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.api.chat.AiChatApi;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationRespDTO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripAgUiMessageReqVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripAgUiRunReqVO;
import cn.iocoder.yudao.module.gift.service.trip.TripAgentService;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentEvent;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AppTripChatMessageControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppTripChatMessageController controller;

    @Mock
    private AiChatApi aiChatApi;
    @Mock
    private TripAgentService tripAgentService;

    @Test
    void runManagedAgUi_shouldTranslateTripEventsToAgUiProtocol() {
        Long conversationId = 1L;
        Long memberId = 2L;
        when(aiChatApi.getConversation(conversationId, memberId, UserTypeEnum.MEMBER.getValue()))
                .thenReturn(new AiChatConversationRespDTO().setId(conversationId));
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<TripAgentEvent> eventConsumer = invocation.getArgument(3);
            eventConsumer.accept(TripAgentEvent.of("stage", "INTAKE", "正在收集信息"));
            eventConsumer.accept(TripAgentEvent.of("model_delta", "INTAKE", "{\"state\":")
                    .setSequence(1));
            eventConsumer.accept(TripAgentEvent.of("question", "INTAKE", "你的预算大约是多少？还需要确认住宿偏好和同行人的年龄。")
                    .setMessageId(9L));
            return null;
        }).when(tripAgentService).handleManagedMessage(eq(conversationId), eq(memberId), any(), any());
        AppTripAgUiRunReqVO reqVO = new AppTripAgUiRunReqVO();
        reqVO.setThreadId("1");
        reqVO.setRunId("run-1");
        reqVO.setMessages(List.of(new AppTripAgUiMessageReqVO().setId("user-message-1").setRole("user")
                .setContent("测试消息")));

        TenantContextHolder.setTenantId(1L);
        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(memberId);

            List<CommonResult<java.util.Map<String, Object>>> responses = controller.runManagedAgUi(reqVO).collectList().block();
            List<java.util.Map<String, Object>> events = responses.stream().map(CommonResult::getData).toList();

            assertTrue(responses.stream().allMatch(response -> response.getCode() == 0));
            assertEquals(List.of("RUN_STARTED", "ACTIVITY_SNAPSHOT", "CUSTOM", "TEXT_MESSAGE_START",
                    "TEXT_MESSAGE_CONTENT", "TEXT_MESSAGE_CONTENT", "TEXT_MESSAGE_CONTENT", "TEXT_MESSAGE_END",
                    "CUSTOM", "CUSTOM", "CUSTOM", "RUN_FINISHED"),
                    events.stream().map(event -> event.get("type")).toList());
            assertEquals("TRIP_PROGRESS", events.get(1).get("activityType"));
            assertEquals("trip_progress_card", events.get(2).get("name"));
            assertEquals("active", ((java.util.Map<?, ?>) events.get(2).get("value")).get("state"));
            assertEquals("9", events.get(3).get("messageId"));
            assertEquals("你的预算大约是多少？还需要确认住宿偏好和同行人的年龄。", events.subList(4, 7).stream()
                    .map(event -> (String) event.get("delta")).collect(Collectors.joining()));
            assertEquals("trip_question_card", events.get(8).get("name"));
            assertEquals("trip-question-9", ((java.util.Map<?, ?>) events.get(8).get("value")).get("cardId"));
            assertEquals("trip_question", events.get(9).get("name"));
            assertEquals("trip_progress_card", events.get(10).get("name"));
            assertEquals("completed", ((java.util.Map<?, ?>) events.get(10).get("value")).get("state"));
            assertEquals("1", ((java.util.Map<?, ?>) events.get(11).get("result")).get("conversationId"));
        } finally {
            TenantContextHolder.clear();
        }
    }

    @Test
    void runManagedAgUi_shouldEndWithRunErrorWhenTripGenerationFails() {
        Long conversationId = 1L;
        Long memberId = 2L;
        when(aiChatApi.getConversation(conversationId, memberId, UserTypeEnum.MEMBER.getValue()))
                .thenReturn(new AiChatConversationRespDTO().setId(conversationId));
        doThrow(new IllegalStateException("upstream unavailable"))
                .when(tripAgentService).handleManagedMessage(eq(conversationId), eq(memberId), any(), any());
        AppTripAgUiRunReqVO reqVO = new AppTripAgUiRunReqVO();
        reqVO.setThreadId("1");
        reqVO.setRunId("run-1");
        reqVO.setMessages(List.of(new AppTripAgUiMessageReqVO().setId("user-message-1").setRole("user")
                .setContent("测试消息")));

        TenantContextHolder.setTenantId(1L);
        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(memberId);

            List<CommonResult<java.util.Map<String, Object>>> responses = controller.runManagedAgUi(reqVO).collectList().block();
            List<java.util.Map<String, Object>> events = responses.stream().map(CommonResult::getData).toList();

            assertTrue(responses.stream().allMatch(response -> response.getCode() == 0));
            assertEquals(List.of("RUN_STARTED", "RUN_ERROR"), events.stream().map(event -> event.get("type")).toList());
            assertEquals("TRIP_GENERATION_FAILED", events.get(1).get("code"));
        } finally {
            TenantContextHolder.clear();
        }
    }

    @Test
    void runManagedAgUi_shouldPublishItineraryCardAfterAssistantText() {
        Long conversationId = 1L;
        Long memberId = 2L;
        when(aiChatApi.getConversation(conversationId, memberId, UserTypeEnum.MEMBER.getValue()))
                .thenReturn(new AiChatConversationRespDTO().setId(conversationId));
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<TripAgentEvent> eventConsumer = invocation.getArgument(3);
            eventConsumer.accept(TripAgentEvent.of("itinerary_skeleton", "ASSEMBLE", "已为你生成三日行程。")
                    .setMessageId(10L).setItinerary(java.util.Map.of("version", 1)));
            return null;
        }).when(tripAgentService).handleManagedMessage(eq(conversationId), eq(memberId), any(), any());

        TenantContextHolder.setTenantId(1L);
        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(memberId);

            List<java.util.Map<String, Object>> events = controller.runManagedAgUi(createAgUiRunRequest()).collectList().block().stream()
                    .map(CommonResult::getData).toList();

            assertEquals("TEXT_MESSAGE_END", events.get(3).get("type"));
            assertEquals("trip_itinerary_card", events.get(4).get("name"));
            assertEquals("trip-itinerary-10", ((java.util.Map<?, ?>) events.get(4).get("value")).get("cardId"));
            assertEquals(java.util.Map.of("version", 1), ((java.util.Map<?, ?>) events.get(4).get("value")).get("itinerary"));
            assertEquals("trip_itinerary_skeleton", events.get(5).get("name"));
        } finally {
            TenantContextHolder.clear();
        }
    }

    private static AppTripAgUiRunReqVO createAgUiRunRequest() {
        AppTripAgUiRunReqVO reqVO = new AppTripAgUiRunReqVO();
        reqVO.setThreadId("1");
        reqVO.setRunId("run-1");
        reqVO.setMessages(List.of(new AppTripAgUiMessageReqVO().setId("user-message-1").setRole("user")
                .setContent("测试消息")));
        return reqVO;
    }

}
