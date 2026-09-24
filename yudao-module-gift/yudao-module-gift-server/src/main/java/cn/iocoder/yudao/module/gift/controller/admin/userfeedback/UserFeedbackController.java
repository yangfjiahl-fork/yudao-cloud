package cn.iocoder.yudao.module.gift.controller.admin.userfeedback;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.gift.controller.admin.userfeedback.vo.UserFeedbackPageReqVO;
import cn.iocoder.yudao.module.gift.controller.admin.userfeedback.vo.UserFeedbackRespVO;
import cn.iocoder.yudao.module.gift.controller.admin.userfeedback.vo.UserFeedbackSaveReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.userfeedback.UserFeedbackDO;
import cn.iocoder.yudao.module.gift.service.userfeedback.UserFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 用户反馈")
@RestController
@RequestMapping("/gift/user-feedback")
@Validated
public class UserFeedbackController {

    @Resource
    private UserFeedbackService userFeedbackService;

    @PostMapping("/create")
    @Operation(summary = "创建用户反馈")
    @PreAuthorize("@ss.hasPermission('gift:user-feedback:create')")
    public CommonResult<Long> createUserFeedback(@Valid @RequestBody UserFeedbackSaveReqVO createReqVO) {
        return success(userFeedbackService.createUserFeedback(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户反馈")
    @PreAuthorize("@ss.hasPermission('gift:user-feedback:update')")
    public CommonResult<Boolean> updateUserFeedback(@Valid @RequestBody UserFeedbackSaveReqVO updateReqVO) {
        userFeedbackService.updateUserFeedback(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户反馈")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('gift:user-feedback:delete')")
    public CommonResult<Boolean> deleteUserFeedback(@RequestParam("id") Long id) {
        userFeedbackService.deleteUserFeedback(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除用户反馈")
    @PreAuthorize("@ss.hasPermission('gift:user-feedback:delete')")
    public CommonResult<Boolean> deleteUserFeedbackList(@RequestParam("ids") List<Long> ids) {
        userFeedbackService.deleteUserFeedbackListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户反馈")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('gift:user-feedback:query')")
    public CommonResult<UserFeedbackRespVO> getUserFeedback(@RequestParam("id") Long id) {
        UserFeedbackDO userFeedback = userFeedbackService.getUserFeedback(id);
        return success(BeanUtils.toBean(userFeedback, UserFeedbackRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户反馈分页")
    @PreAuthorize("@ss.hasPermission('gift:user-feedback:query')")
    public CommonResult<PageResult<UserFeedbackRespVO>> getUserFeedbackPage(@Valid UserFeedbackPageReqVO pageReqVO) {
        PageResult<UserFeedbackDO> pageResult = userFeedbackService.getUserFeedbackPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, UserFeedbackRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用户反馈 Excel")
    @PreAuthorize("@ss.hasPermission('gift:user-feedback:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportUserFeedbackExcel(@Valid UserFeedbackPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<UserFeedbackDO> list = userFeedbackService.getUserFeedbackPage(pageReqVO).getList();
        ExcelUtils.write(response, "用户反馈.xls", "数据", UserFeedbackRespVO.class,
                BeanUtils.toBean(list, UserFeedbackRespVO.class));
    }

}
