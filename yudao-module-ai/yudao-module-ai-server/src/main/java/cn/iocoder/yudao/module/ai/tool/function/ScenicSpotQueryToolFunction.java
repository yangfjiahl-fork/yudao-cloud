package cn.iocoder.yudao.module.ai.tool.function;

import cn.iocoder.yudao.module.ai.framework.ai.core.scenic.ScenicSpotQueryClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.scenic.ScenicSpotQueryClientFacade;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

/**
 * 工具：查询全国景点及其行政区划信息
 */
@RequiredArgsConstructor
public class ScenicSpotQueryToolFunction
        implements Function<ScenicSpotQueryClient.Request, ScenicSpotQueryClient.Response> {

    private final ScenicSpotQueryClientFacade clientFacade;

    @Override
    public ScenicSpotQueryClient.Response apply(ScenicSpotQueryClient.Request request) {
        return clientFacade.query(request);
    }

}
