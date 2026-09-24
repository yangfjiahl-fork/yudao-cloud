package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.core.AmapGeocodingClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.core.AmapPlaceSearchClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItineraryLocationServiceImplTest extends BaseMockitoUnitTest {

    @Mock
    private AmapGeocodingClient amapGeocodingClient;
    @Mock
    private AmapPlaceSearchClient amapPlaceSearchClient;
    @InjectMocks
    private ItineraryLocationServiceImpl service;

    @Test
    void reverseGeocodeConvertsProviderResult() {
        BigDecimal longitude = new BigDecimal("120.155070");
        BigDecimal latitude = new BigDecimal("30.274084");
        when(amapGeocodingClient.reverseGeocode(longitude, latitude)).thenReturn(new AmapGeocodingClient.Location(
                "浙江省", "杭州市", "西湖区", "330106", "浙江省杭州市西湖区西湖街道"));

        ItineraryLocationService.Location result = service.reverseGeocode(longitude, latitude);

        assertEquals("杭州市", result.city());
        assertEquals("330106", result.adcode());
    }

    @Test
    void searchNearbyPlacesReusesPoiCategoryMapping() {
        AmapPlaceSearchClient.Place providerPlace = new AmapPlaceSearchClient.Place(
                "poi-1", "示例餐厅", "西湖区", new BigDecimal("120.1"), new BigDecimal("30.2"),
                "餐饮服务", "050100", "浙江省", "杭州市", "西湖区", "330106", 328L,
                "0571-12345678", "https://example.com/food.jpg");
        when(amapPlaceSearchClient.search(any()))
                .thenReturn(new AmapPlaceSearchClient.SearchResult(1L, List.of(providerPlace)));

        ItineraryLocationService.PlaceSearchResult result = service.searchNearbyPlaces(
                new ItineraryLocationService.PlaceSearchRequest("咖啡", "food", new BigDecimal("120.1"),
                        new BigDecimal("30.2"), 3000, 1, 20));

        ArgumentCaptor<AmapPlaceSearchClient.SearchRequest> captor =
                ArgumentCaptor.forClass(AmapPlaceSearchClient.SearchRequest.class);
        verify(amapPlaceSearchClient).search(captor.capture());
        assertEquals("050000", captor.getValue().typeCode());
        assertEquals("poi-1", result.places().get(0).poiId());
        assertEquals(328L, result.places().get(0).distanceMeters());
    }

}
