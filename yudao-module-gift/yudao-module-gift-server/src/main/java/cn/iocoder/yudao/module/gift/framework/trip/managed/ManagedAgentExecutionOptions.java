package cn.iocoder.yudao.module.gift.framework.trip.managed;

import java.time.Duration;

/** 单次 Managed Agent 执行的通用超时与资源预算。 */
public record ManagedAgentExecutionOptions(
        String operation,
        Duration streamTimeout,
        Duration maxRunDuration,
        int maxModelRequests,
        int maxToolCalls,
        long maxTotalTokens,
        long maxOutputTokens,
        int maxOutputCharacters) {
}
