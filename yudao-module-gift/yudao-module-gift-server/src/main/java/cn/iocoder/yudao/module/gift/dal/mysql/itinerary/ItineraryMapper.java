package cn.iocoder.yudao.module.gift.dal.mysql.itinerary;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryPageReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerary.ItineraryDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.gift.controller.admin.itinerary.vo.*;

/**
 * 行程 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface ItineraryMapper extends BaseMapperX<ItineraryDO> {

    default PageResult<ItineraryDO> selectPage(ItineraryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ItineraryDO>()
                .eqIfPresent(ItineraryDO::getCityId, reqVO.getCityId())
                .eqIfPresent(ItineraryDO::getCategoryId, reqVO.getCategoryId())
                .likeIfPresent(ItineraryDO::getTitle, reqVO.getTitle())
                .eqIfPresent(ItineraryDO::getSubTitle, reqVO.getSubTitle())
                .eqIfPresent(ItineraryDO::getDescription, reqVO.getDescription())
                .eqIfPresent(ItineraryDO::getIcon, reqVO.getIcon())
                .eqIfPresent(ItineraryDO::getPicUrls, reqVO.getPicUrls())
                .eqIfPresent(ItineraryDO::getPicSizes, reqVO.getPicSizes())
                .eqIfPresent(ItineraryDO::getTags, reqVO.getTags())
                .eqIfPresent(ItineraryDO::getCoverUrl, reqVO.getCoverUrl())
                .eqIfPresent(ItineraryDO::getCoverWidth, reqVO.getCoverWidth())
                .eqIfPresent(ItineraryDO::getCoverHeight, reqVO.getCoverHeight())
                .eqIfPresent(ItineraryDO::getNextCityId, reqVO.getNextCityId())
                .eqIfPresent(ItineraryDO::getViewCnt, reqVO.getViewCnt())
                .eqIfPresent(ItineraryDO::getLikeCnt, reqVO.getLikeCnt())
                .eqIfPresent(ItineraryDO::getSort, reqVO.getSort())
                .betweenIfPresent(ItineraryDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ItineraryDO::getId));
    }

    default PageResult<ItineraryDO> selectPage(AppItineraryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ItineraryDO>()
                .eq(ItineraryDO::getCategoryId, reqVO.getCategoryId())
                .orderByDesc(ItineraryDO::getSort)
                .orderByDesc(ItineraryDO::getId));
    }

}
