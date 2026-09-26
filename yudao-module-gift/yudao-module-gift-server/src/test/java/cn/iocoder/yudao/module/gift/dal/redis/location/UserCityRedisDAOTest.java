package cn.iocoder.yudao.module.gift.dal.redis.location;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserCityRedisDAOTest extends BaseMockitoUnitTest {

    @InjectMocks
    private UserCityRedisDAO userCityRedisDAO;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void setStoresCurrentCityForThirtyDays() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        UserCityRedisDAO.UserCity city = new UserCityRedisDAO.UserCity(330100L, "杭州市");

        userCityRedisDAO.set(288L, city);

        verify(valueOperations).set("gift:member:current-city:288", JsonUtils.toJsonString(city), 30, TimeUnit.DAYS);
    }

    @Test
    void getReturnsCachedCurrentCity() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("gift:member:current-city:288"))
                .thenReturn("{\"cityId\":330100,\"cityName\":\"杭州市\"}");

        UserCityRedisDAO.UserCity result = userCityRedisDAO.get(288L);

        assertEquals(330100L, result.cityId());
        assertEquals("杭州市", result.cityName());
    }

}
