package cn.iocoder.yudao.module.ai.framework.ai.core.weather.amap;

import lombok.Data;

import java.util.List;

/**
 * 高德实况天气响应 DTO
 */
@Data
class AmapWeatherRespDTO {

    private String status;

    private String info;

    private String infocode;

    private List<Live> lives;

    @Data
    static class Live {

        private String city;

        private String adcode;

        private String weather;

        private String temperature;

        private String winddirection;

        private String windpower;

        private String humidity;

        private String reporttime;

    }

}
