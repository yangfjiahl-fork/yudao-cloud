package cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 用户行程相邻节点之间的交通段。 */
@TableName("gift_user_itinerary_transport_segment")
@KeySequence("gift_user_itinerary_transport_segment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserItineraryTransportSegmentDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long userItineraryId;
    private Long userItineraryDayId;
    private Integer day;
    private Integer sort;
    private String fromItemId;
    private String fromPoiId;
    private String fromPoiName;
    private BigDecimal fromLongitude;
    private BigDecimal fromLatitude;
    private String toItemId;
    private String toPoiId;
    private String toPoiName;
    private BigDecimal toLongitude;
    private BigDecimal toLatitude;
    private String coordinateSystem;
    private String mode;
    private Long distanceMeters;
    private Integer durationMinutes;
    private String provider;
    private String status;
    private String routePointsJson;

}
