package cn.iocoder.yudao.module.ai.api.chat.dto;

import lombok.Data;

@Data
public class AiChatConversationCreateReqDTO {

    private Long userId;
    private Integer userType;
    private Long roleId;
    private Long provinceId;
    private Long cityId;
    private Long districtId;

}
