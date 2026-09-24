package cn.iocoder.yudao.module.gift.service.itinerary.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/** 行程节点补充过程中的临时状态，不对应独立数据表。 */
@Data
@Accessors(chain = true)
public class ItinerarySlotState {

    private Long id;
    private Long itineraryId;
    private Integer day;
    private String slot;
    private String skeleton;
    private String poiId;
    private String status;
    private Integer resolveStatus;
    private String detail;
    private String candidatesJson;
    private String citationIdsJson;

}
