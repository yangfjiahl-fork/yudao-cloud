package cn.iocoder.yudao.module.gift.controller.app.article.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "用户 APP - 文章分页 Request VO")
@Data
public class AppArticlePageReqVO extends PageParam {

    @Schema(description = "文章分类编号，发布时要求为末级分类", example = "15436")
    private Long categoryId;

    @Schema(description = "状态：0-草稿，1-已发布，2-已下架", example = "1")
    private Integer status;

    @Schema(description = "当前列表最后一条文章的编号；首次加载不传", example = "1024")
    private Long maxId;

}
