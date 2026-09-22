package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentExecutionBudgetDecider;
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
import static org.mockito.ArgumentMatchers.anyLong;
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
        TripRunLogService runLogService = mock(TripRunLogService.class);
        TripItineraryAssembler assembler = mock(TripItineraryAssembler.class);
        ManagedTripPlannerService service = new ManagedTripPlannerService();
        ReflectionTestUtils.setField(service, "managedTripAgentExecutor", executor);
        ReflectionTestUtils.setField(service, "tripRunLogService", runLogService);
        ReflectionTestUtils.setField(service, "tripItineraryAssembler", assembler);
        when(runLogService.create(anyLong(), anyString(), anyString())).thenReturn(8L);
        String managedResponse = """
                {"macro_skeleton":{"days":[
                  {"day":1,"city":"昆明","area":"滇池周边","theme":"轻松亲子","anchorPoiNames":["滇池"],"transferDay":false},
                  {"day":2,"city":"大理","area":"大理古城","theme":"换城人文","anchorPoiNames":["大理古城"],"transferDay":true}
                ]}}
                """;
        ManagedAgentExecutionBudgetDecider.Snapshot budget =
                new ManagedAgentExecutionBudgetDecider.Snapshot(2, 1, 1_200, 300, 1_500, 500, 800);
        when(executor.execute(any(TripPlanDO.class), anyMap(), anyString())).thenAnswer(invocation -> {
            TripPlanDO executionTrip = invocation.getArgument(0);
            executionTrip.setManagedAgentSessionId("session-1");
            return new ManagedTripAgentExecutor.Execution("session-1",
                    new ManagedAgentExecutionResult(managedResponse, budget));
        });
        Map<String, Object> expected = Map.of("daily_itinerary", List.of(), "citation_ids", List.of());
        when(assembler.assemble(anyMap(), any(TripMacroSkeleton.class), any(Consumer.class))).thenReturn(expected);
        TripPlanDO trip = new TripPlanDO().setId(1L).setConversationId(2L);
        Map<String, Object> state = Map.of("destination", "云南", "days", 2);

        Map<String, Object> result = service.plan(trip, state, "请生成行程", ignored -> { });

        assertEquals(expected, result);
        assertEquals("session-1", trip.getManagedAgentSessionId());
        ArgumentCaptor<String> taskCaptor = ArgumentCaptor.forClass(String.class);
        verify(executor).execute(eq(trip), eq(state), taskCaptor.capture());
        assertTrue(taskCaptor.getValue().contains("GENERATE_TRIP_MACRO_SKELETON"));
        assertTrue(taskCaptor.getValue().contains("anchorPoiNames"));
        assertFalse(taskCaptor.getValue().contains("daily_itinerary"));
        ArgumentCaptor<TripMacroSkeleton> macroCaptor = ArgumentCaptor.forClass(TripMacroSkeleton.class);
        verify(assembler).assemble(eq(state), macroCaptor.capture(), any(Consumer.class));
        assertEquals("大理", macroCaptor.getValue().days().get(1).city());
        verify(runLogService).complete(eq(8L), eq("managed-agent"), eq(1_200L), eq(300L), eq(1_500L),
                anyLong(), anyString());
    }

}
