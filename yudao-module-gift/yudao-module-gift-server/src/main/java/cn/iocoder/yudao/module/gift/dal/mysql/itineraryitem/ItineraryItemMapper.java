package cn.iocoder.yudao.module.gift.dal.mysql.itineraryitem;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.itineraryitem.ItineraryItemDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.gift.controller.admin.itineraryitem.vo.*;

/**
 * 文章 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface ItineraryItemMapper extends BaseMapperX<ItineraryItemDO> {

    default PageResult<ItineraryItemDO> selectPage(ItineraryItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ItineraryItemDO>()
                .eqIfPresent(ItineraryItemDO::getItineraryId, reqVO.getItineraryId())
                .eqIfPresent(ItineraryItemDO::getItineraryDayId, reqVO.getItineraryDayId())
                .eqIfPresent(ItineraryItemDO::getType, reqVO.getType())
                .eqIfPresent(ItineraryItemDO::getSlot, reqVO.getSlot())
                .eqIfPresent(ItineraryItemDO::getProvinceId, reqVO.getProvinceId())
                .eqIfPresent(ItineraryItemDO::getCityId, reqVO.getCityId())
                .eqIfPresent(ItineraryItemDO::getDistrictId, reqVO.getDistrictId())
                .eqIfPresent(ItineraryItemDO::getTitle, reqVO.getTitle())
                .eqIfPresent(ItineraryItemDO::getSubTitle, reqVO.getSubTitle())
                .eqIfPresent(ItineraryItemDO::getDescription, reqVO.getDescription())
                .eqIfPresent(ItineraryItemDO::getCoverUrl, reqVO.getCoverUrl())
                .eqIfPresent(ItineraryItemDO::getCoverWidth, reqVO.getCoverWidth())
                .eqIfPresent(ItineraryItemDO::getCoverHeight, reqVO.getCoverHeight())
                .eqIfPresent(ItineraryItemDO::getPicUrls, reqVO.getPicUrls())
                .eqIfPresent(ItineraryItemDO::getPicSizes, reqVO.getPicSizes())
                .eqIfPresent(ItineraryItemDO::getTags, reqVO.getTags())
                .eqIfPresent(ItineraryItemDO::getSort, reqVO.getSort())
                .eqIfPresent(ItineraryItemDO::getGdPosition, reqVO.getGdPosition())
                .betweenIfPresent(ItineraryItemDO::getBusinessTime, reqVO.getBusinessTime())
                .eqIfPresent(ItineraryItemDO::getAddressDetail, reqVO.getAddressDetail())
                .eqIfPresent(ItineraryItemDO::getPhoneNo, reqVO.getPhoneNo())
                .betweenIfPresent(ItineraryItemDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ItineraryItemDO::getId));
    }

}
