package cn.iocoder.yudao.module.gift.dal.mysql.wool;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.wool.WoolDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.gift.controller.admin.wool.vo.*;

/**
 * 羊毛 Mapper
 *
 * @author calvin
 */
@Mapper
public interface WoolMapper extends BaseMapperX<WoolDO> {

    default PageResult<WoolDO> selectPage(WoolPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WoolDO>()
                .eqIfPresent(WoolDO::getBizType, reqVO.getBizType())
                .eqIfPresent(WoolDO::getBizId, reqVO.getBizId())
                .likeIfPresent(WoolDO::getRemark, reqVO.getRemark())
                .eqIfPresent(WoolDO::getStatus, reqVO.getStatus())
                .eqIfPresent(WoolDO::getMemberId, reqVO.getMemberId())
                .betweenIfPresent(WoolDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WoolDO::getId));
    }

    default WoolDO selectByBizTypeAndBizId(String bizType, String bizId) {
        return selectFirstOne(WoolDO::getBizType, bizType, WoolDO::getBizId, bizId);
    }

    default List<WoolDO> selectListByMemberIdAndStatus(Long memberId, String status) {
        return selectList(new LambdaQueryWrapperX<WoolDO>()
                .eq(WoolDO::getMemberId, memberId)
                .eq(WoolDO::getStatus, status)
                .orderByDesc(WoolDO::getId));
    }

    default int updateStatusByIdAndMemberIdAndStatus(Long id, Long memberId, String oldStatus, String newStatus) {
        WoolDO updateObj = new WoolDO();
        updateObj.setStatus(newStatus);
        return update(updateObj, Wrappers.<WoolDO>lambdaUpdate()
                .eq(WoolDO::getId, id)
                .eq(WoolDO::getMemberId, memberId)
                .eq(WoolDO::getStatus, oldStatus));
    }

}
