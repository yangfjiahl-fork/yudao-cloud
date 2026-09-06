package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripItineraryDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripItineraryMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripPlanMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TripMapServiceImplTest {

    @Test
    void getItineraryMap_shouldExposeUnlocatedStopWithoutZeroCoordinate() {
        TripItineraryMapper itineraryMapper = mock(TripItineraryMapper.class);
        TripPlanMapper tripPlanMapper = mock(TripPlanMapper.class);
        TripItineraryDO itinerary = new TripItineraryDO();
        itinerary.setId(10L);
        itinerary.setTripId(20L);
        itinerary.setContentJson("""
                {"daily_itinerary":[{"day":1,"slots":[
                  {"slot":"MORNING","poiId":"B0001","poiName":"西湖","longitude":"120.155070","latitude":"30.274084","plannedStartTime":"09:00","plannedEndTime":"11:00"},
                  {"slot":"LUNCH","poiId":"B0002","poiName":"午餐"}
                ]}]}
                """);
        TripPlanDO plan = new TripPlanDO();
        plan.setId(20L);
        plan.setMemberId(288L);
        when(itineraryMapper.selectById(10L)).thenReturn(itinerary);
        when(tripPlanMapper.selectById(20L)).thenReturn(plan);

        TripMapServiceImpl service = new TripMapServiceImpl();
        ReflectionTestUtils.setField(service, "tripItineraryMapper", itineraryMapper);
        ReflectionTestUtils.setField(service, "tripPlanMapper", tripPlanMapper);

        TripMapService.ItineraryMap result = service.getItineraryMap(288L, 10L);

        List<TripMapService.ItineraryStop> stops = result.days().get(0).stops();
        assertEquals("GCJ-02", result.coordinateSystem());
        assertEquals(2, stops.size());
        assertEquals("B0001", stops.get(0).poiId());
        assertEquals(30.274084D, stops.get(0).latitude());
        assertFalse(stops.get(1).hasCoordinate());
        assertNull(stops.get(1).latitude());
        assertNull(stops.get(1).longitude());
    }

}
