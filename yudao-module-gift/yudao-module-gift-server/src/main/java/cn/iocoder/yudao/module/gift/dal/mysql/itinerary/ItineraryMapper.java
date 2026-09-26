package cn.iocoder.yudao.module.gift.dal.mysql.itinerary;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
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
        LambdaQueryWrapperX<ItineraryDO> query = buildPageQuery(reqVO);
        selectPageFields(query);
        return selectPage(reqVO, query.orderByDesc(ItineraryDO::getId));
    }

    default PageResult<ItineraryDO> selectExportPage(ItineraryPageReqVO reqVO) {
        return selectPage(reqVO, buildPageQuery(reqVO).orderByDesc(ItineraryDO::getId));
    }

    private static LambdaQueryWrapperX<ItineraryDO> buildPageQuery(ItineraryPageReqVO reqVO) {
        return new LambdaQueryWrapperX<ItineraryDO>()
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
                .betweenIfPresent(ItineraryDO::getCreateTime, reqVO.getCreateTime());
    }

    default PageResult<ItineraryDO> selectPage(PageParam pageParam, Integer cityId, Long categoryId) {
        LambdaQueryWrapperX<ItineraryDO> query = new LambdaQueryWrapperX<ItineraryDO>()
                .eqIfPresent(ItineraryDO::getCityId, cityId)
                .eq(ItineraryDO::getCategoryId, categoryId);
        selectPageFields(query);
        return selectPage(pageParam, query
                .orderByDesc(ItineraryDO::getSort)
                .orderByDesc(ItineraryDO::getId));
    }

    private static void selectPageFields(LambdaQueryWrapperX<ItineraryDO> query) {
        query.select(ItineraryDO::getId, ItineraryDO::getCityId, ItineraryDO::getCategoryId,
                ItineraryDO::getTitle, ItineraryDO::getSubTitle, ItineraryDO::getTags,
                ItineraryDO::getCoverUrl, ItineraryDO::getCoverWidth, ItineraryDO::getCoverHeight,
                ItineraryDO::getViewCnt, ItineraryDO::getLikeCnt);
    }

}
