package cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl.aliyunkd100.dto;

import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.kd100.Kd100ExpressQueryRespDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 阿里云快递 100 实时查询 Resp DTO
 */
@Data
public class AliyunKd100QueryRespDTO {

    @JsonProperty("com")
    private String expressCode;
    @JsonProperty("nu")
    private String logisticsNo;
    private String status;
    private String state;
    private Boolean result;
    private String returnCode;
    private String message;
    @JsonProperty("data")
    private List<Kd100ExpressQueryRespDTO.ExpressTrack> tracks;

}
