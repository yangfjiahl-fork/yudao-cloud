package cn.iocoder.yudao.module.gift.dal.mysql.userfeedback;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gift.controller.admin.userfeedback.vo.UserFeedbackPageReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.userfeedback.UserFeedbackDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户反馈 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface UserFeedbackMapper extends BaseMapperX<UserFeedbackDO> {

    default PageResult<UserFeedbackDO> selectPage(UserFeedbackPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UserFeedbackDO>()
                .eqIfPresent(UserFeedbackDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(UserFeedbackDO::getCategory, reqVO.getCategory())
                .eqIfPresent(UserFeedbackDO::getStatus, reqVO.getStatus())
                .eqIfPresent(UserFeedbackDO::getContent, reqVO.getContent())
                .eqIfPresent(UserFeedbackDO::getPoiId, reqVO.getPoiId())
                .likeIfPresent(UserFeedbackDO::getPoiName, reqVO.getPoiName())
                .eqIfPresent(UserFeedbackDO::getPoiProvider, reqVO.getPoiProvider())
                .betweenIfPresent(UserFeedbackDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(UserFeedbackDO::getId));
    }

}
