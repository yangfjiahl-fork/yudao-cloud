package cn.iocoder.yudao.module.ai.tool.function;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.weather.WeatherClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.weather.WeatherClientFacade;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

/**
 * 工具：查询指定城市的天气信息
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
public class WeatherQueryToolFunction
        implements Function<WeatherQueryToolFunction.Request, WeatherQueryToolFunction.Response> {

    private final WeatherClientFacade weatherClientFacade;

    @Data
    @JsonClassDescription("查询指定城市的天气信息")
    public static class Request {

        /**
         * 城市名称
         */
        @JsonProperty(required = true, value = "city")
        @JsonPropertyDescription("城市名称，例如：北京、上海、广州")
        private String city;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {

        /**
         * 城市名称
         */
        private String city;

        /**
         * 天气信息
         */
        private WeatherInfo weatherInfo;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class WeatherInfo {

            /**
             * 温度（摄氏度）
             */
            private Integer temperature;

            /**
             * 天气状况
             */
            private String condition;

            /**
             * 湿度百分比
             */
            private Integer humidity;

            /**
             * 风向
             */
            private String windDirection;

            /**
             * 风力
             */
            private String windPower;

            /**
             * 查询时间
             */
            private String queryTime;

        }

    }

    @Override
    public Response apply(Request request) {
        // 检查城市名称是否为空
        if (request == null || StrUtil.isBlank(request.getCity())) {
            return new Response("未知城市", null);
        }

        // 获取天气数据
        WeatherClient.CurrentWeather weather = weatherClientFacade.getCurrentWeather(request.getCity().trim());
        Response.WeatherInfo weatherInfo = new Response.WeatherInfo(weather.temperature(), weather.condition(),
                weather.humidity(), weather.windDirection(), weather.windPower(), weather.queryTime());
        return new Response(weather.city(), weatherInfo);
    }

}
