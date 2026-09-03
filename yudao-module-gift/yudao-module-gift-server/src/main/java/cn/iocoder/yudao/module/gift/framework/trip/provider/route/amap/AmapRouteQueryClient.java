package cn.iocoder.yudao.module.gift.framework.trip.provider.route.amap;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.gift.framework.trip.provider.config.TripProviderProperties;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/** 高德 Web 服务路径规划客户端，仅负责单段步行或公交路径的事实查询。 */
@AllArgsConstructor
@Slf4j
public class AmapRouteQueryClient {

    private final RestTemplate restTemplate;
    private final TripProviderProperties.Route config;

    public boolean isConfigured() {
        return config != null && StrUtil.isNotBlank(config.getAmapKey());
    }

    @PostConstruct
    void warnWhenRouteProviderUnavailable() {
        if (!isConfigured()) {
            log.warn("[warnWhenRouteProviderUnavailable][高德路径规划未配置；行程交通段将使用本地估算。"
                    + "请配置 AMAP_ROUTE_API_KEY 或 AMAP_WEB_SERVICE_KEY 后重启服务]");
        }
    }

    @Cacheable(cacheNames = "tripRouteGaode#5m", key = "#request == null ? '' : #request.toString()")
    public Route query(Request request) {
        validateRequest(request);
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(request.mode() == Mode.WALKING ? config.getWalkingUrl() : config.getTransitUrl())
                .queryParam("key", config.getAmapKey())
                .queryParam("origin", request.origin())
                .queryParam("destination", request.destination())
                .queryParam("output", "JSON");
        if (request.mode() == Mode.TRANSIT) {
            uriBuilder.queryParam("city", request.city()).queryParam("strategy", 0).queryParam("extensions", "all");
        }
        long start = System.currentTimeMillis();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(uriBuilder.build().encode().toUri(), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || StrUtil.isBlank(response.getBody())) {
                throw new RouteQueryException("HTTP_" + response.getStatusCode().value());
            }
            JsonNode root = JsonUtils.parseTree(response.getBody());
            if (!"1".equals(JsonUtils.getText(root, "status")) || !"10000".equals(JsonUtils.getText(root, "infocode"))) {
                throw new RouteQueryException("AMAP_" + StrUtil.blankToDefault(JsonUtils.getText(root, "infocode"), "UNKNOWN"));
            }
            JsonNode route = root.path("route");
            JsonNode path = request.mode() == Mode.WALKING ? route.path("paths").path(0) : route.path("transits").path(0);
            int distance = path.path("distance").asInt(-1);
            int duration = path.path("duration").asInt(-1);
            if (distance < 0 || duration < 0) {
                throw new IllegalStateException("高德路径规划响应缺少距离或耗时");
            }
            return new Route(request.mode(), distance, duration, collectRoutePoints(path));
        } catch (RuntimeException e) {
            String failureCode = e instanceof RouteQueryException routeException ? routeException.failureCode()
                    : e.getClass().getSimpleName();
            log.warn("[query][高德路径规划失败，mode({}) city({}) cost({}ms) failureCode({})]", request.mode(), request.city(),
                    System.currentTimeMillis() - start, failureCode);
            throw e;
        }
    }

    private void validateRequest(Request request) {
        if (request == null || StrUtil.isBlank(request.origin()) || StrUtil.isBlank(request.destination()) || request.mode() == null) {
            throw new IllegalArgumentException("高德路径规划缺少起点、终点或方式");
        }
        if (request.mode() == Mode.TRANSIT && StrUtil.isBlank(request.city())) {
            throw new IllegalArgumentException("高德公交路径规划缺少城市");
        }
        if (!isConfigured()) {
            throw new IllegalStateException("高德路径规划 API Key 未配置");
        }
    }

    private static List<RoutePoint> collectRoutePoints(JsonNode node) {
        List<String> polylines = new ArrayList<>();
        collectPolylines(node, polylines);
        List<RoutePoint> result = new ArrayList<>();
        for (String polyline : polylines) {
            for (String coordinate : StrUtil.split(polyline, ';')) {
                String[] values = StrUtil.splitToArray(coordinate, ',');
                if (values.length != 2) {
                    continue;
                }
                try {
                    double longitude = Double.parseDouble(values[0]);
                    double latitude = Double.parseDouble(values[1]);
                    if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
                        continue;
                    }
                    RoutePoint point = new RoutePoint(longitude, latitude);
                    // List#getLast is unavailable on the project's Java 17 runtime.
                    if (result.isEmpty() || !result.get(result.size() - 1).equals(point)) {
                        result.add(point);
                    }
                } catch (NumberFormatException ignored) {
                    // 单个坐标异常不影响整段路线的距离和耗时结果。
                }
            }
        }
        return result;
    }

    private static void collectPolylines(JsonNode node, List<String> polylines) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                if ("polyline".equals(entry.getKey()) && entry.getValue().isTextual() && StrUtil.isNotBlank(entry.getValue().asText())) {
                    polylines.add(entry.getValue().asText());
                } else {
                    collectPolylines(entry.getValue(), polylines);
                }
            });
            return;
        }
        if (node.isArray()) {
            node.forEach(item -> collectPolylines(item, polylines));
        }
    }

    public enum Mode {
        WALKING,
        TRANSIT
    }

    public record Request(String city, String origin, String destination, Mode mode) {
    }

    public record Route(Mode mode, int distanceMeters, int durationSeconds, List<RoutePoint> routePoints) {
    }

    private static final class RouteQueryException extends IllegalStateException {

        private final String failureCode;

        private RouteQueryException(String failureCode) {
            super("高德路径规划失败：" + failureCode);
            this.failureCode = failureCode;
        }

        private String failureCode() {
            return failureCode;
        }
    }

    /** 路线点为高德 GCJ-02 坐标，可直接转换为各端地图 SDK 的 LatLng。 */
    public record RoutePoint(double longitude, double latitude) {
    }
}
