package cn.iocoder.yudao.module.gift.dal.mysql.itinerarydayitem;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerarydayitem.ItineraryDayItemDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.gift.controller.admin.itinerarydayitem.vo.*;

/**
 * 通用行程节点 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface ItineraryDayItemMapper extends BaseMapperX<ItineraryDayItemDO> {

    default PageResult<ItineraryDayItemDO> selectPage(ItineraryDayItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ItineraryDayItemDO>()
                .eqIfPresent(ItineraryDayItemDO::getItineraryId, reqVO.getItineraryId())
                .eqIfPresent(ItineraryDayItemDO::getItineraryDayId, reqVO.getItineraryDayId())
                .eqIfPresent(ItineraryDayItemDO::getType, reqVO.getType())
                .eqIfPresent(ItineraryDayItemDO::getSlot, reqVO.getSlot())
                .likeIfPresent(ItineraryDayItemDO::getTitle, reqVO.getTitle())
                .eqIfPresent(ItineraryDayItemDO::getSubTitle, reqVO.getSubTitle())
                .eqIfPresent(ItineraryDayItemDO::getDescription, reqVO.getDescription())
                .eqIfPresent(ItineraryDayItemDO::getSort, reqVO.getSort())
                .betweenIfPresent(ItineraryDayItemDO::getStartTime, reqVO.getStartTime())
                .eqIfPresent(ItineraryDayItemDO::getDurationMinutes, reqVO.getDurationMinutes())
                .eqIfPresent(ItineraryDayItemDO::getPoiId, reqVO.getPoiId())
                .eqIfPresent(ItineraryDayItemDO::getProvinceId, reqVO.getProvinceId())
                .eqIfPresent(ItineraryDayItemDO::getCityId, reqVO.getCityId())
                .eqIfPresent(ItineraryDayItemDO::getDistrictId, reqVO.getDistrictId())
                .eqIfPresent(ItineraryDayItemDO::getLongitude, reqVO.getLongitude())
                .eqIfPresent(ItineraryDayItemDO::getLatitude, reqVO.getLatitude())
                .eqIfPresent(ItineraryDayItemDO::getCoverUrl, reqVO.getCoverUrl())
                .eqIfPresent(ItineraryDayItemDO::getCoverWidth, reqVO.getCoverWidth())
                .eqIfPresent(ItineraryDayItemDO::getCoverHeight, reqVO.getCoverHeight())
                .eqIfPresent(ItineraryDayItemDO::getPicUrls, reqVO.getPicUrls())
                .eqIfPresent(ItineraryDayItemDO::getPicSizes, reqVO.getPicSizes())
                .eqIfPresent(ItineraryDayItemDO::getTags, reqVO.getTags())
                .betweenIfPresent(ItineraryDayItemDO::getBusinessTime, reqVO.getBusinessTime())
                .eqIfPresent(ItineraryDayItemDO::getAddressDetail, reqVO.getAddressDetail())
                .eqIfPresent(ItineraryDayItemDO::getPhoneNo, reqVO.getPhoneNo())
                .betweenIfPresent(ItineraryDayItemDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ItineraryDayItemDO::getId));
    }

}
