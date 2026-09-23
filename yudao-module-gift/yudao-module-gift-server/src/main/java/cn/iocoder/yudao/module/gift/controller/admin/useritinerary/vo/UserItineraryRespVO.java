package cn.iocoder.yudao.module.gift.controller.admin.useritinerary.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 用户行程 Response VO")
@Data
@ExcelIgnoreUnannotated
public class UserItineraryRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "11003")
    @ExcelProperty("主键")
    private Long id;

    @ExcelProperty("会话ID")
    private Long conversationId;

    private Long requestEventId;
    private Long resultEventId;

    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5188")
    @ExcelProperty("会员ID")
    private Long memberId;

    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("标题")
    private String title;

    @Schema(description = "封面图", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @ExcelProperty("封面图")
    private String coverUrl;

    @Schema(description = "宽度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("宽度")
    private Integer coverWidth;

    @Schema(description = "高度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("高度")
    private Integer coverHeight;

    @Schema(description = "开始日期")
    @ExcelProperty("开始日期")
    private LocalDate startDate;

    @Schema(description = "完成日期")
    @ExcelProperty("完成日期")
    private LocalDate endDate;

    @Schema(description = "天数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("天数")
    private Integer dayCnt;

    private String departure;
    @ExcelProperty("目的地")
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

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
