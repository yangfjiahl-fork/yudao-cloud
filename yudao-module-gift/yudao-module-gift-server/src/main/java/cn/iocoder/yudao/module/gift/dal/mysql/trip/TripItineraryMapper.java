package cn.iocoder.yudao.module.gift.dal.mysql.trip;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripItineraryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface TripItineraryMapper extends BaseMapperX<TripItineraryDO> {

    default TripItineraryDO selectByMessageId(Long messageId) {
        return selectOne(TripItineraryDO::getMessageId, messageId);
    }

    default List<TripItineraryDO> selectListByMessageIds(Collection<Long> messageIds) {
        return selectList(new LambdaQueryWrapperX<TripItineraryDO>()
                .inIfPresent(TripItineraryDO::getMessageId, messageIds));
    }

    default Integer selectMaxVersionByTripId(Long tripId) {
        TripItineraryDO itinerary = selectOne(new LambdaQueryWrapperX<TripItineraryDO>()
                .eq(TripItineraryDO::getTripId, tripId)
                .orderByDesc(TripItineraryDO::getVersion)
                .last("LIMIT 1"));
        return itinerary != null ? itinerary.getVersion() : null;
    }
}
