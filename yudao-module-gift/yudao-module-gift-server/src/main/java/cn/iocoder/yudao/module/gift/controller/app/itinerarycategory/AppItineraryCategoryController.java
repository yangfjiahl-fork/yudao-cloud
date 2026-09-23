package cn.iocoder.yudao.module.gift.controller.app.itinerarycategory;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.gift.controller.app.itinerarycategory.vo.AppItineraryCategoryRespVO;
import cn.iocoder.yudao.module.gift.service.itinerarycategory.ItineraryCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 行程类别")
@RestController
@RequestMapping("/gift/itinerary-category")
@Validated
public class AppItineraryCategoryController {

    @Resource
    private ItineraryCategoryService itineraryCategoryService;

    @GetMapping("/list")
    @Operation(summary = "获得行程类别列表")
    @PermitAll
    public CommonResult<List<AppItineraryCategoryRespVO>> getItineraryCategoryList() {
        return success(BeanUtils.toBean(itineraryCategoryService.getItineraryCategoryList(),
                AppItineraryCategoryRespVO.class));
    }

}
