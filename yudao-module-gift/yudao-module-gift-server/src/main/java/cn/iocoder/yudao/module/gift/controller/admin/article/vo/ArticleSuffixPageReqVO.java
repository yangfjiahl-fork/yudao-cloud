package cn.iocoder.yudao.module.gift.controller.admin.article.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 文章后缀分页 Request VO")
@Data
public class ArticleSuffixPageReqVO extends PageParam {

    @Schema(description = "签名标题")
    private String title;

}
