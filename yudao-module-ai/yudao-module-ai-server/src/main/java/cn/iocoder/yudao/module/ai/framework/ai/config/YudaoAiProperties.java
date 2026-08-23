package cn.iocoder.yudao.module.ai.framework.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 芋道 AI 配置类
 *
 * @author fansili
 * @since 1.0
 */
@ConfigurationProperties(prefix = "yudao.ai")
@Data
public class YudaoAiProperties {

    /**
     * 谷歌 Gemini
     */
    private Gemini gemini;

    /**
     * 字节豆包
     */
    private DouBao doubao;

    /**
     * 腾讯混元
     */
    private HunYuan hunyuan;

    /**
     * 硅基流动
     */
    private SiliconFlow siliconflow;

    /**
     * 讯飞星火
     */
    private XingHuo xinghuo;

    /**
     * 百川
     */
    private BaiChuan baichuan;

    /**
     * 文心一言
     */
    private YiYan yiyan;

    /**
     * 智谱
     */
    private ZhiPu zhipu;

    /**
     * MiniMax
     */
    private MiniMax minimax;

    /**
     * 月之暗面
     */
    private Moonshot moonshot;

    /**
     * 阶跃星辰
     */
    private StepFun stepfun;

    /**
     * Grok
     */
    private Grok grok;

    /**
     * Midjourney 绘图
     */
    private Midjourney midjourney;

    /**
     * Suno 音乐
     */
    @SuppressWarnings("SpellCheckingInspection")
    private Suno suno;

    /**
     * 网络搜索
     */
    private WebSearch webSearch;

    /**
     * 天气查询
     */
    private Weather weather = new Weather();

    /**
     * 全国景点查询
     */
    private ScenicSpot scenicSpot;

    @Data
    public static class Gemini {

        private String enable;
        private String apiKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class DouBao {

        private String enable;
        private String apiKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class HunYuan {

        private String enable;
        private String baseUrl;
        private String apiKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class SiliconFlow {

        private String enable;
        private String apiKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class XingHuo {

        private String enable;
        private String apiKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class BaiChuan {

        private String enable;
        private String apiKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class YiYan {

        private String enable;
        private String baseUrl;
        private String apiKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class ZhiPu {

        private String enable;
        private String baseUrl;
        private String apiKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class MiniMax {

        private String enable;
        private String baseUrl;
        private String apiKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class Moonshot {

        private String enable;
        private String baseUrl;
        private String apiKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class StepFun {

        private String enable;
        private String apiKey;
        private String baseUrl;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class Midjourney {

        private String enable;
        private String baseUrl;

        private String apiKey;
        private String notifyUrl;

    }

    @Data
    public static class Suno {

        private boolean enable;

        private String baseUrl;

    }

    @Data
    public static class Grok {

        private String enable;
        private String apiKey;
        private String baseUrl;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class WebSearch {

        private boolean enable;

        private String apiKey;

    }

    @Data
    public static class Weather {

        /**
         * 阿里云市场分配的 AppCode
         */
        private String appCode;

        /**
         * 地名查询天气预报接口地址
         */
        private String url = "https://ali-weather.showapi.com/area-to-weather";

        /**
         * 高德天气配置
         */
        private Amap amap = new Amap();

        @Data
        public static class Amap {

            /**
             * 高德 Web 服务 API Key
             */
            private String apiKey;

            /**
             * 地理编码接口地址
             */
            private String geocodeUrl = "https://restapi.amap.com/v3/geocode/geo";

            /**
             * 天气查询接口地址
             */
            private String weatherUrl = "https://restapi.amap.com/v3/weather/weatherInfo";

        }

    }

    @Data
    public static class ScenicSpot {

        private String baseUrl;

        private String appCode;

        private String amapUrl = "https://restapi.amap.com/v5/place/text";

        private String amapKey;

        /**
         * 高德 POI 分类编码，110000 为风景名胜
         */
        private String amapTypes = "110000";

    }

}
