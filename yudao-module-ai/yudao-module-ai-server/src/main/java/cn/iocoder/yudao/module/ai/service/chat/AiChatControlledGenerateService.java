package cn.iocoder.yudao.module.ai.service.chat;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateRespDTO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.util.AiUtils;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.CHAT_CONVERSATION_NOT_EXISTS;

/**
 * 给垂直业务编排器使用的模型调用入口。
 *
 * 它复用会话在创建时从 chat_role 快照下来的 systemMessage、model 和参数，但刻意不继承通用聊天的
 * 知识库召回、联网搜索、ToolCallback 与 MCP 能力。
 */
@Service
public class AiChatControlledGenerateService {

    @Resource
    private AiChatConversationService conversationService;
    @Resource
    private AiModelService modelService;

    public AiChatGenerateRespDTO generate(AiChatGenerateReqDTO reqDTO) {
        Integer userType = ObjUtil.defaultIfNull(reqDTO.getUserType(), UserTypeEnum.ADMIN.getValue());
        AiChatConversationDO conversation = conversationService.getChatConversation(reqDTO.getConversationId(), userType);
        if (conversation == null || ObjUtil.notEqual(conversation.getUserId(), reqDTO.getUserId())) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        AiModelDO model = modelService.validateModel(conversation.getModelId());
        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
        String systemMessage = StrUtil.blankToDefault(conversation.getSystemMessage(), "你是一个严谨的助手。");
        if (StrUtil.isNotBlank(reqDTO.getInstruction())) {
            systemMessage += "\n\n" + reqDTO.getInstruction();
        }
        messages.add(new SystemMessage(systemMessage));
        messages.add(new UserMessage(reqDTO.getContent()));

        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatOptions options = AiUtils.buildChatOptions(platform, model.getModel(), conversation.getTemperature(),
                conversation.getMaxTokens());
        ChatResponse response = modelService.getChatModel(model.getId()).call(new Prompt(messages, options));
        Usage usage = response != null && response.getMetadata() != null ? response.getMetadata().getUsage() : null;
        return new AiChatGenerateRespDTO().setModel(model.getModel()).setContent(AiUtils.getChatResponseContent(response))
                .setPromptTokens(usage != null ? ObjUtil.defaultIfNull(usage.getPromptTokens(), 0).longValue() : 0L)
                .setCompletionTokens(usage != null ? ObjUtil.defaultIfNull(usage.getCompletionTokens(), 0).longValue() : 0L)
                .setTotalTokens(usage != null ? ObjUtil.defaultIfNull(usage.getTotalTokens(), 0).longValue() : 0L);
    }

}
