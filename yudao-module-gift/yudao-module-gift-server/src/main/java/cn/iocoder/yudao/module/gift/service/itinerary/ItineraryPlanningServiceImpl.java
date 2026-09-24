package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversation.UserItineraryConversationDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversationevent.UserItineraryConversationEventDO;
import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItineraryAgentEvent;
import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItineraryChangeCommand;
import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItineraryRouteResult;
import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItinerarySlotResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** 统一编排行程会话、Agent 规划和行程编辑，Controller 不直接依赖内部能力服务。 */
@Service
@Slf4j
public class ItineraryPlanningServiceImpl implements ItineraryPlanningService {

    private static final String TRAVEL_GUIDE_MESSAGE =
            "请告诉我出发地、目的地、出发日期、旅行天数、同行人数和预算，我来帮你规划旅程。";

    @Resource
    private UserItineraryConversationService userItineraryConversationService;
    @Resource
    private ItineraryAgentService itineraryAgentService;
    @Resource
    private ItineraryPlanEditorService itineraryPlanEditorService;

    @Override
    public ConversationCreated createConversation(Long memberId, Long provinceId, Long cityId, Long districtId) {
        Long conversationId = userItineraryConversationService.create(memberId, provinceId, cityId, districtId);
        String defaultDeparture = itineraryAgentService.createItinerary(
                conversationId, memberId, provinceId, cityId, districtId);
        String content = StrUtil.isNotBlank(defaultDeparture)
                ? "已根据你所在位置暂定从" + defaultDeparture + "出发；如需修改可直接告诉我。" + TRAVEL_GUIDE_MESSAGE
                : TRAVEL_GUIDE_MESSAGE;
        UserItineraryConversationEventDO message = userItineraryConversationService.createEvent(
                conversationId, null, null, "ASSISTANT_MESSAGE", "assistant", "WELCOME", content);
        log.info("[createConversation][conversationId({}) memberId({}) guideMessageId({}) 创建成功]",
                conversationId, memberId, message.getId());
        return new ConversationCreated(conversationId, message.getId(), content);
    }

    @Override
    public void updateConversation(Long conversationId, Long memberId, String title, Boolean pinned) {
        userItineraryConversationService.update(conversationId, memberId, title, pinned);
    }

    @Override
    public List<Conversation> getConversations(Long memberId) {
        return userItineraryConversationService.getList(memberId).stream()
                .map(ItineraryPlanningServiceImpl::convertConversation).toList();
    }

    @Override
    public void deleteConversation(Long conversationId, Long memberId) {
        userItineraryConversationService.delete(conversationId, memberId);
    }

    @Override
    public List<Message> getMessages(Long conversationId, Long memberId) {
        List<UserItineraryConversationEventDO> events =
                userItineraryConversationService.getEvents(conversationId, memberId);
        Collection<Long> messageIds = events.stream().map(UserItineraryConversationEventDO::getId).toList();
        Map<Long, Map<String, Object>> itineraryMap = itineraryAgentService.getItineraryMapByMessageIds(messageIds);
        return events.stream().map(event -> convertMessage(event, itineraryMap.get(event.getId()))).toList();
    }

    @Override
    public void validateConversationAccess(Long conversationId, Long memberId) {
        userItineraryConversationService.getRequired(conversationId, memberId);
    }

    @Override
    public void handleManagedMessage(Long conversationId, Long memberId, String runId, String content,
                                     Consumer<ItineraryAgentEvent> eventConsumer) {
        itineraryAgentService.handleManagedMessage(conversationId, memberId, runId, content, eventConsumer);
    }

    @Override
    public ItinerarySlotResult resolveItinerarySlot(Long conversationId, Long memberId, Long messageId,
                                                         Integer day, String slot) {
        userItineraryConversationService.getRequired(conversationId, memberId);
        return itineraryAgentService.resolveItinerarySlot(conversationId, memberId, messageId, day, slot);
    }

    @Override
    public ItineraryRouteResult resolveItineraryRoute(Long conversationId, Long memberId, Long messageId,
                                                           Integer day) {
        userItineraryConversationService.getRequired(conversationId, memberId);
        return itineraryAgentService.resolveItineraryRoute(conversationId, memberId, messageId, day);
    }

    @Override
    public ItineraryChange changeItinerary(Long conversationId, Long memberId, ItineraryChangeCommand command) {
        userItineraryConversationService.getRequired(conversationId, memberId);
        ItineraryPlanEditorService.EditResult result = itineraryPlanEditorService.apply(conversationId, memberId, command);
        return new ItineraryChange(result.saved().itineraryId(), result.saved().messageId(),
                result.saved().displayText(), result.affectedDays(), result.itinerary());
    }

    private static Conversation convertConversation(UserItineraryConversationDO source) {
        return new Conversation(source.getId(), source.getTitle(), source.getPinned(), source.getProvinceId(),
                source.getCityId(), source.getDistrictId(), source.getCreateTime());
    }

    private static Message convertMessage(UserItineraryConversationEventDO source, Map<String, Object> itinerary) {
        return new Message(source.getId(), source.getReplyEventId(), source.getRole(), source.getContent(), itinerary,
                source.getCreateTime());
    }

}
