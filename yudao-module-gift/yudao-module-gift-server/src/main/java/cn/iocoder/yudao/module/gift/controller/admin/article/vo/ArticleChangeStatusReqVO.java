package cn.iocoder.yudao.module.gift.controller.admin.article.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.gift.enums.ArticleStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 文章状态修改 Request VO")
@Data
public class ArticleChangeStatusReqVO {

    @Schema(description = "文章编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "文章编号不能为空")
    private Long id;

    @Schema(description = "文章状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "文章状态不能为空")
    @InEnum(ArticleStatusEnum.class)
    private Integer status;

}
