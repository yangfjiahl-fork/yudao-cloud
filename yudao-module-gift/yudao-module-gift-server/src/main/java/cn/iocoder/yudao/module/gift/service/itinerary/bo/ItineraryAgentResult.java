package cn.iocoder.yudao.module.gift.service.itinerary.bo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ItineraryAgentResult {

    /** QUESTION 或 ITINERARY。 */
    private String type;
    private Long messageId;
    private String content;
    private Map<String, Object> itinerary;
    private List<String> missingRequired;

}
