package cn.iocoder.yudao.module.gift.controller.admin.itineraryday.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 通用行程每日安排新增/修改 Request VO")
@Data
public class ItineraryDaySaveReqVO {

    @Schema(description = "通用行程日程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17922")
    private Long id;

    @Schema(description = "通用行程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3310")
    @NotNull(message = "通用行程ID不能为空")
    private Long itineraryId;

    @Schema(description = "行程第几天，从1开始", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "行程第几天，从1开始不能为空")
    private Integer day;

    @Schema(description = "当日城市ID", example = "23068")
    private Integer cityId;

    @Schema(description = "当日区县ID", example = "24164")
    private Integer districtId;

    @Schema(description = "当日标题")
    private String title;

    @Schema(description = "当日描述", example = "你猜")
    private String description;

    @Schema(description = "排序值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序值不能为空")
    private Integer sort;

}