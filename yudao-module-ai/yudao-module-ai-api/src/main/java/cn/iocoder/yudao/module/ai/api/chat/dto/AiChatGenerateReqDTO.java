package cn.iocoder.yudao.module.ai.api.chat.dto;

import lombok.Data;

/**
 * 由业务模块发起的受控文本生成请求。
 *
 * 调用方只能使用会话已经绑定的模型和角色设定；不暴露工具、联网搜索或模型配置。
 */
@Data
public class AiChatGenerateReqDTO {

    private Long conversationId;
    private Long userId;
    private Integer userType;
    /** 业务模块固定的附加系统约束。 */
    private String instruction;
    /** 业务模块整理后的输入，不会自动携带通用聊天历史。 */
    private String content;

}
