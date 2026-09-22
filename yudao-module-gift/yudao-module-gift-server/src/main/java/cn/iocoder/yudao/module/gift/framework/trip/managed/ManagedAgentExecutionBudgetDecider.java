package cn.iocoder.yudao.module.gift.framework.trip.managed;

import java.util.HashSet;
import java.util.Set;

/**
 * 托管 Agent 单次执行预算决策器。只根据事件更新计数并给出放行或中断决策，
 * 不负责网络中断、业务兜底或持久化。
 */
public class ManagedAgentExecutionBudgetDecider {

    public Budget newBudget(ManagedAgentExecutionOptions options) {
        return new Budget(options);
    }

    enum EventType {
        MODEL_REQUEST_START,
        TOOL_CALL,
        USAGE
    }

    record Event(EventType type, String key, long inputTokens, long outputTokens, long totalTokens) {

        static Event modelRequestStart(String key) {
            return new Event(EventType.MODEL_REQUEST_START, key, 0, 0, 0);
        }

        static Event toolCall(String key) {
            return new Event(EventType.TOOL_CALL, key, 0, 0, 0);
        }

        static Event usage(String key, long inputTokens, long outputTokens, long totalTokens) {
            return new Event(EventType.USAGE, key, inputTokens, outputTokens, totalTokens);
        }
    }

    public record Decision(boolean allowed, ManagedAgentTerminationReason reason,
                           ManagedAgentExecutionMetrics metrics) {

        static Decision allow(ManagedAgentExecutionMetrics metrics) {
            return new Decision(true, null, metrics);
        }

        static Decision reject(ManagedAgentTerminationReason reason, ManagedAgentExecutionMetrics metrics) {
            return new Decision(false, reason, metrics);
        }
    }

    public static final class Budget {

        private final long startedNanos = System.nanoTime();
        private final long maxRunDurationNanos;
        private final int maxModelRequests;
        private final int maxToolCalls;
        private final long maxTotalTokens;
        private final long maxOutputTokens;
        private final int maxOutputCharacters;
        private final Set<String> modelRequestKeys = new HashSet<>();
        private final Set<String> toolCallKeys = new HashSet<>();
        private final Set<String> usageKeys = new HashSet<>();

        private int modelRequests;
        private int toolCalls;
        private long inputTokens;
        private long outputTokens;
        private long totalTokens;
        private int outputCharacters;

        private Budget(ManagedAgentExecutionOptions options) {
            this.maxRunDurationNanos = options.maxRunDuration().toNanos();
            this.maxModelRequests = options.maxModelRequests();
            this.maxToolCalls = options.maxToolCalls();
            this.maxTotalTokens = options.maxTotalTokens();
            this.maxOutputTokens = options.maxOutputTokens();
            this.maxOutputCharacters = options.maxOutputCharacters();
        }

        public synchronized Decision decide(Event event, int addedOutputCharacters) {
            if (isDurationExceeded()) {
                return Decision.reject(ManagedAgentTerminationReason.MAX_RUN_DURATION, snapshot());
            }
            if (event != null) {
                updateEventCounters(event);
            }
            if (addedOutputCharacters > 0) {
                outputCharacters += addedOutputCharacters;
            }
            if (modelRequests > maxModelRequests) {
                return Decision.reject(ManagedAgentTerminationReason.MAX_MODEL_REQUESTS, snapshot());
            }
            if (toolCalls > maxToolCalls) {
                return Decision.reject(ManagedAgentTerminationReason.MAX_TOOL_CALLS, snapshot());
            }
            if (totalTokens > maxTotalTokens) {
                return Decision.reject(ManagedAgentTerminationReason.MAX_TOTAL_TOKENS, snapshot());
            }
            if (outputTokens > maxOutputTokens) {
                return Decision.reject(ManagedAgentTerminationReason.MAX_OUTPUT_TOKENS, snapshot());
            }
            if (outputCharacters > maxOutputCharacters) {
                return Decision.reject(ManagedAgentTerminationReason.MAX_OUTPUT_CHARACTERS, snapshot());
            }
            return Decision.allow(snapshot());
        }

        public synchronized ManagedAgentExecutionMetrics snapshot() {
            return new ManagedAgentExecutionMetrics(modelRequests, toolCalls, inputTokens, outputTokens, totalTokens,
                    outputCharacters, elapsedMs());
        }

        private void updateEventCounters(Event event) {
            if (event.type() == EventType.MODEL_REQUEST_START && modelRequestKeys.add(event.key())) {
                modelRequests++;
            }
            if (event.type() == EventType.TOOL_CALL && toolCallKeys.add(event.key())) {
                toolCalls++;
            }
            if (event.type() == EventType.USAGE && usageKeys.add(event.key())) {
                inputTokens += event.inputTokens();
                outputTokens += event.outputTokens();
                totalTokens += event.totalTokens() > 0
                        ? event.totalTokens() : event.inputTokens() + event.outputTokens();
            }
        }

        private boolean isDurationExceeded() {
            return System.nanoTime() - startedNanos > maxRunDurationNanos;
        }

        private long elapsedMs() {
            return (System.nanoTime() - startedNanos) / 1_000_000;
        }

    }

}
