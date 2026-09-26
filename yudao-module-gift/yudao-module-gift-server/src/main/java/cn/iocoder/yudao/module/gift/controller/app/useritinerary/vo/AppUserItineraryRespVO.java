package cn.iocoder.yudao.module.gift.controller.app.useritinerary.vo;

import cn.iocoder.yudao.module.gift.controller.common.vo.UserItineraryPageRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "用户 APP - 用户行程详情 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppUserItineraryRespVO extends UserItineraryPageRespVO {

    @Schema(description = "出行人数", example = "4")
    private Integer travelerCount;

    @Schema(description = "总预算", example = "20000")
    private Integer budget;

    @Schema(description = "酒店预算", example = "5000")
    private Integer hotelBudget;

    @Schema(description = "出行人画像 JSON")
    private String travelerProfileJson;

    @Schema(description = "兴趣偏好 JSON")
    private String interestsJson;

    @Schema(description = "行程节奏")
    private String pace;

    @Schema(description = "必去地点 JSON")
    private String mustVisitJson;

    @Schema(description = "其他约束 JSON")
    private String constraintsJson;

    @Schema(description = "每日开始时间")
    private LocalTime dailyStartTime;

    @Schema(description = "每日结束时间")
    private LocalTime dailyEndTime;

    @Schema(description = "行程总览状态")
    private String overviewStatus;

    @Schema(description = "行程总览骨架")
    private String overviewSkeleton;

    @Schema(description = "行程总览详情")
    private String overviewDetail;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
