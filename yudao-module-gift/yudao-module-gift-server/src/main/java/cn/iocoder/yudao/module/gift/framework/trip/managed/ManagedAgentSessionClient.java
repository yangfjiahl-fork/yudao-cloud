package cn.iocoder.yudao.module.gift.framework.trip.managed;

import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.agentstudio.AgentStudioClient;
import com.alibaba.dashscope.agentstudio.message.ClientEvents;
import com.alibaba.dashscope.agentstudio.model.Session;
import com.alibaba.dashscope.agentstudio.param.SessionCreateParam;
import com.alibaba.dashscope.agentstudio.resource.AgentStudioEventStream;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Managed Agents Session API 的窄适配层；业务代码不感知 SDK 事件模型。 */
@RequiredArgsConstructor
public class ManagedAgentSessionClient implements AutoCloseable {

    private final ManagedAgentProperties properties;
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
     * 先建立 SSE，再发送用户事件，避免短任务在订阅建立前完成。返回最后一条包含 JSON 的助手消息。
     */
    public String execute(String sessionId, String task) {
        validateConfig();
        long timeoutMs = properties.getStreamTimeout().toMillis();
        List<String> messages = new ArrayList<>();
        AgentStudioClient currentClient = client();
        try (AgentStudioEventStream stream = currentClient.sessions().events().stream(sessionId, timeoutMs)) {
            currentClient.sessions().events().send(sessionId,
                    Collections.singletonList(ClientEvents.userMessage(task)));
            for (String message : stream.textStream()) {
                if (StrUtil.isNotBlank(message)) {
                    messages.add(message);
                }
            }
        }
        for (int index = messages.size() - 1; index >= 0; index--) {
            String message = messages.get(index);
            if (message.indexOf('{') >= 0 && message.lastIndexOf('}') > message.indexOf('{')) {
                return message;
            }
        }
        throw new IllegalStateException(messages.isEmpty()
                ? "Managed Agents 未返回旅行计划" : "Managed Agents 最终消息不是 JSON 旅行计划");
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
