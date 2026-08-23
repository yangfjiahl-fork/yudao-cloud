package cn.iocoder.yudao.module.ai.api.chat.dto;

import lombok.Data;

@Data
public class AiChatGenerateRespDTO {

    /** 实际调用的模型标识 */
    private String model;
    private String content;
    private Long promptTokens;
    private Long completionTokens;
    private Long totalTokens;

}
