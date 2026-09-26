package cn.iocoder.yudao.module.gift.service.usercity;

import cn.iocoder.yudao.module.gift.dal.dataobject.memberlocationhistory.MemberLocationHistoryDO;
import cn.iocoder.yudao.module.gift.dal.mysql.memberlocationhistory.MemberLocationHistoryMapper;
import cn.iocoder.yudao.module.gift.dal.redis.location.UserCityRedisDAO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

/** 用户当前城市服务实现。 */
@Service
public class UserCityServiceImpl implements UserCityService {

    private static final String SOURCE_TYPE_COORDINATE = "COORDINATE";
    private static final String SOURCE_TYPE_IP = "IP";
    private static final Long DEFAULT_CITY_ID = 310100L;
    private static final String DEFAULT_CITY_NAME = "上海市";

    @Resource
    private UserCityRedisDAO userCityRedisDAO;
    @Resource
    private MemberLocationHistoryMapper memberLocationHistoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setUserCity(Long memberId, Long cityId, String cityName,
                            BigDecimal longitude, BigDecimal latitude, String clientIp) {
        boolean locatedByCoordinate = longitude != null && latitude != null;
        String sourceType = locatedByCoordinate ? SOURCE_TYPE_COORDINATE : SOURCE_TYPE_IP;
        BigDecimal recordedLongitude = locatedByCoordinate ? longitude : null;
        BigDecimal recordedLatitude = locatedByCoordinate ? latitude : null;
        String recordedIp = locatedByCoordinate ? null : clientIp;
        MemberLocationHistoryDO latest = memberLocationHistoryMapper.selectLatestByMemberId(memberId);
        if (hasLocationChanged(latest, sourceType, recordedLongitude, recordedLatitude, recordedIp)) {
            memberLocationHistoryMapper.insert(MemberLocationHistoryDO.builder()
                    .memberId(memberId)
                    .sourceType(sourceType)
                    .longitude(recordedLongitude)
                    .latitude(recordedLatitude)
                    .ip(recordedIp)
                    .cityId(cityId)
                    .cityName(cityName)
                    .build());
        }
        userCityRedisDAO.set(memberId, new UserCityRedisDAO.UserCity(cityId, cityName));
    }

    @Override
    public UserCity getUserCity(Long memberId) {
        UserCityRedisDAO.UserCity city = userCityRedisDAO.get(memberId);
        if (city != null) {
            return new UserCity(city.cityId(), city.cityName());
        }
        MemberLocationHistoryDO latest = memberLocationHistoryMapper.selectLatestByMemberId(memberId);
        city = latest != null
                ? new UserCityRedisDAO.UserCity(latest.getCityId(), latest.getCityName())
                : new UserCityRedisDAO.UserCity(DEFAULT_CITY_ID, DEFAULT_CITY_NAME);
        userCityRedisDAO.set(memberId, city);
        return new UserCity(city.cityId(), city.cityName());
    }

    private static boolean hasLocationChanged(MemberLocationHistoryDO latest, String sourceType,
                                              BigDecimal longitude, BigDecimal latitude, String ip) {
        if (latest == null || !sourceType.equals(latest.getSourceType())) {
            return true;
        }
        if (SOURCE_TYPE_COORDINATE.equals(sourceType)) {
            return latest.getLongitude() == null || latest.getLatitude() == null
                    || latest.getLongitude().compareTo(longitude) != 0
                    || latest.getLatitude().compareTo(latitude) != 0;
        }
        return !Objects.equals(latest.getIp(), ip);
    }

}
