package cn.iocoder.yudao.module.gift.controller.admin.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 文章新增/修改 Request VO")
@Data
public class ArticleSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4112")
    private Long id;

    @Schema(description = "文章作者会员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29715")
    @NotNull(message = "文章作者会员编号不能为空")
    private Long memberId;

    @Schema(description = "文章分类编号，发布时要求为末级分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "15436")
    @NotNull(message = "文章分类编号，发布时要求为末级分类不能为空")
    private Long categoryId;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "标题不能为空")
    private String title;

    @Schema(description = "摘要", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "摘要不能为空")
    private String summary;

    @Schema(description = "封面图地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "封面图地址不能为空")
    private String coverImage;

    @Schema(description = "轮播图地址数组", requiredMode = Schema.RequiredMode.REQUIRED)
//    @NotEmpty(message = "轮播图地址数组不能为空")
    private List<String> sliderPicUrls;

    @Schema(description = "站内富文本正文", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "站内富文本正文不能为空")
    private String content;

    @Schema(description = "浏览次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "31114")
    @NotNull(message = "浏览次数不能为空")
    private Integer viewCount;

    @Schema(description = "点赞次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10852")
    @NotNull(message = "点赞次数不能为空")
    private Integer likeCount;

    @Schema(description = "排序值，越大越靠前", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序值，越大越靠前不能为空")
    private Integer sort;

    @Schema(description = "状态：0-草稿，1-已发布，2-已下架", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态：0-草稿，1-已发布，2-已下架不能为空")
    private Integer status;

    @Schema(description = "发布时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "发布时间不能为空")
    private LocalDateTime publishTime;

}
