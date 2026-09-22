package cn.iocoder.yudao.module.gift.framework.trip.managed;

import com.alibaba.dashscope.agentstudio.message.Message;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 托管 Agent 单次执行预算决策器。只根据事件更新计数并给出放行或中断决策，
 * 不负责网络中断、业务兜底或持久化。
 */
public class ManagedAgentExecutionBudgetDecider {

    private static final int INTAKE_MAX_MODEL_REQUESTS = 1;
    private static final int INTAKE_MAX_TOOL_CALLS = 0;
    private static final Set<String> TOOL_CALL_EVENT_TYPES = Set.of(
            "tool_call", "function_call", "mcp_call", "skill_call", "skill_activation");

    private final ManagedAgentProperties properties;

    public ManagedAgentExecutionBudgetDecider(ManagedAgentProperties properties) {
        this.properties = properties;
    }

    public Budget newBudget(ManagedAgentExecutionStage stage) {
        Objects.requireNonNull(stage, "Managed Agent 执行阶段不能为空");
        if (stage == ManagedAgentExecutionStage.INTAKE) {
            return newIntakeBudget();
        }
        return newPlanBudget();
    }

    private Budget newIntakeBudget() {
        return new Budget(properties.getIntakeMaxRunDuration(), INTAKE_MAX_MODEL_REQUESTS, INTAKE_MAX_TOOL_CALLS,
                properties.getIntakeMaxTotalTokens(), properties.getIntakeMaxOutputTokens(),
                properties.getIntakeMaxOutputCharacters());
    }

    private Budget newPlanBudget() {
        return new Budget(properties.getMaxRunDuration(), properties.getMaxModelRequests(),
                properties.getMaxToolCalls(), properties.getMaxTotalTokens(), properties.getMaxOutputTokens(),
                properties.getMaxOutputCharacters());
    }

    public enum Reason {
        MAX_RUN_DURATION,
        STREAM_IDLE_TIMEOUT,
        MAX_MODEL_REQUESTS,
        MAX_TOOL_CALLS,
        MAX_TOTAL_TOKENS,
        MAX_OUTPUT_TOKENS,
        MAX_OUTPUT_CHARACTERS
    }

    public record Snapshot(int modelRequests, int toolCalls, long inputTokens, long outputTokens,
                           long totalTokens, int outputCharacters, long durationMs) {
    }

    public record Decision(boolean allowed, Reason reason, Snapshot snapshot) {

        static Decision allow(Snapshot snapshot) {
            return new Decision(true, null, snapshot);
        }

        static Decision reject(Reason reason, Snapshot snapshot) {
            return new Decision(false, reason, snapshot);
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

        private Budget(Duration maxRunDuration, int maxModelRequests, int maxToolCalls,
                       long maxTotalTokens, long maxOutputTokens, int maxOutputCharacters) {
            this.maxRunDurationNanos = maxRunDuration.toNanos();
            this.maxModelRequests = maxModelRequests;
            this.maxToolCalls = maxToolCalls;
            this.maxTotalTokens = maxTotalTokens;
            this.maxOutputTokens = maxOutputTokens;
            this.maxOutputCharacters = maxOutputCharacters;
        }

        public synchronized Decision decide(Message event, int addedOutputCharacters) {
            if (isDurationExceeded()) {
                return Decision.reject(Reason.MAX_RUN_DURATION, snapshot());
            }
            if (event != null) {
                updateEventCounters(event);
            }
            if (addedOutputCharacters > 0) {
                outputCharacters += addedOutputCharacters;
            }
            if (modelRequests > maxModelRequests) {
                return Decision.reject(Reason.MAX_MODEL_REQUESTS, snapshot());
            }
            if (toolCalls > maxToolCalls) {
                return Decision.reject(Reason.MAX_TOOL_CALLS, snapshot());
            }
            if (totalTokens > maxTotalTokens) {
                return Decision.reject(Reason.MAX_TOTAL_TOKENS, snapshot());
            }
            if (outputTokens > maxOutputTokens) {
                return Decision.reject(Reason.MAX_OUTPUT_TOKENS, snapshot());
            }
            if (outputCharacters > maxOutputCharacters) {
                return Decision.reject(Reason.MAX_OUTPUT_CHARACTERS, snapshot());
            }
            return Decision.allow(snapshot());
        }

        public synchronized Snapshot snapshot() {
            return new Snapshot(modelRequests, toolCalls, inputTokens, outputTokens, totalTokens,
                    outputCharacters, elapsedMs());
        }

        private void updateEventCounters(Message event) {
            String eventType = event.getType();
            if (matchesEventType(eventType, "model_request_start")
                    && modelRequestKeys.add(eventKey(event, "model-start"))) {
                modelRequests++;
            }
            if (isToolCallEvent(eventType)
                    && toolCallKeys.add(eventKey(event, "tool"))) {
                toolCalls++;
            }
            if (matchesEventType(eventType, "model_request_end") && usageKeys.add(eventKey(event, "usage"))) {
                addUsage(event.getData());
            }
        }

        private static boolean isToolCallEvent(String eventType) {
            return TOOL_CALL_EVENT_TYPES.stream().anyMatch(expected -> matchesEventType(eventType, expected));
        }

        private static boolean matchesEventType(String actual, String expected) {
            return expected.equals(actual) || actual != null && actual.endsWith("." + expected);
        }

        private void addUsage(JsonObject data) {
            if (data == null) {
                return;
            }
            JsonObject usage = getObject(data, "usage");
            if (usage == null) {
                usage = data;
            }
            long currentInput = firstLong(usage, "input_tokens", "prompt_tokens");
            long currentOutput = firstLong(usage, "output_tokens", "completion_tokens");
            long currentTotal = firstLong(usage, "total_tokens");
            inputTokens += currentInput;
            outputTokens += currentOutput;
            totalTokens += currentTotal > 0 ? currentTotal : currentInput + currentOutput;
        }

        private boolean isDurationExceeded() {
            return System.nanoTime() - startedNanos > maxRunDurationNanos;
        }

        private long elapsedMs() {
            return (System.nanoTime() - startedNanos) / 1_000_000;
        }

        private static String eventKey(Message event, String prefix) {
            if (event.getId() != null && !event.getId().isBlank()) {
                return prefix + ":id:" + event.getId();
            }
            if (event.getSequenceNumber() != null) {
                return prefix + ":sequence:" + event.getSequenceNumber();
            }
            return prefix + ":identity:" + System.identityHashCode(event);
        }

        private static JsonObject getObject(JsonObject object, String name) {
            JsonElement value = object.get(name);
            return value != null && value.isJsonObject() ? value.getAsJsonObject() : null;
        }

        private static long firstLong(JsonObject object, String... names) {
            for (String name : names) {
                JsonElement value = object.get(name);
                if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                    return value.getAsLong();
                }
            }
            return 0;
        }
    }

}
