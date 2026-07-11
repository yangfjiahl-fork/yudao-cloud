package cn.iocoder.yudao.module.gift.controller.admin.wool;

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

import cn.iocoder.yudao.module.gift.controller.admin.wool.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.wool.WoolDO;
import cn.iocoder.yudao.module.gift.service.wool.WoolService;

@Tag(name = "管理后台 - 羊毛")
@RestController
@RequestMapping("/gift/wool")
@Validated
public class WoolController {

    @Resource
    private WoolService woolService;

    @PostMapping("/create")
    @Operation(summary = "创建羊毛")
    @PreAuthorize("@ss.hasPermission('gift:wool:create')")
    public CommonResult<Long> createWool(@Valid @RequestBody WoolSaveReqVO createReqVO) {
        return success(woolService.createWool(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新羊毛")
    @PreAuthorize("@ss.hasPermission('gift:wool:update')")
    public CommonResult<Boolean> updateWool(@Valid @RequestBody WoolSaveReqVO updateReqVO) {
        woolService.updateWool(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除羊毛")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('gift:wool:delete')")
    public CommonResult<Boolean> deleteWool(@RequestParam("id") Long id) {
        woolService.deleteWool(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除羊毛")
                @PreAuthorize("@ss.hasPermission('gift:wool:delete')")
    public CommonResult<Boolean> deleteWoolList(@RequestParam("ids") List<Long> ids) {
        woolService.deleteWoolListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得羊毛")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('gift:wool:query')")
    public CommonResult<WoolRespVO> getWool(@RequestParam("id") Long id) {
        WoolDO wool = woolService.getWool(id);
        return success(BeanUtils.toBean(wool, WoolRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得羊毛分页")
    @PreAuthorize("@ss.hasPermission('gift:wool:query')")
    public CommonResult<PageResult<WoolRespVO>> getWoolPage(@Valid WoolPageReqVO pageReqVO) {
        PageResult<WoolDO> pageResult = woolService.getWoolPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WoolRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出羊毛 Excel")
    @PreAuthorize("@ss.hasPermission('gift:wool:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWoolExcel(@Valid WoolPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WoolDO> list = woolService.getWoolPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "羊毛.xls", "数据", WoolRespVO.class,
                        BeanUtils.toBean(list, WoolRespVO.class));
    }

}