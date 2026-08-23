package cn.iocoder.yudao.module.ai.dal.mysql.chat;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 聊天对话 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiChatConversationMapper extends BaseMapperX<AiChatConversationDO> {

    default AiChatConversationDO selectByIdAndUserType(Long id, Integer userType) {
        return selectOne(new LambdaQueryWrapperX<AiChatConversationDO>()
                .eq(AiChatConversationDO::getId, id)
                .eq(AiChatConversationDO::getUserType, userType));
    }

    default List<AiChatConversationDO> selectListByUserId(Long userId, Integer userType) {
        return selectList(new LambdaQueryWrapperX<AiChatConversationDO>()
                .eq(AiChatConversationDO::getUserId, userId)
                .eq(AiChatConversationDO::getUserType, userType)
                .orderByDesc(AiChatConversationDO::getPinned)
                .orderByDesc(AiChatConversationDO::getPinnedTime)
                .orderByDesc(AiChatConversationDO::getId));
    }

    default List<AiChatConversationDO> selectListByUserIdAndPinned(Long userId, Integer userType, boolean pinned) {
        return selectList(new LambdaQueryWrapperX<AiChatConversationDO>()
                .eq(AiChatConversationDO::getUserId, userId)
                .eq(AiChatConversationDO::getUserType, userType)
                .eq(AiChatConversationDO::getPinned, pinned));
    }

    default PageResult<AiChatConversationDO> selectChatConversationPage(AiChatConversationPageReqVO pageReqVO,
                                                                         Integer userType) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<AiChatConversationDO>()
                .eq(AiChatConversationDO::getUserType, userType)
                .eqIfPresent(AiChatConversationDO::getUserId, pageReqVO.getUserId())
                .likeIfPresent(AiChatConversationDO::getTitle, pageReqVO.getTitle())
                .betweenIfPresent(AiChatConversationDO::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(AiChatConversationDO::getId));
    }

    default void increaseTokenUsage(Long id, Integer userType, Long promptTokens, Long completionTokens,
                                    Long cachedTokens, Long totalTokens) {
        update(null, new LambdaUpdateWrapper<AiChatConversationDO>()
                .eq(AiChatConversationDO::getId, id)
                .eq(AiChatConversationDO::getUserType, userType)
                .setSql("prompt_tokens = COALESCE(prompt_tokens, 0) + {0}", promptTokens)
                .setSql("completion_tokens = COALESCE(completion_tokens, 0) + {0}", completionTokens)
                .setSql("cached_tokens = COALESCE(cached_tokens, 0) + {0}", cachedTokens)
                .setSql("total_tokens = COALESCE(total_tokens, 0) + {0}", totalTokens));
    }

}
