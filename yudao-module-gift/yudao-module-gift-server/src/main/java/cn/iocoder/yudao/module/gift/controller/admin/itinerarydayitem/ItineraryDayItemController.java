package cn.iocoder.yudao.module.gift.controller.admin.itinerarydayitem;

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

import cn.iocoder.yudao.module.gift.controller.admin.itinerarydayitem.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerarydayitem.ItineraryDayItemDO;
import cn.iocoder.yudao.module.gift.service.itinerarydayitem.ItineraryDayItemService;

@Tag(name = "管理后台 - 通用行程节点")
@RestController
@RequestMapping("/gift/itinerary-day-item")
@Validated
public class ItineraryDayItemController {

    @Resource
    private ItineraryDayItemService itineraryDayItemService;

    @PostMapping("/create")
    @Operation(summary = "创建通用行程节点")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-day-item:create')")
    public CommonResult<Long> createItineraryDayItem(@Valid @RequestBody ItineraryDayItemSaveReqVO createReqVO) {
        return success(itineraryDayItemService.createItineraryDayItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新通用行程节点")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-day-item:update')")
    public CommonResult<Boolean> updateItineraryDayItem(@Valid @RequestBody ItineraryDayItemSaveReqVO updateReqVO) {
        itineraryDayItemService.updateItineraryDayItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除通用行程节点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('gift:itinerary-day-item:delete')")
    public CommonResult<Boolean> deleteItineraryDayItem(@RequestParam("id") Long id) {
        itineraryDayItemService.deleteItineraryDayItem(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除通用行程节点")
                @PreAuthorize("@ss.hasPermission('gift:itinerary-day-item:delete')")
    public CommonResult<Boolean> deleteItineraryDayItemList(@RequestParam("ids") List<Long> ids) {
        itineraryDayItemService.deleteItineraryDayItemListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得通用行程节点")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-day-item:query')")
    public CommonResult<ItineraryDayItemRespVO> getItineraryDayItem(@RequestParam("id") Long id) {
        ItineraryDayItemDO itineraryDayItem = itineraryDayItemService.getItineraryDayItem(id);
        return success(BeanUtils.toBean(itineraryDayItem, ItineraryDayItemRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得通用行程节点分页")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-day-item:query')")
    public CommonResult<PageResult<ItineraryDayItemRespVO>> getItineraryDayItemPage(@Valid ItineraryDayItemPageReqVO pageReqVO) {
        PageResult<ItineraryDayItemDO> pageResult = itineraryDayItemService.getItineraryDayItemPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ItineraryDayItemRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出通用行程节点 Excel")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-day-item:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportItineraryDayItemExcel(@Valid ItineraryDayItemPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ItineraryDayItemDO> list = itineraryDayItemService.getItineraryDayItemPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "通用行程节点.xls", "数据", ItineraryDayItemRespVO.class,
                        BeanUtils.toBean(list, ItineraryDayItemRespVO.class));
    }

}
