package cn.iocoder.yudao.module.gift.service.usercity;

/** 用户当前城市服务。 */
public interface UserCityService {

    void setUserCity(Long memberId, Long cityId, String cityName);

    UserCity getUserCity(Long memberId);

    record UserCity(Long cityId, String cityName) {
    }

}
