package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

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

}
