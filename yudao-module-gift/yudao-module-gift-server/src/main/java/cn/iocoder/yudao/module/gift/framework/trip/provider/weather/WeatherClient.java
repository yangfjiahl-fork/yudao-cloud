package cn.iocoder.yudao.module.gift.framework.trip.provider.weather;

/**
 * 天气客户端
 */
public interface WeatherClient {

    /**
     * 获得服务提供商
     */
    WeatherProvider getProvider();

    /**
     * 是否已配置完成
     */
    boolean isConfigured();

    /**
     * 查询当前天气
     *
     * @param city 城市或行政区名称、行政区编码
     * @return 当前天气
     */
    CurrentWeather getCurrentWeather(String city);

    record CurrentWeather(
            String city,
            Integer temperature,
            String condition,
            Integer humidity,
            String windDirection,
            String windPower,
            String queryTime) {
    }

}
