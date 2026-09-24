package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversation.UserItineraryConversationDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversationevent.UserItineraryConversationEventDO;
import cn.iocoder.yudao.module.gift.dal.mysql.useritineraryconversation.UserItineraryConversationMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.useritineraryconversationevent.UserItineraryConversationEventMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserItineraryConversationService {

    private static final int STATUS_ACTIVE = 1;

    @Resource
    private UserItineraryConversationMapper userItineraryConversationMapper;
    @Resource
    private UserItineraryConversationEventMapper userItineraryConversationEventMapper;

    public UserItineraryConversationDO getRequired(Long id, Long memberId) {
        UserItineraryConversationDO conversation = userItineraryConversationMapper.selectByIdAndMemberId(id, memberId);
        if (conversation == null) {
            throw new IllegalArgumentException("行程会话不存在");
        }
        return conversation;
    }

    public List<UserItineraryConversationDO> getList(Long memberId) {
        return userItineraryConversationMapper.selectListByMemberId(memberId);
    }

    public Long create(Long memberId, Long provinceId, Long cityId, Long districtId) {
        UserItineraryConversationDO conversation = new UserItineraryConversationDO();
        conversation.setMemberId(memberId);
        conversation.setTitle("新旅行计划");
        conversation.setPinned(false);
        conversation.setProvinceId(provinceId);
        conversation.setCityId(cityId);
        conversation.setDistrictId(districtId);
        conversation.setStateJson("{}");
        conversation.setMissingRequiredJson("[]");
        conversation.setStatus(STATUS_ACTIVE);
        userItineraryConversationMapper.insert(conversation);
        return conversation.getId();
    }

    public void update(Long id, Long memberId, String title, Boolean pinned) {
        getRequired(id, memberId);
        userItineraryConversationMapper.updateById(new UserItineraryConversationDO().setId(id).setTitle(title).setPinned(pinned));
    }

    public void delete(Long id, Long memberId) {
        getRequired(id, memberId);
        userItineraryConversationMapper.deleteById(id);
    }

    public List<UserItineraryConversationEventDO> getEvents(Long conversationId, Long memberId) {
        getRequired(conversationId, memberId);
        return userItineraryConversationEventMapper.selectListByConversationId(conversationId);
    }

    @Transactional(rollbackFor = Exception.class)
    public UserItineraryConversationEventDO createEvent(Long conversationId, String runId, Long replyEventId,
                                                        String eventType, String role, String stage, String content) {
        return createEvent(conversationId, runId, replyEventId, eventType, role, stage, content, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public UserItineraryConversationEventDO createEvent(Long conversationId, String runId, Long replyEventId,
                                                        String eventType, String role, String stage, String content,
                                                        String payloadJson) {
        UserItineraryConversationEventDO event = new UserItineraryConversationEventDO();
        event.setConversationId(conversationId);
        event.setRunId(runId);
        event.setReplyEventId(replyEventId);
        event.setEventType(eventType);
        event.setRole(role);
        event.setStage(stage);
        event.setContent(content);
        event.setPayloadJson(payloadJson);
        event.setStatus(STATUS_ACTIVE);
        userItineraryConversationEventMapper.insert(event);
        return event;
    }

    public void linkItinerary(Long eventId, Long userItineraryId) {
        userItineraryConversationEventMapper.updateById(new UserItineraryConversationEventDO().setId(eventId)
                .setUserItineraryId(userItineraryId));
    }

    public void updateTitle(Long conversationId, String title) {
        userItineraryConversationMapper.updateById(new UserItineraryConversationDO().setId(conversationId).setTitle(title));
    }

    public void updateState(Long conversationId, String stateJson, String missingRequiredJson) {
        userItineraryConversationMapper.updateById(new UserItineraryConversationDO().setId(conversationId)
                .setStateJson(stateJson).setMissingRequiredJson(missingRequiredJson));
    }

}
