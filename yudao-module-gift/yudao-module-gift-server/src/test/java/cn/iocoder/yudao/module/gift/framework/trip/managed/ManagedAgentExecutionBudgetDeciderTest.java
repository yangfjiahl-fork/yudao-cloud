package cn.iocoder.yudao.module.gift.framework.trip.managed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManagedAgentExecutionBudgetDeciderTest {

    private ManagedAgentExecutionBudgetDecider decider;
    private ManagedAgentExecutionBudgetDecider.Budget budget;

    @BeforeEach
    void setUp() {
        decider = new ManagedAgentExecutionBudgetDecider();
        budget = decider.newBudget(planOptions());
    }

    @Test
    void rejectsFifthModelRequestAndIgnoresDuplicateEvent() {
        ManagedAgentExecutionBudgetDecider.Event first = modelRequest("model-1");
        assertTrue(budget.decide(first, 0).allowed());
        assertTrue(budget.decide(first, 0).allowed());
        for (int index = 2; index <= 4; index++) {
            assertTrue(budget.decide(modelRequest("model-" + index), 0).allowed());
        }

        ManagedAgentExecutionBudgetDecider.Decision decision = budget.decide(modelRequest("model-5"), 0);

        assertFalse(decision.allowed());
        assertEquals(ManagedAgentTerminationReason.MAX_MODEL_REQUESTS, decision.reason());
        assertEquals(5, decision.metrics().modelRequests());
    }

    @Test
    void rejectsThirdToolCall() {
        assertTrue(budget.decide(toolCall("tool-1"), 0).allowed());
        assertTrue(budget.decide(toolCall("tool-2"), 0).allowed());

        ManagedAgentExecutionBudgetDecider.Decision decision = budget.decide(toolCall("tool-3"), 0);

        assertFalse(decision.allowed());
        assertEquals(ManagedAgentTerminationReason.MAX_TOOL_CALLS, decision.reason());
        assertEquals(3, decision.metrics().toolCalls());
    }

    @Test
    void intakeAllowsExactlyOneModelRequest() {
        ManagedAgentExecutionBudgetDecider.Budget intake = decider.newBudget(intakeOptions());
        assertTrue(intake.decide(modelRequest("model-1"), 0).allowed());

        ManagedAgentExecutionBudgetDecider.Decision decision = intake.decide(modelRequest("model-2"), 0);

        assertFalse(decision.allowed());
        assertEquals(ManagedAgentTerminationReason.MAX_MODEL_REQUESTS, decision.reason());
        assertEquals(2, decision.metrics().modelRequests());
    }

    @Test
    void intakeRejectsFirstToolCall() {
        ManagedAgentExecutionBudgetDecider.Decision decision = decider.newBudget(intakeOptions())
                .decide(toolCall("tool-1"), 0);

        assertFalse(decision.allowed());
        assertEquals(ManagedAgentTerminationReason.MAX_TOOL_CALLS, decision.reason());
    }

    @Test
    void intakeUsesIndependentLowerOutputTokenBudget() {
        ManagedAgentExecutionBudgetDecider.Decision intakeDecision = decider.newBudget(intakeOptions())
                .decide(usageEvent("intake-usage", 500, 1_001), 0);
        ManagedAgentExecutionBudgetDecider.Decision planDecision = decider.newBudget(planOptions())
                .decide(usageEvent("plan-usage", 500, 1_001), 0);

        assertFalse(intakeDecision.allowed());
        assertEquals(ManagedAgentTerminationReason.MAX_OUTPUT_TOKENS, intakeDecision.reason());
        assertTrue(planDecision.allowed());
    }

    @Test
    void accumulatesUsageAndRejectsExcessOutputTokens() {
        assertTrue(budget.decide(usageEvent("usage-1", 10_000, 2_000), 0).allowed());

        ManagedAgentExecutionBudgetDecider.Decision decision =
                budget.decide(usageEvent("usage-2", 5_000, 2_001), 0);

        assertFalse(decision.allowed());
        assertEquals(ManagedAgentTerminationReason.MAX_OUTPUT_TOKENS, decision.reason());
        assertEquals(15_000, decision.metrics().inputTokens());
        assertEquals(4_001, decision.metrics().outputTokens());
        assertEquals(19_001, decision.metrics().totalTokens());
    }

    @Test
    void rejectsExcessTotalTokens() {
        assertTrue(budget.decide(usageEvent("usage-1", 27_000, 3_000), 0).allowed());

        ManagedAgentExecutionBudgetDecider.Decision decision =
                budget.decide(usageEvent("usage-2", 1, 0), 0);

        assertFalse(decision.allowed());
        assertEquals(ManagedAgentTerminationReason.MAX_TOTAL_TOKENS, decision.reason());
        assertEquals(30_001, decision.metrics().totalTokens());
    }

    @Test
    void calculatesTotalTokensWhenProviderOmitsTotal() {
        ManagedAgentExecutionBudgetDecider.Decision decision = budget.decide(
                ManagedAgentExecutionBudgetDecider.Event.usage("usage-without-total", 20_000, 1_000, 0), 0);

        assertTrue(decision.allowed());
        assertEquals(20_000, decision.metrics().inputTokens());
        assertEquals(1_000, decision.metrics().outputTokens());
        assertEquals(21_000, decision.metrics().totalTokens());
    }

    @Test
    void rejectsExcessAssistantCharacters() {
        ManagedAgentExecutionBudgetDecider.Decision decision = budget.decide(null, 32_769);

        assertFalse(decision.allowed());
        assertEquals(ManagedAgentTerminationReason.MAX_OUTPUT_CHARACTERS, decision.reason());
    }

    private static ManagedAgentExecutionOptions planOptions() {
        return new ManagedAgentExecutionOptions("PLAN", Duration.ofSeconds(45), Duration.ofMinutes(3),
                4, 2, 30_000, 4_000, 32_768);
    }

    private static ManagedAgentExecutionOptions intakeOptions() {
        return new ManagedAgentExecutionOptions("INTAKE", Duration.ofSeconds(45), Duration.ofMinutes(3),
                1, 0, 8_000, 1_000, 8_192);
    }

    private static ManagedAgentExecutionBudgetDecider.Event modelRequest(String id) {
        return ManagedAgentExecutionBudgetDecider.Event.modelRequestStart(id);
    }

    private static ManagedAgentExecutionBudgetDecider.Event toolCall(String id) {
        return ManagedAgentExecutionBudgetDecider.Event.toolCall(id);
    }

    private static ManagedAgentExecutionBudgetDecider.Event usageEvent(
            String id, long inputTokens, long outputTokens) {
        return ManagedAgentExecutionBudgetDecider.Event.usage(id, inputTokens, outputTokens,
                inputTokens + outputTokens);
    }

}
