package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.api.chat.AiChatApi;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationUpdateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageRespDTO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripItineraryDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripItinerarySlotDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripItineraryMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripItinerarySlotMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripPlanMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripItineraryVersionServiceTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TripItineraryVersionService versionService;

    @Mock
    private AiChatApi aiChatApi;
    @Mock
    private TripPlanMapper tripPlanMapper;
    @Mock
    private TripItineraryMapper tripItineraryMapper;
    @Mock
    private TripItinerarySlotMapper tripItinerarySlotMapper;

    @Test
    void saveGeneratedItinerary_shouldPersistVersionSlotsPointerAndTitle() {
        TripPlanDO trip = new TripPlanDO().setId(1L).setConversationId(2L).setMemberId(3L);
        Map<String, Object> state = Map.of("startDate", "2026-10-01", "destination", "云南");
        Map<String, Object> itinerary = new LinkedHashMap<>();
        itinerary.put("summary", "云南亲子六日行程");
        itinerary.put("citation_ids", List.of(8L));
        itinerary.put("overview", Map.of("slot", "TRIP_OVERVIEW", "skeleton", "行程总览"));
        itinerary.put("daily_itinerary", List.of(Map.of(
                "day", 1,
                "overview", Map.of("slot", "DAY_OVERVIEW", "status", "RESOLVED", "detail", "昆明轻松游"),
                "slots", List.of(Map.of("slot", "MORNING", "poiId", "poi-1", "skeleton", "游览滇池")))));
        itinerary.put("transport", Map.of("arrival", Map.of("skeleton", "抵达昆明")));
        when(tripItineraryMapper.selectMaxVersionByTripId(1L)).thenReturn(2);
        when(aiChatApi.createAssistantMessage(any())).thenReturn(new AiChatMessageRespDTO().setId(9L));
        doAnswer(invocation -> {
            TripItineraryDO entity = invocation.getArgument(0);
            entity.setId(10L);
            return 1;
        }).when(tripItineraryMapper).insert(any());

        TenantContextHolder.setTenantId(20L);
        try {
            TripItineraryVersionService.SavedItinerary saved = versionService.saveGeneratedItinerary(
                    trip, 3L, state, itinerary);

            assertEquals(10L, saved.itineraryId());
            assertEquals(9L, saved.messageId());
            assertEquals(3, saved.version());
            assertEquals("云南亲子六日行程", saved.displayText());
            assertEquals(10L, trip.getCurrentItineraryId());
            assertEquals(3, itinerary.get("version"));

            ArgumentCaptor<TripItineraryDO> itineraryCaptor = ArgumentCaptor.forClass(TripItineraryDO.class);
            verify(tripItineraryMapper).insert(itineraryCaptor.capture());
            assertEquals(3, itineraryCaptor.getValue().getVersion());
            assertEquals(9L, itineraryCaptor.getValue().getMessageId());
            assertEquals(3, JsonUtils.parseObject(itineraryCaptor.getValue().getContentJson(), Map.class).get("version"));
            verify(tripItinerarySlotMapper, times(4)).insert(any(TripItinerarySlotDO.class));
            verify(tripPlanMapper).updateById(trip);

            ArgumentCaptor<AiChatConversationUpdateReqDTO> titleCaptor =
                    ArgumentCaptor.forClass(AiChatConversationUpdateReqDTO.class);
            verify(aiChatApi).updateConversation(titleCaptor.capture());
            assertEquals("2026-10-01 云南", titleCaptor.getValue().getTitle());
        } finally {
            TenantContextHolder.clear();
        }
    }

}
