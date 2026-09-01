package cn.iocoder.yudao.module.gift.framework.trip.provider.config;

import cn.iocoder.yudao.module.gift.framework.trip.provider.place.TravelPlaceQueryClient;
import cn.iocoder.yudao.module.gift.framework.trip.provider.place.TravelPlaceQueryClientFacade;
import cn.iocoder.yudao.module.gift.framework.trip.provider.place.gaode.GaodeTravelPlaceQueryClient;
import cn.iocoder.yudao.module.gift.framework.trip.provider.route.amap.AmapRouteQueryClient;
import cn.iocoder.yudao.module.gift.framework.trip.provider.scenic.ScenicSpotQueryClientFacade;
import cn.iocoder.yudao.module.gift.framework.trip.provider.scenic.aliyun.AliyunScenicSpotQueryClient;
import cn.iocoder.yudao.module.gift.framework.trip.provider.scenic.gaode.GaodeScenicSpotQueryClient;
import cn.iocoder.yudao.module.gift.framework.trip.provider.weather.WeatherClientFacade;
import cn.iocoder.yudao.module.gift.framework.trip.provider.weather.amap.AmapWeatherClient;
import cn.iocoder.yudao.module.gift.framework.trip.provider.weather.aliyun.AliyunWeatherClient;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TripProviderProperties.class)
public class TripProviderConfiguration {

    @Bean
    public AliyunWeatherClient aliyunWeatherClient(RestTemplate restTemplate, TripProviderProperties properties) {
        return new AliyunWeatherClient(restTemplate, properties.getWeather());
    }

    @Bean
    public AmapWeatherClient amapWeatherClient(RestTemplate restTemplate, TripProviderProperties properties) {
        return new AmapWeatherClient(restTemplate, properties.getWeather().getAmap());
    }

    @Bean
    public WeatherClientFacade weatherClientFacade(ConfigApi configApi, AliyunWeatherClient aliyunWeatherClient,
                                                   AmapWeatherClient amapWeatherClient) {
        return new WeatherClientFacade(configApi, aliyunWeatherClient, amapWeatherClient);
    }

    @Bean
    public AliyunScenicSpotQueryClient aliyunScenicSpotQueryClient(RestTemplate restTemplate,
                                                                    TripProviderProperties properties) {
        return new AliyunScenicSpotQueryClient(restTemplate, properties.getScenicSpot());
    }

    @Bean
    public GaodeScenicSpotQueryClient gaodeScenicSpotQueryClient(RestTemplate restTemplate,
                                                                  TripProviderProperties properties) {
        return new GaodeScenicSpotQueryClient(restTemplate, properties.getScenicSpot());
    }

    @Bean
    public ScenicSpotQueryClientFacade scenicSpotQueryClientFacade(ConfigApi configApi,
            AliyunScenicSpotQueryClient aliyunClient, GaodeScenicSpotQueryClient gaodeClient) {
        return new ScenicSpotQueryClientFacade(configApi, aliyunClient, gaodeClient);
    }

    @Bean
    public GaodeTravelPlaceQueryClient gaodeTravelPlaceQueryClient(RestTemplate restTemplate,
            TripProviderProperties properties) {
        return new GaodeTravelPlaceQueryClient(restTemplate, properties.getTravelPlace());
    }

    @Bean
    public TravelPlaceQueryClientFacade travelPlaceQueryClientFacade(ConfigApi configApi,
            List<TravelPlaceQueryClient> clients) {
        return new TravelPlaceQueryClientFacade(configApi, clients);
    }

    @Bean
    public AmapRouteQueryClient amapRouteQueryClient(RestTemplate restTemplate, TripProviderProperties properties) {
        return new AmapRouteQueryClient(restTemplate, properties.getRoute());
    }

}
