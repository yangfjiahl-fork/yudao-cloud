package cn.iocoder.yudao.module.gift.controller.admin.useritineraryliked.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 收藏行程分页 Request VO")
@Data
public class UserItineraryLikedPageReqVO extends PageParam {

    @Schema(description = "行程ID", example = "12377")
    private Long itineraryId;

    @Schema(description = "会员ID", example = "13449")
    private Long memberId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}