package cn.iocoder.yudao.module.ai.api.travel.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AiTravelWeatherRespDTO {

    private String provider;
    private String city;
    private Integer temperature;
    private String condition;
    private Integer humidity;
    private String windDirection;
    private String windPower;
    private String queryTime;

}
