package cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/** 用户行程每日安排。 */
@TableName("gift_user_itinerary_day")
@KeySequence("gift_user_itinerary_day_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserItineraryDayDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long userItineraryId;
    private Integer day;
    private LocalDate date;
    private Integer provinceId;
    private Integer cityId;
    private Integer districtId;
    private String city;
    private String area;
    private String theme;
    private String anchorPoiNamesJson;
    private Integer sort;
    private String overviewStatus;
    private String overviewSkeleton;
    private String overviewDetail;
    private String planner;
    private String planningStatus;
    private String macroSource;
    private String selectionStatus;
    private String budgetStatus;
    private Integer requestedScenicCount;
    private Integer selectedScenicCount;
    private LocalTime dayStartTime;
    private LocalTime dayEndTime;
    private String droppedNodeIdsJson;
    private String candidateCountsJson;

}
