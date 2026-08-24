package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** SSE 的业务事件；前端按 event 选择问题卡或行程卡，不能从文本中推断事实。 */
@Data
public class AppTripChatStreamRespVO {

    private String event;
    /** Intake、Research、Composer 等状态机阶段。 */
    private String stage;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long conversationId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long messageId;
    private String content;
    private Integer sequence;
    private String itemType;
    private Map<String, Object> item;
    private Map<String, Object> itinerary;
    private List<String> missingRequired;

}
