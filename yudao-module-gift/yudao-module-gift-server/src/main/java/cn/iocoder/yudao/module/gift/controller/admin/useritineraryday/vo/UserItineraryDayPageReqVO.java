package cn.iocoder.yudao.module.gift.controller.admin.useritineraryday.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用户行程每日安排分页 Request VO")
@Data
public class UserItineraryDayPageReqVO extends PageParam {

    @Schema(description = "用户行程ID", example = "4063")
    private Long userItineraryId;

    @Schema(description = "行程第几天，从1开始")
    private Integer day;

    @Schema(description = "行程日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] date;

    @Schema(description = "当日省级区域ID", example = "10473")
    private Integer provinceId;

    @Schema(description = "当日城市ID", example = "31395")
    private Integer cityId;

    @Schema(description = "当日区县ID", example = "15735")
    private Integer districtId;

    @Schema(description = "当日城市名称")
    private String city;

    @Schema(description = "当日游玩区域")
    private String area;

    @Schema(description = "当日行程主题")
    private String theme;

    @Schema(description = "当日锚点POI名称JSON")
    private String anchorPoiNamesJson;

    @Schema(description = "排序值")
    private Integer sort;

    @Schema(description = "每日总览生成状态", example = "2")
    private String overviewStatus;

    @Schema(description = "每日总览骨架文案")
    private String overviewSkeleton;

    @Schema(description = "每日总览详细文案")
    private String overviewDetail;

    @Schema(description = "当日规划器")
    private String planner;

    @Schema(description = "当日排程状态", example = "1")
    private String planningStatus;

    @Schema(description = "宏观路线来源")
    private String macroSource;

    @Schema(description = "景点选择状态", example = "1")
    private String selectionStatus;

    @Schema(description = "预算校验状态", example = "2")
    private String budgetStatus;

    @Schema(description = "期望景点数量", example = "28139")
    private Integer requestedScenicCount;

    @Schema(description = "实际选择景点数量", example = "16532")
    private Integer selectedScenicCount;

    @Schema(description = "当日开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalTime[] dayStartTime;

    @Schema(description = "当日结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalTime[] dayEndTime;

    @Schema(description = "排程丢弃的节点ID JSON")
    private String droppedNodeIdsJson;

    @Schema(description = "各类候选数量JSON")
    private String candidateCountsJson;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
