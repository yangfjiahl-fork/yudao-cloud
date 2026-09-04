package cn.iocoder.yudao.module.gift.controller.app.trip;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.ip.core.utils.IPUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 旅行定位")
@RestController
@RequestMapping("/ai/trip/location")
@Validated
@Slf4j
public class AppTripLocationController {

    private static final int DEFAULT_IP_AREA_ID = 310115;

    @Resource
    private AmapGeocodingClient amapGeocodingClient;

    @GetMapping("/reverse-geocode")
    @Operation(summary = "根据经纬度获取城市", description = "经纬度均大于 0 时使用高德 GCJ-02 逆地理编码；任一小于等于 0 时使用客户端 IP 本地解析")
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
        if (longitude.signum() <= 0 || latitude.signum() <= 0) {
            return success(resolveByIp());
        }
        AmapGeocodingClient.Location location = amapGeocodingClient.reverseGeocode(longitude, latitude);
        return success(AppTripLocationRespVO.from(location));
    }

    private AppTripLocationRespVO resolveByIp() {
        try {
            String clientIp = ServletUtils.getClientIP();
            Area area = isPrivateOrLocalIp(clientIp) ? AreaUtils.getArea(DEFAULT_IP_AREA_ID) : IPUtils.getArea(clientIp);
            return AppTripLocationRespVO.fromIpArea(area);
        } catch (Exception e) {
            log.warn("[resolveByIp][本地 IP 地区解析失败]", e);
            return AppTripLocationRespVO.fromIpArea(AreaUtils.getArea(DEFAULT_IP_AREA_ID));
        }
    }

    static boolean isPrivateOrLocalIp(String ip) {
        if (ip == null || !ip.matches("[0-9a-fA-F:.]+")) {
            return true;
        }
        try {
            InetAddress address = InetAddress.getByName(ip);
            byte[] addressBytes = address.getAddress();
            boolean isIpv6UniqueLocal = addressBytes.length == 16 && (addressBytes[0] & 0xFE) == 0xFC;
            return address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress() || isIpv6UniqueLocal;
        } catch (UnknownHostException e) {
            return true;
        }
    }

}
