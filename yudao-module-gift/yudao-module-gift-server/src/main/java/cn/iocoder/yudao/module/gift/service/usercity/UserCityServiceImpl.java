package cn.iocoder.yudao.module.gift.service.usercity;

import cn.iocoder.yudao.module.gift.dal.redis.location.UserCityRedisDAO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/** 用户当前城市服务实现。 */
@Service
public class UserCityServiceImpl implements UserCityService {

    @Resource
    private UserCityRedisDAO userCityRedisDAO;

    @Override
    public void setUserCity(Long memberId, Long cityId, String cityName) {
        userCityRedisDAO.set(memberId, new UserCityRedisDAO.UserCity(cityId, cityName));
    }

    @Override
    public UserCity getUserCity(Long memberId) {
        UserCityRedisDAO.UserCity city = userCityRedisDAO.get(memberId);
        return city == null ? null : new UserCity(city.cityId(), city.cityName());
    }

}
