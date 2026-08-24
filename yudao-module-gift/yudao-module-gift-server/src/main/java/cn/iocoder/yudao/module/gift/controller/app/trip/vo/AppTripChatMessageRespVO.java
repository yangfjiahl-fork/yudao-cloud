package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AppTripChatMessageRespVO {

    private Long id;
    private Long replyId;
    private String type;
    private String content;
    /** 仅行程助手消息有值，前端直接以该结构恢复行程卡。 */
    private Map<String, Object> itinerary;
    private LocalDateTime createTime;

}
