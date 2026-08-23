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
    private Long provinceId;
    private Long cityId;
    private Long districtId;
    private LocalDateTime createTime;

}
