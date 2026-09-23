package cn.iocoder.yudao.module.gift.dal.mysql.itineraryday;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.itineraryday.ItineraryDayDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ItineraryDayMapper extends BaseMapperX<ItineraryDayDO> {

    default List<ItineraryDayDO> selectListByItineraryId(Long itineraryId) {
        return selectList(new LambdaQueryWrapperX<ItineraryDayDO>()
                .eq(ItineraryDayDO::getItineraryId, itineraryId)
                .orderByAsc(ItineraryDayDO::getSort));
    }

}
