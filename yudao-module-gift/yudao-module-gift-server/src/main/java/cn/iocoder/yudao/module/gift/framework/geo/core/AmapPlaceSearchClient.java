package cn.iocoder.yudao.module.gift.framework.geo.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.gift.framework.geo.config.AmapProperties;
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

    private final RestTemplate restTemplate;
    private final AmapProperties properties;

    @Cacheable(cacheNames = "giftGeoAmapPlace#10m", key = "#request.toString()")
    public SearchResult search(SearchRequest request) {
        validate(request);
        boolean aroundSearch = request.longitude() != null;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(aroundSearch
                        ? properties.getPlaceAroundSearchUrl() : properties.getPlaceSearchUrl())
                .queryParam("key", properties.getKey())
                .queryParam("keywords", request.keyword())
                .queryParam("page_num", request.pageNo())
                .queryParam("page_size", request.pageSize())
                .queryParam("show_fields", "business,photos")
                .queryParam("output", "JSON");
        if (aroundSearch) {
            builder.queryParam("location", formatCoordinate(request.longitude()) + ","
                            + formatCoordinate(request.latitude()))
                    .queryParam("radius", request.radius())
                    .queryParam("sortrule", "distance");
        } else if (StrUtil.isNotBlank(request.city())) {
            builder.queryParam("region", request.city()).queryParam("city_limit", true);
        }
        URI uri = builder.build().encode().toUri();
        long startTime = System.currentTimeMillis();
        try {
            String responseBody = restTemplate.getForObject(uri, String.class);
            SearchResult result = convertResponse(StrUtil.isBlank(responseBody) ? null : JsonUtils.parseTree(responseBody));
            log.info("[search][高德地点搜索成功，keyword({}) city({}) around({}) count({}) duration({}ms)]",
                    request.keyword(), request.city(), aroundSearch, result.places().size(),
                    System.currentTimeMillis() - startTime);
            return result;
        } catch (RestClientException ex) {
            // RestClientException 可能包含带 Key 的完整 URL，避免将异常内容写入日志或向上透传。
            log.error("[search][调用高德地点搜索接口异常，keyword({}) city({}) around({}) errorType({}) duration({}ms)]",
                    request.keyword(), request.city(), aroundSearch, ex.getClass().getSimpleName(),
                    System.currentTimeMillis() - startTime);
            throw new IllegalStateException("调用高德地点搜索接口失败");
        }
    }

    private SearchResult convertResponse(JsonNode response) {
        if (response == null || !SUCCESS_STATUS.equals(response.path("status").asText())
                || !SUCCESS_INFO_CODE.equals(response.path("infocode").asText())) {
            String info = response == null ? "接口无响应" : response.path("info").asText("未知错误");
            String infoCode = response == null ? "" : response.path("infocode").asText("");
            log.warn("[convertResponse][高德地点搜索失败，info({}) infocode({})]", info, infoCode);
            throw new IllegalStateException("高德地点搜索失败：" + info);
        }
        JsonNode pois = response.path("pois");
        List<Place> places = new ArrayList<>();
        if (pois.isArray()) {
            for (JsonNode poi : pois) {
                places.add(convertPlace(poi));
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
                textValue(business.path("tel")), photoUrl);
    }

    private void validate(SearchRequest request) {
        if (properties == null || StrUtil.isBlank(properties.getKey())) {
            throw new IllegalStateException("高德 Web 服务 API Key 未配置");
        }
        if (request == null || StrUtil.isBlank(request.keyword())) {
            throw new IllegalArgumentException("地点搜索关键词不能为空");
        }
        if ((request.longitude() == null) != (request.latitude() == null)) {
            throw new IllegalArgumentException("经纬度必须同时提供");
        }
        boolean aroundSearch = request.longitude() != null;
        String url = aroundSearch ? properties.getPlaceAroundSearchUrl() : properties.getPlaceSearchUrl();
        if (StrUtil.isBlank(url)) {
            throw new IllegalStateException("高德地点搜索接口地址未配置");
        }
    }

    private static String formatCoordinate(BigDecimal coordinate) {
        return coordinate.stripTrailingZeros().toPlainString();
    }

    private static String textValue(JsonNode node) {
        return node.isTextual() ? node.asText() : "";
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

    public record SearchRequest(String keyword, String city, BigDecimal longitude, BigDecimal latitude,
                                Integer radius, Integer pageNo, Integer pageSize) {
    }

    public record SearchResult(Long total, List<Place> places) {
    }

    /** 坐标为高德 GCJ-02。 */
    public record Place(String poiId, String name, String address, BigDecimal longitude, BigDecimal latitude,
                        String type, String typeCode, String province, String city, String district, String adcode,
                        Long distanceMeters, String telephone, String photoUrl) {
    }

}
