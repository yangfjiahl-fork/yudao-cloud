package cn.iocoder.yudao.module.gift.controller.admin.itinerary;

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

import cn.iocoder.yudao.module.gift.controller.admin.itinerary.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerary.ItineraryDO;
import cn.iocoder.yudao.module.gift.service.itinerary.ItineraryService;
import org.dromara.core.trans.anno.TransMethodResult;

@Tag(name = "管理后台 - 行程")
@RestController
@RequestMapping("/gift/itinerary")
@Validated
public class ItineraryController {

    @Resource
    private ItineraryService itineraryService;

    @PostMapping("/create")
    @Operation(summary = "创建行程")
    @PreAuthorize("@ss.hasPermission('gift:itinerary:create')")
    public CommonResult<Long> createItinerary(@Valid @RequestBody ItinerarySaveReqVO createReqVO) {
        return success(itineraryService.createItinerary(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新行程")
    @PreAuthorize("@ss.hasPermission('gift:itinerary:update')")
    public CommonResult<Boolean> updateItinerary(@Valid @RequestBody ItinerarySaveReqVO updateReqVO) {
        itineraryService.updateItinerary(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除行程")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('gift:itinerary:delete')")
    public CommonResult<Boolean> deleteItinerary(@RequestParam("id") Long id) {
        itineraryService.deleteItinerary(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除行程")
                @PreAuthorize("@ss.hasPermission('gift:itinerary:delete')")
    public CommonResult<Boolean> deleteItineraryList(@RequestParam("ids") List<Long> ids) {
        itineraryService.deleteItineraryListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得行程")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('gift:itinerary:query')")
    @TransMethodResult
    public CommonResult<ItineraryRespVO> getItinerary(@RequestParam("id") Long id) {
        ItineraryDO itinerary = itineraryService.getItinerary(id);
        return success(BeanUtils.toBean(itinerary, ItineraryRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得行程分页")
    @PreAuthorize("@ss.hasPermission('gift:itinerary:query')")
    @TransMethodResult
    public CommonResult<PageResult<ItineraryRespVO>> getItineraryPage(@Valid ItineraryPageReqVO pageReqVO) {
        PageResult<ItineraryDO> pageResult = itineraryService.getItineraryPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ItineraryRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出行程 Excel")
    @PreAuthorize("@ss.hasPermission('gift:itinerary:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportItineraryExcel(@Valid ItineraryPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ItineraryDO> list = itineraryService.getItineraryPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "行程.xls", "数据", ItineraryRespVO.class,
                        BeanUtils.toBean(list, ItineraryRespVO.class));
    }

}
