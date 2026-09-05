package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.framework.tracer.core.util.MdcContextUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripItineraryAssemblerTest {

    @Test
    void assemble_shouldBuildStableSlotsFromExtractedStateAndProviderCandidates() {
        TripTravelQueryService queryService = mock(TripTravelQueryService.class);
        AtomicReference<String> restaurantTraceId = new AtomicReference<>();
        when(queryService.queryScenicSpots(anyString(), anyString(), anyInt())).thenReturn(List.of(
                new TripTravelQueryService.ScenicSpot("gaode", "poi-1", "西湖", "杭州", "120.1000", "30.2000", "", "00:00-24:00", "", ""),
                new TripTravelQueryService.ScenicSpot("gaode", "poi-2", "雷峰塔", "杭州", "120.1100", "30.2050", ""),
                new TripTravelQueryService.ScenicSpot("gaode", "poi-3", "中国茶叶博物馆", "杭州", "120.1200", "30.2100", ""),
                new TripTravelQueryService.ScenicSpot("gaode", "poi-4", "灵隐寺", "杭州", "120.3000", "30.2400", ""),
                new TripTravelQueryService.ScenicSpot("gaode", "poi-5", "飞来峰", "杭州", "120.3050", "30.2450", ""),
                new TripTravelQueryService.ScenicSpot("gaode", "poi-6", "杭州植物园", "杭州", "120.3100", "30.2500", "")
        ));
        when(queryService.queryRestaurants(anyString(), anyInt())).thenAnswer(invocation -> {
            restaurantTraceId.set(MDC.get("traceId"));
            return List.of(
                    new TripTravelQueryService.Place("gaode", "food-1", "知味观", "杭州", "120.1010", "30.2010", "", "", "4.8", "80", ""),
                    new TripTravelQueryService.Place("gaode", "food-2", "楼外楼", "杭州", "120.1150", "30.2080", "", "", "4.7", "120", ""),
                    new TripTravelQueryService.Place("gaode", "food-3", "灵隐素斋", "杭州", "120.3010", "30.2410", "", "", "4.6", "60", ""),
                    new TripTravelQueryService.Place("gaode", "food-4", "龙井味道", "杭州", "120.3090", "30.2490", "", "", "4.5", "90", "")
            );
        });
        when(queryService.queryHotels(anyString(), anyInt())).thenReturn(List.of(
                new TripTravelQueryService.Place("gaode", "hotel-1", "西湖国宾馆", "杭州", "120.0900", "30.1950", "", "", "4.9", "800", "")
        ));
        when(queryService.isRouteAvailable()).thenReturn(true);
        when(queryService.queryRoute(anyString(), anyString(), anyString(), any(TripTravelQueryService.RouteMode.class)))
                .thenReturn(new TripTravelQueryService.Route("gaode", TripTravelQueryService.RouteMode.WALKING, 840, 720,
                        List.of(new TripTravelQueryService.RoutePoint(120.0900D, 30.1950D),
                                new TripTravelQueryService.RoutePoint(120.1000D, 30.2000D))));
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        TripItineraryAssembler assembler = createAssembler(queryService, meterRegistry);

        Map<String, Object> itinerary;
        List<String> progressMessages = new ArrayList<>();
        MDC.put("traceId", "trip-assembler-test-trace");
        try {
            itinerary = assembler.assemble(Map.of(
                    "departure", "上海", "destination", "杭州", "startDate", "2026-09-10", "days", 2,
                    "travelerCount", 2, "budget", "人均预算1,500元", "interests", List.of("人文"), "mustVisit", List.of("西湖")
            ), progressMessages::add);
        } finally {
            MDC.remove("traceId");
        }

        assertEquals(List.of(
                "正在为你挑选景点、餐厅和住宿…",
                "已筛选 6 个景点、4 家餐厅和 1 家住宿，正在安排每日路线…",
                "每日行程已安排完成，正在生成行程卡片…"
        ), progressMessages);
        List<?> days = (List<?>) itinerary.get("daily_itinerary");
        assertEquals(2, days.size());
        assertEquals("TRIP_OVERVIEW", ((Map<?, ?>) itinerary.get("overview")).get("slot"));
        Map<?, ?> firstDay = (Map<?, ?>) days.get(0);
        assertEquals("DAY_OVERVIEW", ((Map<?, ?>) firstDay.get("overview")).get("slot"));
        assertEquals("2026-09-10", firstDay.get("date"));
        List<?> slots = (List<?>) firstDay.get("slots");
        assertEquals(5, slots.size());
        assertTrue(slots.stream().map(Map.class::cast).anyMatch(slot -> "西湖".equals(slot.get("poiName"))));
        assertTrue(slots.stream().map(Map.class::cast).anyMatch(slot -> "在知味观用餐".equals(slot.get("skeleton"))));
        assertEquals("西湖国宾馆", ((Map<?, ?>) slots.get(4)).get("poiName"));
        assertTrue(slots.stream().noneMatch(slot -> "EVENING".equals(((Map<?, ?>) slot).get("slot"))));
        assertTrue(!firstDay.containsKey("transportSegments"));
        assertEquals("OR_TOOLS_TSPTW", ((Map<?, ?>) firstDay.get("planning")).get("solver"));
        assertEquals("FEASIBLE", ((Map<?, ?>) firstDay.get("planning")).get("status"));
        assertTrue(((Map<?, ?>) slots.get(0)).containsKey("plannedStartTime"));
        verify(queryService).queryRestaurants("杭州", 50);
        verify(queryService).queryHotels("杭州", 50);
        Map<?, ?> firstSegment = (Map<?, ?>) assembler.resolveTransportSegments(itinerary, 1).get(0);
        assertEquals("VERIFIED", firstSegment.get("status"));
        assertEquals(12, firstSegment.get("durationMinutes"));
        assertEquals("120.0900", firstSegment.get("fromLongitude"));
        assertTrue(List.of("120.1000", "120.1100").contains(firstSegment.get("toLongitude")));
        assertTrue(((List<?>) firstSegment.get("routePoints")).size() >= 2);
        verify(queryService, atLeastOnce()).isRouteAvailable();
        verify(queryService, atLeastOnce()).queryRoute(anyString(), anyString(), anyString(), any(TripTravelQueryService.RouteMode.class));

        Map<?, ?> secondDay = (Map<?, ?>) days.get(1);
        assertTrue(((List<?>) secondDay.get("slots")).stream().map(Map.class::cast)
                .anyMatch(slot -> "中国茶叶博物馆".equals(slot.get("poiName"))));
        assertEquals("西湖国宾馆", ((Map<?, ?>) ((List<?>) secondDay.get("slots")).get(4)).get("poiName"));
        List<String> scenicPoiIds = Stream.concat(((List<?>) firstDay.get("slots")).stream(), ((List<?>) secondDay.get("slots")).stream())
                .filter(Map.class::isInstance).map(Map.class::cast).map(slot -> (String) slot.get("poiId"))
                .filter(poiId -> poiId != null && poiId.startsWith("poi-")).toList();
        assertEquals(scenicPoiIds.size(), scenicPoiIds.stream().distinct().count());
        assertEquals("trip-assembler-test-trace", restaurantTraceId.get());
        assertEquals(1, meterRegistry.find("yudao.gift.trip.itinerary.assemble")
                .tags("phase", "assemble", "category", "all", "outcome", "success").timer().count());
        assertEquals(3, meterRegistry.find("yudao.gift.trip.itinerary.assemble")
                .tags("phase", "candidate_query", "outcome", "success").timers().stream().mapToLong(Timer::count).sum());
        assertEquals(1, meterRegistry.find("yudao.gift.trip.itinerary.assemble")
                .tags("phase", "daily_schedule", "category", "all", "outcome", "success").timer().count());
        assertEquals(2, meterRegistry.find("yudao.gift.trip.itinerary.assemble")
                .tags("phase", "daily", "category", "all", "outcome", "success").timer().count());
    }

    @Test
    void schema_shouldKeepRequiredAndOptionalFieldsInOneDefinition() {
        assertEquals(6, TripInformationSchema.getRequiredStateKeys().size());
        assertTrue(TripInformationSchema.supports("constraints"));
        assertTrue(TripInformationSchema.supports("dailyStartTime"));
        assertTrue(TripInformationSchema.supports("mustVisit"));
        assertEquals("你的预算大约是多少？", TripInformationSchema.getByMissingKey("budget").question());
        assertEquals(4, TripInformationSchema.getByStateKey("hotelBudget").suggestions().size());
    }

    @Test
    void assemble_shouldReduceScenicCountWhenOnlyOneScenicCandidateIsAvailable() {
        TripTravelQueryService queryService = mock(TripTravelQueryService.class);
        when(queryService.queryScenicSpots(anyString(), anyString(), anyInt())).thenReturn(List.of(
                new TripTravelQueryService.ScenicSpot("gaode", "poi-1", "西湖", "杭州", "120.1000", "30.2000", "", "09:00-18:00", "4.8", "")
        ));
        when(queryService.queryRestaurants(anyString(), anyInt())).thenReturn(List.of(
                new TripTravelQueryService.Place("gaode", "food-1", "知味观", "杭州", "120.1010", "30.2010", "", "", "4.8", "80", ""),
                new TripTravelQueryService.Place("gaode", "food-2", "楼外楼", "杭州", "120.1150", "30.2080", "", "", "4.7", "120", "")
        ));
        when(queryService.queryHotels(anyString(), anyInt())).thenReturn(List.of(
                new TripTravelQueryService.Place("gaode", "hotel-1", "西湖国宾馆", "杭州", "120.0900", "30.1950", "", "", "4.9", "800", "")
        ));
        TripItineraryAssembler assembler = createAssembler(queryService);

        Map<String, Object> itinerary = assembler.assemble(Map.of("destination", "杭州", "days", 1));

        Map<?, ?> planning = (Map<?, ?>) ((Map<?, ?>) ((List<?>) itinerary.get("daily_itinerary")).get(0)).get("planning");
        assertEquals("FEASIBLE", planning.get("status"));
        assertEquals("DEGRADED", planning.get("selectionStatus"));
        assertEquals(2, planning.get("requestedScenicCount"));
        assertEquals(1, planning.get("selectedScenicCount"));
    }

    @Test
    void assemble_shouldUseHotelBudgetWhenRankingCityHotelCandidates() {
        TripTravelQueryService queryService = mock(TripTravelQueryService.class);
        when(queryService.queryScenicSpots(anyString(), anyString(), anyInt())).thenReturn(List.of(
                new TripTravelQueryService.ScenicSpot("gaode", "poi-1", "西湖", "杭州", "120.1000", "30.2000", ""),
                new TripTravelQueryService.ScenicSpot("gaode", "poi-2", "雷峰塔", "杭州", "120.1100", "30.2050", "")
        ));
        when(queryService.queryRestaurants(anyString(), anyInt())).thenReturn(List.of());
        when(queryService.queryHotels(anyString(), anyInt())).thenReturn(List.of(
                new TripTravelQueryService.Place("gaode", "hotel-1", "经济酒店", "杭州", "120.1110", "30.2060", "", "", "4.9", "300", "三星级"),
                new TripTravelQueryService.Place("gaode", "hotel-2", "高档酒店", "杭州", "120.1120", "30.2070", "", "", "4.6", "1200", "五星级")
        ));
        TripItineraryAssembler assembler = createAssembler(queryService);

        Map<String, Object> itinerary = assembler.assemble(Map.of("destination", "杭州", "days", 1, "hotelBudget", 800));

        Map<?, ?> firstDay = (Map<?, ?>) ((List<?>) itinerary.get("daily_itinerary")).get(0);
        assertEquals("高档酒店", ((Map<?, ?>) ((List<?>) firstDay.get("slots")).get(4)).get("poiName"));
    }

    @Test
    void assemble_shouldLocalizeWideAreaCandidatesBeforeScheduling() {
        TripTravelQueryService queryService = mock(TripTravelQueryService.class);
        when(queryService.queryScenicSpots(anyString(), anyString(), anyInt())).thenReturn(List.of(
                new TripTravelQueryService.ScenicSpot("gaode", "km-1", "昆明翠湖", "昆明", "102.7000", "25.0500", "", "", "4.9", ""),
                new TripTravelQueryService.ScenicSpot("gaode", "km-2", "昆明石林", "昆明", "102.8300", "24.8100", "", "", "4.8", ""),
                new TripTravelQueryService.ScenicSpot("gaode", "km-3", "昆明滇池", "昆明", "102.6600", "24.9500", "", "", "4.7", ""),
                new TripTravelQueryService.ScenicSpot("gaode", "lj-1", "玉龙雪山", "丽江", "100.2300", "26.8700", "", "", "4.9", ""),
                new TripTravelQueryService.ScenicSpot("gaode", "lj-2", "丽江古城", "丽江", "100.2400", "26.8700", "", "", "4.8", ""),
                new TripTravelQueryService.ScenicSpot("gaode", "lj-3", "束河古镇", "丽江", "100.2100", "26.8800", "", "", "4.7", "")
        ));
        when(queryService.queryRestaurants(anyString(), anyInt())).thenReturn(List.of(
                new TripTravelQueryService.Place("gaode", "food-1", "昆明米线", "昆明", "102.7010", "25.0510", "", "", "4.8", "60", ""),
                new TripTravelQueryService.Place("gaode", "food-2", "昆明汽锅鸡", "昆明", "102.7020", "25.0520", "", "", "4.7", "80", "")
        ));
        when(queryService.queryHotels(anyString(), anyInt())).thenReturn(List.of(
                new TripTravelQueryService.Place("gaode", "hotel-1", "昆明酒店", "昆明", "102.7000", "25.0500", "", "", "4.8", "300", "")
        ));
        TripItineraryAssembler assembler = createAssembler(queryService);

        Map<String, Object> itinerary = assembler.assemble(Map.of("destination", "云南", "days", 2));

        List<?> days = (List<?>) itinerary.get("daily_itinerary");
        assertTrue(days.stream().map(Map.class::cast)
                .allMatch(day -> "FEASIBLE".equals(((Map<?, ?>) day.get("planning")).get("status"))));
        assertTrue(days.stream().map(Map.class::cast).flatMap(day -> ((List<?>) day.get("slots")).stream()).map(Map.class::cast)
                .noneMatch(slot -> String.valueOf(slot.get("poiName")).contains("丽江")));
    }

    private static TripItineraryAssembler createAssembler(TripTravelQueryService queryService) {
        return createAssembler(queryService, new SimpleMeterRegistry());
    }

    private static TripItineraryAssembler createAssembler(TripTravelQueryService queryService, MeterRegistry meterRegistry) {
        TripItineraryAssembler assembler = new TripItineraryAssembler();
        ReflectionTestUtils.setField(assembler, "tripTravelQueryService", queryService);
        ReflectionTestUtils.setField(assembler, "meterRegistry", meterRegistry);
        ReflectionTestUtils.setField(assembler, "tripItineraryTaskExecutor",
                (Executor) runnable -> ForkJoinPool.commonPool().execute(MdcContextUtils.wrap(runnable)));
        return assembler;
    }

}
