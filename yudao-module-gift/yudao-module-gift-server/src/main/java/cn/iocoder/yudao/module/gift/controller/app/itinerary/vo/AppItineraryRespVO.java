package cn.iocoder.yudao.module.gift.controller.app.itinerary.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 通用行程 Response VO")
@Data
public class AppItineraryRespVO {

    @Schema(description = "行程编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "城市编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "310100")
    private Integer cityId;

    @Schema(description = "行程类别编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long categoryId;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "副标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String subTitle;

    @Schema(description = "描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "图片地址")
    private String picUrls;

    @Schema(description = "图片尺寸")
    private String picSizes;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "封面图")
    private String coverUrl;

    @Schema(description = "封面宽度")
    private Integer coverWidth;

    @Schema(description = "封面高度")
    private Integer coverHeight;

    @Schema(description = "下一城市编号", example = "320100")
    private Integer nextCityId;

    @Schema(description = "浏览数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long viewCnt;

    @Schema(description = "点赞数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long likeCnt;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer sort;

}
