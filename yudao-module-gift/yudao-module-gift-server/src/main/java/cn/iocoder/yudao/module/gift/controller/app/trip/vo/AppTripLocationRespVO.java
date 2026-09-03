package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.enums.AreaTypeEnum;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.gift.framework.geo.core.AmapGeocodingClient;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 经纬度定位 Response VO")
@Data
public class AppTripLocationRespVO {

    @Schema(description = "省级地区编号", example = "330000")
    private Long provinceId;

    @Schema(description = "市级地区编号", example = "330100")
    private Long cityId;

    @Schema(description = "区县级地区编号", example = "330106")
    private Long districtId;

    @Schema(description = "省份名称", example = "浙江省")
    private String province;

    @Schema(description = "城市名称", example = "杭州市")
    private String city;

    @Schema(description = "区县名称", example = "西湖区")
    private String district;

    @Schema(description = "高德行政区编码", example = "330106")
    private String adcode;

    @Schema(description = "格式化地址", example = "浙江省杭州市西湖区西湖街道")
    private String formattedAddress;

    public static AppTripLocationRespVO from(AmapGeocodingClient.Location location) {
        long districtId = Long.parseLong(location.adcode());
        // 省直辖县在高德响应中 city 为空，归一化后 city 与 district 相同，adcode 本身即城市编号。
        long cityId = location.city().equals(location.district()) ? districtId : districtId / 100 * 100;
        return new AppTripLocationRespVO()
                .setProvinceId(districtId / 10000 * 10000)
                .setCityId(cityId)
                .setDistrictId(districtId)
                .setProvince(location.province())
                .setCity(location.city())
                .setDistrict(location.district())
                .setAdcode(location.adcode())
                .setFormattedAddress(location.formattedAddress());
    }

    /**
     * 基于本地 IP 库的城市级定位结果构建响应。
     *
     * IP 库通常只能精确到城市，因此 district 相关字段会保持为空。
     */
    public static AppTripLocationRespVO fromIpArea(Area area) {
        if (area == null) {
            return new AppTripLocationRespVO();
        }
        Area province = findAncestor(area, AreaTypeEnum.PROVINCE);
        Area city = findAncestor(area, AreaTypeEnum.CITY);
        Area district = findAncestor(area, AreaTypeEnum.DISTRICT);
        return new AppTripLocationRespVO()
                .setProvinceId(toLong(province))
                .setCityId(toLong(city))
                .setDistrictId(toLong(district))
                .setProvince(province == null ? null : province.getName())
                .setCity(city == null ? null : city.getName())
                .setDistrict(district == null ? null : district.getName())
                .setAdcode(String.valueOf(area.getId()))
                .setFormattedAddress(AreaUtils.format(area.getId(), ""));
    }

    private static Area findAncestor(Area area, AreaTypeEnum type) {
        for (Area current = area; current != null; current = current.getParent()) {
            if (type.getType().equals(current.getType())) {
                return current;
            }
        }
        return null;
    }

    private static Long toLong(Area area) {
        return area == null ? null : area.getId().longValue();
    }

}
