package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AppTripItinerarySlotResolveRespVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long messageId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long slotId;
    private Integer day;
    private String slot;
    private String status;
    private String detail;
    private List<Map<String, Object>> candidates;
    private List<String> citationIds;

}
