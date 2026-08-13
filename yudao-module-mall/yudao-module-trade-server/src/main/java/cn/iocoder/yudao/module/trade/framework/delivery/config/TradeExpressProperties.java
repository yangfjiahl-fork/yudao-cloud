package cn.iocoder.yudao.module.trade.framework.delivery.config;

import cn.iocoder.yudao.module.trade.framework.delivery.core.enums.ExpressClientEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

// TODO @芋艿：未来要不要放数据库中？考虑 saas 多租户时，不同租户使用不同的配置？
/**
 * 交易运费快递的配置项
 *
 * @author jason
 */
@Component
@ConfigurationProperties(prefix = "yudao.trade.express")
@Data
@Validated
public class TradeExpressProperties {

    /**
     * 快递客户端
     *
     * 默认不提供，需要提醒用户配置一个快递服务商。
     */
    private ExpressClientEnum client = ExpressClientEnum.NOT_PROVIDE;

    /**
     * 快递鸟配置
     */
    @Valid
    private KdNiaoConfig kdNiao;
    /**
     * 快递 100 配置
     */
    @Valid
    private Kd100Config kd100;
    /**
     * 阿里云快递 100 配置
     */
    @Valid
    private AliyunKd100Config aliyunKd100;

    /**
     * 快递鸟配置项目
     */
    @Data
    public static class KdNiaoConfig {

        /**
         * 快递鸟用户 ID
         */
        @NotEmpty(message = "快递鸟用户 ID 配置项不能为空")
        private String businessId;
        /**
         * 快递鸟 API Key
         */
        @NotEmpty(message = "快递鸟 Api Key 配置项不能为空")
        private String apiKey;

        /**
         * 接口指令
         *
         * 1. 1002：免费版（只能查询申通、圆通快递）
         * 2. 8001：付费版
         */
        @NotEmpty(message = "RequestType 配置项不能为空")
        private String requestType = "1002";

    }

    /**
     * 快递 100 配置项
     */
    @Data
    public static class Kd100Config {

        /**
         * 快递 100 授权码
         */
        @NotEmpty(message = "快递 100 授权码配置项不能为空")
        private String customer;
        /**
         * 快递 100 授权 key
         */
        @NotEmpty(message = "快递 100 授权 Key 配置项不能为空")
        private String key;

    }

    /**
     * 阿里云快递 100 配置项
     */
    @Data
    public static class AliyunKd100Config {

        /**
         * 阿里云市场分配的 AppCode
         */
        private String appCode;
        /**
         * 实时查询接口地址
         */
        private String queryUrl = "https://kdapi.kuaidi100.com/test/poll/query";
        /**
         * 订阅接口地址
         */
        private String subscribeUrl = "https://kd100api.kuaidi100.com/express/poll";
        /**
         * 接收物流动态通知的公网地址
         */
        private String callbackUrl;
        /**
         * 回调地址校验令牌
         */
        private String callbackToken;
        /**
         * 订阅结果版本：0 基础轨迹；4 高级状态；8 时效预测
         */
        private String resultV2 = "0";

    }

}
