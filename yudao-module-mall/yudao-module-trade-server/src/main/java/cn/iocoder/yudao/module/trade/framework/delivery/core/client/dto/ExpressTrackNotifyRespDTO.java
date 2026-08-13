package cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto;

import lombok.Data;

import java.util.List;

/**
 * 快递轨迹通知 Resp DTO
 */
@Data
public class ExpressTrackNotifyRespDTO {

    /**
     * 快递公司编码
     */
    private String expressCode;
    /**
     * 快递单号
     */
    private String logisticsNo;
    /**
     * 快递当前状态
     */
    private String state;
    /**
     * 物流轨迹
     */
    private List<ExpressTrackRespDTO> tracks;

}
