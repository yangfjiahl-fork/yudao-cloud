package cn.iocoder.yudao.module.gift.framework.trip.provider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 旅行供应商配置；供应商选择本身由系统配置项控制。 */
@ConfigurationProperties(prefix = "yudao.gift.trip-provider")
@Data
public class TripProviderProperties {

    private Weather weather = new Weather();
    private ScenicSpot scenicSpot = new ScenicSpot();

    @Data
    public static class Weather {

        private String appCode;
        private String url = "https://ali-weather.showapi.com/area-to-weather";
    }

    @Data
    public static class ScenicSpot {
        private String baseUrl;
        private String appCode;
    }

}
