package cn.iocoder.yudao.module.gift.dal.mysql.trip;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TripPlanMapper extends BaseMapperX<TripPlanDO> {

    default TripPlanDO selectByConversationIdAndMemberId(Long conversationId, Long memberId) {
        return selectOne(new LambdaQueryWrapperX<TripPlanDO>()
                .eq(TripPlanDO::getConversationId, conversationId)
                .eq(TripPlanDO::getMemberId, memberId));
    }

}
