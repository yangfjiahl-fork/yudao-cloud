package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 本入口只接收当前轮的 AG-UI user text message。 */
@Data
public class AppTripAgUiMessageReqVO {

    @NotEmpty(message = "消息标识不能为空")
    @Size(max = 128, message = "消息标识长度不能超过 128")
    private String id;

    @NotEmpty(message = "消息角色不能为空")
    private String role;

    @NotEmpty(message = "聊天内容不能为空")
    @Size(max = 10_000, message = "聊天内容长度不能超过 10000")
    private String content;

}
