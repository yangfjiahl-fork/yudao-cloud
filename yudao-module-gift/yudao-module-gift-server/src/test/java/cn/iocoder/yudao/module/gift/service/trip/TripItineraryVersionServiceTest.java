package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.dal.dataobject.itineraryconversation.ItineraryConversationDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.itineraryevent.ItineraryEventDO;
import cn.iocoder.yudao.module.gift.dal.mysql.useritinerary.UserItineraryMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripItineraryVersionServiceTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TripItineraryVersionService versionService;

    @Mock
    private TripStructuredItineraryPersistenceService structuredItineraryPersistenceService;
    @Mock
    private UserItineraryMapper userItineraryMapper;
    @Mock
    private ItineraryConversationService conversationService;

    @Test
    void saveGeneratedItinerary_shouldPersistStructuredVersionEventAndPointers() {
        ItineraryConversationDO trip = new ItineraryConversationDO().setId(2L).setMemberId(3L);
        Map<String, Object> state = Map.of("startDate", "2026-10-01", "destination", "云南");
        Map<String, Object> itinerary = new LinkedHashMap<>();
        itinerary.put("summary", "云南亲子六日行程");
        itinerary.put("daily_itinerary", List.of(Map.of(
                "day", 1, "slots", List.of(Map.of("slot", "MORNING", "poiId", "poi-1")))));
        when(userItineraryMapper.selectMaxVersionByConversationId(2L)).thenReturn(2);
        when(conversationService.createEvent(2L, "run-1", 8L, "ITINERARY", "assistant", "ASSEMBLE",
                "云南亲子六日行程")).thenReturn(new ItineraryEventDO().setId(9L));
        when(structuredItineraryPersistenceService.persist(trip, 3, 8L, 9L, 3L, state, itinerary))
                .thenReturn(10L);

        TripItineraryVersionService.SavedItinerary saved = versionService.saveGeneratedItinerary(
                trip, 3L, "run-1", 8L, state, itinerary);

        assertEquals(10L, saved.itineraryId());
        assertEquals(9L, saved.messageId());
        assertEquals(3, saved.version());
        assertEquals("云南亲子六日行程", saved.displayText());
        assertEquals(10L, trip.getCurrentUserItineraryId());
        assertEquals(3, itinerary.get("version"));
        Map<?, ?> normalizedDay = (Map<?, ?>) ((List<?>) itinerary.get("daily_itinerary")).get(0);
        Map<?, ?> normalizedItem = (Map<?, ?>) ((List<?>) normalizedDay.get("slots")).get(0);
        assertFalse(String.valueOf(normalizedItem.get("itemId")).isBlank());
        assertEquals("ACTIVITY", normalizedItem.get("type"));
        assertEquals("MORNING", normalizedItem.get("timePeriod"));
        assertEquals(150, normalizedItem.get("durationMinutes"));

        verify(conversationService).linkItinerary(9L, 10L);
        verify(conversationService).updateCurrentItinerary(2L, 10L, "2026-10-01 云南");
    }

}
