package cn.iocoder.yudao.module.ai.api.chat;

import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationCreateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationRespDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationUpdateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageCreateAssistantReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageRespDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateRespDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateStreamRespDTO;

import java.util.List;
import java.util.function.Consumer;

/**
 * AI 聊天通用能力 API
 */
public interface AiChatApi {

    Long createConversation(AiChatConversationCreateReqDTO reqDTO);

    void updateConversation(AiChatConversationUpdateReqDTO reqDTO);

    List<AiChatConversationRespDTO> getConversationList(Long userId, Integer userType);

    AiChatConversationRespDTO getConversation(Long id, Long userId, Integer userType);

    void deleteConversation(Long id, Long userId, Integer userType);

    List<AiChatMessageRespDTO> getMessageList(Long conversationId, Long userId, Integer userType);

    AiChatMessageRespDTO createAssistantMessage(AiChatMessageCreateAssistantReqDTO reqDTO);

    AiChatMessageRespDTO createUserMessage(AiChatMessageCreateAssistantReqDTO reqDTO);

    /**
     * 使用会话绑定的 chat_role、模型及模型参数执行一次不带工具的受控生成。
     */
    AiChatGenerateRespDTO generate(AiChatGenerateReqDTO reqDTO);

    /**
     * 使用服务端指定角色进行不带工具的受控流式生成。
     * 每个模型片段立即经 callback 回调；方法返回时代表模型流已结束。
     */
    AiChatGenerateRespDTO generateStream(AiChatGenerateReqDTO reqDTO, Consumer<AiChatGenerateStreamRespDTO> callback);

}
