package cn.iocoder.yudao.module.gift.controller.app.itinerary.vo;

import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.enums.AreaTypeEnum;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.gift.service.itinerary.ItineraryLocationService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 城市识别 Response VO")
@Data
public class AppItineraryLocationRespVO {

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

    public static AppItineraryLocationRespVO from(ItineraryLocationService.Location location) {
        long districtId = Long.parseLong(location.adcode());
        Long cityId = resolveCityId(location, districtId);
        Long resolvedDistrictId = isBlank(location.district()) ? null : districtId;
        return new AppItineraryLocationRespVO()
                .setProvinceId(districtId / 10000 * 10000)
                .setCityId(cityId)
                .setDistrictId(resolvedDistrictId)
                .setProvince(location.province())
                .setCity(location.city())
                .setDistrict(isBlank(location.district()) ? null : location.district())
                .setAdcode(location.adcode())
                .setFormattedAddress(location.formattedAddress());
    }

    private static Long resolveCityId(ItineraryLocationService.Location location, long adcode) {
        // 省直辖县没有单独的市级节点，直接使用区县编码。
        if (location.city().equals(location.district())) {
            return adcode;
        }
        Area city = AreaUtils.getArea(location.city());
        if (city != null && city.getId() != null && AreaTypeEnum.CITY.getType().equals(city.getType())) {
            return city.getId().longValue();
        }
        return adcode / 100 * 100;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}
