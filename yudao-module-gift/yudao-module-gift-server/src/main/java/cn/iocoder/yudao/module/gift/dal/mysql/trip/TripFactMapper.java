package cn.iocoder.yudao.module.gift.dal.mysql.trip;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripFactDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TripFactMapper extends BaseMapperX<TripFactDO> {

    default List<TripFactDO> selectActiveByTripId(Long tripId) {
        return selectList(new LambdaQueryWrapperX<TripFactDO>()
                .eq(TripFactDO::getTripId, tripId)
                .eq(TripFactDO::getStatus, 1)
                .and(wrapper -> wrapper.isNull(TripFactDO::getExpiresAt)
                        .or().gt(TripFactDO::getExpiresAt, LocalDateTime.now())));
    }

    default TripFactDO selectActiveByTripIdAndFactKey(Long tripId, String factKey) {
        return selectOne(new LambdaQueryWrapperX<TripFactDO>()
                .eq(TripFactDO::getTripId, tripId)
                .eq(TripFactDO::getFactKey, factKey)
                .eq(TripFactDO::getStatus, 1)
                .and(wrapper -> wrapper.isNull(TripFactDO::getExpiresAt)
                        .or().gt(TripFactDO::getExpiresAt, LocalDateTime.now()))
                .orderByDesc(TripFactDO::getUpdateTime)
                .last("LIMIT 1"));
    }

    default List<TripFactDO> selectActiveByTripIdAndFactKeyPrefix(Long tripId, String factKeyPrefix) {
        return selectList(new LambdaQueryWrapperX<TripFactDO>()
                .eq(TripFactDO::getTripId, tripId)
                .likeRight(TripFactDO::getFactKey, factKeyPrefix)
                .eq(TripFactDO::getStatus, 1)
                .and(wrapper -> wrapper.isNull(TripFactDO::getExpiresAt)
                        .or().gt(TripFactDO::getExpiresAt, LocalDateTime.now()))
                .orderByDesc(TripFactDO::getUpdateTime));
    }

}
