package cn.iocoder.yudao.module.ai.framework.ai.core.weather.aliyun;

import cn.iocoder.yudao.module.ai.framework.ai.config.YudaoAiProperties;
import cn.iocoder.yudao.module.ai.framework.ai.core.weather.WeatherClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AliyunWeatherClientTest {

    private static final String URL = "https://example.com/area-to-weather";

    private MockRestServiceServer server;
    private AliyunWeatherClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        YudaoAiProperties.Weather config = new YudaoAiProperties.Weather()
                .setAppCode("test-app-code")
                .setUrl(URL);
        client = new AliyunWeatherClient(restTemplate, config);
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void testGetCurrentWeather() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "APPCODE test-app-code"))
                .andExpect(queryParam("area", "%E5%8C%97%E4%BA%AC"))
                .andExpect(queryParam("needMoreDay", "0"))
                .andExpect(queryParam("needIndex", "0"))
                .andExpect(queryParam("need3HourForcast", "0"))
                .andExpect(queryParam("needAlarm", "0"))
                .andExpect(queryParam("needHourData", "0"))
                .andRespond(withSuccess("""
                        {
                          "showapi_res_code": 0,
                          "showapi_res_error": "",
                          "showapi_res_body": {
                            "ret_code": 0,
                            "time": "20260823193000",
                            "cityInfo": {"c3": "北京市"},
                            "now": {
                              "temperature": "28",
                              "weather": "多云",
                              "sd": "70%",
                              "wind_direction": "东风",
                              "wind_power": "2级"
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        WeatherClient.CurrentWeather result = client.getCurrentWeather("北京");

        assertEquals("北京市", result.city());
        assertEquals(28, result.temperature());
        assertEquals("多云", result.condition());
        assertEquals(70, result.humidity());
        assertEquals("东风", result.windDirection());
        assertEquals("2级", result.windPower());
        assertEquals("2026-08-23 19:30:00", result.queryTime());
    }

    @Test
    void testGetCurrentWeather_apiFailed() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(URL)))
                .andRespond(withSuccess("""
                        {"showapi_res_code": 0, "showapi_res_error": "",
                         "showapi_res_body": {"ret_code": -1}}
                        """, MediaType.APPLICATION_JSON));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.getCurrentWeather("不存在的城市"));

        assertEquals("阿里云天气查询失败：ret_code=-1", exception.getMessage());
    }

    @Test
    void testGetCurrentWeather_appCodeMissing() {
        client = new AliyunWeatherClient(new RestTemplate(), new YudaoAiProperties.Weather().setAppCode(""));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.getCurrentWeather("北京"));

        assertEquals("阿里云天气 AppCode 未配置", exception.getMessage());
    }

}
