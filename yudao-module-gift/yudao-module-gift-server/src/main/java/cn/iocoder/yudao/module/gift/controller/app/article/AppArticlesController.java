package cn.iocoder.yudao.module.gift.controller.app.article;

import org.springframework.web.bind.annotation.*;

import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticlePageReqVO;
import cn.iocoder.yudao.module.gift.enums.ArticleStatusEnum;
import cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.module.gift.controller.app.article.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesDO;
import cn.iocoder.yudao.module.gift.service.article.ArticleService;

import java.util.Objects;

@Tag(name = "用户 APP - 文章")
@RestController
@RequestMapping("/gift/article")
@Validated
public class AppArticlesController {

    @Resource
    private ArticleService articleService;

    @GetMapping("/get")
    @Operation(summary = "获得文章")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PermitAll
    public CommonResult<AppArticleRespVO> getArticle(@RequestParam("id") Long id) {
        ArticlesDO article = articleService.getArticle(id);
        if (article == null || !Objects.equals(article.getStatus(), ArticleStatusEnum.ONLINE.getType())) {
            return error(ErrorCodeConstants.ARTICLE_NOT_EXISTS);
        }
        return success(BeanUtils.toBean(article, AppArticleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得文章分页")
    @PermitAll
    public CommonResult<PageResult<AppArticleRespVO>> getArticlePage(@Valid ArticlePageReqVO pageReqVO) {
        pageReqVO.setStatus(ArticleStatusEnum.ONLINE.getType());
        PageResult<ArticlesDO> pageResult = articleService.getArticlePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AppArticleRespVO.class));
    }

}
