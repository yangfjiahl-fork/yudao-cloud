package cn.iocoder.yudao.module.gift.service.trip.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/** 当前个人行程版本在 Agent 上下文中的轻量快照。 */
@Data
@Accessors(chain = true)
public class TripItinerarySnapshot {

    private Long id;
    private Integer version;
    private Long messageId;
    private String contentJson;

}
