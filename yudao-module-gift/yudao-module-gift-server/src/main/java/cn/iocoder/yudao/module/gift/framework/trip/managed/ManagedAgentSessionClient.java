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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/** Managed Agents Session API 的窄适配层；业务代码不感知 SDK 事件模型。 */
@RequiredArgsConstructor
@Slf4j
public class ManagedAgentSessionClient implements AutoCloseable {

    private static final int INTERRUPT_MAX_ATTEMPTS = 3;

    private final ManagedAgentProperties properties;
    private final ManagedAgentExecutionBudgetDecider budgetDecider;
    private final ThreadPoolTaskExecutor taskExecutor;
    private volatile AgentStudioClient client;

    public String createSession(String title, Long tripId, Long conversationId) {
        validateConfig();
        // SDK 2.23.1 的 builder 字段名为 agent，对应 HTTP 请求体中的 agent（官方文档部分示例写作 agentId）。
        Session session = client().sessions().create(SessionCreateParam.builder()
                .agent(properties.getAgentId())
                .environmentId(properties.getEnvironmentId())
                .title(title)
                .metadata(Map.of("trip_id", String.valueOf(tripId),
                        "conversation_id", String.valueOf(conversationId)))
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
    public ManagedAgentExecutionResult execute(String sessionId, String task, ManagedAgentExecutionStage stage) {
        validateConfig();
        ManagedAgentExecutionBudgetDecider.Budget budget = budgetDecider.newBudget(stage);
        AtomicBoolean remoteStopped = new AtomicBoolean();
        Future<ManagedAgentExecutionResult> future = taskExecutor.submit(
                () -> consumeEvents(sessionId, task, stage, budget, remoteStopped));
        try {
            return future.get(maxRunDuration(stage), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            stopRemoteExecution(sessionId, remoteStopped, "max_run_duration");
            throw new ManagedAgentBudgetExceededException(
                    ManagedAgentExecutionBudgetDecider.Reason.MAX_RUN_DURATION, budget.snapshot());
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
            String sessionId, String task, ManagedAgentExecutionStage stage,
            ManagedAgentExecutionBudgetDecider.Budget budget,
            AtomicBoolean remoteStopped) {
        long idleTimeoutMs = streamTimeout(stage);
        List<String> messages = new ArrayList<>();
        AgentStudioClient currentClient = client();
        try (AgentStudioEventStream stream = currentClient.sessions().events().stream(sessionId, idleTimeoutMs)) {
            currentClient.sessions().events().send(sessionId,
                    Collections.singletonList(ClientEvents.userMessage(task)));
            for (Message event : stream) {
                List<String> eventTexts = assistantTexts(event);
                int addedCharacters = eventTexts.stream().mapToInt(String::length).sum();
                long previousTotalTokens = isModelRequestEnd(event) ? budget.snapshot().totalTokens() : -1;
                ManagedAgentExecutionBudgetDecider.Decision decision = budget.decide(event, addedCharacters);
                ManagedAgentExecutionBudgetDecider.Snapshot snapshot = decision.snapshot();
                if (previousTotalTokens >= 0 && snapshot.totalTokens() != previousTotalTokens) {
                    log.info("[consumeEvents][sessionId({}) stage({}) 本次模型请求Token({}) "
                                    + "累计输入Token({}) 累计输出Token({}) 累计总Token({})]",
                            sessionId, stage, snapshot.totalTokens() - previousTotalTokens,
                            snapshot.inputTokens(), snapshot.outputTokens(), snapshot.totalTokens());
                }
                if (!decision.allowed()) {
                    log.warn("[consumeEvents][sessionId({}) stage({}) 预算熔断 reason({}) "
                                    + "模型请求({}) 工具调用({}) 输入Token({}) 输出Token({}) 总Token({})]",
                            sessionId, stage, decision.reason(), snapshot.modelRequests(), snapshot.toolCalls(),
                            snapshot.inputTokens(), snapshot.outputTokens(), snapshot.totalTokens());
                    stopRemoteExecution(sessionId, remoteStopped, decision.reason().name().toLowerCase());
                    throw new ManagedAgentBudgetExceededException(decision.reason(), snapshot);
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
                throw new ManagedAgentBudgetExceededException(
                        ManagedAgentExecutionBudgetDecider.Reason.STREAM_IDLE_TIMEOUT, budget.snapshot());
            }
            throw e;
        }
        for (int index = messages.size() - 1; index >= 0; index--) {
            String message = messages.get(index);
            if (message.indexOf('{') >= 0 && message.lastIndexOf('}') > message.indexOf('{')) {
                ManagedAgentExecutionBudgetDecider.Snapshot snapshot = budget.snapshot();
                log.info("[execute][sessionId({}) stage({}) 模型请求({}) 工具调用({}) 输入Token({}) "
                                + "输出Token({}) 总Token({}) 耗时({}ms)]",
                        sessionId, stage, snapshot.modelRequests(), snapshot.toolCalls(), snapshot.inputTokens(),
                        snapshot.outputTokens(), snapshot.totalTokens(), snapshot.durationMs());
                return new ManagedAgentExecutionResult(message, snapshot);
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

    private long maxRunDuration(ManagedAgentExecutionStage stage) {
        return (stage == ManagedAgentExecutionStage.INTAKE
                ? properties.getIntakeMaxRunDuration() : properties.getMaxRunDuration()).toMillis();
    }

    private long streamTimeout(ManagedAgentExecutionStage stage) {
        return (stage == ManagedAgentExecutionStage.INTAKE
                ? properties.getIntakeStreamTimeout() : properties.getStreamTimeout()).toMillis();
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

    private static boolean isModelRequestEnd(Message event) {
        String eventType = event.getType();
        return "model_request_end".equals(eventType)
                || eventType != null && eventType.endsWith(".model_request_end");
    }

    private void validateConfig() {
        if (StrUtil.isBlank(properties.getApiKey())) {
            throw new IllegalStateException("未配置 yudao.gift.trip-managed-agent.api-key");
        }
        if (StrUtil.isBlank(properties.getAgentId())) {
            throw new IllegalStateException("未配置 yudao.gift.trip-managed-agent.agent-id");
        }
        if (StrUtil.isBlank(properties.getEnvironmentId())) {
            throw new IllegalStateException("未配置 yudao.gift.trip-managed-agent.environment-id");
        }
        if (StrUtil.isBlank(properties.getBaseUrl()) && StrUtil.isBlank(properties.getWorkspace())) {
            throw new IllegalStateException("未配置 yudao.gift.trip-managed-agent.workspace 或 base-url");
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
