package cn.iocoder.yudao.module.gift.controller.admin.article;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleCategoryListReqVO;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleCategoryRespVO;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleCategorySaveReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesCategoryDO;
import cn.iocoder.yudao.module.gift.service.article.ArticleCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 文章分类")
@RestController
@RequestMapping("/gift/article-category")
@Validated
public class ArticlesCategoryController {

    @Resource
    private ArticleCategoryService articleCategoryService;

    @PostMapping("/create")
    @Operation(summary = "创建文章分类")
    @PreAuthorize("@ss.hasPermission('gift:article-category:create')")
    public CommonResult<Long> createArticleCategory(@Valid @RequestBody ArticleCategorySaveReqVO createReqVO) {
        return success(articleCategoryService.createArticleCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文章分类")
    @PreAuthorize("@ss.hasPermission('gift:article-category:update')")
    public CommonResult<Boolean> updateArticleCategory(@Valid @RequestBody ArticleCategorySaveReqVO updateReqVO) {
        articleCategoryService.updateArticleCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文章分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('gift:article-category:delete')")
    public CommonResult<Boolean> deleteArticleCategory(@RequestParam("id") Long id) {
        articleCategoryService.deleteArticleCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文章分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('gift:article-category:query')")
    public CommonResult<ArticleCategoryRespVO> getArticleCategory(@RequestParam("id") Long id) {
        ArticlesCategoryDO category = articleCategoryService.getArticleCategory(id);
        return success(BeanUtils.toBean(category, ArticleCategoryRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得文章分类列表")
    @PreAuthorize("@ss.hasPermission('gift:article-category:query')")
    public CommonResult<List<ArticleCategoryRespVO>> getArticleCategoryList(@Valid ArticleCategoryListReqVO listReqVO) {
        List<ArticlesCategoryDO> list = articleCategoryService.getArticleCategoryList(listReqVO);
        list.sort(Comparator.comparing(ArticlesCategoryDO::getSort));
        return success(BeanUtils.toBean(list, ArticleCategoryRespVO.class));
    }

}
