package cn.iocoder.yudao.module.gift.controller.app.itinerary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.gift.service.itinerary.ItineraryMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @GetMapping("/explore/map")
    @Operation(summary = "获得探索地图 POI")
    @Parameters({
            @Parameter(name = "cityId", description = "优先使用的城市行政区编号", example = "330100"),
            @Parameter(name = "latitude", description = "用户授权的高德 GCJ-02 纬度", example = "30.274084"),
            @Parameter(name = "longitude", description = "用户授权的高德 GCJ-02 经度", example = "120.155070"),
            @Parameter(name = "keyword", description = "地点搜索关键词", example = "亲子"),
            @Parameter(name = "category", description = "recommended/hotel/food/shopping/sightseeing", example = "recommended")
    })
    public CommonResult<ItineraryMapService.ExploreMap> getExploreMap(
            @RequestParam(value = "cityId", required = false) Long cityId,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "pageNo", defaultValue = "1") @Min(1) Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") @Min(1) @Max(50) Integer pageSize) {
        return success(itineraryMapService.getExploreMap(new ItineraryMapService.ExploreMapRequest(cityId, latitude, longitude,
                keyword, category, pageNo, pageSize)));
    }

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
