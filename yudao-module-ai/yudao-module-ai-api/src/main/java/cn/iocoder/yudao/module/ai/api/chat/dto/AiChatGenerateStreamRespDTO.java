package cn.iocoder.yudao.module.ai.api.chat.dto;

import lombok.Data;

/** 受控模型调用的单个流式片段。 */
@Data
public class AiChatGenerateStreamRespDTO {

    private String model;
    private String content;
    private Long promptTokens;
    private Long completionTokens;
    private Long totalTokens;

}
