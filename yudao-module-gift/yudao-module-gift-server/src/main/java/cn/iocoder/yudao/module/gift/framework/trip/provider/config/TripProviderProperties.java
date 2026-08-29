package cn.iocoder.yudao.module.gift.framework.trip.provider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 旅行供应商配置；供应商选择本身由系统配置项控制。 */
@ConfigurationProperties(prefix = "yudao.gift.trip-provider")
@Data
public class TripProviderProperties {

    private Weather weather = new Weather();
    private ScenicSpot scenicSpot = new ScenicSpot();
    private TravelPlace travelPlace = new TravelPlace();

    @Data
    public static class Weather {

        private String appCode;
        private String url = "https://ali-weather.showapi.com/area-to-weather";
        private Amap amap = new Amap();

        @Data
        public static class Amap {
            private String apiKey;
            private String geocodeUrl = "https://restapi.amap.com/v3/geocode/geo";
            private String weatherUrl = "https://restapi.amap.com/v3/weather/weatherInfo";
        }
    }

    @Data
    public static class ScenicSpot {
        private String baseUrl;
        private String appCode;
        private String amapUrl = "https://restapi.amap.com/v5/place/text";
        private String amapKey;
        private String amapTypes = "110000";
    }

    @Data
    public static class TravelPlace {
        private String amapUrl = "https://restapi.amap.com/v5/place/text";
        private String amapKey;
    }
}
