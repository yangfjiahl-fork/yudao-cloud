package cn.iocoder.yudao.module.gift.controller.admin.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 文章后缀 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ArticleSuffixRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "32387")
    @ExcelProperty("ID")
    private Long id;

    @Schema(description = "签名标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("签名标题")
    private String title;

    @Schema(description = "签名内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("签名内容")
    private String content;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
