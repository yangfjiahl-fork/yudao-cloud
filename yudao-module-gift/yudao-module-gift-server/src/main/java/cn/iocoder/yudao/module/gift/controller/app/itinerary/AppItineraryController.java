package cn.iocoder.yudao.module.gift.controller.app.itinerary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryCityPageReqVO;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryPageReqVO;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryRespVO;
import cn.iocoder.yudao.module.gift.controller.common.vo.ItineraryPageRespVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerary.ItineraryDO;
import cn.iocoder.yudao.module.gift.service.itinerary.ItineraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.dromara.core.trans.anno.TransMethodResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 通用行程")
@RestController
@RequestMapping("/gift/itinerary")
@Validated
public class AppItineraryController {

    @Resource
    private ItineraryService itineraryService;

    @GetMapping("/page")
    @Operation(summary = "获得通用行程分页")
    @TransMethodResult
    public CommonResult<PageResult<ItineraryPageRespVO>> getItineraryPage(
            @Valid AppItineraryPageReqVO pageReqVO) {
        PageResult<ItineraryDO> pageResult = itineraryService.getItineraryPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ItineraryPageRespVO.class));
    }

    @GetMapping("/city-page")
    @Operation(summary = "按城市获得通用行程分页")
    @TransMethodResult
    public CommonResult<PageResult<ItineraryPageRespVO>> getItineraryCityPage(
            @Valid AppItineraryCityPageReqVO pageReqVO) {
        PageResult<ItineraryDO> pageResult = itineraryService.getItineraryCityPage(getLoginUserId(), pageReqVO);
        return success(BeanUtils.toBean(pageResult, ItineraryPageRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得通用行程详情")
    @Parameter(name = "id", description = "行程编号", required = true, example = "1")
    @TransMethodResult
    public CommonResult<AppItineraryRespVO> getItinerary(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(itineraryService.getItinerary(id), AppItineraryRespVO.class));
    }

}
