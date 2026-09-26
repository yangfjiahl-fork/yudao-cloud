package cn.iocoder.yudao.module.gift.service.itinerary;

import java.math.BigDecimal;
import java.util.List;

/** APP 旅行定位与地点查询服务。 */
public interface ItineraryLocationService {

    Location reverseGeocode(BigDecimal longitude, BigDecimal latitude);

    Location identifyCurrentCity(BigDecimal longitude, BigDecimal latitude, String clientIp);

    PlaceSearchResult searchPlaces(PlaceSearchRequest request);

    Weather getCurrentWeather(String cityCode);

    record Location(String province, String city, String district, String adcode, String formattedAddress) {
    }

    record PlaceSearchRequest(String keyword, String category, Long cityId, BigDecimal longitude, BigDecimal latitude,
                              Integer radius, Integer pageNo, Integer pageSize) {
    }

    record PlaceSearchResult(Long total, List<Place> places) {
    }

    record Place(String poiId, String name, String address, BigDecimal longitude, BigDecimal latitude,
                 String type, String typeCode, String province, String city, String district, String adcode,
                 Long distanceMeters, String telephone, String photoUrl) {
    }

    record Weather(String city, Integer temperature, String condition, Integer humidity,
                   String windDirection, String windPower, String queryTime) {
    }

}
