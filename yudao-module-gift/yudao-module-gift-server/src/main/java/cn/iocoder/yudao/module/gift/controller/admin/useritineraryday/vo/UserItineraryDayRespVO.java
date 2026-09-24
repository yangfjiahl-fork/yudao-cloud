package cn.iocoder.yudao.module.gift.controller.admin.useritineraryday.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 用户行程每日安排 Response VO")
@Data
@ExcelIgnoreUnannotated
public class UserItineraryDayRespVO {

    @Schema(description = "用户行程日程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17562")
    @ExcelProperty("用户行程日程ID")
    private Long id;

    @Schema(description = "用户行程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "4063")
    @ExcelProperty("用户行程ID")
    private Long userItineraryId;

    @Schema(description = "行程第几天，从1开始", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("行程第几天，从1开始")
    private Integer day;

    @Schema(description = "行程日期")
    @ExcelProperty("行程日期")
    private LocalDate date;

    @Schema(description = "当日省级区域ID", example = "10473")
    @ExcelProperty("当日省级区域ID")
    private Integer provinceId;

    @Schema(description = "当日城市ID", example = "31395")
    @ExcelProperty("当日城市ID")
    private Integer cityId;

    @Schema(description = "当日区县ID", example = "15735")
    @ExcelProperty("当日区县ID")
    private Integer districtId;

    @Schema(description = "当日城市名称")
    @ExcelProperty("当日城市名称")
    private String city;

    @Schema(description = "当日游玩区域")
    @ExcelProperty("当日游玩区域")
    private String area;

    @Schema(description = "当日行程主题")
    @ExcelProperty("当日行程主题")
    private String theme;

    @Schema(description = "当日锚点POI名称JSON")
    @ExcelProperty("当日锚点POI名称JSON")
    private String anchorPoiNamesJson;

    @Schema(description = "排序值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排序值")
    private Integer sort;

    @Schema(description = "每日总览生成状态", example = "2")
    @ExcelProperty("每日总览生成状态")
    private String overviewStatus;

    @Schema(description = "每日总览骨架文案")
    @ExcelProperty("每日总览骨架文案")
    private String overviewSkeleton;

    @Schema(description = "每日总览详细文案")
    @ExcelProperty("每日总览详细文案")
    private String overviewDetail;

    @Schema(description = "当日规划器")
    @ExcelProperty("当日规划器")
    private String planner;

    @Schema(description = "当日排程状态", example = "1")
    @ExcelProperty("当日排程状态")
    private String planningStatus;

    @Schema(description = "宏观路线来源")
    @ExcelProperty("宏观路线来源")
    private String macroSource;

    @Schema(description = "景点选择状态", example = "1")
    @ExcelProperty("景点选择状态")
    private String selectionStatus;

    @Schema(description = "预算校验状态", example = "2")
    @ExcelProperty("预算校验状态")
    private String budgetStatus;

    @Schema(description = "期望景点数量", example = "28139")
    @ExcelProperty("期望景点数量")
    private Integer requestedScenicCount;

    @Schema(description = "实际选择景点数量", example = "16532")
    @ExcelProperty("实际选择景点数量")
    private Integer selectedScenicCount;

    @Schema(description = "当日开始时间")
    @ExcelProperty("当日开始时间")
    private LocalTime dayStartTime;

    @Schema(description = "当日结束时间")
    @ExcelProperty("当日结束时间")
    private LocalTime dayEndTime;

    @Schema(description = "排程丢弃的节点ID JSON")
    @ExcelProperty("排程丢弃的节点ID JSON")
    private String droppedNodeIdsJson;

    @Schema(description = "各类候选数量JSON")
    @ExcelProperty("各类候选数量JSON")
    private String candidateCountsJson;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
