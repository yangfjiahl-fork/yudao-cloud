package cn.iocoder.yudao.module.gift.controller.admin.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Collection;

@Schema(description = "管理后台 - 文章分类列表查询 Request VO")
@Data
public class ArticleCategoryListReqVO {

    @Schema(description = "分类名称", example = "饰品搭配")
    private String name;

    @Schema(description = "开启状态", example = "0")
    private Integer status;

    @Schema(description = "父分类编号", example = "1")
    private Long parentId;

    @Schema(description = "父分类编号数组", example = "1,2,3")
    private Collection<Long> parentIds;

}
