package cn.iocoder.yudao.module.ai.api.chat.dto;

import lombok.Data;

@Data
public class AiChatConversationUpdateReqDTO {

    private Long id;
    private Long userId;
    private Integer userType;
    private String title;
    private Boolean pinned;

}
