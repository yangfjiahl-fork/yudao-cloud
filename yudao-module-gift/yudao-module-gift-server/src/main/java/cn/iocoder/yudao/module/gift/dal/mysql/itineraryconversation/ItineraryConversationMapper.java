package cn.iocoder.yudao.module.gift.dal.mysql.itineraryconversation;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.itineraryconversation.ItineraryConversationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ItineraryConversationMapper extends BaseMapperX<ItineraryConversationDO> {

    default ItineraryConversationDO selectByIdAndMemberId(Long id, Long memberId) {
        return selectOne(new LambdaQueryWrapperX<ItineraryConversationDO>()
                .eq(ItineraryConversationDO::getId, id)
                .eq(ItineraryConversationDO::getMemberId, memberId));
    }

    default List<ItineraryConversationDO> selectListByMemberId(Long memberId) {
        return selectList(new LambdaQueryWrapperX<ItineraryConversationDO>()
                .eq(ItineraryConversationDO::getMemberId, memberId)
                .orderByDesc(ItineraryConversationDO::getPinned)
                .orderByDesc(ItineraryConversationDO::getUpdateTime));
    }

}
