package cn.iocoder.yudao.module.gift.controller.admin.slideritem.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.gift.enums.SliderItemJumpPageEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 轮播图新增/修改 Request VO")
@Data
public class SliderItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "26589")
    private Long id;

    @Schema(description = "轮播ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17759")
    @NotNull(message = "轮播ID不能为空")
    private Long sliderId;

    @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @NotEmpty(message = "图片地址不能为空")
    private String imageUrl;

    @Schema(description = "图片宽度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "图片宽度不能为空")
    private Integer imageWidth;

    @Schema(description = "图片高度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "图片高度不能为空")
    private Integer imageHeight;

    @Schema(description = "顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "顺序不能为空")
    private Integer sort;

    @Schema(description = "跳转页面", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = "SHARE", example = "SHARE")
    @NotEmpty(message = "跳转页面不能为空")
    @InEnum(value = SliderItemJumpPageEnum.class, message = "跳转页面必须是 {value}")
    private String jumpPage;

    @Schema(description = "跳转页面ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "32220")
    @NotNull(message = "跳转页面ID不能为空")
    private Long jumpPageId;

}
