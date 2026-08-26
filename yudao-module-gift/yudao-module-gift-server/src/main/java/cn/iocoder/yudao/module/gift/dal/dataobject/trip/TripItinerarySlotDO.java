package cn.iocoder.yudao.module.gift.dal.dataobject.trip;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 旅行行程的独立补充节点。
 */
@TableName("gift_trip_itinerary_slot")
@KeySequence("gift_trip_itinerary_slot_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class TripItinerarySlotDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long itineraryId;
    private Integer day;
    private String slot;
    private String skeleton;
    private String status;
    private Integer resolveStatus;
    private String detail;
    private String candidatesJson;
    private String citationIdsJson;

}
