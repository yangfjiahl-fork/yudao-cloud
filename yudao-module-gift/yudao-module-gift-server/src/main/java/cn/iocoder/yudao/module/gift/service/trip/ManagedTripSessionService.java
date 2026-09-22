package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripPlanMapper;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentSessionClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** 独立事务创建云端 Session，避免不同任务之间继承模型和工具上下文。 */
@Service
@Slf4j
public class ManagedTripSessionService {

    @Resource
    private ManagedAgentSessionClient managedAgentSessionClient;
    @Resource
    private TripPlanMapper tripPlanMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String createSession(Long tripId, Long conversationId, Map<String, Object> state) {
        TripPlanDO current = tripPlanMapper.selectById(tripId);
        if (current == null) {
            throw new IllegalStateException("旅行计划不存在");
        }
        String sessionId = managedAgentSessionClient.createSession(buildSessionTitle(state), tripId, conversationId);
        tripPlanMapper.updateById(new TripPlanDO().setId(tripId).setManagedAgentSessionId(sessionId));
        log.info("[createSession][tripId({}) Managed Agents sessionId({}) 创建成功]", tripId, sessionId);
        return sessionId;
    }

    private static String buildSessionTitle(Map<String, Object> state) {
        String departure = text(state.get("departure"));
        String destination = text(state.get("destination"));
        Integer days = cn.hutool.core.map.MapUtil.getInt(state, "days");
        String route = StrUtil.isBlank(departure) ? destination : departure + "-" + destination;
        return StrUtil.sub(route + (days == null ? "旅行规划" : days + "日游"), 0, 100);
    }

    private static String text(Object value) {
        return value == null ? "" : StrUtil.trim(String.valueOf(value));
    }

}
