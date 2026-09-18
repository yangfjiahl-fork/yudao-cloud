package cn.iocoder.yudao.module.gift.framework.trip.managed;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ManagedAgentProperties.class)
public class ManagedAgentConfiguration {

    @Bean(destroyMethod = "close")
    public ManagedAgentSessionClient managedAgentSessionClient(ManagedAgentProperties properties) {
        // 延迟创建 SDK Client，未启用旅行功能的服务启动时不强制要求 Managed Agents 配置。
        return new ManagedAgentSessionClient(properties);
    }

}
