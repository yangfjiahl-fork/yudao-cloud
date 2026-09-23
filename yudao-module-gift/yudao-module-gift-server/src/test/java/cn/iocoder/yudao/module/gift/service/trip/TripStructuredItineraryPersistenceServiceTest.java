package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripItineraryDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDayDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryItemDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryTransportSegmentDO;
import cn.iocoder.yudao.module.gift.dal.mysql.useritinerary.UserItineraryDayMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.useritinerary.UserItineraryItemMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.useritinerary.UserItineraryMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.useritinerary.UserItineraryTransportSegmentMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripStructuredItineraryPersistenceServiceTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TripStructuredItineraryPersistenceService service;

    @Mock
    private UserItineraryMapper userItineraryMapper;
    @Mock
    private UserItineraryDayMapper userItineraryDayMapper;
    @Mock
    private UserItineraryItemMapper userItineraryItemMapper;
    @Mock
    private UserItineraryTransportSegmentMapper transportSegmentMapper;

    @Test
    void persist_shouldExpandHeaderDayItemsAndTransportSegments() {
        TripPlanDO trip = new TripPlanDO().setId(1L).setConversationId(2L).setMemberId(3L);
        TripItineraryDO tripItinerary = new TripItineraryDO().setId(10L).setTripId(1L).setMessageId(9L)
                .setVersion(3).setStatus(1);
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("departure", "上海");
        state.put("destination", "云南");
        state.put("startDate", "2026-10-01");
        state.put("days", 2);
        state.put("travelerCount", 4);
        state.put("budget", 4000);
        state.put("interests", List.of("亲子", "人文"));
        state.put("mustVisit", List.of("滇池"));

        Map<String, Object> slot = new LinkedHashMap<>();
        slot.put("itemId", "item-1");
        slot.put("day", 1);
        slot.put("type", "ACTIVITY");
        slot.put("slot", "MORNING");
        slot.put("label", "上午");
        slot.put("sort", 0);
        slot.put("startTime", "09:00");
        slot.put("durationMinutes", 150);
        slot.put("poiId", "poi-1");
        slot.put("poiName", "滇池");
        slot.put("longitude", "102.7000");
        slot.put("latitude", "25.0500");
        slot.put("status", "PENDING");
        slot.put("source", "MANAGED_MACRO_JAVA");
        slot.put("poiSnapshot", Map.of("imageUrl", "https://example.com/dianchi.jpg",
                "address", "昆明市滇池路", "provider", "gaode"));

        Map<String, Object> segment = Map.ofEntries(
                Map.entry("fromPoiId", "poi-1"), Map.entry("fromPoiName", "滇池"),
                Map.entry("fromLongitude", "102.7000"), Map.entry("fromLatitude", "25.0500"),
                Map.entry("toPoiId", "hotel-1"), Map.entry("toPoiName", "酒店"),
                Map.entry("toLongitude", "102.7100"), Map.entry("toLatitude", "25.0600"),
                Map.entry("mode", "TAXI"), Map.entry("distanceMeters", 2300),
                Map.entry("durationMinutes", 12), Map.entry("provider", "gaode"),
                Map.entry("status", "VERIFIED"),
                Map.entry("routePoints", List.of(Map.of("longitude", 102.7, "latitude", 25.05))));
        Map<String, Object> day = new LinkedHashMap<>();
        day.put("day", 1);
        day.put("date", "2026-10-01");
        day.put("city", "昆明");
        day.put("area", "滇池片区");
        day.put("theme", "亲子自然体验");
        day.put("overview", Map.of("status", "PENDING", "skeleton", "昆明亲子一日游"));
        day.put("planning", Map.of("solver", "OR_TOOLS_TSPTW", "status", "FEASIBLE",
                "routeDataStatus", "VERIFIED", "budgetStatus", "WITHIN_BUDGET",
                "dayStartTime", "09:00", "dayEndTime", "20:00"));
        day.put("slots", List.of(slot));
        day.put("transportSegments", List.of(segment));

        Map<String, Object> itinerary = new LinkedHashMap<>();
        itinerary.put("summary", "云南亲子两日行程");
        itinerary.put("overview", Map.of("status", "PENDING", "skeleton", "云南行程总览"));
        itinerary.put("daily_itinerary", List.of(day));
        itinerary.put("transport", Map.of(
                "arrival", Map.of("status", "PENDING", "city", "昆明", "skeleton", "抵达昆明"),
                "departure", Map.of("status", "PENDING", "city", "昆明", "skeleton", "从昆明返程")));
        itinerary.put("citation_ids", List.of("source-1"));
        itinerary.put("planner", Map.of("type", "MANAGED_MACRO_JAVA", "validation", "SERVER_PLANNED"));

        List<UserItineraryItemDO> insertedItems = new ArrayList<>();
        List<UserItineraryTransportSegmentDO> insertedSegments = new ArrayList<>();
        doAnswer(invocation -> {
            UserItineraryDO entity = invocation.getArgument(0);
            entity.setId(100L);
            return 1;
        }).when(userItineraryMapper).insert(any(UserItineraryDO.class));
        doAnswer(invocation -> {
            UserItineraryDayDO entity = invocation.getArgument(0);
            entity.setId(101L);
            return 1;
        }).when(userItineraryDayMapper).insert(any(UserItineraryDayDO.class));
        when(userItineraryItemMapper.insertBatch(any())).thenAnswer(invocation -> {
            Collection<UserItineraryItemDO> entities = invocation.getArgument(0);
            insertedItems.addAll(entities);
            return true;
        });
        when(transportSegmentMapper.insertBatch(any())).thenAnswer(invocation -> {
            Collection<UserItineraryTransportSegmentDO> entities = invocation.getArgument(0);
            insertedSegments.addAll(entities);
            return true;
        });

        TenantContextHolder.setTenantId(20L);
        try {
            assertEquals(100L, service.persist(trip, tripItinerary, 3L, state, itinerary));

            ArgumentCaptor<UserItineraryDO> headerCaptor = ArgumentCaptor.forClass(UserItineraryDO.class);
            verify(userItineraryMapper).insert(headerCaptor.capture());
            UserItineraryDO header = headerCaptor.getValue();
            assertEquals(10L, header.getTripItineraryId());
            assertEquals(3, header.getVersion());
            assertEquals(LocalDate.of(2026, 10, 2), header.getEndDate());
            assertEquals("https://example.com/dianchi.jpg", header.getCoverUrl());
            assertEquals("MANAGED_MACRO_JAVA", header.getPlannerType());

            ArgumentCaptor<UserItineraryDayDO> dayCaptor = ArgumentCaptor.forClass(UserItineraryDayDO.class);
            verify(userItineraryDayMapper).insert(dayCaptor.capture());
            assertEquals("昆明", dayCaptor.getValue().getCity());
            assertEquals("FEASIBLE", dayCaptor.getValue().getPlanningStatus());
            assertEquals(20L, dayCaptor.getValue().getTenantId());

            assertEquals(3, insertedItems.size());
            UserItineraryItemDO activity = insertedItems.stream()
                    .filter(item -> "item-1".equals(item.getItemId())).findFirst().orElseThrow();
            assertEquals("MORNING", activity.getSlot());
            assertEquals(new BigDecimal("102.7000"), activity.getLongitude());
            assertEquals("昆明市滇池路", activity.getAddressDetail());
            assertEquals("GCJ02", activity.getCoordinateSystem());
            assertNotNull(activity.getPoiSnapshotJson());

            assertEquals(1, insertedSegments.size());
            assertEquals("TAXI", insertedSegments.get(0).getMode());
            assertEquals(2300L, insertedSegments.get(0).getDistanceMeters());
            assertEquals(101L, insertedSegments.get(0).getUserItineraryDayId());
        } finally {
            TenantContextHolder.clear();
        }
    }

}
