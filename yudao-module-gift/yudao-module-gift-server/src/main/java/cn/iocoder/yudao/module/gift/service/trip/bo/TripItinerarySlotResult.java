package cn.iocoder.yudao.module.gift.service.trip.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/** 单个行程骨架节点的异步补充结果。 */
@Data
@Accessors(chain = true)
public class TripItinerarySlotResult {

    private Long messageId;
    private Long slotId;
    private Integer day;
    private String slot;
    private String status;
    private String detail;
    private List<Map<String, Object>> candidates;
    private List<String> citationIds;

}
