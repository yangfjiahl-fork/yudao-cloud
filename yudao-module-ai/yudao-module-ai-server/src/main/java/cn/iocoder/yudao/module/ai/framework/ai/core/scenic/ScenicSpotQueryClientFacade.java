package cn.iocoder.yudao.module.ai.framework.ai.core.scenic;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.scenic.aliyun.AliyunScenicSpotQueryClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.scenic.gaode.GaodeScenicSpotQueryClient;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 景点查询客户端门面，根据系统配置选择具体服务
 */
@AllArgsConstructor
@Slf4j
public class ScenicSpotQueryClientFacade {

    public static final String QUERY_FROM_CONFIG_KEY = "scenario.query.from";
    private static final String QUERY_FROM_GAODE = "gaode";

    private final ConfigApi configApi;
    private final AliyunScenicSpotQueryClient aliyunClient;
    private final GaodeScenicSpotQueryClient gaodeClient;

    /**
     * 系统配置 {@value #QUERY_FROM_CONFIG_KEY} 为 gaode 时使用高德，其余情况使用阿里云。
     */
    public ScenicSpotQueryClient.Response query(ScenicSpotQueryClient.Request request) {
        String queryFrom;
        try {
            queryFrom = configApi.getConfigValueByKey(QUERY_FROM_CONFIG_KEY).getCheckedData();
        } catch (RuntimeException ex) {
            log.error("[query][读取景点查询来源配置失败，key({})]", QUERY_FROM_CONFIG_KEY, ex);
            throw ex;
        }
        boolean useGaode = QUERY_FROM_GAODE.equalsIgnoreCase(StrUtil.trim(queryFrom));
        ScenicSpotQueryClient client = useGaode ? gaodeClient : aliyunClient;
        log.info("[query][景点查询路由，key({}) value({}) client({}) type({}) keyword({}) region({}) page({})]",
                QUERY_FROM_CONFIG_KEY, queryFrom, useGaode ? "gaode" : "aliyun",
                request == null ? null : request.getType(), request == null ? null : request.getKeyword(),
                request == null ? null : request.getRegion(), request == null ? null : request.getPage());
        return client.query(request);
    }

}
