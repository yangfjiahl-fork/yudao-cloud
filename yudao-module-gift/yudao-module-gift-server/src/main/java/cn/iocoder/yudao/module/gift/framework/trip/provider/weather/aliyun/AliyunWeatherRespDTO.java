package cn.iocoder.yudao.module.gift.framework.trip.provider.weather.aliyun;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 阿里云市场天气查询响应 DTO
 */
@Data
class AliyunWeatherRespDTO {

    @JsonProperty("showapi_res_code")
    private Integer code;

    @JsonProperty("showapi_res_error")
    private String error;

    @JsonProperty("showapi_res_body")
    private Body body;

    @Data
    static class Body {

        @JsonProperty("ret_code")
        private Integer code;

        private String time;

        private CityInfo cityInfo;

        private Now now;

    }

    @Data
    static class CityInfo {

        /**
         * 城市中文名
         */
        private String c3;

    }

    @Data
    static class Now {

        private String temperature;

        private String weather;

        /**
         * 空气湿度，例如 70%
         */
        private String sd;

        @JsonProperty("wind_direction")
        private String windDirection;

        @JsonProperty("wind_power")
        private String windPower;

    }

}
