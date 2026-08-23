package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 旅行规划会话创建流式 Response VO")
@Data
public class AppTripChatCreateStreamRespVO {

    private Long conversationId;
    private Long messageId;
    private String content;

}
