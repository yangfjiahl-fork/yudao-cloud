package cn.iocoder.yudao.module.gift.controller.admin.slider.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.gift.enums.SliderPositionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 轮播新增/修改 Request VO")
@Data
public class SliderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4841")
    private Long id;

    @Schema(description = "轮播位置", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = "HOME_TOP", example = "HOME_TOP")
    @NotEmpty(message = "轮播位置不能为空")
    @InEnum(value = SliderPositionEnum.class, message = "轮播位置必须是 {value}")
    private String positionCode;

    @Schema(description = "城市ID（可选）", example = "16158")
    private Long cityId;

}
