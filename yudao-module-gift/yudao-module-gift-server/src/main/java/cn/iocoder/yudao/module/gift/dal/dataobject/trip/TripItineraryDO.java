package cn.iocoder.yudao.module.gift.dal.dataobject.trip;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("gift_trip_itinerary")
@KeySequence("gift_trip_itinerary_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class TripItineraryDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long tripId;
    private String contentJson;
    private String citationIdsJson;
    private Integer status;

}
