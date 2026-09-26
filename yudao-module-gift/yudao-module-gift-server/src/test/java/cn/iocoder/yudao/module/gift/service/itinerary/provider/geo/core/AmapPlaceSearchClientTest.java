package cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.core;

import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.config.AmapProperties;
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

    private static final String AROUND_URL = "https://example.com/v5/place/around";
    private static final String TEXT_URL = "https://example.com/v5/place/text";
    private static final String DETAIL_URL = "https://example.com/v5/place/detail";

    private MockRestServiceServer server;
    private AmapPlaceSearchClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        client = new AmapPlaceSearchClient(restTemplate, new AmapProperties().setKey("test-key")
                .setPlaceAroundSearchUrl(AROUND_URL).setPlaceSearchUrl(TEXT_URL).setPlaceDetailUrl(DETAIL_URL));
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void testSearchAroundByKeywordAndType() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(AROUND_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("key", "test-key"))
                .andExpect(queryParam("keywords", "%E8%A5%BF%E6%B9%96"))
                .andExpect(queryParam("types", "110000"))
                .andExpect(queryParam("location", "120.15507,30.274084"))
                .andExpect(queryParam("radius", "5000"))
                .andExpect(queryParam("sortrule", "distance"))
                .andExpect(queryParam("page_num", "1"))
                .andExpect(queryParam("page_size", "20"))
                .andRespond(withSuccess(successResponse("328"), MediaType.APPLICATION_JSON));

        AmapPlaceSearchClient.SearchResult result = client.search(
                new AmapPlaceSearchClient.SearchRequest("西湖", "110000", null, new BigDecimal("120.155070"),
                        new BigDecimal("30.274084"), 5000, 1, 20));

        assertEquals(1L, result.total());
        assertEquals(1, result.places().size());
        AmapPlaceSearchClient.Place place = result.places().get(0);
        assertEquals("B0FFG2URKG", place.poiId());
        assertEquals("杭州西湖风景名胜区", place.name());
        assertEquals(new BigDecimal("120.155070"), place.longitude());
        assertEquals(new BigDecimal("30.274084"), place.latitude());
        assertEquals("0571-12345678", place.telephone());
        assertEquals("https://example.com/west-lake.jpg", place.photoUrl());
        assertEquals("4.8", place.rating());
        assertEquals("08:00-18:00", place.businessHours());
        assertEquals(328L, place.distanceMeters());
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
                new AmapPlaceSearchClient.SearchRequest("咖啡", null, null, new BigDecimal("120.155070"),
                        new BigDecimal("30.274084"), 3000, 1, 10));

        assertEquals(328L, result.places().get(0).distanceMeters());
    }

    @Test
    void testSearchApiFailed() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(AROUND_URL)))
                .andRespond(withSuccess("""
                        {"status":"0","info":"INVALID_USER_KEY","infocode":"10001","pois":[]}
                        """, MediaType.APPLICATION_JSON));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> client.search(
                new AmapPlaceSearchClient.SearchRequest("西湖", null, null, new BigDecimal("120.155070"),
                        new BigDecimal("30.274084"), 5000, 1, 20)));

        assertEquals("高德地点搜索失败：INVALID_USER_KEY", exception.getMessage());
    }

    @Test
    void testSearchCoordinateIncomplete() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> client.search(
                new AmapPlaceSearchClient.SearchRequest("西湖", null, null, new BigDecimal("120.155070"),
                        null, 5000, 1, 20)));

        assertEquals("经纬度必须同时提供", exception.getMessage());
    }

    @Test
    void testSearchConditionMissing() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> client.search(
                new AmapPlaceSearchClient.SearchRequest(null, null, null, new BigDecimal("120.155070"),
                        new BigDecimal("30.274084"), 5000, 1, 20)));

        assertEquals("地点搜索关键词和 POI 类型至少提供一个", exception.getMessage());
    }

    @Test
    void testSearchByCity() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(TEXT_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("keywords", "%E8%A5%BF%E6%B9%96"))
                .andExpect(queryParam("types", "110000"))
                .andExpect(queryParam("region", "%E6%9D%AD%E5%B7%9E%E5%B8%82"))
                .andExpect(queryParam("city_limit", "true"))
                .andExpect(queryParam("page_num", "1"))
                .andExpect(queryParam("page_size", "20"))
                .andRespond(withSuccess(successResponse(""), MediaType.APPLICATION_JSON));

        AmapPlaceSearchClient.SearchResult result = client.search(
                new AmapPlaceSearchClient.SearchRequest("西湖", "110000", "杭州市", null,
                        null, 5000, 1, 20));

        assertEquals(1L, result.total());
        assertNull(result.places().get(0).distanceMeters());
    }

    @Test
    void testGetPlaceDetail() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(DETAIL_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("id", "B0FFG2URKG"))
                .andRespond(withSuccess(successResponse(""), MediaType.APPLICATION_JSON));

        AmapPlaceSearchClient.SearchResult result = client.getPlaceDetail("B0FFG2URKG");

        assertEquals(1, result.places().size());
        assertEquals("杭州西湖风景名胜区", result.places().get(0).name());
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
                    "business":{"tel":"0571-12345678","rating":"4.8","business_time":"08:00-18:00"},
                    "photos":[{"url":"https://example.com/west-lake.jpg"}]
                  }]
                }
                """.formatted(distance);
    }

}
