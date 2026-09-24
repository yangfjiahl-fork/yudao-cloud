package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItineraryAgentResult;
import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItineraryAgentEvent;
import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItineraryRouteResult;
import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItinerarySlotResult;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

public interface ItineraryAgentService {

    void createItinerary(Long conversationId, Long memberId);

    String createItinerary(Long conversationId, Long memberId, Long provinceId, Long cityId, Long districtId);

    /** 使用 Managed Agent 完成 Intake，并在需要生成时继续复用同一 Session。 */
    ItineraryAgentResult handleManagedMessage(Long conversationId, Long memberId, String content,
                                         Consumer<ItineraryAgentEvent> eventConsumer);

    ItineraryAgentResult handleManagedMessage(Long conversationId, Long memberId, String runId, String content,
                                         Consumer<ItineraryAgentEvent> eventConsumer);

    /** 按已授权的聊天消息编号批量恢复完整行程。 */
    Map<Long, Map<String, Object>> getItineraryMapByMessageIds(Collection<Long> messageIds);

    /** 按需解析某一天的交通段与动态地图路线点。 */
    ItineraryRouteResult resolveItineraryRoute(Long conversationId, Long memberId, Long messageId, Integer day);

    ItinerarySlotResult resolveItinerarySlot(Long conversationId, Long memberId, Long messageId,
                                                  Integer day, String slot);

}
