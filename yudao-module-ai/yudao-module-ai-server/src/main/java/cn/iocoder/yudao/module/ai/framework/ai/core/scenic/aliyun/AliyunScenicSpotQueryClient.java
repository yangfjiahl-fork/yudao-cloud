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
        return post(type, path, requestBody);
    }

    private Response post(QueryType type, String path, MultiValueMap<String, String> requestBody) {
        String baseUrl = StrUtil.blankToDefault(config.getBaseUrl(), DEFAULT_BASE_URL);
        String url = StrUtil.removeSuffix(baseUrl, "/") + path;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8"));
        headers.set(HttpHeaders.AUTHORIZATION, "APPCODE " + config.getAppCode());
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers), String.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful() || StrUtil.isBlank(responseEntity.getBody())) {
                return Response.failure(type, "阿里云景点查询服务无有效响应");
            }
            JsonNode root = JsonUtils.parseTree(responseEntity.getBody());
            String code = JsonUtils.getText(root, "code");
            return new Response("200".equals(code), type, code, JsonUtils.getText(root, "msg"),
                    JsonUtils.getText(root, "taskNo"), root.get("data"));
        } catch (RuntimeException ex) {
            log.error("[post][调用阿里云市场景点查询接口失败，url({})]", url, ex);
            return Response.failure(type, "阿里云景点查询服务调用失败");
        }
    }

    private static void addIfNotBlank(MultiValueMap<String, String> requestBody, String name, String value) {
        if (StrUtil.isNotBlank(value)) {
            requestBody.add(name, value);
        }
    }

}
