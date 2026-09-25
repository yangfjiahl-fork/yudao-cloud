package cn.iocoder.yudao.module.gift.controller.app.slider.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 轮播图 Response VO")
@Data
public class AppSliderItemRespVO {

    @Schema(description = "轮播图编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://example.com/banner.png")
    private String imageUrl;

    @Schema(description = "图片宽度", requiredMode = Schema.RequiredMode.REQUIRED, example = "750")
    private Integer imageWidth;

    @Schema(description = "图片高度", requiredMode = Schema.RequiredMode.REQUIRED, example = "320")
    private Integer imageHeight;

    @Schema(description = "顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "跳转页面", requiredMode = Schema.RequiredMode.REQUIRED, example = "SHARE")
    private String jumpPage;

    @Schema(description = "跳转页面编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long jumpPageId;

}
