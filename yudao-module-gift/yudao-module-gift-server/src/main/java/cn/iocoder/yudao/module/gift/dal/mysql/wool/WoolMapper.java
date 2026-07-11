package cn.iocoder.yudao.module.gift.dal.mysql.wool;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.wool.WoolDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    default WoolDO selectByBizTypeAndBizId(Integer bizType, String bizId) {
        return selectFirstOne(WoolDO::getBizType, bizType, WoolDO::getBizId, bizId);
    }

    default List<WoolDO> selectListByMemberIdAndStatus(Long memberId, Integer status) {
        return selectList(new LambdaQueryWrapperX<WoolDO>()
                .eq(WoolDO::getMemberId, memberId)
                .eq(WoolDO::getStatus, status)
                .orderByDesc(WoolDO::getId));
    }

    default Integer selectSumAmountByMemberIdAndStatusAndUpdateTimeBetween(Long memberId, Integer status,
                                                                           LocalDateTime startTime,
                                                                           LocalDateTime endTime) {
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<WoolDO>()
                .select("SUM(amount) AS amount_sum")
                .eq("member_id", memberId)
                .eq("status", status)
                .ge("update_time", startTime)
                .lt("update_time", endTime));
        if (result.isEmpty()) {
            return 0;
        }
        Number amountSum = (Number) result.get(0).get("amount_sum");
        return amountSum == null ? 0 : amountSum.intValue();
    }

    default int updateStatusByIdAndMemberIdAndStatus(Long id, Long memberId, Integer oldStatus, Integer newStatus) {
        WoolDO updateObj = new WoolDO();
        updateObj.setStatus(newStatus);
        return update(updateObj, Wrappers.<WoolDO>lambdaUpdate()
                .eq(WoolDO::getId, id)
                .eq(WoolDO::getMemberId, memberId)
                .eq(WoolDO::getStatus, oldStatus));
    }

}
