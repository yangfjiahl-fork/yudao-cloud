package cn.iocoder.yudao.module.gift.controller.app.wool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 APP - 羊毛 Response VO")
@Data
public class AppWoolRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13660")
    private Long id;

    @Schema(description = "业务场景", requiredMode = Schema.RequiredMode.REQUIRED, example = "31")
    private String bizType;

    @Schema(description = "业务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27723")
    private String bizId;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer amount;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "INIT")
    private String status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
