package cn.iocoder.yudao.module.trade.controller.admin.delivery;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.express.AliyunKd100NotifyRespVO;
import cn.iocoder.yudao.module.trade.service.delivery.ExpressSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "管理后台 - 阿里云快递100通知")
@RestController
@RequestMapping("/trade/express/aliyun-kd100")
@Validated
public class AdminAliyunKd100ExpressController {

    @Resource
    private ExpressSubscriptionService expressSubscriptionService;

    @PostMapping("/notify")
    @Operation(summary = "接收阿里云快递100物流动态通知")
    @PermitAll
    public AliyunKd100NotifyRespVO notifyExpressTrack(@RequestParam("tenant-id") Long tenantId,
                                                      @RequestParam("cache-key") String cacheKey,
                                                      @RequestParam("token") String token,
                                                      HttpServletRequest request) {
        String payload = getNotifyPayload(request);
        expressSubscriptionService.processNotify(tenantId, cacheKey, token, payload);
        return AliyunKd100NotifyRespVO.success();
    }

    private String getNotifyPayload(HttpServletRequest request) {
        String payload = request.getParameter("param");
        if (StrUtil.isNotBlank(payload)) {
            return payload;
        }
        String body = ServletUtils.getBody(request);
        if (JsonUtils.isJsonObject(body)) {
            return body;
        }
        // 兼容文档中直接以表单字段推送 status、message、lastResult 的形式
        Map<String, Object> params = new LinkedHashMap<>();
        request.getParameterMap().forEach((key, values) -> params.put(key, values.length > 0 ? values[0] : null));
        params.remove("tenant-id");
        params.remove("cache-key");
        params.remove("token");
        if (params.size() == 1 && JsonUtils.isJsonObject(params.keySet().iterator().next())) {
            return params.keySet().iterator().next();
        }
        String lastResult = MapUtil.getStr(params, "lastResult");
        if (JsonUtils.isJsonObject(lastResult)) {
            params.put("lastResult", JsonUtils.parseMap(lastResult));
        }
        return JsonUtils.toJsonString(params);
    }

}
