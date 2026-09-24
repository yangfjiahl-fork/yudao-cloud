package cn.iocoder.yudao.module.gift.controller.admin.itineraryday.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 通用行程每日安排 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ItineraryDayRespVO {

    @Schema(description = "通用行程日程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17922")
    @ExcelProperty("通用行程日程ID")
    private Long id;

    @Schema(description = "通用行程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3310")
    @ExcelProperty("通用行程ID")
    private Long itineraryId;

    @Schema(description = "行程第几天，从1开始", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("行程第几天，从1开始")
    private Integer day;

    @Schema(description = "当日城市ID", example = "23068")
    @ExcelProperty("当日城市ID")
    private Integer cityId;

    @Schema(description = "当日区县ID", example = "24164")
    @ExcelProperty("当日区县ID")
    private Integer districtId;

    @Schema(description = "当日标题")
    @ExcelProperty("当日标题")
    private String title;

    @Schema(description = "当日描述", example = "你猜")
    @ExcelProperty("当日描述")
    private String description;

    @Schema(description = "排序值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排序值")
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}