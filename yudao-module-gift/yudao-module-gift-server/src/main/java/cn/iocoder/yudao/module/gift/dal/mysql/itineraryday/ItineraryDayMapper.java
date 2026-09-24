package cn.iocoder.yudao.module.gift.dal.mysql.itineraryday;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.itineraryday.ItineraryDayDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.gift.controller.admin.itineraryday.vo.*;

/**
 * 通用行程每日安排 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface ItineraryDayMapper extends BaseMapperX<ItineraryDayDO> {

    default PageResult<ItineraryDayDO> selectPage(ItineraryDayPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ItineraryDayDO>()
                .eqIfPresent(ItineraryDayDO::getItineraryId, reqVO.getItineraryId())
                .eqIfPresent(ItineraryDayDO::getDay, reqVO.getDay())
                .eqIfPresent(ItineraryDayDO::getCityId, reqVO.getCityId())
                .eqIfPresent(ItineraryDayDO::getDistrictId, reqVO.getDistrictId())
                .eqIfPresent(ItineraryDayDO::getTitle, reqVO.getTitle())
                .eqIfPresent(ItineraryDayDO::getDescription, reqVO.getDescription())
                .eqIfPresent(ItineraryDayDO::getSort, reqVO.getSort())
                .betweenIfPresent(ItineraryDayDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ItineraryDayDO::getId));
    }

}