package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.core.AmapGeocodingClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.core.AmapPlaceSearchClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.weather.WeatherClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.weather.amap.AmapWeatherClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class ItineraryLocationServiceImplTest extends BaseMockitoUnitTest {

    @Mock
    private AmapGeocodingClient amapGeocodingClient;
    @Mock
    private AmapPlaceSearchClient amapPlaceSearchClient;
    @Mock
    private AmapWeatherClient amapWeatherClient;
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
    void identifyCurrentCityUsesCoordinates() {
        BigDecimal longitude = new BigDecimal("120.155070");
        BigDecimal latitude = new BigDecimal("30.274084");
        when(amapGeocodingClient.reverseGeocode(longitude, latitude)).thenReturn(new AmapGeocodingClient.Location(
                "浙江省", "杭州市", "西湖区", "330106", "浙江省杭州市西湖区西湖街道"));

        ItineraryLocationService.Location result = service.identifyCurrentCity(
                longitude, latitude, "114.114.114.114");

        assertEquals("杭州市", result.city());
        verify(amapGeocodingClient, never()).locateByIp(any());
    }

    @Test
    void identifyCurrentCityFallsBackToAmapIpWhenCoordinateMissing() {
        when(amapGeocodingClient.locateByIp("114.114.114.114")).thenReturn(new AmapGeocodingClient.Location(
                "江苏省", "南京市", "", "320100", "江苏省南京市"));

        ItineraryLocationService.Location result = service.identifyCurrentCity(
                null, new BigDecimal("30.274084"), "114.114.114.114");

        assertEquals("南京市", result.city());
        verify(amapGeocodingClient).locateByIp("114.114.114.114");
        verify(amapGeocodingClient, never()).reverseGeocode(any(), any());
    }

    @Test
    void searchPlacesWithCoordinatesReusesPoiCategoryMapping() {
        AmapPlaceSearchClient.Place providerPlace = new AmapPlaceSearchClient.Place(
                "poi-1", "示例餐厅", "西湖区", new BigDecimal("120.1"), new BigDecimal("30.2"),
                "餐饮服务", "050100", "浙江省", "杭州市", "西湖区", "330106", 328L,
                "0571-12345678", "https://example.com/food.jpg", "4.6", "88", "杭帮菜", "10:00-22:00");
        when(amapPlaceSearchClient.search(any()))
                .thenReturn(new AmapPlaceSearchClient.SearchResult(1L, List.of(providerPlace)));

        ItineraryLocationService.PlaceSearchResult result = service.searchPlaces(
                new ItineraryLocationService.PlaceSearchRequest("咖啡", "food", null, new BigDecimal("120.1"),
                        new BigDecimal("30.2"), 3000, 1, 20));

        ArgumentCaptor<AmapPlaceSearchClient.SearchRequest> captor =
                ArgumentCaptor.forClass(AmapPlaceSearchClient.SearchRequest.class);
        verify(amapPlaceSearchClient).search(captor.capture());
        assertEquals("050000", captor.getValue().typeCode());
        assertNull(captor.getValue().region());
        assertEquals("poi-1", result.places().get(0).poiId());
        assertEquals(328L, result.places().get(0).distanceMeters());
    }

    @Test
    void searchPlacesWithCityIdUsesCityName() {
        when(amapPlaceSearchClient.search(any()))
                .thenReturn(new AmapPlaceSearchClient.SearchResult(0L, List.of()));

        service.searchPlaces(new ItineraryLocationService.PlaceSearchRequest(
                "西湖", "sightseeing", 330100L, null, null, 5000, 1, 20));

        ArgumentCaptor<AmapPlaceSearchClient.SearchRequest> captor =
                ArgumentCaptor.forClass(AmapPlaceSearchClient.SearchRequest.class);
        verify(amapPlaceSearchClient).search(captor.capture());
        assertEquals("杭州市", captor.getValue().region());
        assertEquals("110000", captor.getValue().typeCode());
        assertNull(captor.getValue().longitude());
    }

    @Test
    void getCurrentWeatherUsesAmapClient() {
        when(amapWeatherClient.getCurrentWeather("330100")).thenReturn(new WeatherClient.CurrentWeather(
                "杭州市", 31, "多云", 72, "东南风", "3", "2026-08-29 11:35:33"));

        ItineraryLocationService.Weather result = service.getCurrentWeather("330100");

        verify(amapWeatherClient).getCurrentWeather("330100");
        assertEquals("杭州市", result.city());
        assertEquals(31, result.temperature());
        assertEquals("多云", result.condition());
    }

}
