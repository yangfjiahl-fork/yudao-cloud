package cn.iocoder.yudao.module.gift.framework.trip.provider.place.gaode;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.gift.framework.trip.provider.config.TripProviderProperties;
import cn.iocoder.yudao.module.gift.framework.trip.provider.place.TravelPlaceQueryClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/** 高德 Web 服务 POI 搜索的酒店、餐厅实现。 */
@AllArgsConstructor
@Slf4j
public class GaodeTravelPlaceQueryClient implements TravelPlaceQueryClient {

    private static final String DEFAULT_URL = "https://restapi.amap.com/v5/place/text";
    private static final String DEFAULT_AROUND_URL = "https://restapi.amap.com/v5/place/around";
    private static final String HOTEL_TYPES = "100000";
    private static final String RESTAURANT_TYPES = "050000";
    private static final int DEFAULT_LIMIT = 2;
    private static final int MAX_LIMIT = 20;

    private final RestTemplate restTemplate;
    private final TripProviderProperties.TravelPlace config;

    @Override
    public String provider() {
        return "gaode";
    }

    @Override
    @Cacheable(cacheNames = "tripPlaceGaode#10m",
            key = "#request == null ? '' : #request.toString()",
            unless = "#result == null || #result.success != true")
    public Response query(Request request) {
        if (request == null || request.getType() == null || StrUtil.isBlank(request.getRegion())) {
            return Response.failure("旅行地点查询缺少类型或城市");
        }
        if (config == null || StrUtil.isBlank(config.getAmapKey())) {
            log.warn("[query][高德旅行地点查询配置缺失，type({}) region({})]", request.getType(), request.getRegion());
            return Response.failure("高德旅行地点查询服务未配置 AMAP_WEB_SERVICE_KEY");
        }
        int limit = Math.min(Math.max(request.getLimit() == null ? DEFAULT_LIMIT : request.getLimit(), 1), MAX_LIMIT);
        String types = request.getType() == PlaceType.HOTEL ? HOTEL_TYPES : RESTAURANT_TYPES;
        boolean around = StrUtil.isNotBlank(request.getLocation());
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(around ? StrUtil.blankToDefault(config.getAmapAroundUrl(), DEFAULT_AROUND_URL)
                        : StrUtil.blankToDefault(config.getAmapUrl(), DEFAULT_URL))
                .queryParam("key", config.getAmapKey())
                .queryParam("types", types);
        if (around) {
            uriBuilder.queryParam("location", request.getLocation())
                    .queryParam("radius", Math.min(Math.max(request.getRadius() == null ? 1_500 : request.getRadius(), 1), 50_000));
        } else {
            uriBuilder.queryParam("region", request.getRegion()).queryParam("city_limit", true);
        }
        uriBuilder.queryParam("show_fields", "business,photos").queryParam("page_size", limit)
                .queryParam("page_num", 1).queryParam("output", "json");
        long startTime = System.currentTimeMillis();
        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(uriBuilder.build().encode().toUri(), String.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful() || StrUtil.isBlank(responseEntity.getBody())) {
                return Response.failure("高德旅行地点查询服务无有效响应");
            }
            JsonNode root = JsonUtils.parseTree(responseEntity.getBody());
            String code = JsonUtils.getText(root, "infocode");
            boolean success = "1".equals(JsonUtils.getText(root, "status")) && "10000".equals(code);
            List<Place> places = success ? parsePlaces(root.path("pois"), request.getType(), limit) : List.of();
            log.info("[query][高德旅行地点查询完成，type({}) region({}) code({}) success({}) count({}) cost({}ms)]",
                    request.getType(), request.getRegion(), code, success, places.size(), System.currentTimeMillis() - startTime);
            return new Response(success, code, JsonUtils.getText(root, "info"), places);
        } catch (RuntimeException ex) {
            // 高德 Key 位于 URL 查询参数中，避免记录异常详情导致 Key 泄漏
            log.error("[query][调用高德旅行地点查询失败，type({}) region({}) cost({}ms) exceptionType({})]",
                    request.getType(), request.getRegion(), System.currentTimeMillis() - startTime, ex.getClass().getSimpleName());
            return Response.failure("高德旅行地点查询服务调用失败");
        }
    }

    private static List<Place> parsePlaces(JsonNode pois, PlaceType type, int limit) {
        if (!pois.isArray()) {
            return List.of();
        }
        String expectedTypePrefix = type == PlaceType.HOTEL ? "10" : "05";
        List<Place> result = new ArrayList<>();
        for (JsonNode poi : pois) {
            String name = text(poi, "name");
            if (StrUtil.isBlank(name)) {
                continue;
            }
            // 高德偶尔会在 types 查询结果中混入相邻业态；有 typecode 时严格过滤，
            // 没有 typecode 的兼容测试数据仍按名称和地址返回。
            String typecode = text(poi, "typecode");
            if (StrUtil.isNotBlank(typecode) && !typecode.startsWith(expectedTypePrefix)) {
                continue;
            }
            String[] coordinates = StrUtil.splitToArray(text(poi, "location"), ',');
            JsonNode business = poi.path("business");
            Place place = new Place();
            place.setExternalId(text(poi, "id"));
            place.setName(name);
            place.setAddress(text(poi, "address"));
            place.setLongitude(coordinates.length > 0 ? coordinates[0] : "");
            place.setLatitude(coordinates.length > 1 ? coordinates[1] : "");
            place.setImageUrl(firstImage(poi));
            place.setTelephone(text(business, "tel"));
            place.setRating(text(business, "rating"));
            place.setCost(text(business, "cost"));
            place.setTag(text(business, "tag"));
            result.add(place);
            if (result.size() >= limit) {
                break;
            }
        }
        return result;
    }

    private static String firstImage(JsonNode poi) {
        JsonNode photos = poi.path("photos");
        return photos.isArray() && !photos.isEmpty() ? text(photos.get(0), "url") : "";
    }

    private static String text(JsonNode node, String name) {
        return node.path(name).asText();
    }

}
