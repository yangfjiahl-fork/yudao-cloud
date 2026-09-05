package cn.iocoder.yudao.module.gift.controller.app.trip;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.framework.tracer.config.YudaoReactorMdcAutoConfiguration;
import cn.iocoder.yudao.module.ai.api.chat.AiChatApi;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationRespDTO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripChatMessageSendReqVO;
import cn.iocoder.yudao.module.gift.service.trip.TripAgentService;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentEvent;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
    void sendMessageStream_shouldRestoreMdcAfterDelayedSubscription() {
        Long conversationId = 1L;
        Long memberId = 2L;
        AtomicReference<String> traceId = new AtomicReference<>();
        when(aiChatApi.getConversation(conversationId, memberId, UserTypeEnum.MEMBER.getValue()))
                .thenReturn(new AiChatConversationRespDTO().setId(conversationId));
        doAnswer(invocation -> {
            traceId.set(MDC.get("traceId"));
            @SuppressWarnings("unchecked")
            Consumer<TripAgentEvent> eventConsumer = invocation.getArgument(3);
            eventConsumer.accept(TripAgentEvent.of("stage", "INTAKE", "正在收集信息"));
            return null;
        }).when(tripAgentService).handleMessage(eq(conversationId), eq(memberId), any(), any());
        AppTripChatMessageSendReqVO reqVO = new AppTripChatMessageSendReqVO();
        reqVO.setConversationId(conversationId);
        reqVO.setContent("测试消息");

        TenantContextHolder.setTenantId(1L);
        MDC.put("traceId", "sse-test-trace");
        YudaoReactorMdcAutoConfiguration configuration = new YudaoReactorMdcAutoConfiguration();
        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(memberId);

            var stream = controller.sendMessageStream(reqVO);
            MDC.clear(); // 模拟 MVC 异步返回后 TraceFilter 已清理请求线程的 MDC
            stream.collectList().block();

            assertEquals("sse-test-trace", traceId.get());
        } finally {
            configuration.destroy();
            MDC.clear();
            TenantContextHolder.clear();
        }
    }

}
