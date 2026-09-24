package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.core.AmapGeocodingClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.core.AmapPlaceSearchClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.place.AmapPoiTypeEnum;
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

    @Override
    public Location reverseGeocode(BigDecimal longitude, BigDecimal latitude) {
        AmapGeocodingClient.Location location = amapGeocodingClient.reverseGeocode(longitude, latitude);
        return new Location(location.province(), location.city(), location.district(), location.adcode(),
                location.formattedAddress());
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

    private static Place convert(AmapPlaceSearchClient.Place source) {
        return new Place(source.poiId(), source.name(), source.address(), source.longitude(), source.latitude(),
                source.type(), source.typeCode(), source.province(), source.city(), source.district(), source.adcode(),
                source.distanceMeters(), source.telephone(), source.photoUrl());
    }

}
