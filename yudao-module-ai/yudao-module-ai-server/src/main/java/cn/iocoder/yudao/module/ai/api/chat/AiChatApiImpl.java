package cn.iocoder.yudao.module.ai.api.chat;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.api.chat.AiChatApi;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationCreateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationRespDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationUpdateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageCreateAssistantReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageRespDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateRespDTO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationCreateMyReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationUpdateMyReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatMessageDO;
import cn.iocoder.yudao.module.ai.service.chat.AiChatConversationService;
import cn.iocoder.yudao.module.ai.service.chat.AiChatControlledGenerateService;
import cn.iocoder.yudao.module.ai.service.chat.AiChatMessageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiChatApiImpl implements AiChatApi {

    @Resource
    private AiChatConversationService chatConversationService;
    @Resource
    private AiChatMessageService chatMessageService;
    @Resource
    private AiChatControlledGenerateService controlledGenerateService;

    @Override
    public Long createConversation(AiChatConversationCreateReqDTO reqDTO) {
        AiChatConversationCreateMyReqVO serviceReqVO = new AiChatConversationCreateMyReqVO();
        serviceReqVO.setRoleId(reqDTO.getRoleId());
        serviceReqVO.setProvinceId(reqDTO.getProvinceId());
        serviceReqVO.setCityId(reqDTO.getCityId());
        serviceReqVO.setDistrictId(reqDTO.getDistrictId());
        return chatConversationService.createChatConversationMy(serviceReqVO, reqDTO.getUserId(), reqDTO.getUserType());
    }

    @Override
    public void updateConversation(AiChatConversationUpdateReqDTO reqDTO) {
        AiChatConversationUpdateMyReqVO serviceReqVO = BeanUtils.toBean(reqDTO, AiChatConversationUpdateMyReqVO.class);
        chatConversationService.updateChatConversationMy(serviceReqVO, reqDTO.getUserId(), reqDTO.getUserType());
    }

    @Override
    public List<AiChatConversationRespDTO> getConversationList(Long userId, Integer userType) {
        List<AiChatConversationDO> conversations = chatConversationService.getChatConversationListByUserId(userId, userType);
        return BeanUtils.toBean(conversations, AiChatConversationRespDTO.class);
    }

    @Override
    public AiChatConversationRespDTO getConversation(Long id, Long userId, Integer userType) {
        AiChatConversationDO conversation = chatConversationService.getChatConversation(id, userType);
        return conversation != null && userId.equals(conversation.getUserId())
                ? BeanUtils.toBean(conversation, AiChatConversationRespDTO.class) : null;
    }

    @Override
    public void deleteConversation(Long id, Long userId, Integer userType) {
        chatConversationService.deleteChatConversationMy(id, userId, userType);
    }

    @Override
    public List<AiChatMessageRespDTO> getMessageList(Long conversationId, Long userId, Integer userType) {
        AiChatConversationDO conversation = chatConversationService.getChatConversation(conversationId, userType);
        if (conversation == null || !userId.equals(conversation.getUserId())) {
            return List.of();
        }
        List<AiChatMessageDO> messages = chatMessageService.getChatMessageListByConversationId(conversationId, userType);
        return BeanUtils.toBean(messages, AiChatMessageRespDTO.class);
    }

    @Override
    public AiChatMessageRespDTO createAssistantMessage(AiChatMessageCreateAssistantReqDTO reqDTO) {
        AiChatMessageDO message = chatMessageService.createAssistantMessage(reqDTO.getConversationId(), reqDTO.getUserId(),
                reqDTO.getUserType(), reqDTO.getContent());
        return BeanUtils.toBean(message, AiChatMessageRespDTO.class);
    }

    @Override
    public AiChatMessageRespDTO createUserMessage(AiChatMessageCreateAssistantReqDTO reqDTO) {
        AiChatMessageDO message = chatMessageService.createUserMessage(reqDTO.getConversationId(), reqDTO.getUserId(),
                reqDTO.getUserType(), reqDTO.getContent());
        return BeanUtils.toBean(message, AiChatMessageRespDTO.class);
    }

    @Override
    public AiChatGenerateRespDTO generate(AiChatGenerateReqDTO reqDTO) {
        return controlledGenerateService.generate(reqDTO);
    }

}
