package cn.iocoder.yudao.module.gift.service.trip.bo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TripAgentResult {

    /** QUESTION 或 ITINERARY。 */
    private String type;
    private Long messageId;
    private String content;
    private Map<String, Object> itinerary;
    private List<String> missingRequired;

}
