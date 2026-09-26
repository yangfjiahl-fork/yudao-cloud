package cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.config.AmapProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/** 高德 Web 服务地点搜索客户端。 */
@Slf4j
@AllArgsConstructor
public class AmapPlaceSearchClient {

    private static final String SUCCESS_STATUS = "1";
    private static final String SUCCESS_INFO_CODE = "10000";
    private static final int DEFAULT_RADIUS = 1_500;
    private static final int MAX_RADIUS = 50_000;
    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int MAX_PAGE_SIZE = 25;
    private static final int MAX_PAGE_NO = 100;

    private final RestTemplate restTemplate;
    private final AmapProperties properties;

    @Cacheable(cacheNames = "giftGeoAmapPlace#10m", key = "#request.toString()")
    public SearchResult search(SearchRequest request) {
        validateSearchRequest(request);
        boolean around = request.longitude() != null;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(
                        around ? properties.getPlaceAroundSearchUrl() : properties.getPlaceSearchUrl())
                .queryParam("key", properties.getKey());
        if (around) {
            builder.queryParam("location", formatCoordinate(request.longitude()) + ","
                            + formatCoordinate(request.latitude()))
                    .queryParam("radius", clamp(request.radius(), DEFAULT_RADIUS, 1, MAX_RADIUS))
                    .queryParam("sortrule", "distance");
        } else if (StrUtil.isNotBlank(request.region())) {
            builder.queryParam("region", request.region()).queryParam("city_limit", true);
        }
        int pageSize = clamp(request.pageSize(), DEFAULT_PAGE_SIZE, 1, MAX_PAGE_SIZE);
        builder.queryParam("page_num", clamp(request.pageNo(), 1, 1, MAX_PAGE_NO))
                .queryParam("page_size", pageSize)
                .queryParam("show_fields", "business,photos")
                .queryParam("output", "JSON");
        if (StrUtil.isNotBlank(request.keyword())) {
            builder.queryParam("keywords", request.keyword());
        }
        if (StrUtil.isNotBlank(request.typeCode())) {
            builder.queryParam("types", request.typeCode());
        }
        return execute(builder.build().encode().toUri(), request.typeCode(), pageSize, "search");
    }

    @Cacheable(cacheNames = "giftGeoAmapPlaceDetail#30m", key = "#poiId")
    public SearchResult getPlaceDetail(String poiId) {
        validateProperties();
        if (StrUtil.isBlank(poiId)) {
            throw new IllegalArgumentException("地点详情查询缺少 POI 编号");
        }
        if (StrUtil.isBlank(properties.getPlaceDetailUrl())) {
            throw new IllegalStateException("高德地点详情接口地址未配置");
        }
        URI uri = UriComponentsBuilder.fromUriString(properties.getPlaceDetailUrl())
                .queryParam("key", properties.getKey())
                .queryParam("id", poiId)
                .queryParam("show_fields", "business,photos")
                .queryParam("output", "JSON")
                .build().encode().toUri();
        return execute(uri, null, 1, "getPlaceDetail");
    }

    private SearchResult execute(URI uri, String typeCode, int limit, String operation) {
        long startTime = System.currentTimeMillis();
        try {
            String responseBody = restTemplate.getForObject(uri, String.class);
            SearchResult result = convertResponse(
                    StrUtil.isBlank(responseBody) ? null : JsonUtils.parseTree(responseBody), typeCode, limit);
            log.info("[{}][高德地点查询成功，typeCode({}) count({}) duration({}ms)]", operation,
                    typeCode, result.places().size(), System.currentTimeMillis() - startTime);
            return result;
        } catch (RestClientException ex) {
            // RestClientException 可能包含带 Key 的完整 URL，避免将异常内容写入日志或向上透传。
            log.error("[{}][调用高德地点接口异常，typeCode({}) errorType({}) duration({}ms)]", operation,
                    typeCode, ex.getClass().getSimpleName(), System.currentTimeMillis() - startTime);
            throw new IllegalStateException("调用高德地点接口失败");
        }
    }

    private SearchResult convertResponse(JsonNode response, String typeCode, int limit) {
        if (response == null || !SUCCESS_STATUS.equals(response.path("status").asText())
                || !SUCCESS_INFO_CODE.equals(response.path("infocode").asText())) {
            String info = response == null ? "接口无响应" : response.path("info").asText("未知错误");
            String infoCode = response == null ? "" : response.path("infocode").asText("");
            log.warn("[convertResponse][高德地点搜索失败，info({}) infocode({})]", info, infoCode);
            throw new IllegalStateException("高德地点搜索失败：" + info);
        }
        JsonNode pois = response.path("pois");
        List<Place> places = new ArrayList<>();
        String expectedTypePrefix = StrUtil.length(typeCode) >= 2 ? StrUtil.sub(typeCode, 0, 2) : "";
        if (pois.isArray()) {
            for (JsonNode poi : pois) {
                String name = textValue(poi.path("name"));
                String actualTypeCode = textValue(poi.path("typecode"));
                if (StrUtil.isBlank(name) || StrUtil.isNotBlank(expectedTypePrefix)
                        && StrUtil.isNotBlank(actualTypeCode) && !actualTypeCode.startsWith(expectedTypePrefix)) {
                    continue;
                }
                places.add(convertPlace(poi));
                if (places.size() >= limit) {
                    break;
                }
            }
        }
        return new SearchResult(parseLong(response.path("count")), places);
    }

    private static Place convertPlace(JsonNode poi) {
        String[] location = StrUtil.splitToArray(textValue(poi.path("location")), ',');
        JsonNode business = poi.path("business");
        JsonNode photos = poi.path("photos");
        String photoUrl = photos.isArray() && !photos.isEmpty() ? textValue(photos.get(0).path("url")) : "";
        return new Place(textValue(poi.path("id")), textValue(poi.path("name")),
                textValue(poi.path("address")), location.length > 0 ? parseDecimal(location[0]) : null,
                location.length > 1 ? parseDecimal(location[1]) : null, textValue(poi.path("type")),
                textValue(poi.path("typecode")), textValue(poi.path("pname")), textValue(poi.path("cityname")),
                textValue(poi.path("adname")), textValue(poi.path("adcode")), parseLong(poi.path("distance")),
                textValue(business.path("tel")), photoUrl, textValue(business.path("rating")),
                textValue(business.path("cost")), textValue(business.path("tag")), businessHours(business));
    }

    private void validateSearchRequest(SearchRequest request) {
        validateProperties();
        if (request == null || StrUtil.isAllBlank(request.keyword(), request.typeCode())) {
            throw new IllegalArgumentException("地点搜索关键词和 POI 类型至少提供一个");
        }
        if ((request.longitude() == null) != (request.latitude() == null)) {
            throw new IllegalArgumentException("经纬度必须同时提供");
        }
        if (request.longitude() != null && StrUtil.isBlank(properties.getPlaceAroundSearchUrl())) {
            throw new IllegalStateException("高德周边地点搜索接口地址未配置");
        }
        if (request.longitude() == null && StrUtil.isBlank(properties.getPlaceSearchUrl())) {
            throw new IllegalStateException("高德地点关键字搜索接口地址未配置");
        }
    }

    private void validateProperties() {
        if (properties == null || StrUtil.isBlank(properties.getKey())) {
            throw new IllegalStateException("高德 Web 服务 API Key 未配置");
        }
    }

    private static int clamp(Integer value, int defaultValue, int min, int max) {
        return Math.min(Math.max(value == null ? defaultValue : value, min), max);
    }

    private static String formatCoordinate(BigDecimal coordinate) {
        return coordinate.stripTrailingZeros().toPlainString();
    }

    private static String textValue(JsonNode node) {
        return node.isTextual() ? node.asText() : "";
    }

    private static String businessHours(JsonNode business) {
        for (String field : List.of("business_time", "opentime_week", "open_time")) {
            String value = textValue(business.path(field));
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private static BigDecimal parseDecimal(String value) {
        try {
            return StrUtil.isBlank(value) ? null : new BigDecimal(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Long parseLong(JsonNode node) {
        try {
            return node == null || node.isMissingNode() || StrUtil.isBlank(node.asText()) ? null : Long.valueOf(node.asText());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public record SearchRequest(String keyword, String typeCode, String region, BigDecimal longitude, BigDecimal latitude,
                                Integer radius, Integer pageNo, Integer pageSize) {
    }

    public record SearchResult(Long total, List<Place> places) {
    }

    /** 坐标为高德 GCJ-02。 */
    public record Place(String poiId, String name, String address, BigDecimal longitude, BigDecimal latitude,
                        String type, String typeCode, String province, String city, String district, String adcode,
                        Long distanceMeters, String telephone, String photoUrl, String rating, String cost, String tag,
                        String businessHours) {
    }

}
