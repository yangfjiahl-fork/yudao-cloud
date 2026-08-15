package cn.iocoder.yudao.module.gift.controller.admin.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 文章分类新增/更新 Request VO")
@Data
public class ArticleCategorySaveReqVO {

    @Schema(description = "分类编号", example = "2")
    private Long id;

    @Schema(description = "父分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "父分类编号不能为空")
    private Long parentId;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "饰品搭配")
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @Schema(description = "分类图", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分类图不能为空")
    private String picUrl;

    @Schema(description = "分类排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "开启状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "开启状态不能为空")
    private Integer status;

}
