package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AppTripItineraryRouteResolveRespVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long messageId;
    private Integer day;
    private String status;
    private List<Map<String, Object>> transportSegments;

}
