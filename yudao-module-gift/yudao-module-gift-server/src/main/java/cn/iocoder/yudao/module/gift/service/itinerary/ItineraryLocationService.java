package cn.iocoder.yudao.module.gift.service.itinerary;

import java.math.BigDecimal;
import java.util.List;

/** APP 旅行定位与周边地点查询服务。 */
public interface ItineraryLocationService {

    Location reverseGeocode(BigDecimal longitude, BigDecimal latitude);

    PlaceSearchResult searchNearbyPlaces(PlaceSearchRequest request);

    record Location(String province, String city, String district, String adcode, String formattedAddress) {
    }

    record PlaceSearchRequest(String keyword, String category, BigDecimal longitude, BigDecimal latitude,
                              Integer radius, Integer pageNo, Integer pageSize) {
    }

    record PlaceSearchResult(Long total, List<Place> places) {
    }

    record Place(String poiId, String name, String address, BigDecimal longitude, BigDecimal latitude,
                 String type, String typeCode, String province, String city, String district, String adcode,
                 Long distanceMeters, String telephone, String photoUrl) {
    }

}
