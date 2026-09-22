package cn.iocoder.yudao.module.gift.controller.admin.slider.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 轮播新增/修改 Request VO")
@Data
public class SliderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4841")
    private Long id;

    @Schema(description = "轮播位置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "轮播位置不能为空")
    private String positionCode;

    @Schema(description = "城市ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "16158")
    @NotNull(message = "城市ID不能为空")
    private Long cityId;

}