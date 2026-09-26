package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItineraryAgentEvent;
import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItineraryChangeCommand;
import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItineraryRouteResult;
import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItinerarySlotResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** 面向 APP Controller 的行程规划应用服务。 */
public interface ItineraryPlanningService {

    ConversationCreated createConversation(Long memberId, Long provinceId, Long cityId, Long districtId);

    void updateConversation(Long conversationId, Long memberId, String title, Boolean pinned);

    PageResult<Conversation> getConversations(Long memberId, PageParam pageReqVO);

    void deleteConversation(Long conversationId, Long memberId);

    List<Message> getMessages(Long conversationId, Long memberId);

    void validateConversationAccess(Long conversationId, Long memberId);

    void handleManagedMessage(Long conversationId, Long memberId, String runId, String content,
                              Consumer<ItineraryAgentEvent> eventConsumer);

    ItinerarySlotResult resolveItinerarySlot(Long conversationId, Long memberId, Long messageId,
                                                  Integer day, String slot);

    ItineraryRouteResult resolveItineraryRoute(Long conversationId, Long memberId, Long messageId, Integer day);

    ItineraryChange changeItinerary(Long conversationId, Long memberId, ItineraryChangeCommand command);

    record ConversationCreated(Long conversationId, Long messageId, String content) {
    }

    record Conversation(Long id, String title, Boolean pinned, Long provinceId, Long cityId, Long districtId,
                        LocalDateTime createTime) {
    }

    record Message(Long id, Long replyId, String type, String content, Map<String, Object> itinerary,
                   LocalDateTime createTime) {
    }

    record ItineraryChange(Long itineraryId, Long messageId, String content, List<Integer> affectedDays,
                           Map<String, Object> itinerary) {
    }

}
