package cn.iocoder.yudao.module.gift.controller.app.trip;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripLocationRespVO;
import cn.iocoder.yudao.module.gift.framework.geo.core.AmapGeocodingClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 旅行定位")
@RestController
@RequestMapping("/ai/trip/location")
@Validated
public class AppTripLocationController {

    @Resource
    private AmapGeocodingClient amapGeocodingClient;

    @GetMapping("/reverse-geocode")
    @Operation(summary = "根据经纬度获取城市", description = "经纬度需使用高德坐标系（GCJ-02），参数顺序为经度、纬度")
    @Parameters({
            @Parameter(name = "longitude", description = "经度", required = true, example = "120.155070"),
            @Parameter(name = "latitude", description = "纬度", required = true, example = "30.274084")
    })
    public CommonResult<AppTripLocationRespVO> reverseGeocode(
            @RequestParam("longitude")
            @NotNull(message = "经度不能为空")
            @DecimalMin(value = "-180", message = "经度必须在 -180 到 180 之间")
            @DecimalMax(value = "180", message = "经度必须在 -180 到 180 之间")
            @Digits(integer = 3, fraction = 6, message = "经度最多保留 6 位小数") BigDecimal longitude,
            @RequestParam("latitude")
            @NotNull(message = "纬度不能为空")
            @DecimalMin(value = "-90", message = "纬度必须在 -90 到 90 之间")
            @DecimalMax(value = "90", message = "纬度必须在 -90 到 90 之间")
            @Digits(integer = 2, fraction = 6, message = "纬度最多保留 6 位小数") BigDecimal latitude) {
        AmapGeocodingClient.Location location = amapGeocodingClient.reverseGeocode(longitude, latitude);
        return success(AppTripLocationRespVO.from(location));
    }

}
