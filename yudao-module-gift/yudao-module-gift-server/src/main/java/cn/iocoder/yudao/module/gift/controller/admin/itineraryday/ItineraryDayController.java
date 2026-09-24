package cn.iocoder.yudao.module.gift.controller.admin.itineraryday;

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

import cn.iocoder.yudao.module.gift.controller.admin.itineraryday.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.itineraryday.ItineraryDayDO;
import cn.iocoder.yudao.module.gift.service.itineraryday.ItineraryDayService;

@Tag(name = "管理后台 - 通用行程每日安排")
@RestController
@RequestMapping("/gift/itinerary-day")
@Validated
public class ItineraryDayController {

    @Resource
    private ItineraryDayService itineraryDayService;

    @PostMapping("/create")
    @Operation(summary = "创建通用行程每日安排")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-day:create')")
    public CommonResult<Long> createItineraryDay(@Valid @RequestBody ItineraryDaySaveReqVO createReqVO) {
        return success(itineraryDayService.createItineraryDay(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新通用行程每日安排")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-day:update')")
    public CommonResult<Boolean> updateItineraryDay(@Valid @RequestBody ItineraryDaySaveReqVO updateReqVO) {
        itineraryDayService.updateItineraryDay(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除通用行程每日安排")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('gift:itinerary-day:delete')")
    public CommonResult<Boolean> deleteItineraryDay(@RequestParam("id") Long id) {
        itineraryDayService.deleteItineraryDay(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除通用行程每日安排")
                @PreAuthorize("@ss.hasPermission('gift:itinerary-day:delete')")
    public CommonResult<Boolean> deleteItineraryDayList(@RequestParam("ids") List<Long> ids) {
        itineraryDayService.deleteItineraryDayListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得通用行程每日安排")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-day:query')")
    public CommonResult<ItineraryDayRespVO> getItineraryDay(@RequestParam("id") Long id) {
        ItineraryDayDO itineraryDay = itineraryDayService.getItineraryDay(id);
        return success(BeanUtils.toBean(itineraryDay, ItineraryDayRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得通用行程每日安排分页")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-day:query')")
    public CommonResult<PageResult<ItineraryDayRespVO>> getItineraryDayPage(@Valid ItineraryDayPageReqVO pageReqVO) {
        PageResult<ItineraryDayDO> pageResult = itineraryDayService.getItineraryDayPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ItineraryDayRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出通用行程每日安排 Excel")
    @PreAuthorize("@ss.hasPermission('gift:itinerary-day:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportItineraryDayExcel(@Valid ItineraryDayPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ItineraryDayDO> list = itineraryDayService.getItineraryDayPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "通用行程每日安排.xls", "数据", ItineraryDayRespVO.class,
                        BeanUtils.toBean(list, ItineraryDayRespVO.class));
    }

}