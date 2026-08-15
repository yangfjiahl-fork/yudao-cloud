package cn.iocoder.yudao.module.gift.controller.admin.article;

import org.springframework.web.bind.annotation.*;

import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleSuffixPageReqVO;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleSuffixRespVO;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleSuffixSaveReqVO;
import cn.iocoder.yudao.module.gift.service.article.ArticleSuffixService;
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

import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticleSuffixDO;

@Tag(name = "管理后台 - 文章后缀")
@RestController
@RequestMapping("/gift/article-suffix")
@Validated
public class ArticleSuffixController {

    @Resource
    private ArticleSuffixService articleSuffixService;

    @PostMapping("/create")
    @Operation(summary = "创建文章后缀")
    @PreAuthorize("@ss.hasPermission('gift:article-suffix:create')")
    public CommonResult<Long> createArticleSuffix(@Valid @RequestBody ArticleSuffixSaveReqVO createReqVO) {
        return success(articleSuffixService.createArticleSuffix(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文章后缀")
    @PreAuthorize("@ss.hasPermission('gift:article-suffix:update')")
    public CommonResult<Boolean> updateArticleSuffix(@Valid @RequestBody ArticleSuffixSaveReqVO updateReqVO) {
        articleSuffixService.updateArticleSuffix(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文章后缀")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('gift:article-suffix:delete')")
    public CommonResult<Boolean> deleteArticleSuffix(@RequestParam("id") Long id) {
        articleSuffixService.deleteArticleSuffix(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除文章后缀")
                @PreAuthorize("@ss.hasPermission('gift:article-suffix:delete')")
    public CommonResult<Boolean> deleteArticleSuffixList(@RequestParam("ids") List<Long> ids) {
        articleSuffixService.deleteArticleSuffixListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文章后缀")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('gift:article-suffix:query')")
    public CommonResult<ArticleSuffixRespVO> getArticleSuffix(@RequestParam("id") Long id) {
        ArticleSuffixDO articleSuffix = articleSuffixService.getArticleSuffix(id);
        return success(BeanUtils.toBean(articleSuffix, ArticleSuffixRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得文章后缀分页")
    @PreAuthorize("@ss.hasPermission('gift:article-suffix:query')")
    public CommonResult<PageResult<ArticleSuffixRespVO>> getArticleSuffixPage(@Valid ArticleSuffixPageReqVO pageReqVO) {
        PageResult<ArticleSuffixDO> pageResult = articleSuffixService.getArticleSuffixPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ArticleSuffixRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出文章后缀 Excel")
    @PreAuthorize("@ss.hasPermission('gift:article-suffix:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportArticleSuffixExcel(@Valid ArticleSuffixPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ArticleSuffixDO> list = articleSuffixService.getArticleSuffixPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "文章后缀.xls", "数据", ArticleSuffixRespVO.class,
                        BeanUtils.toBean(list, ArticleSuffixRespVO.class));
    }

}
