package cn.iocoder.yudao.module.gift.service.itinerary.provider.place.gaode;

import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.core.AmapPlaceSearchClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.place.AmapPoiTypeEnum;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.place.TravelPlaceQueryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GaodeTravelPlaceQueryClientTest {

    private AmapPlaceSearchClient amapPlaceSearchClient;
    private GaodeTravelPlaceQueryClient client;

    @BeforeEach
    void setUp() {
        amapPlaceSearchClient = mock(AmapPlaceSearchClient.class);
        client = new GaodeTravelPlaceQueryClient(amapPlaceSearchClient);
    }

    @Test
    void queryMapsTravelRequestAndResponse() {
        when(amapPlaceSearchClient.search(any())).thenReturn(result());

        TravelPlaceQueryClient.Response response = client.query(new TravelPlaceQueryClient.Request()
                .setType(AmapPoiTypeEnum.FOOD).setRegion("杭州市").setKeyword("杭帮菜")
                .setLocation("120.1,30.2").setRadius(1_500).setLimit(2).setPage(3));

        assertTrue(response.getSuccess());
        assertEquals("杭州示例餐厅", response.getPlaces().get(0).getName());
        assertEquals("4.5", response.getPlaces().get(0).getRating());
        assertEquals("10:00-22:00", response.getPlaces().get(0).getBusinessHours());
        ArgumentCaptor<AmapPlaceSearchClient.SearchRequest> captor =
                ArgumentCaptor.forClass(AmapPlaceSearchClient.SearchRequest.class);
        verify(amapPlaceSearchClient).search(captor.capture());
        assertEquals(AmapPoiTypeEnum.FOOD.getAmapTypeCode(), captor.getValue().typeCode());
        assertEquals(new BigDecimal("120.1"), captor.getValue().longitude());
        assertEquals(new BigDecimal("30.2"), captor.getValue().latitude());
        assertEquals(1_500, captor.getValue().radius());
        assertEquals(3, captor.getValue().pageNo());
    }

    @Test
    void getPlaceDetailReusesAmapClient() {
        when(amapPlaceSearchClient.getPlaceDetail("B0FFDETAIL")).thenReturn(result());

        TravelPlaceQueryClient.Response response = client.getPlaceDetail("B0FFDETAIL");

        assertTrue(response.getSuccess());
        assertEquals("B0FFDETAIL", response.getPlaces().get(0).getPoiId());
        verify(amapPlaceSearchClient).getPlaceDetail("B0FFDETAIL");
    }

    private static AmapPlaceSearchClient.SearchResult result() {
        AmapPlaceSearchClient.Place place = new AmapPlaceSearchClient.Place(
                "B0FFDETAIL", "杭州示例餐厅", "西湖区", new BigDecimal("120.1"), new BigDecimal("30.2"),
                "餐饮服务", "050000", "浙江省", "杭州市", "西湖区", "330106", 100L,
                "0571-12345678", "https://example.com/food.jpg", "4.5", "88", "杭帮菜", "10:00-22:00");
        return new AmapPlaceSearchClient.SearchResult(1L, List.of(place));
    }

}
