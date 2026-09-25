package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.core.AmapGeocodingClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.core.AmapPlaceSearchClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.place.AmapPoiTypeEnum;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.weather.WeatherClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.weather.amap.AmapWeatherClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** 统一编排 APP 定位相关的高德接口，Controller 不直接依赖供应商 Client。 */
@Service
public class ItineraryLocationServiceImpl implements ItineraryLocationService {

    @Resource
    private AmapGeocodingClient amapGeocodingClient;
    @Resource
    private AmapPlaceSearchClient amapPlaceSearchClient;
    @Resource
    private AmapWeatherClient amapWeatherClient;

    @Override
    public Location reverseGeocode(BigDecimal longitude, BigDecimal latitude) {
        AmapGeocodingClient.Location location = amapGeocodingClient.reverseGeocode(longitude, latitude);
        return new Location(location.province(), location.city(), location.district(), location.adcode(),
                location.formattedAddress());
    }

    @Override
    public Location identifyCurrentCity(BigDecimal longitude, BigDecimal latitude, String clientIp) {
        return longitude != null && latitude != null
                ? reverseGeocode(longitude, latitude)
                : convert(amapGeocodingClient.locateByIp(clientIp));
    }

    @Override
    public PlaceSearchResult searchNearbyPlaces(PlaceSearchRequest request) {
        String typeCode = StrUtil.isBlank(request.category()) ? null
                : AmapPoiTypeEnum.fromCategory(request.category()).getAmapTypeCode();
        AmapPlaceSearchClient.SearchResult result = amapPlaceSearchClient.search(
                new AmapPlaceSearchClient.SearchRequest(request.keyword(), typeCode, request.longitude(),
                        request.latitude(), request.radius(), request.pageNo(), request.pageSize()));
        return new PlaceSearchResult(result.total(),
                result.places().stream().map(ItineraryLocationServiceImpl::convert).toList());
    }

    @Override
    public Weather getCurrentWeather(String cityCode) {
        WeatherClient.CurrentWeather weather = amapWeatherClient.getCurrentWeather(cityCode);
        return new Weather(weather.city(), weather.temperature(), weather.condition(), weather.humidity(),
                weather.windDirection(), weather.windPower(), weather.queryTime());
    }

    private static Place convert(AmapPlaceSearchClient.Place source) {
        return new Place(source.poiId(), source.name(), source.address(), source.longitude(), source.latitude(),
                source.type(), source.typeCode(), source.province(), source.city(), source.district(), source.adcode(),
                source.distanceMeters(), source.telephone(), source.photoUrl());
    }

    private static Location convert(AmapGeocodingClient.Location source) {
        return new Location(source.province(), source.city(), source.district(), source.adcode(),
                source.formattedAddress());
    }

}
