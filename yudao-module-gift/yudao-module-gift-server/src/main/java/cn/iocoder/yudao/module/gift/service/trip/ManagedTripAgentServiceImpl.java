package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.ai.api.chat.AiChatApi;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageCreateAssistantReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageRespDTO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripPlanMapper;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentEvent;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentResult;
import com.baomidou.lock.annotation.Lock4j;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Managed Agents 新链路：读取既有 TripState，生成并按原结构保存完整行程。 */
@Service
@Slf4j
public class ManagedTripAgentServiceImpl implements ManagedTripAgentService {

    @Resource
    private TripPlanMapper tripPlanMapper;
    @Resource
    private ManagedTripPlannerService managedTripPlannerService;
    @Resource
    private AiChatApi aiChatApi;
    @Resource
    private TripItineraryVersionService tripItineraryVersionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = {"#conversationId"}, expire = 360000, acquireTimeout = 3000)
    public TripAgentResult handleMessage(Long conversationId, Long memberId, String content,
                                         Consumer<TripAgentEvent> eventConsumer) {
        TripPlanDO trip = tripPlanMapper.selectByConversationIdAndMemberId(conversationId, memberId);
        if (trip == null) {
            throw new IllegalArgumentException("旅行状态不存在，请先创建旅行会话");
        }
        createTranscriptMessage(conversationId, memberId, content, false);
        Map<String, Object> state = TripAgentFormatUtils.parseMap(trip.getStateJson());
        List<String> missingRequired = TripAgentServiceImpl.validateState(state);
        if (CollUtil.isNotEmpty(missingRequired)) {
            String question = "请先补充" + String.join("、", missingRequired) + "，再生成托管行程。";
            AiChatMessageRespDTO assistant = createTranscriptMessage(conversationId, memberId, question, true);
            eventConsumer.accept(TripAgentEvent.of("intake_completed", "INTAKE", "需求尚未补充完整")
                    .setMissingRequired(missingRequired).setSuggestions(List.of()));
            eventConsumer.accept(TripAgentEvent.of("question", "INTAKE", question)
                    .setMessageId(assistant.getId()).setMissingRequired(missingRequired).setSuggestions(List.of()));
            return new TripAgentResult().setType("QUESTION").setMessageId(assistant.getId()).setContent(question)
                    .setMissingRequired(missingRequired);
        }

        eventConsumer.accept(TripAgentEvent.of("intake_completed", "INTAKE", "需求已整理，开始生成行程。")
                .setMissingRequired(List.of()).setSuggestions(List.of()));
        eventConsumer.accept(TripAgentEvent.of("stage", "ASSEMBLE", "正在启动托管旅行 Agent…"));
        Map<String, Object> itinerary = managedTripPlannerService.plan(trip, state, content,
                progress -> eventConsumer.accept(TripAgentEvent.of("stage", "ASSEMBLE", progress)));
        TripItineraryVersionService.SavedItinerary saved = tripItineraryVersionService.saveGeneratedItinerary(
                trip, memberId, state, itinerary);

        log.info("[handleMessage][Managed Agents tripId({}) itineraryId({}) version({}) 生成完成]",
                trip.getId(), saved.itineraryId(), saved.version());
        TripAgentResult result = new TripAgentResult().setType("ITINERARY_SKELETON").setMessageId(saved.messageId())
                .setContent(saved.displayText()).setItinerary(itinerary).setMissingRequired(List.of());
        eventConsumer.accept(TripAgentEvent.of("itinerary_skeleton", "ASSEMBLE", saved.displayText())
                .setMessageId(saved.messageId()).setItinerary(itinerary).setMissingRequired(List.of())
                .setSuggestions(List.of()));
        return result;
    }

    private AiChatMessageRespDTO createTranscriptMessage(Long conversationId, Long memberId, String content,
                                                          boolean assistant) {
        AiChatMessageCreateAssistantReqDTO req = new AiChatMessageCreateAssistantReqDTO();
        req.setConversationId(conversationId);
        req.setUserId(memberId);
        req.setUserType(UserTypeEnum.MEMBER.getValue());
        req.setContent(content);
        return assistant ? aiChatApi.createAssistantMessage(req) : aiChatApi.createUserMessage(req);
    }

}
