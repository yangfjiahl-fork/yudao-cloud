package cn.iocoder.yudao.module.gift.controller.admin.slideritem;

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

import cn.iocoder.yudao.module.gift.controller.admin.slideritem.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.slideritem.SliderItemDO;
import cn.iocoder.yudao.module.gift.service.slideritem.SliderItemService;

@Tag(name = "管理后台 - 轮播图")
@RestController
@RequestMapping("/gift/slider-item")
@Validated
public class SliderItemController {

    @Resource
    private SliderItemService sliderItemService;

    @PostMapping("/create")
    @Operation(summary = "创建轮播图")
    @PreAuthorize("@ss.hasPermission('gift:slider-item:create')")
    public CommonResult<Long> createSliderItem(@Valid @RequestBody SliderItemSaveReqVO createReqVO) {
        return success(sliderItemService.createSliderItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新轮播图")
    @PreAuthorize("@ss.hasPermission('gift:slider-item:update')")
    public CommonResult<Boolean> updateSliderItem(@Valid @RequestBody SliderItemSaveReqVO updateReqVO) {
        sliderItemService.updateSliderItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除轮播图")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('gift:slider-item:delete')")
    public CommonResult<Boolean> deleteSliderItem(@RequestParam("id") Long id) {
        sliderItemService.deleteSliderItem(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除轮播图")
                @PreAuthorize("@ss.hasPermission('gift:slider-item:delete')")
    public CommonResult<Boolean> deleteSliderItemList(@RequestParam("ids") List<Long> ids) {
        sliderItemService.deleteSliderItemListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得轮播图")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('gift:slider-item:query')")
    public CommonResult<SliderItemRespVO> getSliderItem(@RequestParam("id") Long id) {
        SliderItemDO sliderItem = sliderItemService.getSliderItem(id);
        return success(BeanUtils.toBean(sliderItem, SliderItemRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得轮播图分页")
    @PreAuthorize("@ss.hasPermission('gift:slider-item:query')")
    public CommonResult<PageResult<SliderItemRespVO>> getSliderItemPage(@Valid SliderItemPageReqVO pageReqVO) {
        PageResult<SliderItemDO> pageResult = sliderItemService.getSliderItemPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SliderItemRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出轮播图 Excel")
    @PreAuthorize("@ss.hasPermission('gift:slider-item:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSliderItemExcel(@Valid SliderItemPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SliderItemDO> list = sliderItemService.getSliderItemPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "轮播图.xls", "数据", SliderItemRespVO.class,
                        BeanUtils.toBean(list, SliderItemRespVO.class));
    }

}