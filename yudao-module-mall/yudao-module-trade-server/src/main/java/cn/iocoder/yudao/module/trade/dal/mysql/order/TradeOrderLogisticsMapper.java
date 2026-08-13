package cn.iocoder.yudao.module.trade.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderLogisticsDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradeOrderLogisticsMapper extends BaseMapperX<TradeOrderLogisticsDO> {

    default TradeOrderLogisticsDO selectByOrderId(Long orderId) {
        return selectOne(TradeOrderLogisticsDO::getOrderId, orderId);
    }

    default TradeOrderLogisticsDO selectByCacheKey(String cacheKey) {
        return selectOne(TradeOrderLogisticsDO::getCacheKey, cacheKey);
    }

    default int updateByCacheKey(String cacheKey, TradeOrderLogisticsDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<TradeOrderLogisticsDO>()
                .eq(TradeOrderLogisticsDO::getCacheKey, cacheKey));
    }

}
