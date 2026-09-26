package cn.iocoder.yudao.module.gift.service.itinerary.provider.scenic.gaode;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.core.AmapPlaceSearchClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.place.AmapPoiTypeEnum;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.scenic.ScenicSpotQueryClient;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/** 高德景点查询协议适配器。 */
@AllArgsConstructor
@Slf4j
public class GaodeScenicSpotQueryClient implements ScenicSpotQueryClient {

    private static final int PAGE_SIZE = 25;

    private final AmapPlaceSearchClient amapPlaceSearchClient;

    @Override
    public Response query(Request request) {
        QueryType type = request == null || request.getType() == null ? QueryType.SCENIC_SPOT : request.getType();
        if (type != QueryType.SCENIC_SPOT) {
            log.warn("[query][高德景点查询不支持当前类型，type({})]", type);
            return Response.failure(type, "高德景点查询仅支持 SCENIC_SPOT");
        }
        String keyword = request == null ? null : request.getKeyword();
        String region = request == null ? null : request.getRegion();
        int page = request == null || request.getPage() == null ? 1 : Math.max(request.getPage(), 1);
        try {
            AmapPlaceSearchClient.SearchResult result = amapPlaceSearchClient.search(
                    new AmapPlaceSearchClient.SearchRequest(keyword, AmapPoiTypeEnum.SCENIC.getAmapTypeCode(),
                            region, null, null, null, page, PAGE_SIZE));
            ObjectNode data = JsonUtils.getObjectMapper().createObjectNode();
            data.put("allNum", result.total() == null ? "" : result.total().toString());
            data.put("currentPage", page);
            ArrayNode list = data.putArray("list");
            result.places().forEach(place -> list.add(convertPlace(place)));
            return new Response(true, type, "10000", "OK", null, data);
        } catch (RuntimeException ex) {
            log.error("[query][高德景点查询失败，keyword({}) region({}) page({}) exceptionType({})]",
                    keyword, region, page, ex.getClass().getSimpleName());
            return Response.failure(type, "高德景点查询服务调用失败");
        }
    }

    private static ObjectNode convertPlace(AmapPlaceSearchClient.Place place) {
        ObjectNode item = JsonUtils.getObjectMapper().createObjectNode();
        item.put("id", place.poiId());
        item.put("name", place.name());
        item.put("address", place.address());
        item.put("location", coordinate(place.longitude()) + "," + coordinate(place.latitude()));
        ArrayNode photos = item.putArray("photos");
        if (!place.photoUrl().isBlank()) {
            photos.addObject().put("url", place.photoUrl());
        }
        ObjectNode business = item.putObject("business");
        business.put("tel", place.telephone());
        business.put("rating", place.rating());
        business.put("cost", place.cost());
        business.put("tag", place.tag());
        business.put("business_time", place.businessHours());
        return item;
    }

    private static String coordinate(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

}
