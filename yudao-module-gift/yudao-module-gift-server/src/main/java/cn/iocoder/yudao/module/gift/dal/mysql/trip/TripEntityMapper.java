package cn.iocoder.yudao.module.gift.dal.mysql.trip;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripEntityDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TripEntityMapper extends BaseMapperX<TripEntityDO> {

    default TripEntityDO selectByTripIdTypeAndName(Long tripId, String entityType, String name) {
        return selectOne(new LambdaQueryWrapperX<TripEntityDO>()
                .eq(TripEntityDO::getTripId, tripId)
                .eq(TripEntityDO::getEntityType, entityType)
                .eq(TripEntityDO::getName, name));
    }

    default List<TripEntityDO> selectListByTripId(Long tripId) {
        return selectList(TripEntityDO::getTripId, tripId);
    }

}
