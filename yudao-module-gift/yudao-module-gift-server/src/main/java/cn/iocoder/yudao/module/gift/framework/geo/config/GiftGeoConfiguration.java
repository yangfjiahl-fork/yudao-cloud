package cn.iocoder.yudao.module.gift.framework.geo.config;

import cn.iocoder.yudao.module.gift.framework.geo.core.AmapGeocodingClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AmapProperties.class)
public class GiftGeoConfiguration {

    @Bean
    public AmapGeocodingClient amapGeocodingClient(RestTemplate restTemplate, AmapProperties properties) {
        return new AmapGeocodingClient(restTemplate, properties);
    }

}
