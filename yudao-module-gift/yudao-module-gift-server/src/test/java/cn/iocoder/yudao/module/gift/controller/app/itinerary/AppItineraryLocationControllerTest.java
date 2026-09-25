package cn.iocoder.yudao.module.gift.controller.app.itinerary;

import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.service.itinerary.ItineraryLocationService;
import cn.iocoder.yudao.module.gift.service.usercity.UserCityService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppItineraryLocationControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppItineraryLocationController controller;
    @Mock
    private ItineraryLocationService itineraryLocationService;
    @Mock
    private UserCityService userCityService;

    @Test
    void refreshGeocode_shouldIdentifyAndCacheCurrentUserCity() {
        Long memberId = 288L;
        BigDecimal longitude = new BigDecimal("120.155070");
        BigDecimal latitude = new BigDecimal("30.274084");
        String clientIp = "114.114.114.114";
        when(itineraryLocationService.identifyCurrentCity(longitude, latitude, clientIp))
                .thenReturn(new ItineraryLocationService.Location(
                        "浙江省", "杭州市", "西湖区", "330106", "浙江省杭州市西湖区西湖街道"));

        try (MockedStatic<SecurityFrameworkUtils> securityMock = mockStatic(SecurityFrameworkUtils.class);
             MockedStatic<ServletUtils> servletMock = mockStatic(ServletUtils.class)) {
            securityMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(memberId);
            servletMock.when(ServletUtils::getClientIP).thenReturn(clientIp);

            var result = controller.refreshGeocode(longitude, latitude);

            assertEquals("杭州市", result.getData().getCity());
            verify(itineraryLocationService).identifyCurrentCity(longitude, latitude, clientIp);
            verify(userCityService).setUserCity(memberId, 330100L, "杭州市");
        }
    }

}
