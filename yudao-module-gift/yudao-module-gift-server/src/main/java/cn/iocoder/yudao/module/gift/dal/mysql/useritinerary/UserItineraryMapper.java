package cn.iocoder.yudao.module.gift.dal.mysql.useritinerary;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
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

    default UserItineraryDO selectByConversationId(Long conversationId) {
        return selectOne(UserItineraryDO::getConversationId, conversationId);
    }

    default List<UserItineraryDO> selectListByResultEventIds(Collection<Long> eventIds) {
        return selectList(new LambdaQueryWrapperX<UserItineraryDO>()
                .inIfPresent(UserItineraryDO::getResultEventId, eventIds));
    }

    default UserItineraryDO selectByResultEventId(Long eventId) {
        return selectOne(UserItineraryDO::getResultEventId, eventId);
    }

    default UserItineraryDO selectByIdAndConversationId(Long id, Long conversationId) {
        return selectOne(new LambdaQueryWrapperX<UserItineraryDO>()
                .eq(UserItineraryDO::getId, id).eqIfPresent(UserItineraryDO::getConversationId, conversationId));
    }

    default PageResult<UserItineraryDO> selectPage(UserItineraryPageReqVO reqVO) {
        LambdaQueryWrapperX<UserItineraryDO> query = buildPageQuery(reqVO);
        selectPageFields(query);
        return selectPage(reqVO, query.orderByDesc(UserItineraryDO::getId));
    }

    default PageResult<UserItineraryDO> selectExportPage(UserItineraryPageReqVO reqVO) {
        return selectPage(reqVO, buildPageQuery(reqVO).orderByDesc(UserItineraryDO::getId));
    }

    private static LambdaQueryWrapperX<UserItineraryDO> buildPageQuery(UserItineraryPageReqVO reqVO) {
        return new LambdaQueryWrapperX<UserItineraryDO>()
                .eqIfPresent(UserItineraryDO::getConversationId, reqVO.getConversationId())
                .eqIfPresent(UserItineraryDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(UserItineraryDO::getStatus, reqVO.getStatus())
                .likeIfPresent(UserItineraryDO::getTitle, reqVO.getTitle())
                .eqIfPresent(UserItineraryDO::getCoverUrl, reqVO.getCoverUrl())
                .eqIfPresent(UserItineraryDO::getCoverWidth, reqVO.getCoverWidth())
                .eqIfPresent(UserItineraryDO::getCoverHeight, reqVO.getCoverHeight())
                .betweenIfPresent(UserItineraryDO::getStartDate, reqVO.getStartDate())
                .betweenIfPresent(UserItineraryDO::getEndDate, reqVO.getEndDate())
                .eqIfPresent(UserItineraryDO::getDayCnt, reqVO.getDayCnt())
                .eqIfPresent(UserItineraryDO::getDestination, reqVO.getDestination())
                .betweenIfPresent(UserItineraryDO::getCreateTime, reqVO.getCreateTime());
    }

    default PageResult<UserItineraryDO> selectPageByMemberId(PageParam pageParam, Long memberId) {
        LambdaQueryWrapperX<UserItineraryDO> query = new LambdaQueryWrapperX<UserItineraryDO>()
                .eq(UserItineraryDO::getMemberId, memberId);
        selectPageFields(query);
        return selectPage(pageParam, query
                .orderByDesc(UserItineraryDO::getId));
    }

    private static void selectPageFields(LambdaQueryWrapperX<UserItineraryDO> query) {
        query.select(UserItineraryDO::getId, UserItineraryDO::getConversationId, UserItineraryDO::getMemberId,
                UserItineraryDO::getStatus,
                UserItineraryDO::getTitle, UserItineraryDO::getCoverUrl, UserItineraryDO::getCoverWidth,
                UserItineraryDO::getCoverHeight, UserItineraryDO::getStartDate, UserItineraryDO::getEndDate,
                UserItineraryDO::getDayCnt, UserItineraryDO::getDeparture, UserItineraryDO::getDestination,
                UserItineraryDO::getCreateTime);
    }

    default UserItineraryDO selectByIdAndMemberId(Long id, Long memberId) {
        return selectOne(new LambdaQueryWrapperX<UserItineraryDO>()
                .eq(UserItineraryDO::getId, id)
                .eq(UserItineraryDO::getMemberId, memberId));
    }

    default int deleteByIdAndMemberId(Long id, Long memberId) {
        return delete(new LambdaQueryWrapperX<UserItineraryDO>()
                .eq(UserItineraryDO::getId, id)
                .eq(UserItineraryDO::getMemberId, memberId));
    }

    default int clearConversationId(Long conversationId, Long memberId) {
        return update(null, new LambdaUpdateWrapper<UserItineraryDO>()
                .eq(UserItineraryDO::getConversationId, conversationId)
                .eq(UserItineraryDO::getMemberId, memberId)
                .set(UserItineraryDO::getConversationId, null));
    }

}
