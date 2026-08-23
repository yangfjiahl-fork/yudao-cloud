package cn.iocoder.yudao.module.ai.framework.ai.core.weather;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.weather.amap.AmapWeatherClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.weather.aliyun.AliyunWeatherClient;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WeatherClientRouterTest {

    private final ConfigApi configApi = mock(ConfigApi.class);
    private final AliyunWeatherClient aliyunWeatherClient = mock(AliyunWeatherClient.class);
    private final AmapWeatherClient amapWeatherClient = mock(AmapWeatherClient.class);
    private final WeatherClientFacade facade = new WeatherClientFacade(
            configApi, aliyunWeatherClient, amapWeatherClient);

    @Test
    void testGetCurrentWeather_gaode() {
        WeatherClient.CurrentWeather expected = weather("北京");
        when(configApi.getConfigValueByKey(WeatherClientFacade.QUERY_FROM_CONFIG_KEY))
                .thenReturn(CommonResult.success("gaode"));
        when(amapWeatherClient.getCurrentWeather("北京")).thenReturn(expected);

        WeatherClient.CurrentWeather result = facade.getCurrentWeather("北京");

        assertSame(expected, result);
        verify(aliyunWeatherClient, never()).getCurrentWeather("北京");
    }

    @Test
    void testGetCurrentWeather_aliyun() {
        WeatherClient.CurrentWeather expected = weather("上海");
        when(configApi.getConfigValueByKey(WeatherClientFacade.QUERY_FROM_CONFIG_KEY))
                .thenReturn(CommonResult.success("aliyun"));
        when(aliyunWeatherClient.getCurrentWeather("上海")).thenReturn(expected);

        WeatherClient.CurrentWeather result = facade.getCurrentWeather("上海");

        assertSame(expected, result);
        verify(amapWeatherClient, never()).getCurrentWeather("上海");
    }

    @Test
    void testGetCurrentWeather_configMissingUseAliyun() {
        WeatherClient.CurrentWeather expected = weather("广州");
        when(configApi.getConfigValueByKey(WeatherClientFacade.QUERY_FROM_CONFIG_KEY))
                .thenReturn(CommonResult.success(null));
        when(aliyunWeatherClient.getCurrentWeather("广州")).thenReturn(expected);

        WeatherClient.CurrentWeather result = facade.getCurrentWeather("广州");

        assertSame(expected, result);
        verify(amapWeatherClient, never()).getCurrentWeather("广州");
    }

    @Test
    void testGetCurrentWeather_noFallback() {
        IllegalStateException expected = new IllegalStateException("gaode failed");
        when(configApi.getConfigValueByKey(WeatherClientFacade.QUERY_FROM_CONFIG_KEY))
                .thenReturn(CommonResult.success("gaode"));
        when(amapWeatherClient.getCurrentWeather("北京")).thenThrow(expected);

        IllegalStateException result = assertThrows(IllegalStateException.class,
                () -> facade.getCurrentWeather("北京"));

        assertSame(expected, result);
        verify(aliyunWeatherClient, never()).getCurrentWeather("北京");
    }

    private static WeatherClient.CurrentWeather weather(String city) {
        return new WeatherClient.CurrentWeather(city, 28, "晴", 60, "东风", "2", "2026-08-23 20:00:00");
    }

}
