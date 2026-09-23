package cn.iocoder.yudao.module.gift.controller.app.itinerarycategory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 行程类别 Response VO")
@Data
public class AppItineraryCategoryRespVO {

    @Schema(description = "行程类别编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "亲子游")
    private String title;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer sort;

}
