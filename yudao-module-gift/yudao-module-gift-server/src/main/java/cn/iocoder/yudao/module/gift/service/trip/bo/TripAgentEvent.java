package cn.iocoder.yudao.module.gift.service.trip.bo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/** 服务端旅行状态机向 SSE 层发送的事件。 */
@Data
public class TripAgentEvent {

    private String event;
    private String stage;
    private Long messageId;
    private String content;
    private Integer sequence;
    private String itemType;
    private Map<String, Object> item;
    private Map<String, Object> itinerary;
    private List<String> missingRequired;

    public static TripAgentEvent of(String event, String stage, String content) {
        return new TripAgentEvent().setEvent(event).setStage(stage).setContent(content);
    }

}
