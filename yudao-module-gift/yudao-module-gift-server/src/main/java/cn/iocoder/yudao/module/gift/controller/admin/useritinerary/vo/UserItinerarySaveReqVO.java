package cn.iocoder.yudao.module.gift.controller.admin.useritinerary.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 用户行程新增/修改 Request VO")
@Data
public class UserItinerarySaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "11003")
    private Long id;

    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    private Long requestEventId;
    private Long resultEventId;

    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5188")
    @NotNull(message = "会员ID不能为空")
    private Long memberId;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "标题不能为空")
    private String title;

    @Schema(description = "封面图", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @NotEmpty(message = "封面图不能为空")
    private String coverUrl;

    @Schema(description = "宽度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "宽度不能为空")
    private Integer coverWidth;

    @Schema(description = "高度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "高度不能为空")
    private Integer coverHeight;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "完成日期")
    private LocalDate endDate;

    @Schema(description = "天数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "天数不能为空")
    private Integer dayCnt;

    private String departure;
    private String destination;
    private Integer travelerCount;
    private Integer budget;
    private Integer hotelBudget;
    private String travelerProfileJson;
    private String interestsJson;
    private String pace;
    private String mustVisitJson;
    private String constraintsJson;
    private LocalTime dailyStartTime;
    private LocalTime dailyEndTime;
    private String overviewStatus;
    private String overviewSkeleton;
    private String overviewDetail;
    private String plannerType;
    private String plannerValidation;

}
