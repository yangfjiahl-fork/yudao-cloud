package cn.iocoder.yudao.module.ai.api.chat;

import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationCreateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationRespDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationUpdateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageCreateAssistantReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageRespDTO;

import java.util.List;

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

}
