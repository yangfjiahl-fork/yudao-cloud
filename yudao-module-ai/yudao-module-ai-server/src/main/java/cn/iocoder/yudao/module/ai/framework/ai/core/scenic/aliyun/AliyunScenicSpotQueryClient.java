package cn.iocoder.yudao.module.ai.framework.ai.core.scenic.aliyun;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.config.YudaoAiProperties;
import cn.iocoder.yudao.module.ai.framework.ai.core.scenic.ScenicSpotQueryClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * 阿里云市场景点查询客户端
 */
@AllArgsConstructor
@Slf4j
public class AliyunScenicSpotQueryClient implements ScenicSpotQueryClient {

    private static final String DEFAULT_BASE_URL = "https://jmqgjdcx.market.alicloudapi.com";

    private final RestTemplate restTemplate;
    private final YudaoAiProperties.ScenicSpot config;

    @Override
    public Response query(Request request) {
        QueryType type = request == null || request.getType() == null ? QueryType.SCENIC_SPOT : request.getType();
        if (config == null || StrUtil.isBlank(config.getAppCode())) {
            log.warn("[query][阿里云景点查询配置缺失，type({})]", type);
            return Response.failure(type, "景点查询服务未配置 ALIYUN_SCENIC_SPOT_APPCODE");
        }

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        String path;
        switch (type) {
            case PROVINCE:
                path = "/area/province";
                break;
            case CITY:
                if (request == null || StrUtil.isBlank(request.getProId())) {
                    return Response.failure(type, "查询城市列表时 proId 不能为空");
                }
                path = "/area/city";
                requestBody.add("proId", request.getProId());
                break;
            case DISTRICT:
                if (request == null || StrUtil.isBlank(request.getCityId())) {
                    return Response.failure(type, "查询区县列表时 cityId 不能为空");
                }
                path = "/area/district";
                requestBody.add("cityId", request.getCityId());
                break;
            case SCENIC_SPOT:
            default:
                path = "/area/scenic-spots";
                addIfNotBlank(requestBody, "keyword", request == null ? null : request.getKeyword());
                addIfNotBlank(requestBody, "proId", request == null ? null : request.getProId());
                addIfNotBlank(requestBody, "cityId", request == null ? null : request.getCityId());
                addIfNotBlank(requestBody, "areaId", request == null ? null : request.getAreaId());
                if (request != null && request.getPage() != null) {
                    requestBody.add("page", request.getPage().toString());
                }
                break;
        }
        log.info("[query][准备调用阿里云景点查询，type({}) path({}) keyword({}) proId({}) cityId({}) areaId({}) page({})]",
                type, path, request == null ? null : request.getKeyword(),
                request == null ? null : request.getProId(), request == null ? null : request.getCityId(),
                request == null ? null : request.getAreaId(), request == null ? null : request.getPage());
        return post(type, path, requestBody);
    }

    private Response post(QueryType type, String path, MultiValueMap<String, String> requestBody) {
        String baseUrl = StrUtil.blankToDefault(config.getBaseUrl(), DEFAULT_BASE_URL);
        String url = StrUtil.removeSuffix(baseUrl, "/") + path;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8"));
        headers.set(HttpHeaders.AUTHORIZATION, "APPCODE " + config.getAppCode());
        long startTime = System.currentTimeMillis();
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers), String.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful() || StrUtil.isBlank(responseEntity.getBody())) {
                log.warn("[post][阿里云景点查询无有效响应，type({}) path({}) httpStatus({}) hasBody({}) cost({}ms)]",
                        type, path, responseEntity.getStatusCode(), StrUtil.isNotBlank(responseEntity.getBody()),
                        System.currentTimeMillis() - startTime);
                return Response.failure(type, "阿里云景点查询服务无有效响应");
            }
            JsonNode root = JsonUtils.parseTree(responseEntity.getBody());
            String code = JsonUtils.getText(root, "code");
            boolean success = "200".equals(code);
            log.info("[post][阿里云景点查询完成，type({}) path({}) httpStatus({}) code({}) success({}) taskNo({}) cost({}ms)]",
                    type, path, responseEntity.getStatusCode(), code, success, JsonUtils.getText(root, "taskNo"),
                    System.currentTimeMillis() - startTime);
            return new Response(success, type, code, JsonUtils.getText(root, "msg"),
                    JsonUtils.getText(root, "taskNo"), root.get("data"));
        } catch (RuntimeException ex) {
            log.error("[post][调用阿里云市场景点查询接口失败，type({}) path({}) cost({}ms)]",
                    type, path, System.currentTimeMillis() - startTime, ex);
            return Response.failure(type, "阿里云景点查询服务调用失败");
        }
    }

    private static void addIfNotBlank(MultiValueMap<String, String> requestBody, String name, String value) {
        if (StrUtil.isNotBlank(value)) {
            requestBody.add(name, value);
        }
    }

}
