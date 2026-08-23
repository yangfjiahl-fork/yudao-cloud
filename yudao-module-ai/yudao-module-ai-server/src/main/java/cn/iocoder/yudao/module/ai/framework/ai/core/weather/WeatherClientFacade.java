package cn.iocoder.yudao.module.ai.framework.ai.core.weather;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.weather.amap.AmapWeatherClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.weather.aliyun.AliyunWeatherClient;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 天气客户端门面，根据系统配置选择具体服务
 */
@AllArgsConstructor
@Slf4j
public class WeatherClientFacade {

    public static final String QUERY_FROM_CONFIG_KEY = "weather.query.from";
    private static final String QUERY_FROM_GAODE = "gaode";

    private final ConfigApi configApi;
    private final AliyunWeatherClient aliyunWeatherClient;
    private final AmapWeatherClient amapWeatherClient;

    /**
     * 查询当前天气。系统配置 {@value #QUERY_FROM_CONFIG_KEY} 为 gaode 时使用高德，其余情况使用阿里云。
     *
     * @param city 城市或行政区名称、行政区编码
     * @return 当前天气
     */
    public WeatherClient.CurrentWeather getCurrentWeather(String city) {
        String queryFrom = configApi.getConfigValueByKey(QUERY_FROM_CONFIG_KEY).getCheckedData();
        boolean useGaode = QUERY_FROM_GAODE.equalsIgnoreCase(StrUtil.trim(queryFrom));
        WeatherClient client = useGaode ? amapWeatherClient : aliyunWeatherClient;
        log.info("[getCurrentWeather][读取天气来源配置，key({}) value({}) provider({}) city({})]",
                QUERY_FROM_CONFIG_KEY, queryFrom, useGaode ? "gaode" : "aliyun", city);
        return client.getCurrentWeather(city);
    }

}
