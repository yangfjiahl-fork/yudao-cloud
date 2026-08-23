package cn.iocoder.yudao.module.ai.api.chat.dto;

import lombok.Data;

@Data
public class AiChatMessageCreateAssistantReqDTO {

    private Long conversationId;
    private Long userId;
    private Integer userType;
    private String content;

}
