package cn.iocoder.yudao.module.gift.service.itinerary.provider.place.gaode;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.config.AmapProperties;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.place.AmapPoiTypeEnum;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.place.TravelPlaceQueryClient;
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

    private static final int DEFAULT_LIMIT = 25;
    private static final int MAX_LIMIT = 25;
    private static final int MAX_PAGE = 100;

    private final RestTemplate restTemplate;
    private final AmapProperties config;

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
        if (request.getType() == AmapPoiTypeEnum.SCENIC) {
            return Response.failure("景点请使用景点查询服务");
        }
        if (config == null || StrUtil.isBlank(config.getKey())) {
            log.warn("[query][高德旅行地点查询配置缺失，type({}) region({})]", request.getType(), request.getRegion());
            return Response.failure("高德旅行地点查询服务未配置 AMAP_WEB_SERVICE_KEY");
        }
        int limit = Math.min(Math.max(request.getLimit() == null ? DEFAULT_LIMIT : request.getLimit(), 1), MAX_LIMIT);
        int page = Math.min(Math.max(request.getPage() == null ? 1 : request.getPage(), 1), MAX_PAGE);
        String types = request.getType().getAmapTypeCode();
        boolean around = StrUtil.isNotBlank(request.getLocation());
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(around ? config.getPlaceAroundSearchUrl() : config.getPlaceSearchUrl())
                .queryParam("key", config.getKey())
                .queryParam("types", types);
        if (around) {
            uriBuilder.queryParam("location", request.getLocation())
                    .queryParam("radius", Math.min(Math.max(request.getRadius() == null ? 1_500 : request.getRadius(), 1), 50_000));
        } else {
            uriBuilder.queryParam("region", request.getRegion()).queryParam("city_limit", true);
        }
        if (StrUtil.isNotBlank(request.getKeyword())) {
            uriBuilder.queryParam("keywords", request.getKeyword());
        }
        uriBuilder.queryParam("show_fields", "business,photos").queryParam("page_size", limit)
                .queryParam("page_num", page).queryParam("output", "json");
        long startTime = System.currentTimeMillis();
        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(uriBuilder.build().encode().toUri(), String.class);
            log.info("[query][高德旅行地点查询返回原始数据，type({}) region({}) httpStatus({}) responseBody({})]",
                    request.getType(), request.getRegion(), responseEntity.getStatusCode(), responseEntity.getBody());
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

    @Override
    @Cacheable(cacheNames = "tripPlaceDetailGaode#30m", key = "#poiId",
            unless = "#result == null || #result.success != true")
    public Response getPlaceDetail(String poiId) {
        if (StrUtil.isBlank(poiId)) {
            return Response.failure("旅行地点详情查询缺少 POI 编号");
        }
        if (config == null || StrUtil.isBlank(config.getKey())) {
            log.warn("[getPlaceDetail][高德旅行地点详情配置缺失，poiId({})]", poiId);
            return Response.failure("高德旅行地点详情服务未配置 AMAP_WEB_SERVICE_KEY");
        }
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(config.getPlaceDetailUrl())
                .queryParam("key", config.getKey()).queryParam("id", poiId)
                .queryParam("show_fields", "business,photos").queryParam("output", "json");
        long startTime = System.currentTimeMillis();
        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                    uriBuilder.build().encode().toUri(), String.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful() || StrUtil.isBlank(responseEntity.getBody())) {
                return Response.failure("高德旅行地点详情服务无有效响应");
            }
            JsonNode root = JsonUtils.parseTree(responseEntity.getBody());
            String code = JsonUtils.getText(root, "infocode");
            boolean success = "1".equals(JsonUtils.getText(root, "status")) && "10000".equals(code);
            List<Place> places = success ? parsePlaces(root.path("pois"), null, 1) : List.of();
            log.info("[getPlaceDetail][高德旅行地点详情查询完成，poiId({}) code({}) success({}) count({}) cost({}ms)]",
                    poiId, code, success, places.size(), System.currentTimeMillis() - startTime);
            return new Response(success, code, JsonUtils.getText(root, "info"), places);
        } catch (RuntimeException ex) {
            // 高德 Key 位于 URL 查询参数中，避免记录异常详情导致 Key 泄漏
            log.error("[getPlaceDetail][调用高德旅行地点详情失败，poiId({}) cost({}ms) exceptionType({})]",
                    poiId, System.currentTimeMillis() - startTime, ex.getClass().getSimpleName());
            return Response.failure("高德旅行地点详情服务调用失败");
        }
    }

    private static List<Place> parsePlaces(JsonNode pois, AmapPoiTypeEnum type, int limit) {
        if (!pois.isArray()) {
            return List.of();
        }
        String expectedTypePrefix = type == null ? "" : type.getAmapTypePrefix();
        List<Place> result = new ArrayList<>();
        for (JsonNode poi : pois) {
            String name = text(poi, "name");
            if (StrUtil.isBlank(name)) {
                continue;
            }
            // 高德偶尔会在 types 查询结果中混入相邻业态；有 typecode 时严格过滤，
            // 没有 typecode 的兼容测试数据仍按名称和地址返回。
            String typecode = text(poi, "typecode");
            if (type != null && StrUtil.isNotBlank(typecode) && !typecode.startsWith(expectedTypePrefix)) {
                continue;
            }
            String[] coordinates = StrUtil.splitToArray(text(poi, "location"), ',');
            JsonNode business = poi.path("business");
            Place place = new Place();
            place.setPoiId(text(poi, "id"));
            place.setName(name);
            place.setAddress(text(poi, "address"));
            place.setLongitude(coordinates.length > 0 ? coordinates[0] : "");
            place.setLatitude(coordinates.length > 1 ? coordinates[1] : "");
            place.setImageUrl(firstImage(poi));
            place.setTelephone(text(business, "tel"));
            place.setRating(text(business, "rating"));
            place.setCost(text(business, "cost"));
            place.setTag(text(business, "tag"));
            place.setBusinessHours(businessHours(business));
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

    private static String businessHours(JsonNode business) {
        for (String field : List.of("business_time", "opentime_week", "open_time")) {
            String value = text(business, field);
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private static String text(JsonNode node, String name) {
        return node.path(name).asText();
    }

}
