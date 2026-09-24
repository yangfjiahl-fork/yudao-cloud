package cn.iocoder.yudao.module.gift.service.itinerary.managed;

/** Managed Agent 本次执行被主动终止的统一原因。 */
public enum ManagedAgentTerminationReason {
    MAX_RUN_DURATION,
    STREAM_IDLE_TIMEOUT,
    MAX_MODEL_REQUESTS,
    MAX_TOOL_CALLS,
    MAX_TOTAL_TOKENS,
    MAX_OUTPUT_TOKENS,
    MAX_OUTPUT_CHARACTERS
}
