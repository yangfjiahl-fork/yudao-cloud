package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentEvent;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentResult;

import java.util.function.Consumer;

/** 独立的百炼 Managed Agents 旅行链路，不改变原旅行 Agent。 */
public interface ManagedTripAgentService {

    TripAgentResult handleMessage(Long conversationId, Long memberId, String content,
                                  Consumer<TripAgentEvent> eventConsumer);

}
