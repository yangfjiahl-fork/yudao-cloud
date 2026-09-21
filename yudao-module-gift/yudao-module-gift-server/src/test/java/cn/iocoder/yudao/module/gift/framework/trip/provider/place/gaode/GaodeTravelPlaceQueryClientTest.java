package cn.iocoder.yudao.module.gift.framework.trip.provider.place.gaode;

import cn.iocoder.yudao.module.gift.framework.trip.provider.config.TripProviderProperties;
import cn.iocoder.yudao.module.gift.framework.trip.provider.place.AmapPoiTypeEnum;
import cn.iocoder.yudao.module.gift.framework.trip.provider.place.TravelPlaceQueryClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GaodeTravelPlaceQueryClientTest {

    private static final String AMAP_URL = "https://example.com/v5/place/text";
    private static final String AMAP_AROUND_URL = "https://example.com/v5/place/around";
    private static final String AMAP_DETAIL_URL = "https://example.com/v5/place/detail";

    private MockRestServiceServer server;
    private GaodeTravelPlaceQueryClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        TripProviderProperties.TravelPlace config = new TripProviderProperties.TravelPlace()
                .setAmapUrl(AMAP_URL).setAmapAroundUrl(AMAP_AROUND_URL).setAmapDetailUrl(AMAP_DETAIL_URL)
                .setAmapKey("test-amap-key");
        client = new GaodeTravelPlaceQueryClient(restTemplate, config);
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void queryHotel() {
        server.expect(requestTo(AMAP_URL + "?key=test-amap-key&types=" + AmapPoiTypeEnum.HOTEL.getAmapTypeCode() + "&region="
                        + UriUtils.encodeQueryParam("杭州市", StandardCharsets.UTF_8)
                        + "&city_limit=true&show_fields=business,photos&page_size=2&page_num=1&output=json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"status":"1","info":"OK","infocode":"10000","pois":[{
                          "id":"B0FFH0TEL","name":"杭州示例酒店","typecode":"100000","location":"120.1,30.2","address":"西湖区",
                          "photos":[{"url":"https://example.com/hotel.jpg"}],
                          "business":{"tel":"0571-12345678","rating":"4.5","cost":"600"}
                        }]}
                        """, MediaType.APPLICATION_JSON));

        TravelPlaceQueryClient.Response response = client.query(new TravelPlaceQueryClient.Request()
                .setType(AmapPoiTypeEnum.HOTEL).setRegion("杭州市").setLimit(2));

        assertTrue(response.getSuccess());
        assertEquals("杭州示例酒店", response.getPlaces().get(0).getName());
        assertEquals("4.5", response.getPlaces().get(0).getRating());
        assertEquals("https://example.com/hotel.jpg", response.getPlaces().get(0).getImageUrl());
    }

    @Test
    void queryRestaurant() {
        server.expect(requestTo(AMAP_URL + "?key=test-amap-key&types=" + AmapPoiTypeEnum.FOOD.getAmapTypeCode() + "&region="
                        + UriUtils.encodeQueryParam("杭州市", StandardCharsets.UTF_8)
                        + "&city_limit=true&show_fields=business,photos&page_size=1&page_num=1&output=json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"status":"1","info":"OK","infocode":"10000","pois":[{
                          "id":"B0FFREST","name":"杭州示例餐厅","typecode":"050000","location":"120.3,30.4",
                          "business":{"tag":"杭帮菜","cost":"88","business_time":"10:00-22:00"}
                        }]}
                        """, MediaType.APPLICATION_JSON));

        TravelPlaceQueryClient.Response response = client.query(new TravelPlaceQueryClient.Request()
                .setType(AmapPoiTypeEnum.FOOD).setRegion("杭州市").setLimit(1));

        assertTrue(response.getSuccess());
        assertEquals("杭州示例餐厅", response.getPlaces().get(0).getName());
        assertEquals("杭帮菜", response.getPlaces().get(0).getTag());
        assertEquals("10:00-22:00", response.getPlaces().get(0).getBusinessHours());
    }

    @Test
    void queryRestaurantAround() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(AMAP_AROUND_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("types", AmapPoiTypeEnum.FOOD.getAmapTypeCode()))
                .andExpect(queryParam("location", "120.1,30.2"))
                .andExpect(queryParam("radius", "1500"))
                .andRespond(withSuccess("""
                        {"status":"1","info":"OK","infocode":"10000","pois":[{
                          "id":"B0FFAROUND","name":"西湖边餐厅","typecode":"050000","location":"120.101,30.201"
                        }]}
                        """, MediaType.APPLICATION_JSON));

        TravelPlaceQueryClient.Response response = client.query(new TravelPlaceQueryClient.Request()
                .setType(AmapPoiTypeEnum.FOOD).setRegion("杭州市")
                .setLocation("120.1,30.2").setRadius(1_500).setLimit(1));

        assertTrue(response.getSuccess());
        assertEquals("西湖边餐厅", response.getPlaces().get(0).getName());
    }

    @Test
    void queryShoppingWithKeyword() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(AMAP_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("types", AmapPoiTypeEnum.SHOPPING.getAmapTypeCode()))
                .andExpect(queryParam("keywords", UriUtils.encodeQueryParam("商场", StandardCharsets.UTF_8)))
                .andRespond(withSuccess("""
                        {"status":"1","info":"OK","infocode":"10000","pois":[{
                          "id":"B0FFSHOP","name":"杭州示例商场","typecode":"060100","location":"120.2,30.3"
                        }]}
                        """, MediaType.APPLICATION_JSON));

        TravelPlaceQueryClient.Response response = client.query(new TravelPlaceQueryClient.Request()
                .setType(AmapPoiTypeEnum.SHOPPING).setRegion("杭州市").setKeyword("商场").setLimit(1));

        assertTrue(response.getSuccess());
        assertEquals("B0FFSHOP", response.getPlaces().get(0).getPoiId());
    }

    @Test
    void getPlaceDetail() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(AMAP_DETAIL_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("key", "test-amap-key"))
                .andExpect(queryParam("id", "B0FFDETAIL"))
                .andExpect(queryParam("show_fields", "business,photos"))
                .andExpect(queryParam("output", "json"))
                .andRespond(withSuccess("""
                        {"status":"1","info":"OK","infocode":"10000","pois":[{
                          "id":"B0FFDETAIL","name":"西湖景区","typecode":"110000",
                          "location":"120.1001,30.2002","address":"杭州市西湖区",
                          "photos":[{"url":"https://example.com/scenic.jpg"}],
                          "business":{"tel":"0571-12345678","rating":"4.8","business_time":"08:00-18:00"}
                        }]}
                        """, MediaType.APPLICATION_JSON));

        TravelPlaceQueryClient.Response response = client.getPlaceDetail("B0FFDETAIL");

        assertTrue(response.getSuccess());
        TravelPlaceQueryClient.Place place = response.getPlaces().get(0);
        assertEquals("B0FFDETAIL", place.getPoiId());
        assertEquals("西湖景区", place.getName());
        assertEquals("120.1001", place.getLongitude());
        assertEquals("30.2002", place.getLatitude());
        assertEquals("4.8", place.getRating());
        assertEquals("08:00-18:00", place.getBusinessHours());
    }

}
