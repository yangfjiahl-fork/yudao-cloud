package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.iocoder.yudao.module.trade.api.message.order.TradeOrderLifecycleMessage;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderLifecycleTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 发布交易订单生命周期消息
 *
 * @author calvin
 */
@Component
public class TradeOrderLifecycleMessageHandler implements TradeOrderHandler {

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;

    @Override
    public void afterOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        publishMessage(order, orderItems, TradeOrderLifecycleTypeEnum.CREATED);
    }

    @Override
    public void afterPayOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        publishMessage(order, orderItems, TradeOrderLifecycleTypeEnum.PAID);
    }

    @Override
    public void afterCancelOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        publishMessage(order, orderItems, TradeOrderLifecycleTypeEnum.CANCELED);
    }

    @Override
    public void afterDeliveryOrder(TradeOrderDO order) {
        publishMessage(order, tradeOrderItemMapper.selectListByOrderId(order.getId()),
                TradeOrderLifecycleTypeEnum.DELIVERED);
    }

    @Override
    public void afterReceiveOrder(TradeOrderDO order) {
        publishMessage(order, tradeOrderItemMapper.selectListByOrderId(order.getId()),
                TradeOrderLifecycleTypeEnum.RECEIVED);
    }

    private void publishMessage(TradeOrderDO order, List<TradeOrderItemDO> orderItems,
                                TradeOrderLifecycleTypeEnum lifecycleType) {
        boolean payStatus = TradeOrderLifecycleTypeEnum.PAID.equals(lifecycleType)
                || Boolean.TRUE.equals(order.getPayStatus());
        String productName = orderItems.stream().map(TradeOrderItemDO::getSpuName)
                .filter(Objects::nonNull).distinct().collect(Collectors.joining("、"));
        applicationEventPublisher.publishEvent(new TradeOrderLifecycleMessage()
                .setOrderId(order.getId()).setOrderNo(order.getNo()).setUserId(order.getUserId())
                .setProductName(productName).setUsePoint(order.getUsePoint() != null ? order.getUsePoint() : 0)
                .setOrderType(order.getType()).setDeliveryType(order.getDeliveryType())
                .setPayStatus(payStatus).setLifecycleType(lifecycleType.getType()));
    }

}
