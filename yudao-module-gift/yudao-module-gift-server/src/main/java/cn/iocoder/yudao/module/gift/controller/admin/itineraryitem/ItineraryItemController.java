package cn.iocoder.yudao.module.gift.controller.admin.itineraryitem;

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

import cn.iocoder.yudao.module.gift.controller.admin.itineraryitem.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.itineraryitem.ItineraryItemDO;
import cn.iocoder.yudao.module.gift.service.itineraryitem.ItineraryItemService;

@Tag(name = "管理后台 - 文章")
@RestController
@RequestMapping("/gift/itinerary-item")
@Validated
public class ItineraryItemController {

    @Resource
    private ItineraryItemService itineraryItemService;

    @PostMapping("/create")
    @Operation(summary = "创建文章")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-item:create')")
    public CommonResult<Long> createItineraryItem(@Valid @RequestBody ItineraryItemSaveReqVO createReqVO) {
        return success(itineraryItemService.createItineraryItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文章")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-item:update')")
    public CommonResult<Boolean> updateItineraryItem(@Valid @RequestBody ItineraryItemSaveReqVO updateReqVO) {
        itineraryItemService.updateItineraryItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文章")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('gift:itinerary-item:delete')")
    public CommonResult<Boolean> deleteItineraryItem(@RequestParam("id") Long id) {
        itineraryItemService.deleteItineraryItem(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除文章")
                @PreAuthorize("@ss.hasPermission('gift:itinerary-item:delete')")
    public CommonResult<Boolean> deleteItineraryItemList(@RequestParam("ids") List<Long> ids) {
        itineraryItemService.deleteItineraryItemListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文章")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-item:query')")
    public CommonResult<ItineraryItemRespVO> getItineraryItem(@RequestParam("id") Long id) {
        ItineraryItemDO itineraryItem = itineraryItemService.getItineraryItem(id);
        return success(BeanUtils.toBean(itineraryItem, ItineraryItemRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得文章分页")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-item:query')")
    public CommonResult<PageResult<ItineraryItemRespVO>> getItineraryItemPage(@Valid ItineraryItemPageReqVO pageReqVO) {
        PageResult<ItineraryItemDO> pageResult = itineraryItemService.getItineraryItemPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ItineraryItemRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出文章 Excel")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-item:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportItineraryItemExcel(@Valid ItineraryItemPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ItineraryItemDO> list = itineraryItemService.getItineraryItemPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "文章.xls", "数据", ItineraryItemRespVO.class,
                        BeanUtils.toBean(list, ItineraryItemRespVO.class));
    }

}