package cn.iocoder.yudao.module.gift.framework.asr.config;

import cn.iocoder.yudao.module.gift.framework.asr.core.AliyunAsrClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AliyunAsrProperties.class)
public class AliyunAsrConfiguration {

    @Bean
    public AliyunAsrClient aliyunAsrClient(AliyunAsrProperties properties) {
        return new AliyunAsrClient(properties);
    }

}
