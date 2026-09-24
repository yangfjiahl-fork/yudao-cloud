package cn.iocoder.yudao.module.gift.controller.admin.useritinerarydayitem;

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

import cn.iocoder.yudao.module.gift.controller.admin.useritinerarydayitem.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDayItemDO;
import cn.iocoder.yudao.module.gift.service.useritinerarydayitem.UserItineraryDayItemService;

@Tag(name = "管理后台 - 用户行程节点")
@RestController
@RequestMapping("/gift/user-itinerary-day-item")
@Validated
public class UserItineraryDayItemController {

    @Resource
    private UserItineraryDayItemService userItineraryDayItemService;

    @PostMapping("/create")
    @Operation(summary = "创建用户行程节点")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-day-item:create')")
    public CommonResult<Long> createUserItineraryDayItem(@Valid @RequestBody UserItineraryDayItemSaveReqVO createReqVO) {
        return success(userItineraryDayItemService.createUserItineraryDayItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户行程节点")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-day-item:update')")
    public CommonResult<Boolean> updateUserItineraryDayItem(@Valid @RequestBody UserItineraryDayItemSaveReqVO updateReqVO) {
        userItineraryDayItemService.updateUserItineraryDayItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户行程节点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-day-item:delete')")
    public CommonResult<Boolean> deleteUserItineraryDayItem(@RequestParam("id") Long id) {
        userItineraryDayItemService.deleteUserItineraryDayItem(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除用户行程节点")
                @PreAuthorize("@ss.hasPermission('gift:user-itinerary-day-item:delete')")
    public CommonResult<Boolean> deleteUserItineraryDayItemList(@RequestParam("ids") List<Long> ids) {
        userItineraryDayItemService.deleteUserItineraryDayItemListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户行程节点")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-day-item:query')")
    public CommonResult<UserItineraryDayItemRespVO> getUserItineraryDayItem(@RequestParam("id") Long id) {
        UserItineraryDayItemDO userItineraryDayItem = userItineraryDayItemService.getUserItineraryDayItem(id);
        return success(BeanUtils.toBean(userItineraryDayItem, UserItineraryDayItemRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户行程节点分页")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-day-item:query')")
    public CommonResult<PageResult<UserItineraryDayItemRespVO>> getUserItineraryDayItemPage(@Valid UserItineraryDayItemPageReqVO pageReqVO) {
        PageResult<UserItineraryDayItemDO> pageResult = userItineraryDayItemService.getUserItineraryDayItemPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, UserItineraryDayItemRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用户行程节点 Excel")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-day-item:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportUserItineraryDayItemExcel(@Valid UserItineraryDayItemPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<UserItineraryDayItemDO> list = userItineraryDayItemService.getUserItineraryDayItemPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "用户行程节点.xls", "数据", UserItineraryDayItemRespVO.class,
                        BeanUtils.toBean(list, UserItineraryDayItemRespVO.class));
    }

}