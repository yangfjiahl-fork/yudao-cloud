package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ItineraryMapServiceImplTest {

    @Test
    void getItineraryMap_shouldExposeUnlocatedStopWithoutZeroCoordinate() {
        UserItineraryQueryService queryService = mock(UserItineraryQueryService.class);
        UserItineraryDO itinerary = new UserItineraryDO();
        itinerary.setId(10L);
        itinerary.setMemberId(288L);
        java.util.Map<String, Object> itineraryMap = ItineraryAgentFormatUtils.parseMap("""
                {"daily_itinerary":[{"day":1,"slots":[
                  {"slot":"MORNING","poiId":"B0001","poiName":"西湖","longitude":"120.155070","latitude":"30.274084","plannedStartTime":"09:00","plannedEndTime":"11:00"},
                  {"slot":"LUNCH","poiId":"B0002","poiName":"午餐"}
                ]}]}
                """);
        when(queryService.getById(10L, null)).thenReturn(itinerary);
        when(queryService.toMap(itinerary)).thenReturn(itineraryMap);

        ItineraryMapServiceImpl service = new ItineraryMapServiceImpl();
        ReflectionTestUtils.setField(service, "userItineraryQueryService", queryService);

        ItineraryMapService.ItineraryMap result = service.getItineraryMap(288L, 10L);

        List<ItineraryMapService.ItineraryStop> stops = result.days().get(0).stops();
        assertEquals("GCJ-02", result.coordinateSystem());
        assertEquals(2, stops.size());
        assertEquals("B0001", stops.get(0).poiId());
        assertEquals(30.274084D, stops.get(0).latitude());
        assertFalse(stops.get(1).hasCoordinate());
        assertNull(stops.get(1).latitude());
        assertNull(stops.get(1).longitude());
    }

}
