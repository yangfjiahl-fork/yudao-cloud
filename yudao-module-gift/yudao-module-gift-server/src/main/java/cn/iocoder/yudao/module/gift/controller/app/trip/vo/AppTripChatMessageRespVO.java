package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppTripChatMessageRespVO {

    private Long id;
    private Long replyId;
    private String type;
    private String content;
    private LocalDateTime createTime;

}
