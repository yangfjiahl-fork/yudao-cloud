package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.framework.trip.provider.place.TravelPlaceQueryClient;
import cn.iocoder.yudao.module.gift.framework.trip.provider.place.TravelPlaceQueryClientFacade;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class TripTravelQueryServiceTest extends BaseMockitoUnitTest {

    @Mock
    private TravelPlaceQueryClientFacade travelPlaceQueryClientFacade;
    @InjectMocks
    private TripTravelQueryService service;

    @Test
    void getPlaceDetailConvertsProviderPlace() {
        TravelPlaceQueryClient.Place providerPlace = new TravelPlaceQueryClient.Place()
                .setPoiId("poi-1").setName("西湖景区").setAddress("杭州市西湖区")
                .setLongitude("120.1001").setLatitude("30.2002")
                .setImageUrl("https://example.com/scenic.jpg").setTelephone("0571-12345678")
                .setRating("4.8").setCost("0").setTag("风景名胜").setBusinessHours("08:00-18:00");
        when(travelPlaceQueryClientFacade.getPlaceDetail("poi-1"))
                .thenReturn(new TravelPlaceQueryClient.Response(true, "10000", "OK", List.of(providerPlace)));

        TripTravelQueryService.Place place = service.getPlaceDetail("poi-1");

        assertEquals("gaode", place.provider());
        assertEquals("poi-1", place.poiId());
        assertEquals("西湖景区", place.name());
        assertEquals("120.1001", place.longitude());
        assertEquals("4.8", place.rating());
        assertEquals("08:00-18:00", place.businessHours());
    }

}
