package cn.iocoder.yudao.module.gift.service.usercity;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.dal.dataobject.memberlocationhistory.MemberLocationHistoryDO;
import cn.iocoder.yudao.module.gift.dal.mysql.memberlocationhistory.MemberLocationHistoryMapper;
import cn.iocoder.yudao.module.gift.dal.redis.location.UserCityRedisDAO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserCityServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private UserCityServiceImpl userCityService;
    @Mock
    private UserCityRedisDAO userCityRedisDAO;
    @Mock
    private MemberLocationHistoryMapper memberLocationHistoryMapper;

    @Test
    void setUserCityStoresCoordinateLocation() {
        BigDecimal longitude = new BigDecimal("120.155070");
        BigDecimal latitude = new BigDecimal("30.274084");

        userCityService.setUserCity(288L, 330100L, "杭州市", longitude, latitude, "114.114.114.114");

        verify(userCityRedisDAO).set(288L, new UserCityRedisDAO.UserCity(330100L, "杭州市"));
        ArgumentCaptor<MemberLocationHistoryDO> captor = ArgumentCaptor.forClass(MemberLocationHistoryDO.class);
        verify(memberLocationHistoryMapper).insert(captor.capture());
        MemberLocationHistoryDO history = captor.getValue();
        assertEquals(288L, history.getMemberId());
        assertEquals("COORDINATE", history.getSourceType());
        assertEquals(longitude, history.getLongitude());
        assertEquals(latitude, history.getLatitude());
        assertNull(history.getIp());
        assertEquals(330100L, history.getCityId());
        assertEquals("杭州市", history.getCityName());
    }

    @Test
    void setUserCityStoresIpLocation() {
        userCityService.setUserCity(288L, 330100L, "杭州市", null, null, "114.114.114.114");

        ArgumentCaptor<MemberLocationHistoryDO> captor = ArgumentCaptor.forClass(MemberLocationHistoryDO.class);
        verify(memberLocationHistoryMapper).insert(captor.capture());
        MemberLocationHistoryDO history = captor.getValue();
        assertEquals("IP", history.getSourceType());
        assertNull(history.getLongitude());
        assertNull(history.getLatitude());
        assertEquals("114.114.114.114", history.getIp());
        verify(userCityRedisDAO).set(288L, new UserCityRedisDAO.UserCity(330100L, "杭州市"));
    }

    @Test
    void setUserCitySkipsHistoryWhenCoordinateHasNotChanged() {
        BigDecimal longitude = new BigDecimal("120.155070");
        BigDecimal latitude = new BigDecimal("30.274084");
        when(memberLocationHistoryMapper.selectLatestByMemberId(288L)).thenReturn(MemberLocationHistoryDO.builder()
                .memberId(288L)
                .sourceType("COORDINATE")
                .longitude(new BigDecimal("120.15507"))
                .latitude(new BigDecimal("30.274084"))
                .cityId(330100L)
                .cityName("杭州市")
                .build());

        userCityService.setUserCity(288L, 330100L, "杭州市", longitude, latitude, "114.114.114.114");

        verify(memberLocationHistoryMapper, never()).insert(any(MemberLocationHistoryDO.class));
        verify(userCityRedisDAO).set(288L, new UserCityRedisDAO.UserCity(330100L, "杭州市"));
    }

    @Test
    void getUserCityReturnsCachedCity() {
        when(userCityRedisDAO.get(288L)).thenReturn(new UserCityRedisDAO.UserCity(330100L, "杭州市"));

        UserCityService.UserCity result = userCityService.getUserCity(288L);

        assertEquals(330100L, result.cityId());
        assertEquals("杭州市", result.cityName());
    }

    @Test
    void getUserCityReturnsNullWhenCacheMissing() {
        assertNull(userCityService.getUserCity(288L));
    }

}
