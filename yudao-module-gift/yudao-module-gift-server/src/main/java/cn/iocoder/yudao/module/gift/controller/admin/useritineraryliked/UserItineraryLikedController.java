package cn.iocoder.yudao.module.gift.controller.admin.useritineraryliked;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import org.dromara.core.trans.anno.TransMethodResult;

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

import cn.iocoder.yudao.module.gift.controller.admin.useritineraryliked.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryliked.UserItineraryLikedDO;
import cn.iocoder.yudao.module.gift.service.useritineraryliked.UserItineraryLikedService;

@Tag(name = "管理后台 - 收藏行程")
@RestController
@RequestMapping("/gift/user-itinerary-liked")
@Validated
public class UserItineraryLikedController {

    @Resource
    private UserItineraryLikedService userItineraryLikedService;

    @PostMapping("/create")
    @Operation(summary = "创建收藏行程")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-liked:create')")
    public CommonResult<Long> createUserItineraryLiked(@Valid @RequestBody UserItineraryLikedSaveReqVO createReqVO) {
        return success(userItineraryLikedService.createUserItineraryLiked(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新收藏行程")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-liked:update')")
    public CommonResult<Boolean> updateUserItineraryLiked(@Valid @RequestBody UserItineraryLikedSaveReqVO updateReqVO) {
        userItineraryLikedService.updateUserItineraryLiked(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除收藏行程")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-liked:delete')")
    public CommonResult<Boolean> deleteUserItineraryLiked(@RequestParam("id") Long id) {
        userItineraryLikedService.deleteUserItineraryLiked(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除收藏行程")
                @PreAuthorize("@ss.hasPermission('gift:user-itinerary-liked:delete')")
    public CommonResult<Boolean> deleteUserItineraryLikedList(@RequestParam("ids") List<Long> ids) {
        userItineraryLikedService.deleteUserItineraryLikedListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得收藏行程")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-liked:query')")
    public CommonResult<UserItineraryLikedRespVO> getUserItineraryLiked(@RequestParam("id") Long id) {
        UserItineraryLikedDO userItineraryLiked = userItineraryLikedService.getUserItineraryLiked(id);
        return success(BeanUtils.toBean(userItineraryLiked, UserItineraryLikedRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得收藏行程分页")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-liked:query')")
    @TransMethodResult
    public CommonResult<PageResult<UserItineraryLikedRespVO>> getUserItineraryLikedPage(@Valid UserItineraryLikedPageReqVO pageReqVO) {
        PageResult<UserItineraryLikedDO> pageResult = userItineraryLikedService.getUserItineraryLikedPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, UserItineraryLikedRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出收藏行程 Excel")
    @PreAuthorize("@ss.hasPermission('gift:user-itinerary-liked:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportUserItineraryLikedExcel(@Valid UserItineraryLikedPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<UserItineraryLikedDO> list = userItineraryLikedService.getUserItineraryLikedPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "收藏行程.xls", "数据", UserItineraryLikedRespVO.class,
                        BeanUtils.toBean(list, UserItineraryLikedRespVO.class));
    }

}
