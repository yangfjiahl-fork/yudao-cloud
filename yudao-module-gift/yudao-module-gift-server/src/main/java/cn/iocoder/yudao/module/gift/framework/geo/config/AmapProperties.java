package cn.iocoder.yudao.module.gift.framework.geo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yudao.gift.amap")
@Data
public class AmapProperties {

    /** 高德 Web 服务 API Key。 */
    private String key;

    /** 高德逆地理编码接口地址。 */
    private String reverseGeocodingUrl = "https://restapi.amap.com/v3/geocode/regeo";

}
