package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.gift.framework.geo.core.AmapGeocodingClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTripLocationRespVOTest {

    @Test
    void testFrom() {
        AmapGeocodingClient.Location location = new AmapGeocodingClient.Location(
                "浙江省", "杭州市", "西湖区", "330106", "浙江省杭州市西湖区西湖街道");

        AppTripLocationRespVO result = AppTripLocationRespVO.from(location);

        assertEquals(330000L, result.getProvinceId());
        assertEquals(330100L, result.getCityId());
        assertEquals(330106L, result.getDistrictId());
        assertEquals("杭州市", result.getCity());
    }

    @Test
    void testFromProvinceDirectCounty() {
        AmapGeocodingClient.Location location = new AmapGeocodingClient.Location(
                "河南省", "济源市", "济源市", "419001", "河南省济源市沁园街道");

        AppTripLocationRespVO result = AppTripLocationRespVO.from(location);

        assertEquals(410000L, result.getProvinceId());
        assertEquals(419001L, result.getCityId());
        assertEquals(419001L, result.getDistrictId());
    }

    @Test
    void testFromIpArea() {
        AppTripLocationRespVO result = AppTripLocationRespVO.fromIpArea(AreaUtils.getArea(420600));

        assertEquals(420000L, result.getProvinceId());
        assertEquals(420600L, result.getCityId());
        assertEquals("湖北省", result.getProvince());
        assertEquals("襄阳市", result.getCity());
        assertEquals(null, result.getDistrictId());
        assertEquals(null, result.getDistrict());
    }

    @Test
    void testFromIpArea_defaultPudong() {
        AppTripLocationRespVO result = AppTripLocationRespVO.fromIpArea(AreaUtils.getArea(310115));

        assertEquals(310000L, result.getProvinceId());
        assertEquals(310100L, result.getCityId());
        assertEquals(310115L, result.getDistrictId());
        assertEquals("上海市", result.getProvince());
        assertEquals("上海市", result.getCity());
        assertEquals("浦东新区", result.getDistrict());
    }

}
