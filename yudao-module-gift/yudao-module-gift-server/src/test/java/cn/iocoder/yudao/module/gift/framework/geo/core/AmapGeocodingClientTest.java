package cn.iocoder.yudao.module.gift.framework.geo.core;

import cn.iocoder.yudao.module.gift.framework.geo.config.AmapProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AmapGeocodingClientTest {

    private static final String URL = "https://example.com/v3/geocode/regeo";

    private MockRestServiceServer server;
    private AmapGeocodingClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        client = new AmapGeocodingClient(restTemplate, new AmapProperties()
                .setKey("test-key")
                .setReverseGeocodingUrl(URL));
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void testReverseGeocode() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("key", "test-key"))
                .andExpect(queryParam("location", "120.15507,30.274084"))
                .andExpect(queryParam("extensions", "base"))
                .andExpect(queryParam("output", "JSON"))
                .andRespond(withSuccess("""
                        {
                          "status": "1",
                          "info": "OK",
                          "regeocode": {
                            "formatted_address": "浙江省杭州市西湖区西湖街道",
                            "addressComponent": {
                              "province": "浙江省",
                              "city": "杭州市",
                              "district": "西湖区",
                              "adcode": "330106"
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        AmapGeocodingClient.Location result = client.reverseGeocode(
                new BigDecimal("120.155070"), new BigDecimal("30.274084"));

        assertEquals("浙江省", result.province());
        assertEquals("杭州市", result.city());
        assertEquals("西湖区", result.district());
        assertEquals("330106", result.adcode());
        assertEquals("浙江省杭州市西湖区西湖街道", result.formattedAddress());
    }

    @Test
    void testReverseGeocodeMunicipality() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(URL)))
                .andRespond(withSuccess("""
                        {
                          "status": "1",
                          "info": "OK",
                          "regeocode": {
                            "formatted_address": "北京市海淀区燕园街道",
                            "addressComponent": {
                              "province": "北京市",
                              "city": [],
                              "district": "海淀区",
                              "adcode": "110108"
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        AmapGeocodingClient.Location result = client.reverseGeocode(
                new BigDecimal("116.310003"), new BigDecimal("39.991957"));

        assertEquals("北京市", result.city());
    }

    @Test
    void testReverseGeocodeProvinceDirectCounty() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(URL)))
                .andRespond(withSuccess("""
                        {
                          "status": "1",
                          "info": "OK",
                          "regeocode": {
                            "formatted_address": "河南省济源市沁园街道",
                            "addressComponent": {
                              "province": "河南省",
                              "city": [],
                              "district": "济源市",
                              "adcode": "419001"
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        AmapGeocodingClient.Location result = client.reverseGeocode(
                new BigDecimal("112.590047"), new BigDecimal("35.090378"));

        assertEquals("济源市", result.city());
    }

    @Test
    void testReverseGeocodeApiFailed() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(URL)))
                .andRespond(withSuccess("""
                        {"status": "0", "info": "INVALID_USER_KEY", "regeocode": []}
                        """, MediaType.APPLICATION_JSON));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.reverseGeocode(new BigDecimal("120.155070"), new BigDecimal("30.274084")));

        assertEquals("高德逆地理编码失败：INVALID_USER_KEY", exception.getMessage());
    }

    @Test
    void testReverseGeocodeKeyMissing() {
        client = new AmapGeocodingClient(new RestTemplate(), new AmapProperties().setKey(""));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.reverseGeocode(new BigDecimal("120.155070"), new BigDecimal("30.274084")));

        assertEquals("高德 Web 服务 API Key 未配置", exception.getMessage());
    }

}
