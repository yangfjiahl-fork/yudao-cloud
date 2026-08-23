package cn.iocoder.yudao.module.ai.service.chat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationCreateMyReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationUpdateMyReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.mysql.chat.AiChatConversationMapper;
import cn.iocoder.yudao.module.ai.service.knowledge.AiKnowledgeService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.CHAT_CONVERSATION_MODEL_ERROR;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.CHAT_CONVERSATION_NOT_EXISTS;

/**
 * AI 聊天对话 Service 实现类
 *
 * @author fansili
 */
@Service
@Validated
@Slf4j
public class AiChatConversationServiceImpl implements AiChatConversationService {

    @Resource
    private AiChatConversationMapper chatConversationMapper;

    @Resource
    private AiModelService modalService;
    @Resource
    private AiChatRoleService chatRoleService;
    @Resource
    private AiKnowledgeService knowledgeService;

    @Override
    public Long createChatConversationMy(AiChatConversationCreateMyReqVO createReqVO, Long userId) {
        return createChatConversationMy(createReqVO, userId, UserTypeEnum.ADMIN.getValue());
    }

    @Override
    public Long createChatConversationMy(AiChatConversationCreateMyReqVO createReqVO, Long userId, Integer userType) {
        // 1.1 获得 AiChatRoleDO 聊天角色
        AiChatRoleDO role = createReqVO.getRoleId() != null ? chatRoleService.validateChatRole(createReqVO.getRoleId(), userType) : null;
        // 1.2 获得 AiModelDO 聊天模型
        AiModelDO model = role != null && role.getModelId() != null ? modalService.validateModel(role.getModelId())
                : modalService.getRequiredDefaultModel(AiModelTypeEnum.CHAT.getType());
        Assert.notNull(model, "必须找到默认模型");
        validateChatModel(model);

        // 1.3 校验知识库
        if (Objects.nonNull(createReqVO.getKnowledgeId())) {
            knowledgeService.validateKnowledgeExists(createReqVO.getKnowledgeId());
        }

        // 2. 创建 AiChatConversationDO 聊天对话
        AiChatConversationDO conversation = new AiChatConversationDO().setUserId(userId).setUserType(userType).setPinned(false)
                .setProvinceId(createReqVO.getProvinceId()).setCityId(createReqVO.getCityId())
                .setDistrictId(createReqVO.getDistrictId())
                .setModelId(model.getId()).setModel(model.getModel())
                .setTemperature(model.getTemperature()).setMaxTokens(model.getMaxTokens()).setMaxContexts(model.getMaxContexts());
        if (role != null) {
            conversation.setTitle(role.getName()).setRoleId(role.getId()).setSystemMessage(role.getSystemMessage());
        } else {
            conversation.setTitle(AiChatConversationDO.TITLE_DEFAULT);
        }
        chatConversationMapper.insert(conversation);
        return conversation.getId();
    }

    @Override
    public void updateChatConversationMy(AiChatConversationUpdateMyReqVO updateReqVO, Long userId) {
        updateChatConversationMy(updateReqVO, userId, UserTypeEnum.ADMIN.getValue());
    }

    @Override
    public void updateChatConversationMy(AiChatConversationUpdateMyReqVO updateReqVO, Long userId, Integer userType) {
        // 1.1 校验对话是否存在
        AiChatConversationDO conversation = validateChatConversationExists(updateReqVO.getId(), userType);
        if (ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        // 1.2 校验模型是否存在（修改模型的情况）
        AiModelDO model = null;
        if (updateReqVO.getModelId() != null) {
            model = modalService.validateModel(updateReqVO.getModelId());
        }

        // 1.3 校验知识库是否存在
        if (updateReqVO.getKnowledgeId() != null) {
            knowledgeService.validateKnowledgeExists(updateReqVO.getKnowledgeId());
        }

        // 2. 更新对话信息
        AiChatConversationDO updateObj = BeanUtils.toBean(updateReqVO, AiChatConversationDO.class);
        if (Boolean.TRUE.equals(updateReqVO.getPinned())) {
            updateObj.setPinnedTime(LocalDateTime.now());
        }
        if (model != null) {
            updateObj.setModel(model.getModel());
        }
        chatConversationMapper.updateById(updateObj);
    }

    @Override
    public List<AiChatConversationDO> getChatConversationListByUserId(Long userId) {
        return getChatConversationListByUserId(userId, UserTypeEnum.ADMIN.getValue());
    }

    @Override
    public List<AiChatConversationDO> getChatConversationListByUserId(Long userId, Integer userType) {
        return chatConversationMapper.selectListByUserId(userId, userType);
    }

    @Override
    public AiChatConversationDO getChatConversation(Long id) {
        return getChatConversation(id, UserTypeEnum.ADMIN.getValue());
    }

    @Override
    public AiChatConversationDO getChatConversation(Long id, Integer userType) {
        return chatConversationMapper.selectByIdAndUserType(id, userType);
    }

    @Override
    public void deleteChatConversationMy(Long id, Long userId) {
        deleteChatConversationMy(id, userId, UserTypeEnum.ADMIN.getValue());
    }

    @Override
    public void deleteChatConversationMy(Long id, Long userId, Integer userType) {
        // 1. 校验对话是否存在
        AiChatConversationDO conversation = validateChatConversationExists(id, userType);
        if (conversation == null || ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        // 2. 执行删除
        chatConversationMapper.deleteById(id);
    }

    @Override
    public void deleteChatConversationByAdmin(Long id) {
        // 1. 校验对话是否存在
        AiChatConversationDO conversation = validateChatConversationExists(id, UserTypeEnum.ADMIN.getValue());
        if (conversation == null) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        // 2. 执行删除
        chatConversationMapper.deleteById(id);
    }

    private void validateChatModel(AiModelDO model) {
        if (ObjectUtil.isAllNotEmpty(model.getTemperature(), model.getMaxTokens(), model.getMaxContexts())) {
            return;
        }
        Assert.equals(model.getType(), AiModelTypeEnum.CHAT.getType(), "模型类型不正确：" + model);
        throw exception(CHAT_CONVERSATION_MODEL_ERROR);
    }

    @Override
    public AiChatConversationDO validateChatConversationExists(Long id) {
        return validateChatConversationExists(id, UserTypeEnum.ADMIN.getValue());
    }

    @Override
    public AiChatConversationDO validateChatConversationExists(Long id, Integer userType) {
        AiChatConversationDO conversation = chatConversationMapper.selectByIdAndUserType(id, userType);
        if (conversation == null) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        return conversation;
    }

    @Override
    public void deleteChatConversationMyByUnpinned(Long userId) {
        deleteChatConversationMyByUnpinned(userId, UserTypeEnum.ADMIN.getValue());
    }

    @Override
    public void deleteChatConversationMyByUnpinned(Long userId, Integer userType) {
        List<AiChatConversationDO> list = chatConversationMapper.selectListByUserIdAndPinned(userId, userType, false);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        chatConversationMapper.deleteByIds(convertList(list, AiChatConversationDO::getId));
    }

    @Override
    public PageResult<AiChatConversationDO> getChatConversationPage(AiChatConversationPageReqVO pageReqVO) {
        return chatConversationMapper.selectChatConversationPage(pageReqVO, UserTypeEnum.ADMIN.getValue());
    }

    @Override
    public void increaseTokenUsage(Long id, Integer userType, Long promptTokens, Long completionTokens,
                                   Long cachedTokens, Long totalTokens) {
        chatConversationMapper.increaseTokenUsage(id, userType, promptTokens, completionTokens, cachedTokens, totalTokens);
    }

}
