package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 旅行天气 Response VO")
@Data
public class AppTripWeatherRespVO {

    @Schema(description = "城市名称", example = "杭州市")
    private String city;

    @Schema(description = "温度（摄氏度）", example = "31")
    private Integer temperature;

    @Schema(description = "天气状况", example = "多云")
    private String condition;

    @Schema(description = "湿度百分比", example = "72")
    private Integer humidity;

    @Schema(description = "风向", example = "东南风")
    private String windDirection;

    @Schema(description = "风力", example = "3")
    private String windPower;

    @Schema(description = "天气数据更新时间", example = "2026-08-29 11:35:33")
    private String queryTime;
}
