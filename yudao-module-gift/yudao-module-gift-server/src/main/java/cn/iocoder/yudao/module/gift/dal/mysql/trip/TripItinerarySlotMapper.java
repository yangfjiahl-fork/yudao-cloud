package cn.iocoder.yudao.module.gift.dal.mysql.trip;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripItinerarySlotDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface TripItinerarySlotMapper extends BaseMapperX<TripItinerarySlotDO> {

    default TripItinerarySlotDO selectByItineraryIdAndDayAndSlot(Long itineraryId, Integer day, String slot) {
        return selectOne(new LambdaQueryWrapperX<TripItinerarySlotDO>()
                .eq(TripItinerarySlotDO::getItineraryId, itineraryId)
                .eq(TripItinerarySlotDO::getDay, day)
                .eq(TripItinerarySlotDO::getSlot, slot));
    }

    default List<TripItinerarySlotDO> selectListByItineraryIds(Collection<Long> itineraryIds) {
        return selectList(new LambdaQueryWrapperX<TripItinerarySlotDO>()
                .inIfPresent(TripItinerarySlotDO::getItineraryId, itineraryIds));
    }

    default boolean claimForResolve(Long id, int pendingStatus, int failedStatus, int processingStatus) {
        return update(null, new LambdaUpdateWrapper<TripItinerarySlotDO>()
                .eq(TripItinerarySlotDO::getId, id)
                .in(TripItinerarySlotDO::getResolveStatus, pendingStatus, failedStatus)
                .set(TripItinerarySlotDO::getResolveStatus, processingStatus)) > 0;
    }

    @Insert("""
            INSERT IGNORE INTO gift_trip_itinerary_slot
            (tenant_id, itinerary_id, day, slot, skeleton, status, resolve_status)
            VALUES (#{tenantId}, #{itineraryId}, #{day}, #{slot}, #{skeleton}, #{status}, #{resolveStatus})
            """)
    int insertIgnore(TripItinerarySlotDO slot);

}
