package cn.iocoder.yudao.module.gift.controller.admin.useritinerary;

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

import cn.iocoder.yudao.module.gift.controller.admin.useritinerary.vo.*;
import cn.iocoder.yudao.module.gift.controller.common.vo.UserItineraryPageRespVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import cn.iocoder.yudao.module.gift.service.useritinerary.UserItineraryService;

@Tag(name = "管理后台 - 用户行程")
@RestController
@RequestMapping("/gift/user-itinerary")
@Validated
public class UserItineraryController {

    @Resource
    private UserItineraryService userItineraryService;

    @PostMapping("/create")
    @Operation(summary = "创建用户行程")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary:create')")
    public CommonResult<Long> createUserItinerary(@Valid @RequestBody UserItinerarySaveReqVO createReqVO) {
        return success(userItineraryService.createUserItinerary(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户行程")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary:update')")
    public CommonResult<Boolean> updateUserItinerary(@Valid @RequestBody UserItinerarySaveReqVO updateReqVO) {
        userItineraryService.updateUserItinerary(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户行程")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary:delete')")
    public CommonResult<Boolean> deleteUserItinerary(@RequestParam("id") Long id) {
        userItineraryService.deleteUserItinerary(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除用户行程")
                @PreAuthorize("@ss.hasPermission('gift:user-itinerary:delete')")
    public CommonResult<Boolean> deleteUserItineraryList(@RequestParam("ids") List<Long> ids) {
        userItineraryService.deleteUserItineraryListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户行程")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary:query')")
    public CommonResult<UserItineraryRespVO> getUserItinerary(@RequestParam("id") Long id) {
        UserItineraryDO userItinerary = userItineraryService.getUserItinerary(id);
        return success(BeanUtils.toBean(userItinerary, UserItineraryRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户行程分页")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary:query')")
    public CommonResult<PageResult<UserItineraryPageRespVO>> getUserItineraryPage(
            @Valid UserItineraryPageReqVO pageReqVO) {
        PageResult<UserItineraryDO> pageResult = userItineraryService.getUserItineraryPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, UserItineraryPageRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用户行程 Excel")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportUserItineraryExcel(@Valid UserItineraryPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<UserItineraryDO> list = userItineraryService.getUserItineraryExportPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "用户行程.xls", "数据", UserItineraryRespVO.class,
                        BeanUtils.toBean(list, UserItineraryRespVO.class));
    }

}
