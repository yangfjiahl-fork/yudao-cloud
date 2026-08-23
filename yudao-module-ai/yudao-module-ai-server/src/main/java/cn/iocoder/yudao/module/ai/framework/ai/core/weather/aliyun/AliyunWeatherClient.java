package cn.iocoder.yudao.module.ai.framework.ai.core.weather.aliyun;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.config.YudaoAiProperties;
import cn.iocoder.yudao.module.ai.framework.ai.core.weather.WeatherClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.weather.WeatherProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 阿里云市场天气客户端
 *
 * @see <a href="https://market.aliyun.com/detail/cmapi010812">天气预报查询</a>
 */
@Slf4j
@AllArgsConstructor
public class AliyunWeatherClient implements WeatherClient {

    private static final Integer SUCCESS_CODE = 0;
    private static final DateTimeFormatter RESPONSE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter RESULT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final YudaoAiProperties.Weather config;

    @Override
    public WeatherProvider getProvider() {
        return WeatherProvider.ALIYUN;
    }

    @Override
    public boolean isConfigured() {
        return config != null && StrUtil.isNotBlank(config.getAppCode()) && StrUtil.isNotBlank(config.getUrl());
    }

    /**
     * 查询城市当前天气
     *
     * @param city 城市名称
     * @return 当前天气
     */
    @Override
    public CurrentWeather getCurrentWeather(String city) {
        validateConfig();
        if (StrUtil.isBlank(city)) {
            throw new IllegalArgumentException("城市名称不能为空");
        }

        URI uri = UriComponentsBuilder.fromUriString(config.getUrl())
                .queryParam("area", city)
                .queryParam("needMoreDay", "0")
                .queryParam("needIndex", "0")
                .queryParam("need3HourForcast", "0")
                .queryParam("needAlarm", "0")
                .queryParam("needHourData", "0")
                .build().encode().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "APPCODE " + config.getAppCode());
        try {
            ResponseEntity<AliyunWeatherRespDTO> responseEntity = restTemplate.exchange(uri, HttpMethod.GET,
                    new HttpEntity<>(headers), AliyunWeatherRespDTO.class);
            return convertResponse(city, responseEntity.getBody());
        } catch (RestClientException ex) {
            log.error("[getCurrentWeather][调用阿里云天气接口失败，city({})]", city, ex);
            throw new IllegalStateException("调用阿里云天气接口失败", ex);
        }
    }

    private CurrentWeather convertResponse(String requestedCity, AliyunWeatherRespDTO response) {
        if (response == null || !SUCCESS_CODE.equals(response.getCode()) || response.getBody() == null
                || !SUCCESS_CODE.equals(response.getBody().getCode()) || response.getBody().getNow() == null) {
            String error = response == null ? "接口无响应" : StrUtil.blankToDefault(response.getError(),
                    response.getBody() == null ? "响应数据为空" : "ret_code=" + response.getBody().getCode());
            throw new IllegalStateException("阿里云天气查询失败：" + error);
        }
        AliyunWeatherRespDTO.Body body = response.getBody();
        AliyunWeatherRespDTO.Now now = body.getNow();
        String city = body.getCityInfo() == null
                ? requestedCity : StrUtil.blankToDefault(body.getCityInfo().getC3(), requestedCity);
        return new CurrentWeather(city, parseInteger(now.getTemperature()), now.getWeather(),
                parsePercentage(now.getSd()), now.getWindDirection(), now.getWindPower(), formatTime(body.getTime()));
    }

    private void validateConfig() {
        if (config == null || StrUtil.isBlank(config.getAppCode())) {
            throw new IllegalStateException("阿里云天气 AppCode 未配置");
        }
        if (StrUtil.isBlank(config.getUrl())) {
            throw new IllegalStateException("阿里云天气接口地址未配置");
        }
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

    private static Integer parsePercentage(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        return parseInteger(StrUtil.removeSuffix(value, "%"));
    }

    private static String formatTime(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, RESPONSE_TIME_FORMATTER).format(RESULT_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
            return value;
        }
    }

}
