package cn.iocoder.yudao.module.gift.mq.consumer.wool;

import cn.iocoder.yudao.module.gift.service.wool.WoolService;
import cn.iocoder.yudao.module.member.api.message.user.MemberUserCreateMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户注册时，发放羊毛的消费者，基于 {@link MemberUserCreateMessage} 消息
 *
 * @author calvin
 */
@Component
@Slf4j
public class WoolGrantByRegisterConsumer {

    @Resource
    private WoolService woolService;

    @EventListener
    @Async // Spring Event 默认在 Producer 发送的线程，通过 @Async 实现异步
    public void onMessage(MemberUserCreateMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        woolService.grantWoolByRegister(message.getUserId());
    }

}
