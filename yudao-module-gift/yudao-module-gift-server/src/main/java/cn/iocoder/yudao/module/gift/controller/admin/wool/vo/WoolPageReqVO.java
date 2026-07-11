package cn.iocoder.yudao.module.gift.controller.admin.wool.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 羊毛分页 Request VO")
@Data
public class WoolPageReqVO extends PageParam {

    @Schema(description = "业务场景", example = "REGISTER")
    private String bizType;

    @Schema(description = "业务编号", example = "27723")
    private String bizId;

    @Schema(description = "状态", example = "INIT")
    private String status;

    @Schema(description = "会员编号", example = "289")
    private Long memberId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}