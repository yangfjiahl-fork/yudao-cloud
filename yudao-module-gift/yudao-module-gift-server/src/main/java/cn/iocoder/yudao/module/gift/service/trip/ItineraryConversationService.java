package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.module.gift.dal.dataobject.itineraryconversation.ItineraryConversationDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.itineraryevent.ItineraryEventDO;
import cn.iocoder.yudao.module.gift.dal.mysql.itineraryconversation.ItineraryConversationMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.itineraryevent.ItineraryEventMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItineraryConversationService {

    private static final int STATUS_ACTIVE = 1;

    @Resource
    private ItineraryConversationMapper conversationMapper;
    @Resource
    private ItineraryEventMapper eventMapper;

    public ItineraryConversationDO getRequired(Long id, Long memberId) {
        ItineraryConversationDO conversation = conversationMapper.selectByIdAndMemberId(id, memberId);
        if (conversation == null) {
            throw new IllegalArgumentException("行程会话不存在");
        }
        return conversation;
    }

    public List<ItineraryConversationDO> getList(Long memberId) {
        return conversationMapper.selectListByMemberId(memberId);
    }

    public Long create(Long memberId, Long provinceId, Long cityId, Long districtId) {
        ItineraryConversationDO conversation = new ItineraryConversationDO();
        conversation.setMemberId(memberId);
        conversation.setTitle("新旅行计划");
        conversation.setPinned(false);
        conversation.setProvinceId(provinceId);
        conversation.setCityId(cityId);
        conversation.setDistrictId(districtId);
        conversation.setStateJson("{}");
        conversation.setMissingRequiredJson("[]");
        conversation.setStatus(STATUS_ACTIVE);
        conversationMapper.insert(conversation);
        return conversation.getId();
    }

    public void update(Long id, Long memberId, String title, Boolean pinned) {
        getRequired(id, memberId);
        conversationMapper.updateById(new ItineraryConversationDO().setId(id).setTitle(title).setPinned(pinned));
    }

    public void delete(Long id, Long memberId) {
        getRequired(id, memberId);
        conversationMapper.deleteById(id);
    }

    public List<ItineraryEventDO> getEvents(Long conversationId, Long memberId) {
        getRequired(conversationId, memberId);
        return eventMapper.selectListByConversationId(conversationId);
    }

    @Transactional(rollbackFor = Exception.class)
    public ItineraryEventDO createEvent(Long conversationId, String runId, Long replyEventId, String eventType,
                                        String role, String stage, String content) {
        ItineraryEventDO event = new ItineraryEventDO();
        event.setConversationId(conversationId);
        event.setRunId(runId);
        event.setReplyEventId(replyEventId);
        event.setEventType(eventType);
        event.setRole(role);
        event.setStage(stage);
        event.setContent(content);
        event.setStatus(STATUS_ACTIVE);
        eventMapper.insert(event);
        return event;
    }

    public void linkItinerary(Long eventId, Long userItineraryId) {
        eventMapper.updateById(new ItineraryEventDO().setId(eventId).setUserItineraryId(userItineraryId));
    }

    public void updateCurrentItinerary(Long conversationId, Long userItineraryId, String title) {
        conversationMapper.updateById(new ItineraryConversationDO().setId(conversationId)
                .setCurrentUserItineraryId(userItineraryId).setTitle(title));
    }

    public void updateState(Long conversationId, String stateJson, String missingRequiredJson) {
        conversationMapper.updateById(new ItineraryConversationDO().setId(conversationId)
                .setStateJson(stateJson).setMissingRequiredJson(missingRequiredJson));
    }

}
