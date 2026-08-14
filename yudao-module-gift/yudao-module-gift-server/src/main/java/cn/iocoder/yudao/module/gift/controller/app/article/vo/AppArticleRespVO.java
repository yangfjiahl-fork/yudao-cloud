package cn.iocoder.yudao.module.gift.controller.app.article.vo;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.gift.service.article.ArticleLikeAutoTransService;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;
import org.dromara.core.trans.vo.VO;

@Schema(description = "用户 APP - 文章 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AppArticleRespVO implements VO {

    @Trans(type = TransType.AUTO_TRANS, key = ArticleLikeAutoTransService.NAMESPACE,
            fields = "liked", ref = "liked")
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4112")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "文章作者会员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29715")
    @ExcelProperty("文章作者会员编号")
    private Long memberId;

    @Schema(description = "文章作者")
    @ExcelProperty("文章作者")
    private String author;

    @Schema(description = "官方名称")
    @ExcelProperty("官方名称")
    private String officialName;

    @Schema(description = "文章分类编号，发布时要求为末级分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "15436")
    @ExcelProperty("文章分类编号，发布时要求为末级分类")
    private Long categoryId;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("标题")
    private String title;

    @Schema(description = "摘要", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("摘要")
    private String summary;

    @Schema(description = "封面图地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("封面图地址")
    private String coverImage;

    @Schema(description = "封面宽度", example = "750")
    @ExcelProperty("封面宽度")
    private Integer coverWidth;

    @Schema(description = "封面高度", example = "422")
    @ExcelProperty("封面高度")
    private Integer coverHeight;

    @Schema(description = "封面方向", example = "landscape")
    @ExcelProperty("封面方向")
    private String coverOrientation;

    @Schema(description = "轮播图地址数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("轮播图地址数组")
    private List<String> sliderPicUrls;

    @Schema(description = "站内富文本正文", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("站内富文本正文")
    private String content;

    @Schema(description = "浏览次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "31114")
    @ExcelProperty("浏览次数")
    private Integer viewCount;

    @Schema(description = "点赞次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10852")
    @ExcelProperty("点赞次数")
    private Integer likeCount;

    @Schema(description = "当前用户是否已点赞", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean liked;

    @Schema(description = "排序值，越大越靠前", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排序值，越大越靠前")
    private Integer sort;

    @Schema(description = "状态：0-草稿，1-已发布，2-已下架", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "状态：0-草稿，1-已发布，2-已下架", converter = DictConvert.class)
    @DictFormat("gift_article_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

    @Schema(description = "发布时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("发布时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime publishTime;

}
