package cn.iocoder.yudao.module.gift.service.itinerary.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/** 当前个人行程在 Agent 上下文中的轻量快照。 */
@Data
@Accessors(chain = true)
public class ItinerarySnapshot {

    private Long id;
    private Long messageId;
    private String contentJson;

}
