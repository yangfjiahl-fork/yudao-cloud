package cn.iocoder.yudao.module.gift.dal.mysql.useritinerary;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDayDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;

import java.util.List;

@Mapper
public interface UserItineraryDayMapper extends BaseMapperX<UserItineraryDayDO> {

    default List<UserItineraryDayDO> selectListByUserItineraryId(Long id) {
        return selectList(new LambdaQueryWrapperX<UserItineraryDayDO>()
                .eq(UserItineraryDayDO::getUserItineraryId, id).orderByAsc(UserItineraryDayDO::getSort));
    }

    default UserItineraryDayDO selectByUserItineraryIdAndDay(Long itineraryId, Integer day) {
        return selectOne(new LambdaQueryWrapperX<UserItineraryDayDO>()
                .eq(UserItineraryDayDO::getUserItineraryId, itineraryId)
                .eq(UserItineraryDayDO::getDay, day));
    }
}
