package cn.iocoder.yudao.module.ai.framework.ai.core.scenic;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.scenic.aliyun.AliyunScenicSpotQueryClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.scenic.gaode.GaodeScenicSpotQueryClient;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import lombok.AllArgsConstructor;

/**
 * 景点查询客户端门面，根据系统配置选择具体服务
 */
@AllArgsConstructor
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
        String queryFrom = configApi.getConfigValueByKey(QUERY_FROM_CONFIG_KEY).getCheckedData();
        ScenicSpotQueryClient client = QUERY_FROM_GAODE.equalsIgnoreCase(StrUtil.trim(queryFrom))
                ? gaodeClient : aliyunClient;
        return client.query(request);
    }

}
