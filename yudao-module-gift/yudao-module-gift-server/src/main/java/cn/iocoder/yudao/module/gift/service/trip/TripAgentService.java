package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentResult;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentEvent;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripItinerarySlotResult;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

public interface TripAgentService {

    void createTrip(Long conversationId, Long memberId);

    TripAgentResult handleMessage(Long conversationId, Long memberId, String content,
                                  Consumer<TripAgentEvent> eventConsumer);

    /** 按已授权的聊天消息编号批量恢复完整行程。 */
    Map<Long, Map<String, Object>> getItineraryMapByMessageIds(Collection<Long> messageIds);

    TripItinerarySlotResult resolveItinerarySlot(Long conversationId, Long memberId, Long messageId,
                                                  Integer day, String slot);

}
