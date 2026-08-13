package cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl.aliyunkd100.dto;

import lombok.Data;

/**
 * 阿里云快递 100 订阅 Resp DTO
 */
@Data
public class AliyunKd100SubscribeRespDTO {

    private Boolean result;
    private String returnCode;
    private String message;

}
