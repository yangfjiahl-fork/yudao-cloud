package cn.iocoder.yudao.module.gift.mq.consumer.order;

import cn.iocoder.yudao.module.gift.enums.MessageTemplateConstants;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.trade.api.message.order.TradeOrderLifecycleMessage;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderLifecycleTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * 积分兑换订单生命周期通知
 *
 * @author calvin
 */
@Component
@Slf4j
public class PointExchangeNotifyConsumer {

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onMessage(TradeOrderLifecycleMessage message) {
        if (!TradeOrderTypeEnum.isPoint(message.getOrderType())) {
            return;
        }
        String templateCode = getTemplateCode(message);
        if (templateCode == null) {
            return;
        }
        log.info("[onMessage][消息内容({})]", message);
        notifyMessageSendApi.sendSingleMessageToMember(new NotifySendSingleToUserReqDTO()
                .setUserId(message.getUserId())
                .setTemplateCode(templateCode)
                .setTemplateParams(Map.of("orderNo", message.getOrderNo(),
                        "productName", message.getProductName(), "usePoint", message.getUsePoint()))).checkError();
    }

    private String getTemplateCode(TradeOrderLifecycleMessage message) {
        if (isPointOrderCompleted(message)) {
            return MessageTemplateConstants.NOTIFY_POINT_EXCHANGE_SUCCESS;
        }
        if (TradeOrderLifecycleTypeEnum.DELIVERED.getType().equals(message.getLifecycleType())) {
            return MessageTemplateConstants.NOTIFY_POINT_EXCHANGE_DELIVERED;
        }
        if (TradeOrderLifecycleTypeEnum.RECEIVED.getType().equals(message.getLifecycleType())) {
            return MessageTemplateConstants.NOTIFY_POINT_EXCHANGE_RECEIVED;
        }
        return null;
    }

    private boolean isPointOrderCompleted(TradeOrderLifecycleMessage message) {
        if (TradeOrderLifecycleTypeEnum.PAID.getType().equals(message.getLifecycleType())) {
            return true;
        }
        return TradeOrderLifecycleTypeEnum.CREATED.getType().equals(message.getLifecycleType())
                && Boolean.TRUE.equals(message.getPayStatus());
    }

}
