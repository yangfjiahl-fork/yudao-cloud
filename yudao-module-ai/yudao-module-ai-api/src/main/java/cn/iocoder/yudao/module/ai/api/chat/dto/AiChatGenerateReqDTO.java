package cn.iocoder.yudao.module.ai.api.chat.dto;

import lombok.Data;

import java.util.Map;

/**
 * 由业务模块发起的受控文本生成请求。
 *
 * 调用方只能使用服务端指定的角色设定；不暴露工具、联网搜索或模型配置。
 */
@Data
public class AiChatGenerateReqDTO {

    private Long conversationId;
    private Long userId;
    private Integer userType;
    /**
     * 业务模块内部使用的角色编号。不会由 C 端请求传入或返回。
     * <p>
     * 为空时兼容使用会话创建时快照的角色和模型。
     */
    private Long roleId;
    /** 业务模块整理后的输入，不会自动携带通用聊天历史。 */
    private String content;
    /**
     * System Prompt 模板变量。模板语法为 {@code {{variableName}}}。
     */
    private Map<String, Object> promptVariables;

}
