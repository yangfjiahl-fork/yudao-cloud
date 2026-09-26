package cn.iocoder.yudao.module.gift.controller.admin.itineraryday.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 通用行程每日安排分页 Request VO")
@Data
public class ItineraryDayPageReqVO extends PageParam {

    @Schema(description = "通用行程ID", example = "3310")
    private Long itineraryId;

    @Schema(description = "行程第几天，从1开始")
    private Integer day;

    @Schema(description = "当日城市ID", example = "23068")
    private Integer cityId;

    @Schema(description = "当日区县ID", example = "24164")
    private Integer districtId;

    @Schema(description = "当日标题，支持模糊匹配")
    private String title;

    @Schema(description = "当日描述", example = "你猜")
    private String description;

    @Schema(description = "排序值")
    private Integer sort;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
