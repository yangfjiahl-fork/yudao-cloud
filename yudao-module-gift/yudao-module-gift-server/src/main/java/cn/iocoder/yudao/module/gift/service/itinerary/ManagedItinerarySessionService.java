package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversation.UserItineraryConversationDO;
import cn.iocoder.yudao.module.gift.dal.mysql.useritineraryconversation.UserItineraryConversationMapper;
import cn.iocoder.yudao.module.gift.service.itinerary.managed.ManagedAgentClient;
import cn.iocoder.yudao.module.gift.service.itinerary.managed.ManagedAgentSessionCreateRequest;
import cn.iocoder.yudao.module.gift.service.itinerary.managed.ManagedItineraryAgentProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** 在独立事务中获取或创建需求收集/行程生成 Agent 各自的云端 Session。 */
@Service
@Slf4j
public class ManagedItinerarySessionService {

    @Resource
    private ManagedAgentClient managedAgentClient;
    @Resource
    private ManagedItineraryAgentProperties properties;
    @Resource
    private UserItineraryConversationMapper userItineraryConversationMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String getOrCreateSession(Long conversationId, Map<String, Object> state,
                                     ManagedItineraryAgentStage stage) {
        UserItineraryConversationDO current = userItineraryConversationMapper.selectById(conversationId);
        if (current == null) {
            throw new IllegalStateException("行程会话不存在");
        }
        String persistedSessionId = getSessionId(current, stage);
        if (StrUtil.isNotBlank(persistedSessionId)) {
            log.info("[getOrCreateSession][conversationId({}) stage({}) 复用 Managed Agents sessionId({})]",
                    conversationId, stage, persistedSessionId);
            return persistedSessionId;
        }
        String sessionId = managedAgentClient.createSession(new ManagedAgentSessionCreateRequest(
                getAgentId(stage), getEnvironmentId(stage), buildSessionTitle(state, stage),
                Map.of("conversation_id", String.valueOf(conversationId),
                        "agent_stage", stage.name())));
        UserItineraryConversationDO update = new UserItineraryConversationDO().setId(conversationId);
        if (stage == ManagedItineraryAgentStage.INTAKE) {
            update.setIntakeAgentSessionId(sessionId);
        } else {
            update.setPlanAgentSessionId(sessionId);
        }
        userItineraryConversationMapper.updateById(update);
        log.info("[getOrCreateSession][conversationId({}) stage({}) Managed Agents sessionId({}) 创建成功]",
                conversationId, stage, sessionId);
        return sessionId;
    }

    private static String getSessionId(UserItineraryConversationDO conversation, ManagedItineraryAgentStage stage) {
        return stage == ManagedItineraryAgentStage.INTAKE
                ? conversation.getIntakeAgentSessionId() : conversation.getPlanAgentSessionId();
    }

    private String getAgentId(ManagedItineraryAgentStage stage) {
        String agentId = stage == ManagedItineraryAgentStage.INTAKE
                ? properties.getIntakeAgentId() : properties.getPlanAgentId();
        if (StrUtil.isBlank(agentId)) {
            throw new IllegalStateException("未配置旅行 " + stage + " Managed Agent ID");
        }
        String otherAgentId = stage == ManagedItineraryAgentStage.INTAKE
                ? properties.getPlanAgentId() : properties.getIntakeAgentId();
        if (StrUtil.isNotBlank(otherAgentId) && StrUtil.equals(agentId, otherAgentId)) {
            throw new IllegalStateException("需求收集与行程生成必须配置不同的 Managed Agent ID");
        }
        return agentId;
    }

    private String getEnvironmentId(ManagedItineraryAgentStage stage) {
        String environmentId = stage == ManagedItineraryAgentStage.INTAKE
                ? properties.getIntakeEnvironmentId() : properties.getPlanEnvironmentId();
        if (StrUtil.isBlank(environmentId)) {
            throw new IllegalStateException("未配置旅行 " + stage + " Managed Agent Environment ID");
        }
        return environmentId;
    }

    private static String buildSessionTitle(Map<String, Object> state, ManagedItineraryAgentStage stage) {
        String departure = text(state.get("departure"));
        String destination = text(state.get("destination"));
        Integer days = cn.hutool.core.map.MapUtil.getInt(state, "days");
        String route = StrUtil.isBlank(departure) ? destination : departure + "-" + destination;
        String suffix = stage == ManagedItineraryAgentStage.INTAKE ? "需求收集" : "行程生成";
        return StrUtil.sub(route + (days == null ? "" : days + "日游") + "-" + suffix, 0, 100);
    }

    private static String text(Object value) {
        return value == null ? "" : StrUtil.trim(String.valueOf(value));
    }

}
