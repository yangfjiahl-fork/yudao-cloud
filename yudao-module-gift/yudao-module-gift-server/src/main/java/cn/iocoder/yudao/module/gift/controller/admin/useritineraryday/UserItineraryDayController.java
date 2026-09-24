package cn.iocoder.yudao.module.gift.controller.admin.useritineraryday;

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

import cn.iocoder.yudao.module.gift.controller.admin.useritineraryday.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDayDO;
import cn.iocoder.yudao.module.gift.service.useritineraryday.UserItineraryDayService;

@Tag(name = "管理后台 - 用户行程每日安排")
@RestController
@RequestMapping("/gift/user-itinerary-day")
@Validated
public class UserItineraryDayController {

    @Resource
    private UserItineraryDayService userItineraryDayService;

    @PostMapping("/create")
    @Operation(summary = "创建用户行程每日安排")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-day:create')")
    public CommonResult<Long> createUserItineraryDay(@Valid @RequestBody UserItineraryDaySaveReqVO createReqVO) {
        return success(userItineraryDayService.createUserItineraryDay(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户行程每日安排")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-day:update')")
    public CommonResult<Boolean> updateUserItineraryDay(@Valid @RequestBody UserItineraryDaySaveReqVO updateReqVO) {
        userItineraryDayService.updateUserItineraryDay(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户行程每日安排")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-day:delete')")
    public CommonResult<Boolean> deleteUserItineraryDay(@RequestParam("id") Long id) {
        userItineraryDayService.deleteUserItineraryDay(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除用户行程每日安排")
                @PreAuthorize("@ss.hasPermission('gift:user-itinerary-day:delete')")
    public CommonResult<Boolean> deleteUserItineraryDayList(@RequestParam("ids") List<Long> ids) {
        userItineraryDayService.deleteUserItineraryDayListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户行程每日安排")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-day:query')")
    public CommonResult<UserItineraryDayRespVO> getUserItineraryDay(@RequestParam("id") Long id) {
        UserItineraryDayDO userItineraryDay = userItineraryDayService.getUserItineraryDay(id);
        return success(BeanUtils.toBean(userItineraryDay, UserItineraryDayRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户行程每日安排分页")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-day:query')")
    public CommonResult<PageResult<UserItineraryDayRespVO>> getUserItineraryDayPage(@Valid UserItineraryDayPageReqVO pageReqVO) {
        PageResult<UserItineraryDayDO> pageResult = userItineraryDayService.getUserItineraryDayPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, UserItineraryDayRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用户行程每日安排 Excel")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-day:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportUserItineraryDayExcel(@Valid UserItineraryDayPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<UserItineraryDayDO> list = userItineraryDayService.getUserItineraryDayPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "用户行程每日安排.xls", "数据", UserItineraryDayRespVO.class,
                        BeanUtils.toBean(list, UserItineraryDayRespVO.class));
    }

}