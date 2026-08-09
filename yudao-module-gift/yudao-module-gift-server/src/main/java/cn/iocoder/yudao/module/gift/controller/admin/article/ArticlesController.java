package cn.iocoder.yudao.module.gift.controller.admin.article;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.gift.controller.admin.article.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesDO;
import cn.iocoder.yudao.module.gift.service.article.ArticleService;

@Tag(name = "管理后台 - 文章")
@RestController
@RequestMapping("/gift/article")
@Validated
public class ArticlesController {

    @Resource
    private ArticleService articleService;

    @PostMapping("/create")
    @Operation(summary = "创建文章")
    @PreAuthorize("@ss.hasPermission('gift:article:create')")
    public CommonResult<Long> createArticle(@Valid @RequestBody ArticleSaveReqVO createReqVO) {
        return success(articleService.createArticle(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文章")
    @PreAuthorize("@ss.hasPermission('gift:article:update')")
    public CommonResult<Boolean> updateArticle(@Valid @RequestBody ArticleSaveReqVO updateReqVO) {
        articleService.updateArticle(updateReqVO);
        return success(true);
    }

    @PutMapping("/change-status")
    @Operation(summary = "修改文章状态")
    @PreAuthorize("@ss.hasPermission('gift:article:update')")
    public CommonResult<Boolean> changeArticleStatus(@Valid @RequestBody ArticleChangeStatusReqVO reqVO) {
        articleService.changeArticleStatus(reqVO.getIds(), reqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文章")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('gift:article:delete')")
    public CommonResult<Boolean> deleteArticle(@RequestParam("id") Long id) {
        articleService.deleteArticle(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除文章")
                @PreAuthorize("@ss.hasPermission('gift:article:delete')")
    public CommonResult<Boolean> deleteArticleList(@RequestParam("ids") List<Long> ids) {
        articleService.deleteArticleListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文章")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('gift:article:query')")
    public CommonResult<ArticleRespVO> getArticle(@RequestParam("id") Long id) {
        ArticlesDO article = articleService.getArticle(id);
        return success(BeanUtils.toBean(article, ArticleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得文章分页")
    @PreAuthorize("@ss.hasPermission('gift:article:query')")
    public CommonResult<PageResult<ArticleRespVO>> getArticlePage(@Valid ArticlePageReqVO pageReqVO) {
        PageResult<ArticlesDO> pageResult = articleService.getArticlePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ArticleRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出文章 Excel")
    @PreAuthorize("@ss.hasPermission('gift:article:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportArticleExcel(@Valid ArticlePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ArticlesDO> list = articleService.getArticlePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "文章.xls", "数据", ArticleRespVO.class,
                        BeanUtils.toBean(list, ArticleRespVO.class));
    }

}
