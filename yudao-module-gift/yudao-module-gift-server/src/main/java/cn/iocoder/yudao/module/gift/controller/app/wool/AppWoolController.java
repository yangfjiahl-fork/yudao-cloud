package cn.iocoder.yudao.module.gift.controller.app.wool;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.gift.controller.app.wool.vo.AppWoolRespVO;
import cn.iocoder.yudao.module.gift.controller.app.wool.vo.AppWoolSummaryRespVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.wool.WoolDO;
import cn.iocoder.yudao.module.gift.service.wool.WoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 羊毛")
@RestController
@RequestMapping("/gift/wool")
@Validated
public class AppWoolController {

    @Resource
    private WoolService woolService;

    @GetMapping("/list-init")
    @Operation(summary = "获得当前用户待收取羊毛列表")
    public CommonResult<List<AppWoolRespVO>> getWaitReceiveWoolList() {
        List<WoolDO> list = woolService.getWaitReceiveWoolList(getLoginUserId());
        return success(BeanUtils.toBean(list, AppWoolRespVO.class));
    }

    @PostMapping("/receive")
    @Operation(summary = "收取羊毛")
    @Parameter(name = "id", description = "羊毛编号", required = true, example = "1024")
    public CommonResult<Integer> receiveWool(@RequestParam("id") Long id) {
        return success(woolService.receiveWool(getLoginUserId(), id));
    }

    @GetMapping("/summary")
    @Operation(summary = "获得当前用户羊毛汇总信息")
    public CommonResult<AppWoolSummaryRespVO> getWoolSummary(
            @Parameter(description = "是否需要统计本月获取羊毛总量", example = "true")
            @RequestParam(value = "needThisMonth", required = false, defaultValue = "false") Boolean needThisMonth,
            @Parameter(description = "是否需要统计历史获取羊毛总量", example = "true")
            @RequestParam(value = "needHistoryTotal", required = false, defaultValue = "false") Boolean needHistoryTotal) {
        Long memberId = getLoginUserId();
        AppWoolSummaryRespVO respVO = new AppWoolSummaryRespVO()
                .setTodayReceivedAmount(woolService.getTodayReceivedWoolAmount(memberId));
        if (Boolean.TRUE.equals(needThisMonth)) {
            respVO.setThisMonthReceivedAmount(woolService.getThisMonthReceivedWoolAmount(memberId));
        }
        if (Boolean.TRUE.equals(needHistoryTotal)) {
            respVO.setHistoryTotalReceivedAmount(woolService.getHistoryTotalReceivedWoolAmount(memberId));
        }
        return success(respVO);
    }

}
