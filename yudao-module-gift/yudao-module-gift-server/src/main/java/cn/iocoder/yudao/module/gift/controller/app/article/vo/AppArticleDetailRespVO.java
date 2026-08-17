package cn.iocoder.yudao.module.gift.controller.app.article.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticleSuffixDO;
import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

import java.util.List;

@Schema(description = "用户 APP - 文章详情 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppArticleDetailRespVO extends AppArticleRespVO {

    @JsonIgnore
    @Trans(type = TransType.SIMPLE, target = ArticleSuffixDO.class, fields = "content", ref = "suffixContent")
    private Long suffixContentId;

    @Schema(description = "文章后缀内容")
    private String suffixContent;
}
