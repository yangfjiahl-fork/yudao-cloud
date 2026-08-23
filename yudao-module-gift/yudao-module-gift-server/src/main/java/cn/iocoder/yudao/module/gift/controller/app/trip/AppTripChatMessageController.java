package cn.iocoder.yudao.module.gift.controller.app.trip;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.api.chat.AiChatApi;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationRespDTO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripChatMessageRespVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripChatMessageSendReqVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripChatStreamRespVO;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.gift.service.trip.TripAgentService;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.CHAT_CONVERSATION_NOT_EXISTS;

@Tag(name = "用户 APP - 旅行规划消息")
@RestController
@RequestMapping("/ai/chat/message")
@Validated
@Slf4j
public class AppTripChatMessageController {

    @Resource
    private AiChatApi aiChatApi;
    @Resource
    private TripAgentService tripAgentService;

    @GetMapping("/list-by-conversation-id")
    @Operation(summary = "获得旅行规划消息列表")
    @Parameter(name = "conversationId", required = true, description = "对话编号", example = "1024")
    public CommonResult<List<AppTripChatMessageRespVO>> getMessageList(@RequestParam("conversationId") Long conversationId) {
        return success(BeanUtils.toBean(aiChatApi.getMessageList(conversationId, getLoginUserId(),
                UserTypeEnum.MEMBER.getValue()), AppTripChatMessageRespVO.class));
    }

    @PostMapping(value = "/send-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "发送旅行规划消息（流式）")
    public Flux<CommonResult<AppTripChatStreamRespVO>> sendMessageStream(@Valid @RequestBody AppTripChatMessageSendReqVO reqVO) {
        AiChatConversationRespDTO conversation = aiChatApi.getConversation(reqVO.getConversationId(), getLoginUserId(),
                UserTypeEnum.MEMBER.getValue());
        if (conversation == null) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        Long memberId = getLoginUserId();
        log.info("[sendMessageStream][conversationId({}) memberId({}) tenantId({}) 创建 SSE 流]",
                reqVO.getConversationId(), memberId, tenantId);
        CommonResult<AppTripChatStreamRespVO> accepted = success(new AppTripChatStreamRespVO().setEvent("accepted")
                .setConversationId(reqVO.getConversationId()));
        CommonResult<AppTripChatStreamRespVO> hint = success(new AppTripChatStreamRespVO().setEvent("hint")
                .setConversationId(reqVO.getConversationId()).setContent("正在理解你的出行偏好…"));
        Flux<CommonResult<AppTripChatStreamRespVO>> execution = Flux.<CommonResult<AppTripChatStreamRespVO>>create(sink -> {
            try {
                TenantUtils.execute(tenantId, () -> {
                    TripAgentResult agentResult = tripAgentService.handleMessage(reqVO.getConversationId(), memberId,
                            reqVO.getContent(), progressContent -> sink.next(success(new AppTripChatStreamRespVO()
                                    .setEvent("progress").setConversationId(reqVO.getConversationId())
                                    .setContent(progressContent))));
                    sink.next(success(new AppTripChatStreamRespVO().setEvent(agentResult.getType().toLowerCase())
                            .setConversationId(reqVO.getConversationId()).setMessageId(agentResult.getMessageId())
                            .setContent(agentResult.getContent()).setItinerary(agentResult.getItinerary())
                            .setMissingRequired(agentResult.getMissingRequired())));
                });
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
        CommonResult<AppTripChatStreamRespVO> done = success(new AppTripChatStreamRespVO().setEvent("done")
                .setConversationId(reqVO.getConversationId()));
        return Flux.concat(Flux.just(accepted, hint), execution, Flux.just(done))
                .onErrorResume(e -> {
                    log.error("[sendMessageStream][conversationId({}) 生成旅行方案失败]", reqVO.getConversationId(), e);
                    return Flux.just(success(new AppTripChatStreamRespVO().setEvent("error")
                            .setConversationId(reqVO.getConversationId()).setContent("暂时无法生成旅行方案，请稍后重试。")));
                })
                .doOnSubscribe(subscription -> log.info("[sendMessageStream][conversationId({}) SSE 已订阅]",
                        reqVO.getConversationId()))
                .doOnNext(response -> log.info("[sendMessageStream][conversationId({}) 发送事件({})]",
                        reqVO.getConversationId(), response.getData().getEvent()))
                .doOnCancel(() -> log.info("[sendMessageStream][conversationId({}) 客户端取消 SSE]",
                        reqVO.getConversationId()))
                .doOnComplete(() -> log.info("[sendMessageStream][conversationId({}) SSE 完成]",
                        reqVO.getConversationId()));
    }

}
