package cn.iocoder.yudao.module.gift.framework.rpc.config;

import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.member.api.point.MemberPointApi;
import cn.iocoder.yudao.module.member.api.signin.MemberSignInRecordApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "giftRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {ConfigApi.class, MemberPointApi.class, MemberSignInRecordApi.class})
public class RpcConfiguration {
}
