package cn.iocoder.yudao.module.gift.service.itinerary.provider.place.gaode;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.core.AmapPlaceSearchClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.place.AmapPoiTypeEnum;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.place.TravelPlaceQueryClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

/** 高德酒店、餐厅地点查询协议适配器。 */
@AllArgsConstructor
@Slf4j
public class GaodeTravelPlaceQueryClient implements TravelPlaceQueryClient {

    private final AmapPlaceSearchClient amapPlaceSearchClient;

    @Override
    public String provider() {
        return "gaode";
    }

    @Override
    public Response query(Request request) {
        if (request == null || request.getType() == null || StrUtil.isBlank(request.getRegion())) {
            return Response.failure("旅行地点查询缺少类型或城市");
        }
        if (request.getType() == AmapPoiTypeEnum.SCENIC) {
            return Response.failure("景点请使用景点查询服务");
        }
        try {
            BigDecimal[] location = parseLocation(request.getLocation());
            AmapPlaceSearchClient.SearchResult result = amapPlaceSearchClient.search(
                    new AmapPlaceSearchClient.SearchRequest(request.getKeyword(),
                            request.getType().getAmapTypeCode(), request.getRegion(), location[0], location[1],
                            request.getRadius(), request.getPage(), request.getLimit()));
            return success(result);
        } catch (RuntimeException ex) {
            log.error("[query][高德旅行地点查询失败，type({}) region({}) exceptionType({})]",
                    request.getType(), request.getRegion(), ex.getClass().getSimpleName());
            return Response.failure("高德旅行地点查询服务调用失败");
        }
    }

    @Override
    public Response getPlaceDetail(String poiId) {
        if (StrUtil.isBlank(poiId)) {
            return Response.failure("旅行地点详情查询缺少 POI 编号");
        }
        try {
            return success(amapPlaceSearchClient.getPlaceDetail(poiId));
        } catch (RuntimeException ex) {
            log.error("[getPlaceDetail][高德旅行地点详情查询失败，poiId({}) exceptionType({})]",
                    poiId, ex.getClass().getSimpleName());
            return Response.failure("高德旅行地点详情服务调用失败");
        }
    }

    private static Response success(AmapPlaceSearchClient.SearchResult result) {
        List<Place> places = result.places().stream().map(GaodeTravelPlaceQueryClient::convertPlace).toList();
        return new Response(true, "10000", "OK", places);
    }

    private static Place convertPlace(AmapPlaceSearchClient.Place source) {
        Place target = new Place();
        target.setPoiId(source.poiId());
        target.setName(source.name());
        target.setAddress(source.address());
        target.setLongitude(toPlainString(source.longitude()));
        target.setLatitude(toPlainString(source.latitude()));
        target.setImageUrl(source.photoUrl());
        target.setTelephone(source.telephone());
        target.setRating(source.rating());
        target.setCost(source.cost());
        target.setTag(source.tag());
        target.setBusinessHours(source.businessHours());
        return target;
    }

    private static BigDecimal[] parseLocation(String location) {
        if (StrUtil.isBlank(location)) {
            return new BigDecimal[]{null, null};
        }
        String[] values = StrUtil.splitToArray(location, ',');
        if (values.length != 2) {
            throw new IllegalArgumentException("高德坐标格式错误");
        }
        try {
            return new BigDecimal[]{new BigDecimal(values[0].trim()), new BigDecimal(values[1].trim())};
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("高德坐标格式错误");
        }
    }

    private static String toPlainString(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

}
