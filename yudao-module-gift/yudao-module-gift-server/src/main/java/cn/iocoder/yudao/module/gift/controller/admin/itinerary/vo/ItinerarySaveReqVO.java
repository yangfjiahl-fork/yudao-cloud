package cn.iocoder.yudao.module.gift.controller.admin.itinerary.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 行程新增/修改 Request VO")
@Data
public class ItinerarySaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10691")
    private Long id;

    @Schema(description = "城市ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7848")
    @NotNull(message = "城市ID不能为空")
    private Integer cityId;

    @Schema(description = "类别ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1668")
    @NotNull(message = "类别ID不能为空")
    private Long categoryId;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "标题不能为空")
    private String title;

    @Schema(description = "副标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "副标题不能为空")
    private String subTitle;

    @Schema(description = "描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "随便")
    @NotEmpty(message = "描述不能为空")
    private String description;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "多图片", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "多图片不能为空")
    private String picUrls;

    @Schema(description = "图片尺寸")
    private String picSizes;

    @Schema(description = "标签", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "标签不能为空")
    private String tags;

    @Schema(description = "封面图", example = "https://www.iocoder.cn")
    private String coverUrl;

    @Schema(description = "封面宽度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "封面宽度不能为空")
    private Integer coverWidth;

    @Schema(description = "封面高度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "封面高度不能为空")
    private Integer coverHeight;

    @Schema(description = "城市ID", example = "23770")
    private Integer nextCityId;

    @Schema(description = "浏览数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "浏览数不能为空")
    private Long viewCnt;

    @Schema(description = "点赞数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "点赞数不能为空")
    private Long likeCnt;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    private Integer sort;

}
