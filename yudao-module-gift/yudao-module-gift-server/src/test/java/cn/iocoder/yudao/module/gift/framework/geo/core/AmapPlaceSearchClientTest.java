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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AmapPlaceSearchClientTest {

    private static final String TEXT_URL = "https://example.com/v5/place/text";
    private static final String AROUND_URL = "https://example.com/v5/place/around";

    private MockRestServiceServer server;
    private AmapPlaceSearchClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        client = new AmapPlaceSearchClient(restTemplate, new AmapProperties().setKey("test-key")
                .setPlaceSearchUrl(TEXT_URL).setPlaceAroundSearchUrl(AROUND_URL));
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void testSearchByKeywordAndCity() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(TEXT_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("key", "test-key"))
                .andExpect(queryParam("keywords", "%E8%A5%BF%E6%B9%96"))
                .andExpect(queryParam("region", "%E6%9D%AD%E5%B7%9E%E5%B8%82"))
                .andExpect(queryParam("city_limit", "true"))
                .andExpect(queryParam("page_num", "1"))
                .andExpect(queryParam("page_size", "20"))
                .andRespond(withSuccess(successResponse(""), MediaType.APPLICATION_JSON));

        AmapPlaceSearchClient.SearchResult result = client.search(
                new AmapPlaceSearchClient.SearchRequest("西湖", "杭州市", null, null, 5000, 1, 20));

        assertEquals(1L, result.total());
        assertEquals(1, result.places().size());
        AmapPlaceSearchClient.Place place = result.places().get(0);
        assertEquals("B0FFG2URKG", place.poiId());
        assertEquals("杭州西湖风景名胜区", place.name());
        assertEquals(new BigDecimal("120.155070"), place.longitude());
        assertEquals(new BigDecimal("30.274084"), place.latitude());
        assertEquals("0571-12345678", place.telephone());
        assertEquals("https://example.com/west-lake.jpg", place.photoUrl());
        assertNull(place.distanceMeters());
    }

    @Test
    void testSearchAround() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(AROUND_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("location", "120.15507,30.274084"))
                .andExpect(queryParam("radius", "3000"))
                .andExpect(queryParam("sortrule", "distance"))
                .andRespond(withSuccess(successResponse("328"), MediaType.APPLICATION_JSON));

        AmapPlaceSearchClient.SearchResult result = client.search(
                new AmapPlaceSearchClient.SearchRequest("咖啡", null, new BigDecimal("120.155070"),
                        new BigDecimal("30.274084"), 3000, 1, 10));

        assertEquals(328L, result.places().get(0).distanceMeters());
    }

    @Test
    void testSearchApiFailed() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(TEXT_URL)))
                .andRespond(withSuccess("""
                        {"status":"0","info":"INVALID_USER_KEY","infocode":"10001","pois":[]}
                        """, MediaType.APPLICATION_JSON));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> client.search(
                new AmapPlaceSearchClient.SearchRequest("西湖", null, null, null, 5000, 1, 20)));

        assertEquals("高德地点搜索失败：INVALID_USER_KEY", exception.getMessage());
    }

    @Test
    void testSearchCoordinateIncomplete() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> client.search(
                new AmapPlaceSearchClient.SearchRequest("西湖", null, new BigDecimal("120.155070"),
                        null, 5000, 1, 20)));

        assertEquals("经纬度必须同时提供", exception.getMessage());
    }

    private static String successResponse(String distance) {
        return """
                {
                  "status":"1",
                  "info":"OK",
                  "infocode":"10000",
                  "count":"1",
                  "pois":[{
                    "id":"B0FFG2URKG",
                    "name":"杭州西湖风景名胜区",
                    "address":"浙江省杭州市西湖区龙井路1号",
                    "location":"120.155070,30.274084",
                    "type":"风景名胜;风景名胜;世界遗产",
                    "typecode":"110202",
                    "pname":"浙江省",
                    "cityname":"杭州市",
                    "adname":"西湖区",
                    "adcode":"330106",
                    "distance":"%s",
                    "business":{"tel":"0571-12345678"},
                    "photos":[{"url":"https://example.com/west-lake.jpg"}]
                  }]
                }
                """.formatted(distance);
    }

}
