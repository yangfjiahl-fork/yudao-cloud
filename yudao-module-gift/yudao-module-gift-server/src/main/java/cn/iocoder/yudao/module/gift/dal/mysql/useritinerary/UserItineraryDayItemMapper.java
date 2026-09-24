package cn.iocoder.yudao.module.gift.dal.mysql.useritinerary;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDayItemDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface UserItineraryDayItemMapper extends BaseMapperX<UserItineraryDayItemDO> {

    default List<UserItineraryDayItemDO> selectListByUserItineraryId(Long id) {
        return selectList(new LambdaQueryWrapperX<UserItineraryDayItemDO>()
                .eq(UserItineraryDayItemDO::getUserItineraryId, id)
                .orderByAsc(UserItineraryDayItemDO::getDay).orderByAsc(UserItineraryDayItemDO::getSort));
    }

    default UserItineraryDayItemDO selectByUserItineraryIdAndDayAndSlot(Long itineraryId, Integer day, String slot) {
        return selectOne(new LambdaQueryWrapperX<UserItineraryDayItemDO>()
                .eq(UserItineraryDayItemDO::getUserItineraryId, itineraryId)
                .eq(UserItineraryDayItemDO::getDay, day)
                .eq(UserItineraryDayItemDO::getSlot, slot));
    }

    @Delete("DELETE FROM gift_user_itinerary_day_item WHERE user_itinerary_id = #{userItineraryId}")
    int deletePhysicallyByUserItineraryId(Long userItineraryId);
}
