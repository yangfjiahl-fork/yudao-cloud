package cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl.aliyunkd100.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 阿里云快递 100 订阅 Req DTO
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AliyunKd100SubscribeReqDTO {

    private String company;
    private String number;
    private Parameters parameters;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Parameters {

        private String callbackurl;
        private String resultv2;
        private String phone;

    }

}
