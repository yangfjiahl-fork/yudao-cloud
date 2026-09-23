package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripPlanMapper;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentClient;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentSessionCreateRequest;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedTripAgentProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** 在独立事务中获取或创建需求收集/行程生成 Agent 各自的云端 Session。 */
@Service
@Slf4j
public class ManagedTripSessionService {

    @Resource
    private ManagedAgentClient managedAgentClient;
    @Resource
    private ManagedTripAgentProperties properties;
    @Resource
    private TripPlanMapper tripPlanMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String getOrCreateSession(Long tripId, Long conversationId, Map<String, Object> state,
                                     ManagedTripAgentStage stage) {
        TripPlanDO current = tripPlanMapper.selectById(tripId);
        if (current == null) {
            throw new IllegalStateException("旅行计划不存在");
        }
        String persistedSessionId = getSessionId(current, stage);
        if (StrUtil.isNotBlank(persistedSessionId)) {
            log.info("[getOrCreateSession][tripId({}) stage({}) 复用 Managed Agents sessionId({})]",
                    tripId, stage, persistedSessionId);
            return persistedSessionId;
        }
        String sessionId = managedAgentClient.createSession(new ManagedAgentSessionCreateRequest(
                getAgentId(stage), getEnvironmentId(stage), buildSessionTitle(state, stage),
                Map.of("trip_id", String.valueOf(tripId), "conversation_id", String.valueOf(conversationId),
                        "agent_stage", stage.name())));
        TripPlanDO update = new TripPlanDO().setId(tripId);
        if (stage == ManagedTripAgentStage.INTAKE) {
            update.setIntakeAgentSessionId(sessionId);
        } else {
            update.setPlanAgentSessionId(sessionId);
        }
        tripPlanMapper.updateById(update);
        log.info("[getOrCreateSession][tripId({}) stage({}) Managed Agents sessionId({}) 创建成功]",
                tripId, stage, sessionId);
        return sessionId;
    }

    private static String getSessionId(TripPlanDO trip, ManagedTripAgentStage stage) {
        return stage == ManagedTripAgentStage.INTAKE
                ? trip.getIntakeAgentSessionId() : trip.getPlanAgentSessionId();
    }

    private String getAgentId(ManagedTripAgentStage stage) {
        String agentId = stage == ManagedTripAgentStage.INTAKE
                ? properties.getIntakeAgentId() : properties.getPlanAgentId();
        if (StrUtil.isBlank(agentId)) {
            throw new IllegalStateException("未配置旅行 " + stage + " Managed Agent ID");
        }
        String otherAgentId = stage == ManagedTripAgentStage.INTAKE
                ? properties.getPlanAgentId() : properties.getIntakeAgentId();
        if (StrUtil.isNotBlank(otherAgentId) && StrUtil.equals(agentId, otherAgentId)) {
            throw new IllegalStateException("需求收集与行程生成必须配置不同的 Managed Agent ID");
        }
        return agentId;
    }

    private String getEnvironmentId(ManagedTripAgentStage stage) {
        String environmentId = stage == ManagedTripAgentStage.INTAKE
                ? properties.getIntakeEnvironmentId() : properties.getPlanEnvironmentId();
        if (StrUtil.isBlank(environmentId)) {
            throw new IllegalStateException("未配置旅行 " + stage + " Managed Agent Environment ID");
        }
        return environmentId;
    }

    private static String buildSessionTitle(Map<String, Object> state, ManagedTripAgentStage stage) {
        String departure = text(state.get("departure"));
        String destination = text(state.get("destination"));
        Integer days = cn.hutool.core.map.MapUtil.getInt(state, "days");
        String route = StrUtil.isBlank(departure) ? destination : departure + "-" + destination;
        String suffix = stage == ManagedTripAgentStage.INTAKE ? "需求收集" : "行程生成";
        return StrUtil.sub(route + (days == null ? "" : days + "日游") + "-" + suffix, 0, 100);
    }

    private static String text(Object value) {
        return value == null ? "" : StrUtil.trim(String.valueOf(value));
    }

}
