package cn.iocoder.yudao.module.gift.dal.redis.location;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.module.gift.dal.redis.RedisKeyConstants.USER_CURRENT_CITY;

/** 会员当前城市 Redis DAO。 */
@Repository
public class UserCityRedisDAO {

    private static final long CACHE_EXPIRE_DAYS = 30;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void set(Long memberId, UserCity city) {
        stringRedisTemplate.opsForValue().set(getKey(memberId), JsonUtils.toJsonString(city),
                CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    public UserCity get(Long memberId) {
        return JsonUtils.parseObject(stringRedisTemplate.opsForValue().get(getKey(memberId)), UserCity.class);
    }

    private static String getKey(Long memberId) {
        return USER_CURRENT_CITY.formatted(memberId);
    }

    public record UserCity(Long cityId, String cityName) {
    }

}
