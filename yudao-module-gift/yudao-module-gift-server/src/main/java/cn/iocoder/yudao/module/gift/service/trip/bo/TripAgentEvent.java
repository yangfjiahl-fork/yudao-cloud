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
    /** event=model_delta 时为仅服务端使用的模型原始文本分片。 */
    private String content;
    private Integer sequence;
    private String itemType;
    private Map<String, Object> item;
    private Map<String, Object> itinerary;
    private List<String> missingRequired;
    /** 结构化输入卡片；所有快捷选择和表单输入统一通过该字段输出。 */
    private List<Map<String, Object>> inputCards;

    public static TripAgentEvent of(String event, String stage, String content) {
        return new TripAgentEvent().setEvent(event).setStage(stage).setContent(content);
    }

}
