package cn.iocoder.yudao.module.gift.framework.trip.provider.weather.amap;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.framework.trip.provider.config.TripProviderProperties;
import cn.iocoder.yudao.module.gift.framework.trip.provider.weather.WeatherClient;
import cn.iocoder.yudao.module.gift.framework.trip.provider.weather.WeatherProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * 高德天气客户端
 *
 * @see <a href="https://lbs.amap.com/api/webservice/guide/api/weatherinfo">天气查询</a>
 */
@Slf4j
@AllArgsConstructor
public class AmapWeatherClient implements WeatherClient {

    private static final String SUCCESS_STATUS = "1";
    private static final String SUCCESS_INFO_CODE = "10000";
    private static final String ADCODE_PATTERN = "\\d{6}";

    private final RestTemplate restTemplate;
    private final TripProviderProperties.Weather.Amap config;

    @Override
    public WeatherProvider getProvider() {
        return WeatherProvider.AMAP;
    }

    @Override
    public boolean isConfigured() {
        return config != null && StrUtil.isAllNotBlank(config.getApiKey(), config.getGeocodeUrl(),
                config.getWeatherUrl());
    }

    @Override
    @Cacheable(cacheNames = "tripWeatherGaode#10m",
            key = "#city == null ? '' : #city.trim().toLowerCase()",
            unless = "#result == null")
    public CurrentWeather getCurrentWeather(String city) {
        validateConfig();
        if (StrUtil.isBlank(city)) {
            throw new IllegalArgumentException("城市名称不能为空");
        }
        String requestedCity = city.trim();
        log.info("[getCurrentWeather][开始调用高德天气接口，city({})]", requestedCity);
        String adcode = requestedCity.matches(ADCODE_PATTERN) ? requestedCity : resolveAdcode(requestedCity);
        AmapWeatherRespDTO response = queryWeather(adcode, requestedCity);
        AmapWeatherRespDTO.Live live = response.getLives().get(0);
        CurrentWeather weather = new CurrentWeather(StrUtil.blankToDefault(live.getCity(), requestedCity),
                parseInteger(live.getTemperature()), live.getWeather(), parseInteger(live.getHumidity()),
                live.getWinddirection(), live.getWindpower(), live.getReporttime());
        log.info("[getCurrentWeather][高德天气查询成功，requestCity({}) adcode({}) responseCity({}) queryTime({})]",
                requestedCity, adcode, weather.city(), weather.queryTime());
        return weather;
    }

    private String resolveAdcode(String city) {
        URI uri = UriComponentsBuilder.fromUriString(config.getGeocodeUrl())
                .queryParam("address", city)
                .queryParam("key", config.getApiKey())
                .queryParam("output", "JSON")
                .build().encode().toUri();
        AmapGeocodeRespDTO response;
        try {
            response = restTemplate.getForObject(uri, AmapGeocodeRespDTO.class);
        } catch (RestClientException exception) {
            log.error("[resolveAdcode][调用高德地理编码接口失败，city({})，exception({})]",
                    city, exception.getClass().getSimpleName());
            throw new IllegalStateException("调用高德地理编码接口失败");
        }
        if (!isSuccess(response == null ? null : response.getStatus(),
                response == null ? null : response.getInfocode())
                || CollUtil.isEmpty(response.getGeocodes())
                || StrUtil.isBlank(response.getGeocodes().get(0).getAdcode())) {
            throw new IllegalStateException("高德地理编码失败：" + getErrorInfo(response));
        }
        String adcode = response.getGeocodes().get(0).getAdcode();
        log.info("[resolveAdcode][高德地理编码成功，city({}) adcode({})]", city, adcode);
        return adcode;
    }

    private AmapWeatherRespDTO queryWeather(String adcode, String requestedCity) {
        URI uri = UriComponentsBuilder.fromUriString(config.getWeatherUrl())
                .queryParam("city", adcode)
                .queryParam("key", config.getApiKey())
                .queryParam("extensions", "base")
                .queryParam("output", "JSON")
                .build().encode().toUri();
        AmapWeatherRespDTO response;
        try {
            response = restTemplate.getForObject(uri, AmapWeatherRespDTO.class);
        } catch (RestClientException exception) {
            log.error("[queryWeather][调用高德天气接口失败，city({})，adcode({})，exception({})]",
                    requestedCity, adcode, exception.getClass().getSimpleName());
            throw new IllegalStateException("调用高德天气接口失败");
        }
        if (!isSuccess(response == null ? null : response.getStatus(),
                response == null ? null : response.getInfocode()) || CollUtil.isEmpty(response.getLives())) {
            throw new IllegalStateException("高德天气查询失败：" + getErrorInfo(response));
        }
        return response;
    }

    private void validateConfig() {
        if (config == null || StrUtil.isBlank(config.getApiKey())) {
            throw new IllegalStateException("高德天气 API Key 未配置");
        }
        if (StrUtil.hasBlank(config.getGeocodeUrl(), config.getWeatherUrl())) {
            throw new IllegalStateException("高德天气接口地址未配置");
        }
    }

    private static boolean isSuccess(String status, String infoCode) {
        return SUCCESS_STATUS.equals(status) && SUCCESS_INFO_CODE.equals(infoCode);
    }

    private static String getErrorInfo(AmapGeocodeRespDTO response) {
        return response == null ? "接口无响应" : StrUtil.blankToDefault(response.getInfo(), response.getInfocode());
    }

    private static String getErrorInfo(AmapWeatherRespDTO response) {
        return response == null ? "接口无响应" : StrUtil.blankToDefault(response.getInfo(), response.getInfocode());
    }

    private static Integer parseInteger(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

}
