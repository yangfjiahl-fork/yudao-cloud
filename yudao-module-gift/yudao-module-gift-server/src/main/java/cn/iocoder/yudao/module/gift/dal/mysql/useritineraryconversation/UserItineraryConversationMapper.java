package cn.iocoder.yudao.module.gift.dal.mysql.useritineraryconversation;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversation.UserItineraryConversationDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserItineraryConversationMapper extends BaseMapperX<UserItineraryConversationDO> {

    default UserItineraryConversationDO selectByIdAndMemberId(Long id, Long memberId) {
        return selectOne(new LambdaQueryWrapperX<UserItineraryConversationDO>()
                .eq(UserItineraryConversationDO::getId, id)
                .eq(UserItineraryConversationDO::getMemberId, memberId));
    }

    default PageResult<UserItineraryConversationDO> selectPageByMemberId(PageParam pageReqVO, Long memberId) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<UserItineraryConversationDO>()
                .eq(UserItineraryConversationDO::getMemberId, memberId)
                .orderByDesc(UserItineraryConversationDO::getPinned)
                .orderByDesc(UserItineraryConversationDO::getUpdateTime));
    }

}
