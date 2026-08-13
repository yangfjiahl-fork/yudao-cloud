package cn.iocoder.yudao.module.gift.controller.app.article;

import org.springframework.web.bind.annotation.*;

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
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

import cn.iocoder.yudao.module.gift.controller.app.article.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesDO;
import cn.iocoder.yudao.module.gift.service.article.ArticleService;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
        articleService.addArticleViewCount(id);
        AppArticleRespVO respVO = BeanUtils.toBean(article, AppArticleRespVO.class);
        fillLiked(getLoginUserId(), List.of(respVO));
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得文章分页")
    @PermitAll
    public CommonResult<PageResult<AppArticleRespVO>> getArticlePage(@Valid AppArticlePageReqVO pageReqVO) {
        pageReqVO.setStatus(ArticleStatusEnum.ONLINE.getType());
        pageReqVO.setPageNo(1);
        PageResult<ArticlesDO> pageResult = articleService.getArticlePage(pageReqVO);
        PageResult<AppArticleRespVO> respVOPage = BeanUtils.toBean(pageResult, AppArticleRespVO.class);
        fillLiked(getLoginUserId(), respVOPage.getList());
        return success(respVOPage);
    }

    @PostMapping("/like")
    @Operation(summary = "点赞文章")
    @Parameter(name = "id", description = "文章编号", required = true, example = "1024")
    public CommonResult<Boolean> likeArticle(@RequestParam("id") Long id) {
        articleService.likeArticle(getLoginUserId(), id);
        return success(true);
    }

    @DeleteMapping("/like")
    @Operation(summary = "取消点赞文章")
    @Parameter(name = "id", description = "文章编号", required = true, example = "1024")
    public CommonResult<Boolean> unlikeArticle(@RequestParam("id") Long id) {
        articleService.unlikeArticle(getLoginUserId(), id);
        return success(true);
    }

    private void fillLiked(Long memberId, Collection<AppArticleRespVO> articles) {
        Set<Long> likedArticleIds = articleService.getLikedArticleIds(memberId,
                articles.stream().map(AppArticleRespVO::getId).toList());
        articles.forEach(article -> article.setLiked(likedArticleIds.contains(article.getId())));
    }

}
