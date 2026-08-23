package cn.iocoder.yudao.module.ai.api.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiChatConversationRespDTO {

    private Long id;
    private Long userId;
    private Integer userType;
    private String title;
    private Boolean pinned;
    private LocalDateTime createTime;

}
