package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 旅行规划会话创建流式 Response VO")
@Data
public class AppTripChatCreateStreamRespVO {

    private String event;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long conversationId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long messageId;
    private String content;

}
