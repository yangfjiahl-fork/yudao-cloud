package cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl.aliyunkd100;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.ExpressSubscribeClient;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.ExpressTrackCacheKeyUtils;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackNotifyRespDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackRespDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl.aliyunkd100.dto.AliyunKd100QueryRespDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl.aliyunkd100.dto.AliyunKd100SubscribeReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl.aliyunkd100.dto.AliyunKd100SubscribeRespDTO;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.EXPRESS_API_QUERY_ERROR;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.EXPRESS_API_QUERY_FAILED;
import static cn.iocoder.yudao.module.trade.framework.delivery.core.client.convert.ExpressQueryConvert.INSTANCE;

/**
 * 阿里云市场快递 100 客户端
 */
@Slf4j
@AllArgsConstructor
public class AliyunKd100ExpressClient implements ExpressSubscribeClient {

    private static final String SUCCESS_CODE = "200";

    private final RestTemplate restTemplate;
    private final TradeExpressProperties.AliyunKd100Config config;

    @Override
    public List<ExpressTrackRespDTO> getExpressTrackList(ExpressTrackQueryReqDTO reqDTO) {
        validateConfig(false);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("com", reqDTO.getExpressCode().toLowerCase(Locale.ROOT));
        requestBody.add("num", reqDTO.getLogisticsNo());
        if (StrUtil.isNotBlank(reqDTO.getPhone())) {
            requestBody.add("phone", reqDTO.getPhone());
        }
        AliyunKd100QueryRespDTO response = post(config.getQueryUrl(), requestBody,
                MediaType.APPLICATION_FORM_URLENCODED, AliyunKd100QueryRespDTO.class);
        validateQueryResponse(response);
        if (CollUtil.isEmpty(response.getTracks())) {
            return Collections.emptyList();
        }
        return INSTANCE.convertList2(response.getTracks());
    }

    @Override
    public void subscribeExpressTrack(ExpressTrackQueryReqDTO reqDTO) {
        validateConfig(true);
        String cacheKey = ExpressTrackCacheKeyUtils.build(reqDTO.getExpressCode(), reqDTO.getLogisticsNo(),
                reqDTO.getPhone());
        String callbackUrl = UriComponentsBuilder.fromUriString(config.getCallbackUrl())
                .queryParam("tenant-id", TenantContextHolder.getRequiredTenantId())
                .queryParam("cache-key", cacheKey)
                .queryParam("token", config.getCallbackToken())
                .build().encode().toUriString();
        AliyunKd100SubscribeReqDTO request = new AliyunKd100SubscribeReqDTO()
                .setCompany(reqDTO.getExpressCode().toLowerCase(Locale.ROOT))
                .setNumber(reqDTO.getLogisticsNo())
                .setParameters(new AliyunKd100SubscribeReqDTO.Parameters()
                        .setCallbackurl(callbackUrl)
                        .setResultv2(config.getResultV2())
                        .setPhone(StrUtil.blankToDefault(reqDTO.getPhone(), null)));
        AliyunKd100SubscribeRespDTO response = post(config.getSubscribeUrl(), request,
                MediaType.APPLICATION_JSON, AliyunKd100SubscribeRespDTO.class);
        if (response == null || !Boolean.TRUE.equals(response.getResult())
                || !SUCCESS_CODE.equals(response.getReturnCode())) {
            throw exception(EXPRESS_API_QUERY_FAILED, response == null ? "订阅接口无响应" : response.getMessage());
        }
    }

    @Override
    public ExpressTrackNotifyRespDTO parseExpressTrackNotify(String payload) {
        JsonNode root = JsonUtils.parseTree(payload);
        JsonNode lastResult = root.path("lastResult");
        if (lastResult.isTextual()) {
            lastResult = JsonUtils.parseTree(lastResult.asText());
        }
        if (lastResult.isMissingNode() || lastResult.isNull()) {
            return new ExpressTrackNotifyRespDTO().setTracks(Collections.emptyList());
        }
        AliyunKd100QueryRespDTO result = JsonUtils.parseObject(lastResult.toString(), AliyunKd100QueryRespDTO.class);
        List<ExpressTrackRespDTO> tracks = CollUtil.isEmpty(result.getTracks())
                ? Collections.emptyList() : INSTANCE.convertList2(result.getTracks());
        return new ExpressTrackNotifyRespDTO().setExpressCode(result.getExpressCode())
                .setLogisticsNo(result.getLogisticsNo()).setState(result.getState()).setTracks(tracks);
    }

    private void validateQueryResponse(AliyunKd100QueryRespDTO response) {
        if (response == null) {
            throw exception(EXPRESS_API_QUERY_ERROR);
        }
        if (Boolean.FALSE.equals(response.getResult())
                || (StrUtil.isNotBlank(response.getStatus()) && !SUCCESS_CODE.equals(response.getStatus()))
                || (StrUtil.isNotBlank(response.getReturnCode()) && !SUCCESS_CODE.equals(response.getReturnCode()))) {
            throw exception(EXPRESS_API_QUERY_FAILED, response.getMessage());
        }
    }

    private <Req, Resp> Resp post(String url, Req requestBody, MediaType contentType, Class<Resp> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        headers.set(HttpHeaders.AUTHORIZATION, "APPCODE " + config.getAppCode());
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers), String.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                throw exception(EXPRESS_API_QUERY_ERROR);
            }
            return JsonUtils.parseObject(responseEntity.getBody(), responseType);
        } catch (RestClientException ex) {
            log.error("[post][调用阿里云快递 100 接口失败，url({})]", url, ex);
            throw exception(EXPRESS_API_QUERY_ERROR);
        }
    }

    private void validateConfig(boolean subscription) {
        if (config == null || StrUtil.isBlank(config.getAppCode())) {
            throw new IllegalStateException("阿里云快递 100 AppCode 未配置");
        }
        if (subscription && (StrUtil.isBlank(config.getCallbackUrl()) || StrUtil.isBlank(config.getCallbackToken()))) {
            throw new IllegalStateException("阿里云快递 100 callback-url 或 callback-token 未配置");
        }
    }

}
