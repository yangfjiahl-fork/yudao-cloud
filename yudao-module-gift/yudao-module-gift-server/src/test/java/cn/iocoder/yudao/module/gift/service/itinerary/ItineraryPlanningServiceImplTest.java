package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversationevent.UserItineraryConversationEventDO;
import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItineraryChangeCommand;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItineraryPlanningServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ItineraryPlanningServiceImpl service;

    @Mock
    private UserItineraryConversationService userItineraryConversationService;
    @Mock
    private ItineraryAgentService itineraryAgentService;
    @Mock
    private ItineraryPlanEditorService itineraryPlanEditorService;

    @Test
    void createConversationOrchestratesInitializationAndWelcomeMessage() {
        when(userItineraryConversationService.create(2L, 310000L, 310100L, 310115L)).thenReturn(10L);
        when(itineraryAgentService.createItinerary(10L, 2L, 310000L, 310100L, 310115L)).thenReturn("上海市");
        when(userItineraryConversationService.createEvent(
                eq(10L), isNull(), isNull(), eq("ASSISTANT_MESSAGE"), eq("assistant"), eq("WELCOME"), any()))
                .thenReturn(new UserItineraryConversationEventDO().setId(11L));

        ItineraryPlanningService.ConversationCreated result =
                service.createConversation(2L, 310000L, 310100L, 310115L);

        assertEquals(10L, result.conversationId());
        assertEquals(11L, result.messageId());
        assertTrue(result.content().contains("暂定从上海市出发"));
    }

    @Test
    void getMessagesCombinesConversationEventsAndItineraries() {
        LocalDateTime createTime = LocalDateTime.of(2026, 10, 1, 9, 0);
        UserItineraryConversationEventDO event = new UserItineraryConversationEventDO().setId(12L)
                .setReplyEventId(9L).setRole("assistant").setContent("已生成行程");
        event.setCreateTime(createTime);
        when(userItineraryConversationService.getEvents(10L, 2L)).thenReturn(List.of(event));
        when(itineraryAgentService.getItineraryMapByMessageIds(List.of(12L)))
                .thenReturn(Map.of(12L, Map.of("summary", "云南行程")));

        List<ItineraryPlanningService.Message> result = service.getMessages(10L, 2L);

        assertEquals(1, result.size());
        assertEquals(9L, result.get(0).replyId());
        assertEquals("云南行程", result.get(0).itinerary().get("summary"));
        assertEquals(createTime, result.get(0).createTime());
    }

    @Test
    void changeItineraryValidatesConversationAndConvertsResult() {
        ItineraryChangeCommand command = new ItineraryChangeCommand(ItineraryChangeCommand.Operation.MOVE_ITEM,
                "item-1", 2, "AFTERNOON", 1, Map.of());
        ItinerarySaveService.SavedItinerary saved =
                new ItinerarySaveService.SavedItinerary(20L, 21L, "已更新行程");
        when(itineraryPlanEditorService.apply(10L, 2L, command)).thenReturn(
                new ItineraryPlanEditorService.EditResult(saved, List.of(2), Map.of("summary", "新行程")));

        ItineraryPlanningService.ItineraryChange result = service.changeItinerary(10L, 2L, command);

        verify(userItineraryConversationService).getRequired(10L, 2L);
        assertEquals(20L, result.itineraryId());
        assertEquals(List.of(2), result.affectedDays());
        assertEquals("新行程", result.itinerary().get("summary"));
    }

}
