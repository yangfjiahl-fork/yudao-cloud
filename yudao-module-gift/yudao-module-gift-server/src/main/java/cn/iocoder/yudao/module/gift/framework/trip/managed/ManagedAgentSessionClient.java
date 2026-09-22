package cn.iocoder.yudao.module.gift.framework.trip.managed;

import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.agentstudio.AgentStudioClient;
import com.alibaba.dashscope.agentstudio.message.ClientEvents;
import com.alibaba.dashscope.agentstudio.message.ContentBlock;
import com.alibaba.dashscope.agentstudio.message.Message;
import com.alibaba.dashscope.agentstudio.model.Session;
import com.alibaba.dashscope.agentstudio.param.SessionCreateParam;
import com.alibaba.dashscope.agentstudio.resource.AgentStudioEventStream;
import com.alibaba.dashscope.exception.ApiException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/** 阿里云 Managed Agents Session API 适配器；仅通过 {@link ManagedAgentClient} 对外暴露。 */
@RequiredArgsConstructor
@Slf4j
final class ManagedAgentSessionClient implements ManagedAgentClient {

    private static final int INTERRUPT_MAX_ATTEMPTS = 3;
    private static final List<String> TOOL_CALL_EVENT_TYPES = List.of(
            "tool_call", "function_call", "mcp_call", "skill_call", "skill_activation");

    private final ManagedAgentClientProperties properties;
    private final ManagedAgentExecutionBudgetDecider budgetDecider;
    private final ThreadPoolTaskExecutor taskExecutor;
    private volatile AgentStudioClient client;

    @Override
    public String createSession(ManagedAgentSessionCreateRequest request) {
        validateConfig();
        validateSessionRequest(request);
        // SDK 2.23.1 的 builder 字段名为 agent，对应 HTTP 请求体中的 agent（官方文档部分示例写作 agentId）。
        Session session = client().sessions().create(SessionCreateParam.builder()
                .agent(request.agentId())
                .environmentId(request.environmentId())
                .title(request.title())
                .metadata(request.metadata())
                .build());
        if (session == null || StrUtil.isBlank(session.getId())) {
            throw new IllegalStateException("Managed Agents 创建 Session 未返回 sessionId");
        }
        return session.getId();
    }

    /**
     * 建立 SSE 后发送用户事件，并在独立工作线程消费原始事件。调用线程负责绝对超时，
     * 事件预算由决策器负责；任何预算超限都会中断本次运行，但保留云端 Session 与历史事件。
     */
    @Override
    public ManagedAgentExecutionResult execute(String sessionId, String task, ManagedAgentExecutionOptions options) {
        validateConfig();
        ManagedAgentExecutionBudgetDecider.Budget budget = budgetDecider.newBudget(options);
        AtomicBoolean remoteStopped = new AtomicBoolean();
        log.info("[execute][sessionId({}) operation({}) 开始执行 maxDuration({}) maxModelRequests({}) "
                        + "maxToolCalls({}) maxTotalTokens({}) maxOutputTokens({})]",
                sessionId, options.operation(), options.maxRunDuration(), options.maxModelRequests(),
                options.maxToolCalls(), options.maxTotalTokens(), options.maxOutputTokens());
        Future<ManagedAgentExecutionResult> future = taskExecutor.submit(
                () -> consumeEvents(sessionId, task, options, budget, remoteStopped));
        try {
            return future.get(options.maxRunDuration().toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            stopRemoteExecution(sessionId, remoteStopped, "max_run_duration");
            throw new ManagedAgentExecutionTerminatedException(
                    ManagedAgentTerminationReason.MAX_RUN_DURATION, budget.snapshot());
        } catch (InterruptedException e) {
            future.cancel(true);
            stopRemoteExecution(sessionId, remoteStopped, "caller_interrupted");
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Managed Agents 执行被中断", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Managed Agents 执行失败", cause);
        }
    }

    private ManagedAgentExecutionResult consumeEvents(
            String sessionId, String task, ManagedAgentExecutionOptions options,
            ManagedAgentExecutionBudgetDecider.Budget budget,
            AtomicBoolean remoteStopped) {
        long idleTimeoutMs = options.streamTimeout().toMillis();
        List<String> messages = new ArrayList<>();
        AgentStudioClient currentClient = client();
        try (AgentStudioEventStream stream = currentClient.sessions().events().stream(sessionId, idleTimeoutMs)) {
            currentClient.sessions().events().send(sessionId,
                    Collections.singletonList(ClientEvents.userMessage(task)));
            for (Message event : stream) {
                List<String> eventTexts = assistantTexts(event);
                int addedCharacters = eventTexts.stream().mapToInt(String::length).sum();
                ManagedAgentExecutionBudgetDecider.Event budgetEvent = toBudgetEvent(event);
                long previousTotalTokens = budgetEvent != null
                        && budgetEvent.type() == ManagedAgentExecutionBudgetDecider.EventType.USAGE
                        ? budget.snapshot().totalTokens() : -1;
                ManagedAgentExecutionBudgetDecider.Decision decision = budget.decide(budgetEvent, addedCharacters);
                ManagedAgentExecutionMetrics metrics = decision.metrics();
                if (previousTotalTokens >= 0 && metrics.totalTokens() != previousTotalTokens) {
                    log.info("[consumeEvents][sessionId({}) operation({}) 本次模型请求Token({}) "
                                    + "累计输入Token({}) 累计输出Token({}) 累计总Token({})]",
                            sessionId, options.operation(), metrics.totalTokens() - previousTotalTokens,
                            metrics.inputTokens(), metrics.outputTokens(), metrics.totalTokens());
                }
                if (!decision.allowed()) {
                    log.warn("[consumeEvents][sessionId({}) operation({}) 预算熔断 reason({}) "
                                    + "模型请求({}) 工具调用({}) 输入Token({}) 输出Token({}) 总Token({})]",
                            sessionId, options.operation(), decision.reason(), metrics.modelRequests(),
                            metrics.toolCalls(), metrics.inputTokens(), metrics.outputTokens(), metrics.totalTokens());
                    stopRemoteExecution(sessionId, remoteStopped, decision.reason().name().toLowerCase());
                    throw new ManagedAgentExecutionTerminatedException(decision.reason(), metrics);
                }
                messages.addAll(eventTexts);
                if (isTerminal(event)) {
                    break;
                }
                if ("error".equals(event.getType())) {
                    throw new IllegalStateException("Managed Agents 返回错误事件: " + event.getMessage());
                }
            }
        } catch (ApiException e) {
            if (e.getStatus() != null && "stream_timeout".equals(e.getStatus().getCode())) {
                stopRemoteExecution(sessionId, remoteStopped, "stream_idle_timeout");
                throw new ManagedAgentExecutionTerminatedException(
                        ManagedAgentTerminationReason.STREAM_IDLE_TIMEOUT, budget.snapshot());
            }
            throw e;
        }
        for (int index = messages.size() - 1; index >= 0; index--) {
            String message = messages.get(index);
            if (message.indexOf('{') >= 0 && message.lastIndexOf('}') > message.indexOf('{')) {
                ManagedAgentExecutionMetrics metrics = budget.snapshot();
                log.info("[execute][sessionId({}) operation({}) 模型请求({}) 工具调用({}) 输入Token({}) "
                                + "输出Token({}) 总Token({}) 耗时({}ms)]",
                        sessionId, options.operation(), metrics.modelRequests(), metrics.toolCalls(),
                        metrics.inputTokens(), metrics.outputTokens(), metrics.totalTokens(), metrics.durationMs());
                return new ManagedAgentExecutionResult(message, metrics);
            }
        }
        throw new IllegalStateException(messages.isEmpty()
                ? "Managed Agents 未返回结果" : "Managed Agents 最终消息不是有效 JSON");
    }

    private void stopRemoteExecution(String sessionId, AtomicBoolean remoteStopped, String reason) {
        if (!remoteStopped.compareAndSet(false, true)) {
            return;
        }
        AgentStudioClient currentClient = client();
        RuntimeException lastException = null;
        for (int attempt = 1; attempt <= INTERRUPT_MAX_ATTEMPTS; attempt++) {
            try {
                currentClient.sessions().events().send(sessionId,
                        Collections.singletonList(ClientEvents.userInterrupt()));
                log.info("[stopRemoteExecution][sessionId({}) reason({}) interrupt 已发送 attempt({})]",
                        sessionId, reason, attempt);
                return;
            } catch (RuntimeException e) {
                lastException = e;
                log.warn("[stopRemoteExecution][sessionId({}) reason({}) 发送 interrupt 失败 attempt({})]",
                        sessionId, reason, attempt, e);
            }
        }
        remoteStopped.set(false);
        log.error("[stopRemoteExecution][sessionId({}) reason({}) interrupt 重试后仍失败]",
                sessionId, reason, lastException);
    }

    private static List<String> assistantTexts(Message event) {
        if (!"message".equals(event.getType()) || !"assistant".equals(event.getRole())
                || event.getContent() == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (ContentBlock block : event.getContent()) {
            if (block instanceof ContentBlock.Text textBlock && StrUtil.isNotBlank(textBlock.getText())) {
                result.add(textBlock.getText());
            }
        }
        return result;
    }

    private static boolean isTerminal(Message event) {
        if (!"session_status".equals(event.getType()) || event.getData() == null
                || !event.getData().has("session_status")) {
            return false;
        }
        String status = event.getData().get("session_status").getAsString();
        return "idle".equals(status) || "terminated".equals(status)
                || "rescheduled".equals(status) || "deleted".equals(status);
    }

    private static ManagedAgentExecutionBudgetDecider.Event toBudgetEvent(Message event) {
        String eventType = event.getType();
        if (matchesEventType(eventType, "model_request_start")) {
            return ManagedAgentExecutionBudgetDecider.Event.modelRequestStart(eventKey(event, "model-start"));
        }
        if (TOOL_CALL_EVENT_TYPES.stream().anyMatch(expected -> matchesEventType(eventType, expected))) {
            return ManagedAgentExecutionBudgetDecider.Event.toolCall(eventKey(event, "tool"));
        }
        if (!matchesEventType(eventType, "model_request_end")) {
            return null;
        }
        JsonObject data = event.getData();
        JsonObject usage = getObject(data, "usage");
        if (usage == null) {
            usage = data;
        }
        return ManagedAgentExecutionBudgetDecider.Event.usage(eventKey(event, "usage"),
                firstLong(usage, "input_tokens", "prompt_tokens"),
                firstLong(usage, "output_tokens", "completion_tokens"),
                firstLong(usage, "total_tokens"));
    }

    private static boolean matchesEventType(String actual, String expected) {
        return expected.equals(actual) || actual != null && actual.endsWith("." + expected);
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
        if (object == null) {
            return null;
        }
        JsonElement value = object.get(name);
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : null;
    }

    private static long firstLong(JsonObject object, String... names) {
        if (object == null) {
            return 0;
        }
        for (String name : names) {
            JsonElement value = object.get(name);
            if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                return value.getAsLong();
            }
        }
        return 0;
    }

    private void validateConfig() {
        if (StrUtil.isBlank(properties.getApiKey())) {
            throw new IllegalStateException("未配置 yudao.gift.trip-managed-agent.api-key");
        }
        if (StrUtil.isBlank(properties.getBaseUrl()) && StrUtil.isBlank(properties.getWorkspace())) {
            throw new IllegalStateException("未配置 yudao.gift.trip-managed-agent.workspace 或 base-url");
        }
    }

    private static void validateSessionRequest(ManagedAgentSessionCreateRequest request) {
        if (request == null || StrUtil.isBlank(request.agentId())) {
            throw new IllegalArgumentException("Managed Agent agentId 不能为空");
        }
        if (StrUtil.isBlank(request.environmentId())) {
            throw new IllegalArgumentException("Managed Agent environmentId 不能为空");
        }
    }

    private AgentStudioClient client() {
        AgentStudioClient result = client;
        if (result != null) {
            return result;
        }
        synchronized (this) {
            if (client == null) {
                validateConfig();
                AgentStudioClient.Builder builder = AgentStudioClient.builder();
                builder.apiKey(properties.getApiKey());
                if (StrUtil.isNotBlank(properties.getBaseUrl())) {
                    builder.baseUrl(properties.getBaseUrl());
                } else {
                    builder.workspace(properties.getWorkspace()).region(properties.getRegion());
                }
                client = builder.build();
            }
            return client;
        }
    }

    @Override
    public void close() {
        AgentStudioClient result = client;
        if (result != null) {
            result.close();
        }
    }

}
