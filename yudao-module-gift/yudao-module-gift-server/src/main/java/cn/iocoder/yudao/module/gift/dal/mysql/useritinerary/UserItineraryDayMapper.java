package cn.iocoder.yudao.module.gift.dal.mysql.useritinerary;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.gift.controller.admin.useritineraryday.vo.UserItineraryDayPageReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDayDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface UserItineraryDayMapper extends BaseMapperX<UserItineraryDayDO> {

    default PageResult<UserItineraryDayDO> selectPage(UserItineraryDayPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UserItineraryDayDO>()
                .eqIfPresent(UserItineraryDayDO::getUserItineraryId, reqVO.getUserItineraryId())
                .eqIfPresent(UserItineraryDayDO::getDay, reqVO.getDay())
                .betweenIfPresent(UserItineraryDayDO::getDate, reqVO.getDate())
                .eqIfPresent(UserItineraryDayDO::getProvinceId, reqVO.getProvinceId())
                .eqIfPresent(UserItineraryDayDO::getCityId, reqVO.getCityId())
                .eqIfPresent(UserItineraryDayDO::getDistrictId, reqVO.getDistrictId())
                .eqIfPresent(UserItineraryDayDO::getCity, reqVO.getCity())
                .eqIfPresent(UserItineraryDayDO::getArea, reqVO.getArea())
                .eqIfPresent(UserItineraryDayDO::getTheme, reqVO.getTheme())
                .eqIfPresent(UserItineraryDayDO::getAnchorPoiNamesJson, reqVO.getAnchorPoiNamesJson())
                .eqIfPresent(UserItineraryDayDO::getSort, reqVO.getSort())
                .eqIfPresent(UserItineraryDayDO::getOverviewStatus, reqVO.getOverviewStatus())
                .eqIfPresent(UserItineraryDayDO::getOverviewSkeleton, reqVO.getOverviewSkeleton())
                .eqIfPresent(UserItineraryDayDO::getOverviewDetail, reqVO.getOverviewDetail())
                .eqIfPresent(UserItineraryDayDO::getPlanner, reqVO.getPlanner())
                .eqIfPresent(UserItineraryDayDO::getPlanningStatus, reqVO.getPlanningStatus())
                .eqIfPresent(UserItineraryDayDO::getMacroSource, reqVO.getMacroSource())
                .eqIfPresent(UserItineraryDayDO::getSelectionStatus, reqVO.getSelectionStatus())
                .eqIfPresent(UserItineraryDayDO::getBudgetStatus, reqVO.getBudgetStatus())
                .eqIfPresent(UserItineraryDayDO::getRequestedScenicCount, reqVO.getRequestedScenicCount())
                .eqIfPresent(UserItineraryDayDO::getSelectedScenicCount, reqVO.getSelectedScenicCount())
                .betweenIfPresent(UserItineraryDayDO::getDayStartTime, reqVO.getDayStartTime())
                .betweenIfPresent(UserItineraryDayDO::getDayEndTime, reqVO.getDayEndTime())
                .eqIfPresent(UserItineraryDayDO::getDroppedNodeIdsJson, reqVO.getDroppedNodeIdsJson())
                .eqIfPresent(UserItineraryDayDO::getCandidateCountsJson, reqVO.getCandidateCountsJson())
                .betweenIfPresent(UserItineraryDayDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(UserItineraryDayDO::getId));
    }

    default List<UserItineraryDayDO> selectListByUserItineraryId(Long id) {
        return selectList(new LambdaQueryWrapperX<UserItineraryDayDO>()
                .eq(UserItineraryDayDO::getUserItineraryId, id).orderByAsc(UserItineraryDayDO::getSort));
    }

    default UserItineraryDayDO selectByUserItineraryIdAndDay(Long itineraryId, Integer day) {
        return selectOne(new LambdaQueryWrapperX<UserItineraryDayDO>()
                .eq(UserItineraryDayDO::getUserItineraryId, itineraryId)
                .eq(UserItineraryDayDO::getDay, day));
    }

    @Delete("DELETE FROM gift_user_itinerary_day WHERE user_itinerary_id = #{userItineraryId}")
    int deletePhysicallyByUserItineraryId(Long userItineraryId);
}
