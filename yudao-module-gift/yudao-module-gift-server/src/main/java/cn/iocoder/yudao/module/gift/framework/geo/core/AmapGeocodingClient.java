package cn.iocoder.yudao.module.gift.framework.geo.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.framework.geo.config.AmapProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Set;

@Slf4j
@AllArgsConstructor
public class AmapGeocodingClient {

    private static final String SUCCESS_STATUS = "1";
    private static final Set<String> MUNICIPALITIES = Set.of("北京市", "上海市", "天津市", "重庆市");

    private final RestTemplate restTemplate;
    private final AmapProperties properties;

    /**
     * 将高德坐标系的经纬度转换为行政区信息。
     */
    public Location reverseGeocode(BigDecimal longitude, BigDecimal latitude) {
        validateConfig();
        URI uri = UriComponentsBuilder.fromUriString(properties.getReverseGeocodingUrl())
                .queryParam("key", properties.getKey())
                .queryParam("location", formatCoordinate(longitude) + "," + formatCoordinate(latitude))
                .queryParam("extensions", "base")
                .queryParam("output", "JSON")
                .build().encode().toUri();
        try {
            return convertResponse(restTemplate.getForObject(uri, JsonNode.class));
        } catch (RestClientException ex) {
            // RestClientException 可能包含带 Key 的完整 URL，避免将异常内容写入日志或向上透传。
            log.error("[reverseGeocode][调用高德逆地理编码接口失败，longitude({}) latitude({}) errorType({})]",
                    longitude, latitude, ex.getClass().getSimpleName());
            throw new IllegalStateException("调用高德逆地理编码接口失败");
        }
    }

    private Location convertResponse(JsonNode response) {
        if (response == null || !SUCCESS_STATUS.equals(response.path("status").asText())) {
            String info = response == null ? "接口无响应" : response.path("info").asText("未知错误");
            throw new IllegalStateException("高德逆地理编码失败：" + info);
        }
        JsonNode regeocode = response.path("regeocode");
        JsonNode addressComponent = regeocode.path("addressComponent");
        String province = textValue(addressComponent.path("province"));
        String district = textValue(addressComponent.path("district"));
        String city = resolveCity(textValue(addressComponent.path("city")), province, district);
        String adcode = textValue(addressComponent.path("adcode"));
        if (StrUtil.isBlank(city) || StrUtil.isBlank(adcode)) {
            throw new IllegalStateException("高德逆地理编码失败：响应缺少城市或行政区编码");
        }
        return new Location(province, city, district, adcode, textValue(regeocode.path("formatted_address")));
    }

    private void validateConfig() {
        if (properties == null || StrUtil.isBlank(properties.getKey())) {
            throw new IllegalStateException("高德 Web 服务 API Key 未配置");
        }
        if (StrUtil.isBlank(properties.getReverseGeocodingUrl())) {
            throw new IllegalStateException("高德逆地理编码接口地址未配置");
        }
    }

    private static String formatCoordinate(BigDecimal coordinate) {
        return coordinate.stripTrailingZeros().toPlainString();
    }

    private static String resolveCity(String city, String province, String district) {
        if (StrUtil.isNotBlank(city)) {
            return city;
        }
        // 高德对四个直辖市和省直辖县均返回空 city；前者使用省名，后者使用区县名。
        return MUNICIPALITIES.contains(province) ? province : district;
    }

    /** 高德空字段可能返回空数组，统一转换为空字符串。 */
    private static String textValue(JsonNode node) {
        return node.isTextual() ? node.asText() : "";
    }

    public record Location(String province, String city, String district, String adcode, String formattedAddress) {
    }

}
