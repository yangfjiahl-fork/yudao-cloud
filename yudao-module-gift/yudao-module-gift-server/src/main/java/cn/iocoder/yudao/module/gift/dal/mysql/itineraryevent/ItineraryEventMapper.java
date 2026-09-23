package cn.iocoder.yudao.module.gift.dal.mysql.itineraryevent;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.itineraryevent.ItineraryEventDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ItineraryEventMapper extends BaseMapperX<ItineraryEventDO> {

    default List<ItineraryEventDO> selectListByConversationId(Long conversationId) {
        return selectList(new LambdaQueryWrapperX<ItineraryEventDO>()
                .eq(ItineraryEventDO::getConversationId, conversationId)
                .in(ItineraryEventDO::getEventType, "USER_MESSAGE", "ASSISTANT_MESSAGE", "ITINERARY")
                .orderByAsc(ItineraryEventDO::getId));
    }

}
