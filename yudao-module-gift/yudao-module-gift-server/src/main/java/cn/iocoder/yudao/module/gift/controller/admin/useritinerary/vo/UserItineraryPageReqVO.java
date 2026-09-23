package cn.iocoder.yudao.module.gift.controller.admin.useritinerary.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用户行程分页 Request VO")
@Data
public class UserItineraryPageReqVO extends PageParam {

    private Long conversationId;

    @Schema(description = "会员ID", example = "5188")
    private Long memberId;

    private Integer version;
    private Integer status;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "封面图", example = "https://www.iocoder.cn")
    private String coverUrl;

    @Schema(description = "宽度")
    private Integer coverWidth;

    @Schema(description = "高度")
    private Integer coverHeight;

    @Schema(description = "开始日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] startDate;

    @Schema(description = "完成日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] endDate;

    @Schema(description = "天数")
    private Integer dayCnt;

    private String destination;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
