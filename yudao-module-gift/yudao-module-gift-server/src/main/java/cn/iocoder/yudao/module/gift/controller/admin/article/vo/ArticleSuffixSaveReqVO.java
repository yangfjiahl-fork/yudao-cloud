package cn.iocoder.yudao.module.gift.controller.admin.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 文章后缀新增/修改 Request VO")
@Data
public class ArticleSuffixSaveReqVO {

    @Schema(description = "编号", example = "2")
    private Long id;

    @Schema(description = "签名标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "签名标题不能为空")
    private String title;

    @Schema(description = "签名内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "签名内容不能为空")
    private String content;

}
