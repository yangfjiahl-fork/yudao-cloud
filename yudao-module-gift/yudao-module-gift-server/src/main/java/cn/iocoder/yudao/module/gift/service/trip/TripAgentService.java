package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentResult;

public interface TripAgentService {

    void createTrip(Long conversationId, Long memberId);

    TripAgentResult handleMessage(Long conversationId, Long memberId, String content);

}
