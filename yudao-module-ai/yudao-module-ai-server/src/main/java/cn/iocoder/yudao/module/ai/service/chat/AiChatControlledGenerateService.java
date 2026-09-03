package cn.iocoder.yudao.module.ai.service.chat;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.tracer.core.annotation.BizTrace;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateRespDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateStreamRespDTO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.util.AiChatPromptUtils;
import cn.iocoder.yudao.module.ai.util.AiUtils;
import jakarta.annotation.Resource;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.CHAT_CONVERSATION_NOT_EXISTS;

/**
 * 给垂直业务编排器使用的模型调用入口。
 *
 * 它校验调用者拥有会话，再由服务端选择 chat_role 的 systemMessage、model 和参数；刻意不继承通用聊天的
 * 知识库召回、联网搜索、ToolCallback 与 MCP 能力。
 */
@Service
@Slf4j
public class AiChatControlledGenerateService {

    private static final String CONTROLLED_GENERATE_METRIC_NAME = "yudao.ai.controlled.generate";

    @Resource
    private AiChatConversationService conversationService;
    @Resource
    private AiModelService modelService;
    @Resource
    private AiChatRoleService chatRoleService;
    @Resource
    private MeterRegistry meterRegistry;

    @BizTrace(operationName = "ai.controlled.generate", type = "'ai.chat.conversation'", id = "#reqDTO.conversationId")
    public AiChatGenerateRespDTO generate(AiChatGenerateReqDTO reqDTO) {
        GenerateContext context = buildContext(reqDTO);
        ChatResponse response = observeGenerate(context, "sync",
                () -> modelService.getChatModel(context.model().getId()).call(context.prompt()));
        String responseContent = AiUtils.getChatResponseContent(response);
        log.info("[generate][conversationId({}) userId({}) roleId({}) model({}) responseLength({})]",
                reqDTO.getConversationId(), reqDTO.getUserId(), reqDTO.getRoleId(), context.model().getModel(),
                StrUtil.length(responseContent));
        Usage usage = response != null && response.getMetadata() != null ? response.getMetadata().getUsage() : null;
        return new AiChatGenerateRespDTO().setModel(context.model().getModel()).setContent(responseContent)
                .setPromptTokens(usage != null ? ObjUtil.defaultIfNull(usage.getPromptTokens(), 0).longValue() : 0L)
                .setCompletionTokens(usage != null ? ObjUtil.defaultIfNull(usage.getCompletionTokens(), 0).longValue() : 0L)
                .setTotalTokens(usage != null ? ObjUtil.defaultIfNull(usage.getTotalTokens(), 0).longValue() : 0L);
    }

    @BizTrace(operationName = "ai.controlled.generate.stream", type = "'ai.chat.conversation'", id = "#reqDTO.conversationId")
    public AiChatGenerateRespDTO generateStream(AiChatGenerateReqDTO reqDTO,
                                                Consumer<AiChatGenerateStreamRespDTO> callback) {
        GenerateContext context = buildContext(reqDTO);
        StringBuilder responseContent = new StringBuilder();
        AtomicReference<AiChatGenerateStreamRespDTO> lastChunk = new AtomicReference<>();
        observeGenerate(context, "stream", () -> {
            modelService.getChatModel(context.model().getId()).stream(context.prompt()).doOnNext(chunk -> {
                Usage usage = chunk.getMetadata() != null ? chunk.getMetadata().getUsage() : null;
                AiChatGenerateStreamRespDTO response = new AiChatGenerateStreamRespDTO()
                        .setModel(context.model().getModel())
                        .setContent(StrUtil.nullToDefault(AiUtils.getChatResponseContent(chunk), ""))
                        .setPromptTokens(usage != null ? ObjUtil.defaultIfNull(usage.getPromptTokens(), 0).longValue() : 0L)
                        .setCompletionTokens(usage != null ? ObjUtil.defaultIfNull(usage.getCompletionTokens(), 0).longValue() : 0L)
                        .setTotalTokens(usage != null ? ObjUtil.defaultIfNull(usage.getTotalTokens(), 0).longValue() : 0L);
                responseContent.append(response.getContent());
                lastChunk.set(response);
                callback.accept(response);
            }).blockLast();
            return null;
        });
        AiChatGenerateStreamRespDTO last = lastChunk.get();
        if (last == null) {
            throw new IllegalStateException("模型流未返回内容");
        }
        log.info("[generateStream][conversationId({}) userId({}) roleId({}) model({}) responseLength({}) 完成]",
                reqDTO.getConversationId(), reqDTO.getUserId(), reqDTO.getRoleId(), context.model().getModel(),
                responseContent.length());
        return new AiChatGenerateRespDTO().setModel(last.getModel()).setContent(responseContent.toString())
                .setPromptTokens(last.getPromptTokens()).setCompletionTokens(last.getCompletionTokens())
                .setTotalTokens(last.getTotalTokens());
    }

    private <T> T observeGenerate(GenerateContext context, String mode, Supplier<T> supplier) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";
        try {
            return supplier.get();
        } catch (RuntimeException | Error ex) {
            outcome = "error";
            throw ex;
        } finally {
            sample.stop(Timer.builder(CONTROLLED_GENERATE_METRIC_NAME)
                    .tags("mode", mode, "platform", context.model().getPlatform(), "outcome", outcome)
                    .register(meterRegistry));
        }
    }

    private GenerateContext buildContext(AiChatGenerateReqDTO reqDTO) {
        Integer userType = ObjUtil.defaultIfNull(reqDTO.getUserType(), UserTypeEnum.ADMIN.getValue());
        AiChatConversationDO conversation = conversationService.getChatConversation(reqDTO.getConversationId(), userType);
        if (conversation == null || ObjUtil.notEqual(conversation.getUserId(), reqDTO.getUserId())) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        AiChatRoleDO role = reqDTO.getRoleId() != null
                ? chatRoleService.validateChatRole(reqDTO.getRoleId(), userType) : null;
        AiModelDO model = modelService.validateModel(role != null ? role.getModelId() : conversation.getModelId());
        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
        String systemMessage = StrUtil.blankToDefault(role != null ? role.getSystemMessage() : conversation.getSystemMessage(),
                "你是一个严谨的助手。");
        systemMessage = AiChatPromptUtils.buildSystemMessage(systemMessage, reqDTO.getPromptVariables());
        messages.add(new SystemMessage(systemMessage));
        messages.add(new UserMessage(reqDTO.getContent()));

        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        Double temperature = role != null ? model.getTemperature() : conversation.getTemperature();
        Integer maxTokens = role != null ? model.getMaxTokens() : conversation.getMaxTokens();
        ChatOptions options = AiUtils.buildChatOptions(platform, model.getModel(), temperature, maxTokens);
        log.info("[generate][conversationId({}) userId({}) userType({}) roleId({}) model({}) temperature({}) maxTokens({}) "
                        + "systemMessageLength({}) userMessageLength({})]",
                reqDTO.getConversationId(), reqDTO.getUserId(), userType, reqDTO.getRoleId(), model.getModel(), temperature,
                maxTokens, systemMessage.length(), StrUtil.length(reqDTO.getContent()));
        return new GenerateContext(model, new Prompt(messages, options));
    }

    private record GenerateContext(AiModelDO model, Prompt prompt) {
    }

}
