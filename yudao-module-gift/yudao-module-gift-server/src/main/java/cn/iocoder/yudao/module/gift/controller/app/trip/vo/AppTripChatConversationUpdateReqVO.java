package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 APP - 旅行规划会话更新 Request VO")
@Data
public class AppTripChatConversationUpdateReqVO {

    @NotNull(message = "对话编号不能为空")
    @Schema(description = "对话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "对话标题", example = "我的旅行计划")
    private String title;

    @Schema(description = "是否置顶", example = "true")
    private Boolean pinned;

}
