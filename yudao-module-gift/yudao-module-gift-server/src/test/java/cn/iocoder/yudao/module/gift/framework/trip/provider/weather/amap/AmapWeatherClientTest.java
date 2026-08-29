package cn.iocoder.yudao.module.gift.framework.trip.provider.weather.amap;

import cn.iocoder.yudao.module.gift.framework.trip.provider.config.TripProviderProperties;
import cn.iocoder.yudao.module.gift.framework.trip.provider.weather.WeatherClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AmapWeatherClientTest {

    private static final String GEOCODE_URL = "https://example.com/v3/geocode/geo";
    private static final String WEATHER_URL = "https://example.com/v3/weather/weatherInfo";

    private MockRestServiceServer server;
    private AmapWeatherClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        TripProviderProperties.Weather.Amap config = new TripProviderProperties.Weather.Amap()
                .setApiKey("test-api-key")
                .setGeocodeUrl(GEOCODE_URL)
                .setWeatherUrl(WEATHER_URL);
        client = new AmapWeatherClient(restTemplate, config);
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void testGetCurrentWeather_byCityName() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(GEOCODE_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("address", "%E5%8C%97%E4%BA%AC%E5%B8%82%E6%9C%9D%E9%98%B3%E5%8C%BA"))
                .andExpect(queryParam("key", "test-api-key"))
                .andExpect(queryParam("output", "JSON"))
                .andRespond(withSuccess("""
                        {"status":"1","info":"OK","infocode":"10000",
                         "geocodes":[{"adcode":"110105"}]}
                        """, MediaType.APPLICATION_JSON));
        expectWeatherRequest("110105", "朝阳区");

        WeatherClient.CurrentWeather result = client.getCurrentWeather("北京市朝阳区");

        assertEquals("朝阳区", result.city());
        assertEquals(29, result.temperature());
        assertEquals("晴", result.condition());
        assertEquals(63, result.humidity());
        assertEquals("东南", result.windDirection());
        assertEquals("≤3", result.windPower());
        assertEquals("2026-08-23 20:02:31", result.queryTime());
    }

    @Test
    void testGetCurrentWeather_byAdcode() {
        expectWeatherRequest("110105", "朝阳区");

        WeatherClient.CurrentWeather result = client.getCurrentWeather("110105");

        assertEquals("朝阳区", result.city());
    }

    @Test
    void testGetCurrentWeather_geocodeFailed() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(GEOCODE_URL)))
                .andRespond(withSuccess("""
                        {"status":"0","info":"INVALID_USER_KEY","infocode":"10001","geocodes":[]}
                        """, MediaType.APPLICATION_JSON));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.getCurrentWeather("北京"));

        assertEquals("高德地理编码失败：INVALID_USER_KEY", exception.getMessage());
    }

    @Test
    void testGetCurrentWeather_apiKeyMissing() {
        client = new AmapWeatherClient(new RestTemplate(),
                new TripProviderProperties.Weather.Amap().setApiKey(""));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.getCurrentWeather("北京"));

        assertEquals("高德天气 API Key 未配置", exception.getMessage());
    }

    private void expectWeatherRequest(String adcode, String city) {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(WEATHER_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("city", adcode))
                .andExpect(queryParam("key", "test-api-key"))
                .andExpect(queryParam("extensions", "base"))
                .andExpect(queryParam("output", "JSON"))
                .andRespond(withSuccess("""
                        {"status":"1","count":"1","info":"OK","infocode":"10000",
                         "lives":[{"city":"%s","adcode":"%s","weather":"晴","temperature":"29",
                                   "winddirection":"东南","windpower":"≤3","humidity":"63",
                                   "reporttime":"2026-08-23 20:02:31"}]}
                        """.formatted(city, adcode), MediaType.APPLICATION_JSON));
    }

}
