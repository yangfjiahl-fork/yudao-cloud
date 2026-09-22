package cn.iocoder.yudao.module.gift.dal.mysql.useritinerary;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.gift.controller.admin.useritinerary.vo.*;

/**
 * 用户行程 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface UserItineraryMapper extends BaseMapperX<UserItineraryDO> {

    default PageResult<UserItineraryDO> selectPage(UserItineraryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UserItineraryDO>()
                .eqIfPresent(UserItineraryDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(UserItineraryDO::getTitle, reqVO.getTitle())
                .eqIfPresent(UserItineraryDO::getCoverUrl, reqVO.getCoverUrl())
                .eqIfPresent(UserItineraryDO::getCoverWidth, reqVO.getCoverWidth())
                .eqIfPresent(UserItineraryDO::getCoverHeight, reqVO.getCoverHeight())
                .betweenIfPresent(UserItineraryDO::getStartDate, reqVO.getStartDate())
                .betweenIfPresent(UserItineraryDO::getEndDate, reqVO.getEndDate())
                .eqIfPresent(UserItineraryDO::getDayCnt, reqVO.getDayCnt())
                .eqIfPresent(UserItineraryDO::getCityId, reqVO.getCityId())
                .eqIfPresent(UserItineraryDO::getNextCityId, reqVO.getNextCityId())
                .eqIfPresent(UserItineraryDO::getPreference, reqVO.getPreference())
                .betweenIfPresent(UserItineraryDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(UserItineraryDO::getId));
    }

}