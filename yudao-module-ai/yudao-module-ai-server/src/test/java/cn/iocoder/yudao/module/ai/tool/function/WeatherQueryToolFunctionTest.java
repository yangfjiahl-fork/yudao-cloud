package cn.iocoder.yudao.module.ai.tool.function;

import cn.iocoder.yudao.module.ai.framework.ai.core.weather.WeatherClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.weather.WeatherClientFacade;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WeatherQueryToolFunctionTest {

    private final WeatherClientFacade weatherClientFacade = mock(WeatherClientFacade.class);
    private final WeatherQueryToolFunction function = new WeatherQueryToolFunction(weatherClientFacade);

    @Test
    void testApply() {
        when(weatherClientFacade.getCurrentWeather("北京")).thenReturn(new WeatherClient.CurrentWeather(
                "北京市", 28, "多云", 70, "东风", "2级", "2026-08-23 19:30:00"));
        WeatherQueryToolFunction.Request request = new WeatherQueryToolFunction.Request().setCity(" 北京 ");

        WeatherQueryToolFunction.Response response = function.apply(request);

        assertEquals("北京市", response.getCity());
        assertEquals(28, response.getWeatherInfo().getTemperature());
        assertEquals("多云", response.getWeatherInfo().getCondition());
        assertEquals(70, response.getWeatherInfo().getHumidity());
        assertEquals("东风", response.getWeatherInfo().getWindDirection());
        assertEquals("2级", response.getWeatherInfo().getWindPower());
        assertEquals("2026-08-23 19:30:00", response.getWeatherInfo().getQueryTime());
    }

    @Test
    void testApply_cityBlank() {
        WeatherQueryToolFunction.Request request = new WeatherQueryToolFunction.Request().setCity(" ");

        WeatherQueryToolFunction.Response response = function.apply(request);

        assertEquals("未知城市", response.getCity());
        assertNull(response.getWeatherInfo());
        verifyNoInteractions(weatherClientFacade);
    }

}
