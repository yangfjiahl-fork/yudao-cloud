package cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.config;

import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.core.AmapGeocodingClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.core.AmapPlaceSearchClient;
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

    @Bean
    public AmapPlaceSearchClient amapPlaceSearchClient(RestTemplate restTemplate, AmapProperties properties) {
        return new AmapPlaceSearchClient(restTemplate, properties);
    }

}
