package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversation.UserItineraryConversationDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversationevent.UserItineraryConversationEventDO;
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

class TripItinerarySaveServiceTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TripItinerarySaveService saveService;

    @Mock
    private TripStructuredItineraryPersistenceService structuredItineraryPersistenceService;
    @Mock
    private UserItineraryConversationService userItineraryConversationService;

    @Test
    void saveGeneratedItinerary_shouldPersistCurrentItineraryAndEvent() {
        UserItineraryConversationDO trip = new UserItineraryConversationDO().setId(2L).setMemberId(3L);
        Map<String, Object> state = Map.of("startDate", "2026-10-01", "destination", "云南");
        Map<String, Object> itinerary = new LinkedHashMap<>();
        itinerary.put("summary", "云南亲子六日行程");
        itinerary.put("daily_itinerary", List.of(Map.of(
                "day", 1, "slots", List.of(Map.of("slot", "MORNING", "poiId", "poi-1")))));
        when(userItineraryConversationService.createEvent(2L, "run-1", 8L, "ITINERARY", "assistant", "ASSEMBLE",
                "云南亲子六日行程")).thenReturn(new UserItineraryConversationEventDO().setId(9L));
        when(structuredItineraryPersistenceService.persist(trip, 8L, 9L, 3L, state, itinerary))
                .thenReturn(10L);

        TripItinerarySaveService.SavedItinerary saved = saveService.saveGeneratedItinerary(
                trip, 3L, "run-1", 8L, state, itinerary);

        assertEquals(10L, saved.itineraryId());
        assertEquals(9L, saved.messageId());
        assertEquals("云南亲子六日行程", saved.displayText());
        Map<?, ?> normalizedDay = (Map<?, ?>) ((List<?>) itinerary.get("daily_itinerary")).get(0);
        Map<?, ?> normalizedItem = (Map<?, ?>) ((List<?>) normalizedDay.get("slots")).get(0);
        assertFalse(String.valueOf(normalizedItem.get("itemId")).isBlank());
        assertEquals("ACTIVITY", normalizedItem.get("type"));
        assertEquals("MORNING", normalizedItem.get("timePeriod"));
        assertEquals(150, normalizedItem.get("durationMinutes"));

        verify(userItineraryConversationService).linkItinerary(9L, 10L);
        verify(userItineraryConversationService).updateTitle(2L, "2026-10-01 云南");
    }

}
