package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppTripChatMessageSendReqVO {

    @NotNull(message = "对话编号不能为空")
    private Long conversationId;

    @NotEmpty(message = "聊天内容不能为空")
    private String content;

}
