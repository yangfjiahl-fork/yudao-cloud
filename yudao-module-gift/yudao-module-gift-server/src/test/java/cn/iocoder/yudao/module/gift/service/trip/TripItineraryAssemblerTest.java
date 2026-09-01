package cn.iocoder.yudao.module.gift.service.trip;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripItineraryAssemblerTest {

    @Test
    void assemble_shouldBuildStableSlotsFromExtractedStateAndProviderCandidates() {
        TripTravelQueryService queryService = mock(TripTravelQueryService.class);
        when(queryService.queryScenicSpots(anyString(), anyString(), anyInt())).thenReturn(List.of(
                new TripTravelQueryService.ScenicSpot("gaode", "poi-1", "西湖", "杭州", "120.1000", "30.2000", ""),
                new TripTravelQueryService.ScenicSpot("gaode", "poi-2", "雷峰塔", "杭州", "120.1100", "30.2050", ""),
                new TripTravelQueryService.ScenicSpot("gaode", "poi-3", "中国茶叶博物馆", "杭州", "120.1200", "30.2100", ""),
                new TripTravelQueryService.ScenicSpot("gaode", "poi-4", "灵隐寺", "杭州", "120.3000", "30.2400", ""),
                new TripTravelQueryService.ScenicSpot("gaode", "poi-5", "飞来峰", "杭州", "120.3050", "30.2450", ""),
                new TripTravelQueryService.ScenicSpot("gaode", "poi-6", "杭州植物园", "杭州", "120.3100", "30.2500", "")
        ));
        when(queryService.queryRestaurants(anyString(), anyInt())).thenReturn(List.of(
                new TripTravelQueryService.Place("gaode", "food-1", "知味观", "杭州", "120.1010", "30.2010", "", "", "4.8", "80", ""),
                new TripTravelQueryService.Place("gaode", "food-2", "楼外楼", "杭州", "120.1150", "30.2080", "", "", "4.7", "120", ""),
                new TripTravelQueryService.Place("gaode", "food-3", "灵隐素斋", "杭州", "120.3010", "30.2410", "", "", "4.6", "60", ""),
                new TripTravelQueryService.Place("gaode", "food-4", "龙井味道", "杭州", "120.3090", "30.2490", "", "", "4.5", "90", "")
        ));
        when(queryService.queryHotels(anyString(), anyInt())).thenReturn(List.of(
                new TripTravelQueryService.Place("gaode", "hotel-1", "西湖国宾馆", "杭州", "120.0900", "30.1950", "", "", "4.9", "800", "")
        ));
        when(queryService.queryHotelsAround(anyString(), anyString(), anyString(), anyInt())).thenReturn(
                List.of(
                        new TripTravelQueryService.Place("gaode", "hotel-2", "西湖舒适酒店", "杭州", "120.1110", "30.2060", "", "", "4.0", "400", "舒适型"),
                        new TripTravelQueryService.Place("gaode", "hotel-3", "西湖豪华酒店", "杭州", "120.1120", "30.2070", "", "", "4.9", "1200", "五星级")
                ),
                List.of(new TripTravelQueryService.Place("gaode", "hotel-4", "灵隐精品酒店", "杭州", "120.3010", "30.2410", "", "", "4.6", "600", "四星级"))
        );
        when(queryService.isRouteAvailable()).thenReturn(true);
        when(queryService.queryRoute(anyString(), anyString(), anyString(), any(TripTravelQueryService.RouteMode.class)))
                .thenReturn(new TripTravelQueryService.Route("gaode", TripTravelQueryService.RouteMode.WALKING, 840, 720,
                        List.of(new TripTravelQueryService.RoutePoint(120.0900D, 30.1950D),
                                new TripTravelQueryService.RoutePoint(120.1000D, 30.2000D))));
        TripItineraryAssembler assembler = new TripItineraryAssembler();
        ReflectionTestUtils.setField(assembler, "tripTravelQueryService", queryService);

        Map<String, Object> itinerary = assembler.assemble(Map.of(
                "departure", "上海", "destination", "杭州", "startDate", "2026-09-10", "days", 2,
                "travelerCount", 2, "budget", "1500", "interests", List.of("人文")
        ));

        List<?> days = (List<?>) itinerary.get("daily_itinerary");
        assertEquals(2, days.size());
        assertEquals("TRIP_OVERVIEW", ((Map<?, ?>) itinerary.get("overview")).get("slot"));
        Map<?, ?> firstDay = (Map<?, ?>) days.get(0);
        assertEquals("DAY_OVERVIEW", ((Map<?, ?>) firstDay.get("overview")).get("slot"));
        assertEquals("2026-09-10", firstDay.get("date"));
        List<?> slots = (List<?>) firstDay.get("slots");
        assertEquals(5, slots.size());
        Map<?, ?> morning = (Map<?, ?>) slots.get(0);
        assertEquals("西湖", morning.get("poiName"));
        assertEquals("在知味观用餐", ((Map<?, ?>) slots.get(1)).get("skeleton"));
        assertEquals("西湖舒适酒店", ((Map<?, ?>) slots.get(4)).get("poiName"));
        assertTrue(slots.stream().noneMatch(slot -> "EVENING".equals(((Map<?, ?>) slot).get("slot"))));
        assertTrue(!firstDay.containsKey("transportSegments"));
        verify(queryService, never()).isRouteAvailable();
        verify(queryService, never()).queryRoute(anyString(), anyString(), anyString(), any(TripTravelQueryService.RouteMode.class));

        Map<?, ?> firstSegment = (Map<?, ?>) assembler.resolveTransportSegments(itinerary, 1).get(0);
        assertEquals("VERIFIED", firstSegment.get("status"));
        assertEquals(12, firstSegment.get("durationMinutes"));
        assertEquals("120.1110", firstSegment.get("fromLongitude"));
        assertEquals("120.1000", firstSegment.get("toLongitude"));
        assertEquals(3, ((List<?>) firstSegment.get("routePoints")).size());

        Map<?, ?> secondDay = (Map<?, ?>) days.get(1);
        assertEquals("中国茶叶博物馆", ((Map<?, ?>) ((List<?>) secondDay.get("slots")).get(0)).get("poiName"));
        assertEquals("灵隐精品酒店", ((Map<?, ?>) ((List<?>) secondDay.get("slots")).get(4)).get("poiName"));
        verify(queryService, times(2)).queryHotelsAround(anyString(), anyString(), anyString(), anyInt());
        List<String> scenicPoiIds = Stream.concat(((List<?>) firstDay.get("slots")).stream(), ((List<?>) secondDay.get("slots")).stream())
                .filter(Map.class::isInstance).map(Map.class::cast).map(slot -> (String) slot.get("poiId"))
                .filter(poiId -> poiId != null && poiId.startsWith("poi-")).toList();
        assertEquals(scenicPoiIds.size(), scenicPoiIds.stream().distinct().count());
    }

    @Test
    void schema_shouldKeepRequiredAndOptionalFieldsInOneDefinition() {
        assertEquals(6, TripInformationSchema.getRequiredStateKeys().size());
        assertTrue(TripInformationSchema.supports("constraints"));
        assertEquals("你的预算大约是多少？", TripInformationSchema.getByMissingKey("budget").question());
    }

}
