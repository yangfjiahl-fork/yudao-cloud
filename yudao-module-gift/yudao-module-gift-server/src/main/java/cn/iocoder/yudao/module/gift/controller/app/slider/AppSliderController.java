package cn.iocoder.yudao.module.gift.controller.app.slider;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.gift.controller.app.slider.vo.AppSliderItemRespVO;
import cn.iocoder.yudao.module.gift.enums.SliderPositionEnum;
import cn.iocoder.yudao.module.gift.service.slider.SliderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 轮播图")
@RestController
@RequestMapping("/gift/slider")
@Validated
public class AppSliderController {

    @Resource
    private SliderService sliderService;

    @GetMapping("/list")
    @Operation(summary = "获得轮播图列表")
    @Parameter(name = "positionCode", description = "轮播位置", required = true, example = "HOME_TOP")
    public CommonResult<List<AppSliderItemRespVO>> getSliderItemList(
            @RequestParam("positionCode")
            @NotBlank(message = "轮播位置不能为空")
            @InEnum(value = SliderPositionEnum.class, message = "轮播位置必须是 {value}") String positionCode) {
        return success(BeanUtils.toBean(sliderService.getSliderItemList(getLoginUserId(), positionCode),
                AppSliderItemRespVO.class));
    }

}
