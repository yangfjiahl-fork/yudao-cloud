package cn.iocoder.yudao.module.gift.dal.mysql.useritineraryliked;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryliked.UserItineraryLikedDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.gift.controller.admin.useritineraryliked.vo.UserItineraryLikedPageReqVO;

/**
 * 收藏行程 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface UserItineraryLikedMapper extends BaseMapperX<UserItineraryLikedDO> {

    default PageResult<UserItineraryLikedDO> selectPage(UserItineraryLikedPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UserItineraryLikedDO>()
                .eqIfPresent(UserItineraryLikedDO::getItineraryId, reqVO.getItineraryId())
                .eqIfPresent(UserItineraryLikedDO::getMemberId, reqVO.getMemberId())
                .betweenIfPresent(UserItineraryLikedDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(UserItineraryLikedDO::getId));
    }

}
