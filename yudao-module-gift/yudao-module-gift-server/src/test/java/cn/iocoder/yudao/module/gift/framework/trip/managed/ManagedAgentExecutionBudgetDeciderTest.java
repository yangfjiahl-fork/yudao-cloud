package cn.iocoder.yudao.module.gift.framework.trip.managed;

import com.alibaba.dashscope.agentstudio.message.ContentBlock;
import com.alibaba.dashscope.agentstudio.message.Message;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManagedAgentExecutionBudgetDeciderTest {

    private ManagedAgentExecutionBudgetDecider decider;
    private ManagedAgentExecutionBudgetDecider.Budget budget;

    @BeforeEach
    void setUp() {
        ManagedAgentProperties properties = new ManagedAgentProperties()
                .setMaxRunDuration(Duration.ofMinutes(3))
                .setMaxModelRequests(4)
                .setMaxToolCalls(2)
                .setMaxTotalTokens(30_000)
                .setMaxOutputTokens(4_000)
                .setMaxOutputCharacters(32_768);
        decider = new ManagedAgentExecutionBudgetDecider(properties);
        budget = decider.newBudget(ManagedAgentExecutionStage.PLAN);
    }

    @Test
    void rejectsFifthModelRequestAndIgnoresDuplicateEvent() {
        Message first = event("model_request_start", "model-1");
        assertTrue(budget.decide(first, 0).allowed());
        assertTrue(budget.decide(first, 0).allowed());
        for (int index = 2; index <= 4; index++) {
            String type = index == 3 ? "span.model_request_start" : "model_request_start";
            assertTrue(budget.decide(event(type, "model-" + index), 0).allowed());
        }

        ManagedAgentExecutionBudgetDecider.Decision decision =
                budget.decide(event("model_request_start", "model-5"), 0);

        assertFalse(decision.allowed());
        assertEquals(ManagedAgentExecutionBudgetDecider.Reason.MAX_MODEL_REQUESTS, decision.reason());
        assertEquals(5, decision.snapshot().modelRequests());
    }

    @Test
    void rejectsThirdToolCallAcrossToolTypes() {
        assertTrue(budget.decide(event("tool_call", "tool-1"), 0).allowed());
        assertTrue(budget.decide(event("span.mcp_call", "tool-2"), 0).allowed());

        ManagedAgentExecutionBudgetDecider.Decision decision =
                budget.decide(event("function_call", "tool-3"), 0);

        assertFalse(decision.allowed());
        assertEquals(ManagedAgentExecutionBudgetDecider.Reason.MAX_TOOL_CALLS, decision.reason());
        assertEquals(3, decision.snapshot().toolCalls());
    }

    @Test
    void intakeAllowsExactlyOneModelRequest() {
        ManagedAgentExecutionBudgetDecider.Budget intake = decider.newBudget(ManagedAgentExecutionStage.INTAKE);
        assertTrue(intake.decide(event("model_request_start", "model-1"), 0).allowed());

        ManagedAgentExecutionBudgetDecider.Decision decision =
                intake.decide(event("model_request_start", "model-2"), 0);

        assertFalse(decision.allowed());
        assertEquals(ManagedAgentExecutionBudgetDecider.Reason.MAX_MODEL_REQUESTS, decision.reason());
        assertEquals(2, decision.snapshot().modelRequests());
    }

    @Test
    void intakeRejectsFirstToolOrSkillCall() {
        ManagedAgentExecutionBudgetDecider.Budget toolBudget = decider.newBudget(ManagedAgentExecutionStage.INTAKE);
        ManagedAgentExecutionBudgetDecider.Decision toolDecision =
                toolBudget.decide(event("mcp_call", "tool-1"), 0);
        ManagedAgentExecutionBudgetDecider.Budget skillBudget = decider.newBudget(ManagedAgentExecutionStage.INTAKE);
        ManagedAgentExecutionBudgetDecider.Decision skillDecision =
                skillBudget.decide(event("span.skill_call", "skill-1"), 0);

        assertFalse(toolDecision.allowed());
        assertEquals(ManagedAgentExecutionBudgetDecider.Reason.MAX_TOOL_CALLS, toolDecision.reason());
        assertFalse(skillDecision.allowed());
        assertEquals(ManagedAgentExecutionBudgetDecider.Reason.MAX_TOOL_CALLS, skillDecision.reason());
    }

    @Test
    void intakeUsesIndependentLowerOutputTokenBudget() {
        ManagedAgentExecutionBudgetDecider.Decision intakeDecision = decider
                .newBudget(ManagedAgentExecutionStage.INTAKE)
                .decide(usageEvent("intake-usage", 500, 1_001), 0);
        ManagedAgentExecutionBudgetDecider.Decision planDecision = decider
                .newBudget(ManagedAgentExecutionStage.PLAN)
                .decide(usageEvent("plan-usage", 500, 1_001), 0);

        assertFalse(intakeDecision.allowed());
        assertEquals(ManagedAgentExecutionBudgetDecider.Reason.MAX_OUTPUT_TOKENS, intakeDecision.reason());
        assertTrue(planDecision.allowed());
    }

    @Test
    void accumulatesUsageAndRejectsExcessOutputTokens() {
        assertTrue(budget.decide(usageEvent("usage-1", 10_000, 2_000), 0).allowed());

        ManagedAgentExecutionBudgetDecider.Decision decision =
                budget.decide(usageEvent("usage-2", 5_000, 2_001), 0);

        assertFalse(decision.allowed());
        assertEquals(ManagedAgentExecutionBudgetDecider.Reason.MAX_OUTPUT_TOKENS, decision.reason());
        assertEquals(15_000, decision.snapshot().inputTokens());
        assertEquals(4_001, decision.snapshot().outputTokens());
        assertEquals(19_001, decision.snapshot().totalTokens());
    }

    @Test
    void rejectsExcessTotalTokens() {
        assertTrue(budget.decide(usageEvent("usage-1", 27_000, 3_000), 0).allowed());

        ManagedAgentExecutionBudgetDecider.Decision decision =
                budget.decide(usageEvent("usage-2", 1, 0), 0);

        assertFalse(decision.allowed());
        assertEquals(ManagedAgentExecutionBudgetDecider.Reason.MAX_TOTAL_TOKENS, decision.reason());
        assertEquals(30_001, decision.snapshot().totalTokens());
    }

    @Test
    void calculatesTotalTokensWhenProviderOmitsTotal() {
        Message usageEvent = usageEvent("usage-without-total", 20_000, 1_000);
        usageEvent.getData().getAsJsonObject("usage").remove("total_tokens");

        ManagedAgentExecutionBudgetDecider.Decision decision = budget.decide(usageEvent, 0);

        assertTrue(decision.allowed());
        assertEquals(20_000, decision.snapshot().inputTokens());
        assertEquals(1_000, decision.snapshot().outputTokens());
        assertEquals(21_000, decision.snapshot().totalTokens());
    }

    @Test
    void rejectsExcessAssistantCharacters() {
        ManagedAgentExecutionBudgetDecider.Decision decision = budget.decide(null, 32_769);

        assertFalse(decision.allowed());
        assertEquals(ManagedAgentExecutionBudgetDecider.Reason.MAX_OUTPUT_CHARACTERS, decision.reason());
    }

    private static Message event(String type, String id) {
        Message message = new Message();
        message.setType(type);
        message.setId(id);
        return message;
    }

    private static Message usageEvent(String id, long inputTokens, long outputTokens) {
        JsonObject usage = new JsonObject();
        usage.addProperty("input_tokens", inputTokens);
        usage.addProperty("output_tokens", outputTokens);
        usage.addProperty("total_tokens", inputTokens + outputTokens);
        JsonObject data = new JsonObject();
        data.add("usage", usage);
        ContentBlock.DataContent dataContent = new ContentBlock.DataContent();
        dataContent.setData(data);
        Message message = event("model_request_end", id);
        message.setContent(List.of(dataContent));
        return message;
    }

}
