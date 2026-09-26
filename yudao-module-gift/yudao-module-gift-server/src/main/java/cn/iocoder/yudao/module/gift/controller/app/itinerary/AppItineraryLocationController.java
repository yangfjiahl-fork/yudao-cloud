package cn.iocoder.yudao.module.gift.controller.app.itinerary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryLocationRespVO;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryPlaceSearchReqVO;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryPlaceSearchRespVO;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryWeatherRespVO;
import cn.iocoder.yudao.module.gift.service.itinerary.ItineraryLocationService;
import cn.iocoder.yudao.module.gift.service.usercity.UserCityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 旅行定位")
@RestController
@RequestMapping("/ai/trip/location")
@Validated
public class AppItineraryLocationController {

    @Resource
    private ItineraryLocationService itineraryLocationService;
    @Resource
    private UserCityService userCityService;

    @GetMapping("/places")
    @Operation(summary = "搜索周边地点", description = "根据高德 GCJ-02 中心点搜索周边地点，可按业务 POI 分类和关键词过滤")
    public CommonResult<AppItineraryPlaceSearchRespVO> searchPlaces(@Valid AppItineraryPlaceSearchReqVO reqVO) {
        ItineraryLocationService.PlaceSearchResult result = itineraryLocationService.searchNearbyPlaces(
                new ItineraryLocationService.PlaceSearchRequest(reqVO.getKeyword(), reqVO.getCategory(), reqVO.getLongitude(),
                        reqVO.getLatitude(), reqVO.getRadius(), reqVO.getPageNo(), reqVO.getPageSize()));
        return success(AppItineraryPlaceSearchRespVO.from(result, reqVO.getPageNo(), reqVO.getPageSize()));
    }

    @GetMapping("/weather")
    @Operation(summary = "根据城市编码查询当前天气", description = "使用高德天气服务查询实况天气")
    @Parameter(name = "cityCode", description = "高德行政区划编码", required = true, example = "330100")
    public CommonResult<AppItineraryWeatherRespVO> getCurrentWeather(
            @RequestParam("cityCode")
            @NotBlank(message = "城市编码不能为空")
            @Pattern(regexp = "\\d{6}", message = "城市编码必须为 6 位数字") String cityCode) {
        return success(AppItineraryWeatherRespVO.from(itineraryLocationService.getCurrentWeather(cityCode)));
    }

    @GetMapping("/refresh-geocode")
    @Operation(summary = "刷新用户所在城市", description = "支持重复调用；经纬度均存在时按高德 GCJ-02 坐标识别，否则按客户端 IP 识别")
    @Parameters({
            @Parameter(name = "longitude", description = "经度；需与纬度同时传入", example = "120.155070"),
            @Parameter(name = "latitude", description = "纬度；需与经度同时传入", example = "30.274084")
    })
    public CommonResult<AppItineraryLocationRespVO> refreshGeocode(
            @RequestParam(value = "longitude", required = false)
            @DecimalMin(value = "-180", message = "经度必须在 -180 到 180 之间")
            @DecimalMax(value = "180", message = "经度必须在 -180 到 180 之间")
            @Digits(integer = 3, fraction = 6, message = "经度最多保留 6 位小数") BigDecimal longitude,
            @RequestParam(value = "latitude", required = false)
            @DecimalMin(value = "-90", message = "纬度必须在 -90 到 90 之间")
            @DecimalMax(value = "90", message = "纬度必须在 -90 到 90 之间")
            @Digits(integer = 2, fraction = 6, message = "纬度最多保留 6 位小数") BigDecimal latitude) {
        String clientIp = ServletUtils.getClientIP();
        ItineraryLocationService.Location location = itineraryLocationService.identifyCurrentCity(
                longitude, latitude, clientIp);
        AppItineraryLocationRespVO result = AppItineraryLocationRespVO.from(location);
        userCityService.setUserCity(getLoginUserId(), result.getCityId(), result.getCity(),
                longitude, latitude, clientIp);
        return success(result);
    }

}
