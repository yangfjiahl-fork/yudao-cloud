package cn.iocoder.yudao.module.gift.dal.mysql.useritinerary;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryItemDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface UserItineraryItemMapper extends BaseMapperX<UserItineraryItemDO> {

    default List<UserItineraryItemDO> selectListByUserItineraryId(Long id) {
        return selectList(new LambdaQueryWrapperX<UserItineraryItemDO>()
                .eq(UserItineraryItemDO::getUserItineraryId, id)
                .orderByAsc(UserItineraryItemDO::getDay).orderByAsc(UserItineraryItemDO::getSort));
    }

    default UserItineraryItemDO selectByUserItineraryIdAndDayAndSlot(Long itineraryId, Integer day, String slot) {
        return selectOne(new LambdaQueryWrapperX<UserItineraryItemDO>()
                .eq(UserItineraryItemDO::getUserItineraryId, itineraryId)
                .eq(UserItineraryItemDO::getDay, day)
                .eq(UserItineraryItemDO::getSlot, slot));
    }

    @Delete("DELETE FROM gift_user_itinerary_item WHERE user_itinerary_id = #{userItineraryId}")
    int deletePhysicallyByUserItineraryId(Long userItineraryId);
}
