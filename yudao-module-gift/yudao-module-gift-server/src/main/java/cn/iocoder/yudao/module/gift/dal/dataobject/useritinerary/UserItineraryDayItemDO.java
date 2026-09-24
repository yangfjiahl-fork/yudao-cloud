package cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalTime;

/** 用户行程节点，包括游玩、餐饮、住宿及到达/返程节点。 */
@TableName("gift_user_itinerary_day_item")
@KeySequence("gift_user_itinerary_day_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserItineraryDayItemDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long userItineraryId;
    private Long userItineraryDayId;
    private String itemId;
    private Integer day;
    private String type;
    private String slot;
    private String label;
    private Integer sort;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;
    private String poiId;
    private String poiName;
    private Integer provinceId;
    private Integer cityId;
    private Integer districtId;
    private String city;
    private String area;
    private String addressDetail;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String coordinateSystem;
    private String businessHours;
    private String phoneNo;
    private String coverUrl;
    private BigDecimal rating;
    private BigDecimal cost;
    private String tagsJson;
    private String skeleton;
    private String detail;
    private String status;
    private Integer resolveStatus;
    private String planningStatus;
    private String poiVerificationStatus;
    private Boolean mustVisit;
    private Boolean locked;
    private String source;
    private String provider;
    private String poiSnapshotJson;
    private String candidatesJson;
    private String citationIdsJson;

}
