package cn.iocoder.yudao.module.gift.controller.app.wool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "用户 APP - 签到羊毛 Response VO")
@Data
@Accessors(chain = true)
public class AppSignInWoolRespVO {

    @Schema(description = "签到记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11903")
    private Long signInRecordId;

    @Schema(description = "羊毛编号；签到奖励为 0 时为空", example = "13660")
    private Long woolId;

    @Schema(description = "第几天签到", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer day;

    @Schema(description = "羊毛数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer amount;

    @Schema(description = "签到的经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer experience;

    @Schema(description = "签到时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
