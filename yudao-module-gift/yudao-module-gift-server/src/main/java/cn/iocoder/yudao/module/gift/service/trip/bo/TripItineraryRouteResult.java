package cn.iocoder.yudao.module.gift.service.trip.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/** 单日行程交通段的按需解析结果。 */
@Data
@Accessors(chain = true)
public class TripItineraryRouteResult {

    private Long messageId;
    private Integer day;
    private String status;
    private List<Map<String, Object>> transportSegments;

}
