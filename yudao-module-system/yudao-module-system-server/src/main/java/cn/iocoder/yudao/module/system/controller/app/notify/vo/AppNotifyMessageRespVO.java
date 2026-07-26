package cn.iocoder.yudao.module.system.controller.app.notify.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 站内信 Response VO")
@Data
public class AppNotifyMessageRespVO {

    @Schema(description = "消息编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模板发送人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "积分商城")
    private String templateNickname;

    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "您的积分兑换订单已兑换成功")
    private String templateContent;

    @Schema(description = "模板类型，1-通知公告，2-系统消息", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer templateType;

    @Schema(description = "是否已读", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean readStatus;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
