package cn.iocoder.yudao.module.gift.controller.app.trip;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.api.chat.AiChatApi;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationRespDTO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripChatMessageRespVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripChatMessageSendReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.CHAT_CONVERSATION_NOT_EXISTS;

@Tag(name = "用户 APP - 旅行规划消息")
@RestController
@RequestMapping("/ai/chat/message")
@Validated
public class AppTripChatMessageController {

    @Resource
    private AiChatApi aiChatApi;

    @GetMapping("/list-by-conversation-id")
    @Operation(summary = "获得旅行规划消息列表")
    @Parameter(name = "conversationId", required = true, description = "对话编号", example = "1024")
    public CommonResult<List<AppTripChatMessageRespVO>> getMessageList(@RequestParam("conversationId") Long conversationId) {
        return success(BeanUtils.toBean(aiChatApi.getMessageList(conversationId, getLoginUserId(),
                UserTypeEnum.MEMBER.getValue()), AppTripChatMessageRespVO.class));
    }

    @PostMapping(value = "/send-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "发送旅行规划消息（流式）", description = "大模型交互待接入")
    public Flux<CommonResult<Void>> sendMessageStream(@Valid @RequestBody AppTripChatMessageSendReqVO reqVO) {
        AiChatConversationRespDTO conversation = aiChatApi.getConversation(reqVO.getConversationId(), getLoginUserId(),
                UserTypeEnum.MEMBER.getValue());
        if (conversation == null) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        // TODO：在此根据旅行 Agent 编排调用 AI 通用能力，并流式返回。
        return Flux.empty();
    }

}
