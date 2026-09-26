package cn.iocoder.yudao.module.gift.service.usercity;

import java.math.BigDecimal;

/** 用户当前城市服务。 */
public interface UserCityService {

    void setUserCity(Long memberId, Long cityId, String cityName,
                     BigDecimal longitude, BigDecimal latitude, String clientIp);

    UserCity getUserCity(Long memberId);

    record UserCity(Long cityId, String cityName) {
    }

}
