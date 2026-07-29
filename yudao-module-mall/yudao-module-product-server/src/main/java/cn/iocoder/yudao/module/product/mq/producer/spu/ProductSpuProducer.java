package cn.iocoder.yudao.module.product.mq.producer.spu;

import cn.iocoder.yudao.module.product.api.message.spu.ProductSpuMessage;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 商品 SPU 消息 Producer
 *
 * @author 芋道源码
 */
@Component
public class ProductSpuProducer {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 发送 {@link ProductSpuMessage} 消息
     *
     * @param spuId 商品 SPU 编号
     */
    public void sendSpuMessage(Long spuId) {
        applicationContext.publishEvent(new ProductSpuMessage().setSpuId(spuId));
    }

}
