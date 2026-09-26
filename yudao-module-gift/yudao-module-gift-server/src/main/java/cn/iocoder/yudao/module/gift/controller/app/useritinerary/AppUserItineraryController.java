package cn.iocoder.yudao.module.gift.controller.app.useritinerary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.gift.controller.app.useritinerary.vo.AppUserItineraryRespVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import cn.iocoder.yudao.module.gift.service.useritinerary.UserItineraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 用户行程")
@RestController
@RequestMapping("/gift/user-itinerary")
@Validated
public class AppUserItineraryController {

    @Resource
    private UserItineraryService userItineraryService;

    @GetMapping("/page")
    @Operation(summary = "获得我的行程分页")
    public CommonResult<PageResult<AppUserItineraryRespVO>> getUserItineraryPage(@Valid PageParam pageReqVO) {
        PageResult<UserItineraryDO> pageResult = userItineraryService
                .getUserItineraryPage(getLoginUserId(), pageReqVO);
        return success(BeanUtils.toBean(pageResult, AppUserItineraryRespVO.class));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除我的行程")
    @Parameter(name = "id", description = "用户行程编号", required = true, example = "1024")
    public CommonResult<Boolean> deleteUserItinerary(@RequestParam("id") Long id) {
        userItineraryService.deleteUserItinerary(getLoginUserId(), id);
        return success(true);
    }

}
