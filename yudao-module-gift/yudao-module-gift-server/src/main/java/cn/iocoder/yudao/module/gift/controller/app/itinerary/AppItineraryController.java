package cn.iocoder.yudao.module.gift.controller.app.itinerary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryPageReqVO;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryRespVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerary.ItineraryDO;
import cn.iocoder.yudao.module.gift.service.itinerary.ItineraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 通用行程")
@RestController
@RequestMapping("/gift/itinerary")
@Validated
public class AppItineraryController {

    @Resource
    private ItineraryService itineraryService;

    @GetMapping("/page")
    @Operation(summary = "获得通用行程分页")
    public CommonResult<PageResult<AppItineraryRespVO>> getItineraryPage(
            @Valid AppItineraryPageReqVO pageReqVO) {
        PageResult<ItineraryDO> pageResult = itineraryService.getItineraryPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AppItineraryRespVO.class));
    }

}
