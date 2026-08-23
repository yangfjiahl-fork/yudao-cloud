package cn.iocoder.yudao.module.ai.api.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiChatMessageRespDTO {

    private Long id;
    private Long conversationId;
    private Long replyId;
    private String type;
    private String content;
    private LocalDateTime createTime;

}
