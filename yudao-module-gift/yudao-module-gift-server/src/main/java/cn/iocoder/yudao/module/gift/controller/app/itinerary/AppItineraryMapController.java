package cn.iocoder.yudao.module.gift.controller.app.itinerary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.gift.service.itinerary.ItineraryMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 旅行地图")
@RestController
@RequestMapping("/gift/trip")
@Validated
public class AppItineraryMapController {

    @Resource
    private ItineraryMapService itineraryMapService;

    @GetMapping("/footprints/map")
    @Operation(summary = "获得我的足迹地图")
    public CommonResult<ItineraryMapService.FootprintMap> getFootprintMap(
            @RequestParam(value = "year", required = false) Integer year) {
        return success(itineraryMapService.getFootprintMap(getLoginUserId(), year));
    }

    @GetMapping("/itineraries/{id}/map")
    @Operation(summary = "获得旅行行程地图")
    @Parameter(name = "id", description = "旅行行程编号", required = true, example = "1024")
    public CommonResult<ItineraryMapService.ItineraryMap> getItineraryMap(@PathVariable("id") Long itineraryId) {
        return success(itineraryMapService.getItineraryMap(getLoginUserId(), itineraryId));
    }

}
