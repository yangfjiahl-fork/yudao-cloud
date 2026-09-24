package cn.iocoder.yudao.module.gift.dal.mysql.useritinerary;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.gift.controller.admin.useritinerarydayitem.vo.UserItineraryDayItemPageReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDayItemDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface UserItineraryDayItemMapper extends BaseMapperX<UserItineraryDayItemDO> {

    default PageResult<UserItineraryDayItemDO> selectPage(UserItineraryDayItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UserItineraryDayItemDO>()
                .eqIfPresent(UserItineraryDayItemDO::getUserItineraryId, reqVO.getUserItineraryId())
                .eqIfPresent(UserItineraryDayItemDO::getUserItineraryDayId, reqVO.getUserItineraryDayId())
                .eqIfPresent(UserItineraryDayItemDO::getItemId, reqVO.getItemId())
                .eqIfPresent(UserItineraryDayItemDO::getDay, reqVO.getDay())
                .eqIfPresent(UserItineraryDayItemDO::getType, reqVO.getType())
                .eqIfPresent(UserItineraryDayItemDO::getSlot, reqVO.getSlot())
                .eqIfPresent(UserItineraryDayItemDO::getLabel, reqVO.getLabel())
                .eqIfPresent(UserItineraryDayItemDO::getSort, reqVO.getSort())
                .betweenIfPresent(UserItineraryDayItemDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(UserItineraryDayItemDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(UserItineraryDayItemDO::getDurationMinutes, reqVO.getDurationMinutes())
                .eqIfPresent(UserItineraryDayItemDO::getPoiId, reqVO.getPoiId())
                .likeIfPresent(UserItineraryDayItemDO::getPoiName, reqVO.getPoiName())
                .eqIfPresent(UserItineraryDayItemDO::getProvinceId, reqVO.getProvinceId())
                .eqIfPresent(UserItineraryDayItemDO::getCityId, reqVO.getCityId())
                .eqIfPresent(UserItineraryDayItemDO::getDistrictId, reqVO.getDistrictId())
                .eqIfPresent(UserItineraryDayItemDO::getCity, reqVO.getCity())
                .eqIfPresent(UserItineraryDayItemDO::getArea, reqVO.getArea())
                .eqIfPresent(UserItineraryDayItemDO::getAddressDetail, reqVO.getAddressDetail())
                .eqIfPresent(UserItineraryDayItemDO::getLongitude, reqVO.getLongitude())
                .eqIfPresent(UserItineraryDayItemDO::getLatitude, reqVO.getLatitude())
                .eqIfPresent(UserItineraryDayItemDO::getCoordinateSystem, reqVO.getCoordinateSystem())
                .eqIfPresent(UserItineraryDayItemDO::getBusinessHours, reqVO.getBusinessHours())
                .eqIfPresent(UserItineraryDayItemDO::getPhoneNo, reqVO.getPhoneNo())
                .eqIfPresent(UserItineraryDayItemDO::getCoverUrl, reqVO.getCoverUrl())
                .eqIfPresent(UserItineraryDayItemDO::getRating, reqVO.getRating())
                .eqIfPresent(UserItineraryDayItemDO::getCost, reqVO.getCost())
                .eqIfPresent(UserItineraryDayItemDO::getTagsJson, reqVO.getTagsJson())
                .eqIfPresent(UserItineraryDayItemDO::getSkeleton, reqVO.getSkeleton())
                .eqIfPresent(UserItineraryDayItemDO::getDetail, reqVO.getDetail())
                .eqIfPresent(UserItineraryDayItemDO::getStatus, reqVO.getStatus())
                .eqIfPresent(UserItineraryDayItemDO::getResolveStatus, reqVO.getResolveStatus())
                .eqIfPresent(UserItineraryDayItemDO::getPlanningStatus, reqVO.getPlanningStatus())
                .eqIfPresent(UserItineraryDayItemDO::getPoiVerificationStatus, reqVO.getPoiVerificationStatus())
                .eqIfPresent(UserItineraryDayItemDO::getMustVisit, reqVO.getMustVisit())
                .eqIfPresent(UserItineraryDayItemDO::getLocked, reqVO.getLocked())
                .eqIfPresent(UserItineraryDayItemDO::getSource, reqVO.getSource())
                .eqIfPresent(UserItineraryDayItemDO::getProvider, reqVO.getProvider())
                .eqIfPresent(UserItineraryDayItemDO::getPoiSnapshotJson, reqVO.getPoiSnapshotJson())
                .eqIfPresent(UserItineraryDayItemDO::getCandidatesJson, reqVO.getCandidatesJson())
                .eqIfPresent(UserItineraryDayItemDO::getCitationIdsJson, reqVO.getCitationIdsJson())
                .betweenIfPresent(UserItineraryDayItemDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(UserItineraryDayItemDO::getId));
    }

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
