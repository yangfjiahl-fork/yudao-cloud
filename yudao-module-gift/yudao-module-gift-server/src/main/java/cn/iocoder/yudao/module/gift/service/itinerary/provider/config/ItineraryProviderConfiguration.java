package cn.iocoder.yudao.module.gift.service.itinerary.provider.config;

import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.config.AmapProperties;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.place.TravelPlaceQueryClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.place.TravelPlaceQueryClientFacade;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.place.gaode.GaodeTravelPlaceQueryClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.route.amap.AmapRouteQueryClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.scenic.ScenicSpotQueryClientFacade;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.scenic.aliyun.AliyunScenicSpotQueryClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.scenic.gaode.GaodeScenicSpotQueryClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.weather.WeatherClientFacade;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.weather.amap.AmapWeatherClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.weather.aliyun.AliyunWeatherClient;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ItineraryProviderProperties.class)
public class ItineraryProviderConfiguration {

    @Bean
    public AliyunWeatherClient aliyunWeatherClient(RestTemplate restTemplate, ItineraryProviderProperties properties) {
        return new AliyunWeatherClient(restTemplate, properties.getWeather());
    }

    @Bean
    public AmapWeatherClient amapWeatherClient(RestTemplate restTemplate, AmapProperties amapProperties) {
        return new AmapWeatherClient(restTemplate, amapProperties);
    }

    @Bean
    public WeatherClientFacade weatherClientFacade(ConfigApi configApi, AliyunWeatherClient aliyunWeatherClient,
                                                   AmapWeatherClient amapWeatherClient) {
        return new WeatherClientFacade(configApi, aliyunWeatherClient, amapWeatherClient);
    }

    @Bean
    public AliyunScenicSpotQueryClient aliyunScenicSpotQueryClient(RestTemplate restTemplate,
                                                                    ItineraryProviderProperties properties) {
        return new AliyunScenicSpotQueryClient(restTemplate, properties.getScenicSpot());
    }

    @Bean
    public GaodeScenicSpotQueryClient gaodeScenicSpotQueryClient(RestTemplate restTemplate,
                                                                  AmapProperties amapProperties) {
        return new GaodeScenicSpotQueryClient(restTemplate, amapProperties);
    }

    @Bean
    public ScenicSpotQueryClientFacade scenicSpotQueryClientFacade(ConfigApi configApi,
            AliyunScenicSpotQueryClient aliyunClient, GaodeScenicSpotQueryClient gaodeClient) {
        return new ScenicSpotQueryClientFacade(configApi, aliyunClient, gaodeClient);
    }

    @Bean
    public GaodeTravelPlaceQueryClient gaodeTravelPlaceQueryClient(RestTemplate restTemplate,
            AmapProperties amapProperties) {
        return new GaodeTravelPlaceQueryClient(restTemplate, amapProperties);
    }

    @Bean
    public TravelPlaceQueryClientFacade travelPlaceQueryClientFacade(ConfigApi configApi,
            List<TravelPlaceQueryClient> clients) {
        return new TravelPlaceQueryClientFacade(configApi, clients);
    }

    @Bean
    public AmapRouteQueryClient amapRouteQueryClient(RestTemplateBuilder restTemplateBuilder,
                                                      AmapProperties amapProperties) {
        RestTemplate routeRestTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(2))
                .readTimeout(Duration.ofSeconds(2))
                .build();
        return new AmapRouteQueryClient(routeRestTemplate, amapProperties);
    }

}
