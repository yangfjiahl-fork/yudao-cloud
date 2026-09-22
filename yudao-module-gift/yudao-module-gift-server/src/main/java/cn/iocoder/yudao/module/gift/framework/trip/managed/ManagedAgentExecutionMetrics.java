package cn.iocoder.yudao.module.gift.framework.trip.managed;

/** 单次 Managed Agent 执行的统一计量快照。 */
public record ManagedAgentExecutionMetrics(
        int modelRequests,
        int toolCalls,
        long inputTokens,
        long outputTokens,
        long totalTokens,
        int outputCharacters,
        long durationMs) {
}
