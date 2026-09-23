package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversation.UserItineraryConversationDO;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentExecutionMetrics;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentExecutionResult;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripMacroSkeleton;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManagedTripPlannerServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void plan_shouldGenerateMacroThenDelegateFactsAndSchedulingToJava() {
        ManagedTripAgentExecutor executor = mock(ManagedTripAgentExecutor.class);
        TripItineraryAssembler assembler = mock(TripItineraryAssembler.class);
        ManagedTripPlannerService service = new ManagedTripPlannerService();
        ReflectionTestUtils.setField(service, "managedTripAgentExecutor", executor);
        ReflectionTestUtils.setField(service, "tripItineraryAssembler", assembler);
        String managedResponse = """
                {"macro_skeleton":{"days":[
                  {"day":1,"city":"昆明","area":"滇池周边","theme":"轻松亲子","anchorPoiNames":["滇池"]},
                  {"day":2,"city":"大理","area":"大理古城","theme":"换城人文","anchorPoiNames":["大理古城"]}
                ]}}
                """;
        ManagedAgentExecutionMetrics metrics =
                new ManagedAgentExecutionMetrics(2, 1, 1_200, 300, 1_500, 500, 800);
        when(executor.execute(any(UserItineraryConversationDO.class), anyMap(), anyString(),
                eq(ManagedTripAgentStage.PLAN)))
                .thenReturn(new ManagedTripAgentExecutor.Execution("session-1",
                        new ManagedAgentExecutionResult(managedResponse, metrics)));
        Map<String, Object> expected = Map.of("daily_itinerary", List.of(), "citation_ids", List.of());
        when(assembler.assemble(anyMap(), any(TripMacroSkeleton.class), any(Consumer.class))).thenReturn(expected);
        UserItineraryConversationDO trip = new UserItineraryConversationDO().setId(2L);
        Map<String, Object> state = Map.of("destination", "云南", "days", 2);

        Map<String, Object> result = service.plan(trip, state, ignored -> { });

        assertEquals(expected, result);
        ArgumentCaptor<String> taskCaptor = ArgumentCaptor.forClass(String.class);
        verify(executor).execute(eq(trip), eq(state), taskCaptor.capture(), eq(ManagedTripAgentStage.PLAN));
        assertTrue(taskCaptor.getValue().contains("GENERATE_TRIP_MACRO_SKELETON"));
        assertTrue(taskCaptor.getValue().contains("anchorPoiNames"));
        assertTrue(taskCaptor.getValue().contains("dailyBudgetPerPerson\":500"));
        assertTrue(taskCaptor.getValue().contains("transportMode\":\"TAXI"));
        assertFalse(taskCaptor.getValue().contains("transferDay"));
        assertFalse(taskCaptor.getValue().contains("daily_itinerary"));
        assertFalse(taskCaptor.getValue().contains("latestUserMessage"));
        ArgumentCaptor<TripMacroSkeleton> macroCaptor = ArgumentCaptor.forClass(TripMacroSkeleton.class);
        verify(assembler).assemble(eq(state), macroCaptor.capture(), any(Consumer.class));
        assertEquals("大理", macroCaptor.getValue().days().get(1).city());
    }

}
