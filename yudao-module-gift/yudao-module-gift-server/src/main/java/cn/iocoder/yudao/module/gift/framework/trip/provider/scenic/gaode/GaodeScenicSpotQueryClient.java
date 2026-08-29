package cn.iocoder.yudao.module.gift.framework.trip.provider.scenic.gaode;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.gift.framework.trip.provider.config.TripProviderProperties;
import cn.iocoder.yudao.module.gift.framework.trip.provider.scenic.ScenicSpotQueryClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 高德 POI 景点查询客户端
 */
@AllArgsConstructor
@Slf4j
public class GaodeScenicSpotQueryClient implements ScenicSpotQueryClient {

    private static final String DEFAULT_URL = "https://restapi.amap.com/v5/place/text";
    private static final String DEFAULT_TYPES = "110000";

    private final RestTemplate restTemplate;
    private final TripProviderProperties.ScenicSpot config;

    @Override
    public Response query(Request request) {
        QueryType type = request == null || request.getType() == null ? QueryType.SCENIC_SPOT : request.getType();
        if (type != QueryType.SCENIC_SPOT) {
            log.warn("[query][高德景点查询不支持当前类型，type({})]", type);
            return Response.failure(type, "高德景点查询仅支持 SCENIC_SPOT");
        }
        if (config == null || StrUtil.isBlank(config.getAmapKey())) {
            log.warn("[query][高德景点查询配置缺失，type({})]", type);
            return Response.failure(type, "高德景点查询服务未配置 AMAP_WEB_SERVICE_KEY");
        }
        String keywords = request == null ? null : request.getKeyword();
        String types = StrUtil.blankToDefault(config.getAmapTypes(), DEFAULT_TYPES);
        if (StrUtil.isAllBlank(keywords, types)) {
            log.warn("[query][高德景点查询参数无效，keyword 和 types 同时为空]");
            return Response.failure(type, "高德景点查询的 keyword 和 types 不能同时为空");
        }

        int page = request == null || request.getPage() == null ? 1 : request.getPage();
        String region = request == null ? null : request.getRegion();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(StrUtil.blankToDefault(config.getAmapUrl(), DEFAULT_URL))
                .queryParam("key", config.getAmapKey())
                .queryParam("types", types)
                .queryParam("show_fields", "business,photos")
                .queryParam("page_size", 20)
                .queryParam("page_num", page)
                .queryParam("output", "json");
        if (StrUtil.isNotBlank(keywords)) {
            uriBuilder.queryParam("keywords", keywords);
        }
        if (StrUtil.isNotBlank(region)) {
            uriBuilder.queryParam("region", region).queryParam("city_limit", true);
        }

        log.info("[query][准备调用高德景点查询，type({}) keyword({}) region({}) page({}) types({})]",
                type, keywords, region, page, types);
        long startTime = System.currentTimeMillis();
        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                    uriBuilder.build().encode().toUri(), String.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful() || StrUtil.isBlank(responseEntity.getBody())) {
                log.warn("[query][高德景点查询无有效响应，httpStatus({}) hasBody({}) cost({}ms)]",
                        responseEntity.getStatusCode(), StrUtil.isNotBlank(responseEntity.getBody()),
                        System.currentTimeMillis() - startTime);
                return Response.failure(type, "高德景点查询服务无有效响应");
            }
            JsonNode root = JsonUtils.parseTree(responseEntity.getBody());
            String code = JsonUtils.getText(root, "infocode");
            boolean success = "1".equals(JsonUtils.getText(root, "status")) && "10000".equals(code);
            ObjectNode data = JsonUtils.getObjectMapper().createObjectNode();
            data.put("allNum", JsonUtils.getText(root, "count"));
            data.put("currentPage", page);
            data.set("list", root.has("pois") ? root.get("pois")
                    : JsonUtils.getObjectMapper().createArrayNode());
            log.info("[query][高德景点查询完成，httpStatus({}) code({}) success({}) count({}) page({}) cost({}ms)]",
                    responseEntity.getStatusCode(), code, success, JsonUtils.getText(root, "count"), page,
                    System.currentTimeMillis() - startTime);
            return new Response(success, type, code, JsonUtils.getText(root, "info"), null, data);
        } catch (RuntimeException ex) {
            // 高德 Key 位于 URL 查询参数中，避免将异常详情写入日志导致 Key 泄漏
            log.error("[query][调用高德景点查询接口失败，type({}) keyword({}) region({}) page({}) cost({}ms) 异常类型({})]",
                    type, keywords, region, page, System.currentTimeMillis() - startTime,
                    ex.getClass().getSimpleName());
            return Response.failure(type, "高德景点查询服务调用失败");
        }
    }

}
