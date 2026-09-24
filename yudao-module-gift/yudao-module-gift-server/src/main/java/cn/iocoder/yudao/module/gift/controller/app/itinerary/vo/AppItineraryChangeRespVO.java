package cn.iocoder.yudao.module.gift.controller.app.itinerary.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/** App 手动编辑行程响应。 */
@Data
public class AppItineraryChangeRespVO {

    private Long itineraryId;
    private Long messageId;
    private String content;
    private List<Integer> affectedDays;
    private Map<String, Object> itinerary;

}
