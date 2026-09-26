package cn.iocoder.yudao.module.gift.controller.app.useritinerary.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "用户 APP - 用户行程分页项 Response VO")
@Data
public class AppUserItineraryRespVO {

    @Schema(description = "行程编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "会话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long conversationId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "云南亲子六日游")
    private String title;

    @Schema(description = "封面图地址", example = "https://example.com/cover.jpg")
    private String coverUrl;

    @Schema(description = "封面宽度", example = "750")
    private Integer coverWidth;

    @Schema(description = "封面高度", example = "420")
    private Integer coverHeight;

    @Schema(description = "开始日期", example = "2026-10-01")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2026-10-06")
    private LocalDate endDate;

    @Schema(description = "行程天数", example = "6")
    private Integer dayCnt;

    @Schema(description = "出发地", example = "上海市")
    private String departure;

    @Schema(description = "目的地", example = "云南省")
    private String destination;

    @Schema(description = "出行人数", example = "4")
    private Integer travelerCount;

    @Schema(description = "总预算", example = "20000")
    private Integer budget;

    @Schema(description = "酒店预算", example = "5000")
    private Integer hotelBudget;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
