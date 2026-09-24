package cn.iocoder.yudao.module.gift.controller.admin.itinerarycategory;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.constraints.*;
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

import cn.iocoder.yudao.module.gift.controller.admin.itinerarycategory.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerarycategory.ItineraryCategoryDO;
import cn.iocoder.yudao.module.gift.service.itinerarycategory.ItineraryCategoryService;

@Tag(name = "管理后台 - 行程类别")
@RestController
@RequestMapping("/gift/itinerary-category")
@Validated
public class ItineraryCategoryController {

    @Resource
    private ItineraryCategoryService itineraryCategoryService;

    @PostMapping("/create")
    @Operation(summary = "创建行程类别")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-category:create')")
    public CommonResult<Long> createItineraryCategory(@Valid @RequestBody ItineraryCategorySaveReqVO createReqVO) {
        return success(itineraryCategoryService.createItineraryCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新行程类别")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-category:update')")
    public CommonResult<Boolean> updateItineraryCategory(@Valid @RequestBody ItineraryCategorySaveReqVO updateReqVO) {
        itineraryCategoryService.updateItineraryCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除行程类别")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('gift:itinerary-category:delete')")
    public CommonResult<Boolean> deleteItineraryCategory(@RequestParam("id") Long id) {
        itineraryCategoryService.deleteItineraryCategory(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除行程类别")
                @PreAuthorize("@ss.hasPermission('gift:itinerary-category:delete')")
    public CommonResult<Boolean> deleteItineraryCategoryList(@RequestParam("ids") List<Long> ids) {
        itineraryCategoryService.deleteItineraryCategoryListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得行程类别")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-category:query')")
    public CommonResult<ItineraryCategoryRespVO> getItineraryCategory(@RequestParam("id") Long id) {
        ItineraryCategoryDO itineraryCategory = itineraryCategoryService.getItineraryCategory(id);
        return success(BeanUtils.toBean(itineraryCategory, ItineraryCategoryRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得行程类别分页")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-category:query')")
    public CommonResult<PageResult<ItineraryCategoryRespVO>> getItineraryCategoryPage(@Valid ItineraryCategoryPageReqVO pageReqVO) {
        PageResult<ItineraryCategoryDO> pageResult = itineraryCategoryService.getItineraryCategoryPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ItineraryCategoryRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出行程类别 Excel")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-category:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportItineraryCategoryExcel(@Valid ItineraryCategoryPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ItineraryCategoryDO> list = itineraryCategoryService.getItineraryCategoryPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "行程类别.xls", "数据", ItineraryCategoryRespVO.class,
                        BeanUtils.toBean(list, ItineraryCategoryRespVO.class));
    }

}
