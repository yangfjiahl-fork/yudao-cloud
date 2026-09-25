package cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yudao.gift.amap")
@Data
public class AmapProperties {

    /** 高德 Web 服务 API Key。 */
    private String key;

    /** 高德逆地理编码接口地址。 */
    private String reverseGeocodingUrl = "https://restapi.amap.com/v3/geocode/regeo";

    /** 高德地理编码接口地址。 */
    private String geocodingUrl = "https://restapi.amap.com/v3/geocode/geo";

    /** 高德 IP 定位接口地址；仅支持国内 IPv4，定位不到城市时调用失败。 */
    private String ipLocationUrl = "https://restapi.amap.com/v3/ip";

    /** 高德地点关键字搜索接口地址。 */
    private String placeSearchUrl = "https://restapi.amap.com/v5/place/text";

    /** 高德周边地点搜索接口地址。 */
    private String placeAroundSearchUrl = "https://restapi.amap.com/v5/place/around";

    /** 高德地点详情接口地址。 */
    private String placeDetailUrl = "https://restapi.amap.com/v5/place/detail";

    /** 高德天气查询接口地址。 */
    private String weatherUrl = "https://restapi.amap.com/v3/weather/weatherInfo";

    /** 高德步行路径规划接口地址。 */
    private String walkingRouteUrl = "https://restapi.amap.com/v3/direction/walking";

    /** 高德公交路径规划接口地址。 */
    private String transitRouteUrl = "https://restapi.amap.com/v3/direction/transit/integrated";

    /** 高德驾车路径规划接口地址。 */
    private String drivingRouteUrl = "https://restapi.amap.com/v3/direction/driving";

}
