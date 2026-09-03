package cn.iocoder.yudao.module.gift.service.trip.bo;

import cn.iocoder.yudao.module.gift.service.trip.TripTravelQueryService;
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
    private TripTravelQueryService.Weather weather;
    /** 当前日按行程顺序测得的相邻 POI 交通段；day=0 时为空。 */
    private List<Map<String, Object>> transportSegments;

}
