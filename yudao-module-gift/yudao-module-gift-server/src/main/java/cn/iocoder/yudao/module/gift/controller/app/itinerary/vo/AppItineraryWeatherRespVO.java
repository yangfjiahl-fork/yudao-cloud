package cn.iocoder.yudao.module.gift.controller.app.itinerary.vo;

import cn.iocoder.yudao.module.gift.service.itinerary.ItineraryLocationService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 旅行天气 Response VO")
@Data
public class AppItineraryWeatherRespVO {

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

    public static AppItineraryWeatherRespVO from(ItineraryLocationService.Weather weather) {
        return new AppItineraryWeatherRespVO()
                .setCity(weather.city())
                .setTemperature(weather.temperature())
                .setCondition(weather.condition())
                .setHumidity(weather.humidity())
                .setWindDirection(weather.windDirection())
                .setWindPower(weather.windPower())
                .setQueryTime(weather.queryTime());
    }
}
