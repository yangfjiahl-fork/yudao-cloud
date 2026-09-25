package cn.iocoder.yudao.module.gift.service.usercity;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.dal.redis.location.UserCityRedisDAO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserCityServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private UserCityServiceImpl userCityService;
    @Mock
    private UserCityRedisDAO userCityRedisDAO;

    @Test
    void setUserCityStoresCity() {
        userCityService.setUserCity(288L, 330100L, "杭州市");

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
