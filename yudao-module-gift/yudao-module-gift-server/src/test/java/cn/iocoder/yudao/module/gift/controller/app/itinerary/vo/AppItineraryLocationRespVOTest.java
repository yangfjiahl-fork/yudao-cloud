package cn.iocoder.yudao.module.gift.controller.app.itinerary.vo;

import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.gift.service.itinerary.ItineraryLocationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppItineraryLocationRespVOTest {

    @Test
    void testFrom() {
        ItineraryLocationService.Location location = new ItineraryLocationService.Location(
                "浙江省", "杭州市", "西湖区", "330106", "浙江省杭州市西湖区西湖街道");

        AppItineraryLocationRespVO result = AppItineraryLocationRespVO.from(location);

        assertEquals(330000L, result.getProvinceId());
        assertEquals(330100L, result.getCityId());
        assertEquals(330106L, result.getDistrictId());
        assertEquals("杭州市", result.getCity());
    }

    @Test
    void testFromProvinceDirectCounty() {
        ItineraryLocationService.Location location = new ItineraryLocationService.Location(
                "河南省", "济源市", "济源市", "419001", "河南省济源市沁园街道");

        AppItineraryLocationRespVO result = AppItineraryLocationRespVO.from(location);

        assertEquals(410000L, result.getProvinceId());
        assertEquals(419001L, result.getCityId());
        assertEquals(419001L, result.getDistrictId());
    }

    @Test
    void testFromIpArea() {
        AppItineraryLocationRespVO result = AppItineraryLocationRespVO.fromIpArea(AreaUtils.getArea(420600));

        assertEquals(420000L, result.getProvinceId());
        assertEquals(420600L, result.getCityId());
        assertEquals("湖北省", result.getProvince());
        assertEquals("襄阳市", result.getCity());
        assertEquals(null, result.getDistrictId());
        assertEquals(null, result.getDistrict());
    }

    @Test
    void testFromIpArea_defaultPudong() {
        AppItineraryLocationRespVO result = AppItineraryLocationRespVO.fromIpArea(AreaUtils.getArea(310115));

        assertEquals(310000L, result.getProvinceId());
        assertEquals(310100L, result.getCityId());
        assertEquals(310115L, result.getDistrictId());
        assertEquals("上海市", result.getProvince());
        assertEquals("上海市", result.getCity());
        assertEquals("浦东新区", result.getDistrict());
    }

}
