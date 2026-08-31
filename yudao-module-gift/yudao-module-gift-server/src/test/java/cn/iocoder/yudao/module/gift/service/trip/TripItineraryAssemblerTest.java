package cn.iocoder.yudao.module.gift.service.trip;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TripItineraryAssemblerTest {

    @Test
    void assemble_shouldBuildStableSlotsFromExtractedStateAndProviderCandidates() {
        TripTravelQueryService queryService = mock(TripTravelQueryService.class);
        when(queryService.queryScenicSpots(anyString(), anyString(), anyInt())).thenReturn(List.of(
                new TripTravelQueryService.ScenicSpot("aliyun", "poi-1", "西湖", "杭州", "120", "30", "")
        ));
        when(queryService.queryRestaurants(anyString(), anyInt())).thenReturn(List.of(
                new TripTravelQueryService.Place("gaode", "food-1", "知味观", "杭州", "", "", "", "", "", "", "")
        ));
        when(queryService.queryHotels(anyString(), anyInt())).thenReturn(List.of(
                new TripTravelQueryService.Place("gaode", "hotel-1", "西湖国宾馆", "杭州", "", "", "", "", "", "", "")
        ));
        TripItineraryAssembler assembler = new TripItineraryAssembler();
        ReflectionTestUtils.setField(assembler, "tripTravelQueryService", queryService);

        Map<String, Object> itinerary = assembler.assemble(Map.of(
                "departure", "上海", "destination", "杭州", "startDate", "2026-09-10", "days", 2,
                "travelerCount", 2, "budget", "1500", "interests", List.of("人文")
        ));

        List<?> days = (List<?>) itinerary.get("daily_itinerary");
        assertEquals(2, days.size());
        Map<?, ?> firstDay = (Map<?, ?>) days.getFirst();
        assertEquals("2026-09-10", firstDay.get("date"));
        List<?> slots = (List<?>) firstDay.get("slots");
        assertEquals(6, slots.size());
        Map<?, ?> morning = (Map<?, ?>) slots.getFirst();
        assertEquals("西湖", morning.get("poiName"));
        assertEquals("在知味观用餐", ((Map<?, ?>) slots.get(1)).get("skeleton"));
        assertTrue(((Map<?, ?>) slots.get(5)).get("skeleton").toString().contains("西湖国宾馆"));
    }

    @Test
    void schema_shouldKeepRequiredAndOptionalFieldsInOneDefinition() {
        assertEquals(6, TripInformationSchema.getRequiredStateKeys().size());
        assertTrue(TripInformationSchema.supports("constraints"));
        assertEquals("你的预算大约是多少？", TripInformationSchema.getByMissingKey("budget").question());
    }

}
