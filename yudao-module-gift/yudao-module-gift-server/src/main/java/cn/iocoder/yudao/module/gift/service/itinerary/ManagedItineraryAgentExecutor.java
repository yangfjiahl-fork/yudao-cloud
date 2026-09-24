package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversation.UserItineraryConversationDO;
import cn.iocoder.yudao.module.gift.service.itinerary.managed.ManagedAgentClient;
import cn.iocoder.yudao.module.gift.service.itinerary.managed.ManagedAgentExecutionOptions;
import cn.iocoder.yudao.module.gift.service.itinerary.managed.ManagedAgentExecutionResult;
import cn.iocoder.yudao.module.gift.service.itinerary.managed.ManagedItineraryAgentProperties;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

/** Managed Agent 运行入口，统一复用旅行会话 Session 并执行任务。 */
@Service
public class ManagedItineraryAgentExecutor {

    private static final int INTAKE_MAX_MODEL_REQUESTS = 1;
    private static final int INTAKE_MAX_TOOL_CALLS = 0;

    @Resource
    private ManagedItinerarySessionService managedTripSessionService;
    @Resource
    private ManagedAgentClient managedAgentClient;
    @Resource
    private ManagedItineraryAgentProperties properties;

    public Execution execute(UserItineraryConversationDO conversation, Map<String, Object> state, String task,
                             ManagedItineraryAgentStage stage) {
        String sessionId = managedTripSessionService.getOrCreateSession(
                conversation.getId(), state, stage);
        return new Execution(sessionId, managedAgentClient.execute(sessionId, task, executionOptions(stage)));
    }

    private ManagedAgentExecutionOptions executionOptions(ManagedItineraryAgentStage stage) {
        if (stage == ManagedItineraryAgentStage.INTAKE) {
            return new ManagedAgentExecutionOptions(stage.name(), properties.getIntakeStreamTimeout(),
                    properties.getIntakeMaxRunDuration(), INTAKE_MAX_MODEL_REQUESTS, INTAKE_MAX_TOOL_CALLS,
                    properties.getIntakeMaxTotalTokens(), properties.getIntakeMaxOutputTokens(),
                    properties.getIntakeMaxOutputCharacters());
        }
        return new ManagedAgentExecutionOptions(stage.name(), properties.getStreamTimeout(),
                properties.getMaxRunDuration(), properties.getMaxModelRequests(), properties.getMaxToolCalls(),
                properties.getMaxTotalTokens(), properties.getMaxOutputTokens(),
                properties.getMaxOutputCharacters());
    }

    public record Execution(String sessionId, ManagedAgentExecutionResult result) {
    }

}
