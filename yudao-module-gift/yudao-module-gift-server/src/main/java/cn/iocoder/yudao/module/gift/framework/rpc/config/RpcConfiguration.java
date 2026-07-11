package cn.iocoder.yudao.module.gift.framework.rpc.config;

import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "giftRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {ConfigApi.class})
public class RpcConfiguration {
}
