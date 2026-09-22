package cn.iocoder.yudao.module.gift.framework.trip.managed;

public record ManagedAgentExecutionResult(
        String response,
        ManagedAgentExecutionBudgetDecider.Snapshot budget) {
}
