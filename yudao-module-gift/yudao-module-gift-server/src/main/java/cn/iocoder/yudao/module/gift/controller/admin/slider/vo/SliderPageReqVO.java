package cn.iocoder.yudao.module.gift.controller.admin.slider.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.gift.enums.SliderPositionEnum;
import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 轮播分页 Request VO")
@Data
public class SliderPageReqVO extends PageParam {

    @Schema(description = "轮播位置", allowableValues = "HOME_TOP", example = "HOME_TOP")
    @InEnum(value = SliderPositionEnum.class, message = "轮播位置必须是 {value}")
    private String positionCode;

    @Schema(description = "城市ID（可选）", example = "16158")
    private Long cityId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
