package cn.iocoder.yudao.module.gift.controller.app.itinerary.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AppItinerarySlotResolveRespVO {

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
    private AppItineraryWeatherRespVO weather;
    private List<Map<String, Object>> transportSegments;

}
