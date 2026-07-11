package cn.iocoder.yudao.module.gift.controller.app.wool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "用户 APP - 羊毛汇总 Response VO")
@Data
@Accessors(chain = true)
public class AppWoolSummaryRespVO {

    @Schema(description = "今日获取羊毛总量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer todayReceivedAmount;

    @Schema(description = "本月获取羊毛总量，needThisMonth 为 true 时返回", example = "100")
    private Integer thisMonthReceivedAmount;

    @Schema(description = "历史获取羊毛总量，needHistoryTotal 为 true 时返回", example = "1000")
    private Integer historyTotalReceivedAmount;

}
