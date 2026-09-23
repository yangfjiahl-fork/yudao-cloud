package cn.iocoder.yudao.module.gift.dal.mysql.useritineraryconversationevent;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversationevent.UserItineraryConversationEventDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserItineraryConversationEventMapper extends BaseMapperX<UserItineraryConversationEventDO> {

    default List<UserItineraryConversationEventDO> selectListByConversationId(Long conversationId) {
        return selectList(new LambdaQueryWrapperX<UserItineraryConversationEventDO>()
                .eq(UserItineraryConversationEventDO::getConversationId, conversationId)
                .in(UserItineraryConversationEventDO::getEventType, "USER_MESSAGE", "ASSISTANT_MESSAGE", "ITINERARY")
                .orderByAsc(UserItineraryConversationEventDO::getId));
    }

}
