package cn.iocoder.yudao.module.promotion.mq.consumer.point;

import cn.iocoder.yudao.module.product.api.message.spu.ProductSpuMessage;
import cn.iocoder.yudao.module.promotion.service.point.PointActivityService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 商品创建时，同步创建积分商城活动，基于 {@link ProductSpuMessage} 消息
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class PointActivitySycByProductSpuConsumer {

    @Resource
    private PointActivityService pointActivityService;

    @EventListener
    @Async // Spring Event 默认在 Producer 发送的线程，通过 @Async 实现异步
    public void onMessage(ProductSpuMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        pointActivityService.syncPointActivityByProduct(message.getSpuId());
    }

}
