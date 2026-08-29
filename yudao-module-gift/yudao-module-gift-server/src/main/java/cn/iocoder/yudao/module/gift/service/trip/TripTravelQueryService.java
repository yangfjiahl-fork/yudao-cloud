package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.framework.trip.provider.place.TravelPlaceQueryClient;
import cn.iocoder.yudao.module.gift.framework.trip.provider.place.TravelPlaceQueryClientFacade;
import cn.iocoder.yudao.module.gift.framework.trip.provider.scenic.ScenicSpotQueryClient;
import cn.iocoder.yudao.module.gift.framework.trip.provider.scenic.ScenicSpotQueryClientFacade;
import cn.iocoder.yudao.module.gift.framework.trip.provider.weather.WeatherClient;
import cn.iocoder.yudao.module.gift.framework.trip.provider.weather.WeatherClientFacade;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 旅行业务使用的统一查询服务；供应商选择和协议细节由 framework 层负责。 */
@Service
public class TripTravelQueryService {

    @Resource
    private WeatherClientFacade weatherClientFacade;
    @Resource
    private ScenicSpotQueryClientFacade scenicSpotQueryClientFacade;
    @Resource
    private TravelPlaceQueryClientFacade travelPlaceQueryClientFacade;

    /**
     * 查询城市当前天气。天气在短时间内变化有限，按城市缓存 10 分钟，避免每个 Slot 重复调用供应商。
     * 供应商失败会抛出异常，不会写入缓存；城市键统一按小写规范化。
     */
    @Cacheable(cacheNames = "tripWeather#10m",
            key = "T(cn.iocoder.yudao.module.gift.service.trip.TripTravelQueryService).weatherCacheKey(#city)",
            unless = "#result == null")
    public Weather getCurrentWeather(String city) {
        WeatherClient.CurrentWeather weather = weatherClientFacade.getCurrentWeather(city);
        return new Weather(weatherClientFacade.provider(), weather.city(), weather.temperature(), weather.condition(),
                weather.humidity(), weather.windDirection(), weather.windPower(), weather.queryTime());
    }

    public static String weatherCacheKey(String city) {
        return city == null ? "" : city.trim().toLowerCase(Locale.ROOT);
    }

    public List<ScenicSpot> queryScenicSpots(String city, String keyword, int limit) {
        ScenicSpotQueryClient.Response response = scenicSpotQueryClientFacade.query(new ScenicSpotQueryClient.Request()
                .setType(ScenicSpotQueryClient.QueryType.SCENIC_SPOT).setRegion(city).setKeyword(keyword).setPage(1));
        if (!Boolean.TRUE.equals(response.getSuccess())) {
            throw new IllegalStateException(StrUtil.blankToDefault(response.getMessage(), "景点查询失败"));
        }
        String provider = scenicSpotQueryClientFacade.provider();
        List<ScenicSpot> result = new ArrayList<>();
        JsonNode list = response.getData() == null ? null : response.getData().path("list");
        if (list == null || !list.isArray()) {
            return result;
        }
        for (JsonNode item : list) {
            if (result.size() >= Math.max(1, limit)) {
                break;
            }
            String name = text(item, "name", "scenicName", "title");
            if (StrUtil.isBlank(name)) {
                continue;
            }
            String location = text(item, "location");
            String[] coordinate = StrUtil.splitToArray(location, ',');
            result.add(new ScenicSpot(provider, text(item, "id", "scenicId", "code"), name,
                    text(item, "address", "addr"), coordinate.length > 0 ? coordinate[0] : text(item, "longitude", "lng"),
                    coordinate.length > 1 ? coordinate[1] : text(item, "latitude", "lat"), firstImage(item)));
        }
        return result;
    }

    public List<Place> queryHotels(String city, int limit) {
        return queryPlaces(TravelPlaceQueryClient.PlaceType.HOTEL, city, limit);
    }

    public List<Place> queryRestaurants(String city, int limit) {
        return queryPlaces(TravelPlaceQueryClient.PlaceType.RESTAURANT, city, limit);
    }

    private List<Place> queryPlaces(TravelPlaceQueryClient.PlaceType type, String city, int limit) {
        TravelPlaceQueryClient.Response response = travelPlaceQueryClientFacade.query(new TravelPlaceQueryClient.Request()
                .setType(type).setRegion(city).setLimit(limit));
        if (!Boolean.TRUE.equals(response.getSuccess())) {
            throw new IllegalStateException(StrUtil.blankToDefault(response.getMessage(), "旅行地点查询失败"));
        }
        String provider = travelPlaceQueryClientFacade.provider(type);
        return response.getPlaces().stream()
                .map(place -> new Place(provider, place.getExternalId(), place.getName(), place.getAddress(),
                        place.getLongitude(), place.getLatitude(), place.getImageUrl(), place.getTelephone(),
                        place.getRating(), place.getCost(), place.getTag()))
                .toList();
    }

    private static String firstImage(JsonNode item) {
        String imageUrl = text(item, "imageUrl", "image", "imgUrl", "cover");
        if (StrUtil.isNotBlank(imageUrl)) {
            return imageUrl;
        }
        JsonNode photos = item.path("photos");
        return photos.isArray() && !photos.isEmpty() ? text(photos.get(0), "url", "imageUrl") : "";
    }

    private static String text(JsonNode item, String... names) {
        for (String name : names) {
            String value = item.path(name).asText();
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return "";
    }

    public record Weather(String provider, String city, Integer temperature, String condition, Integer humidity,
                          String windDirection, String windPower, String queryTime) {
    }

    public record ScenicSpot(String provider, String externalId, String name, String address, String longitude,
                             String latitude, String imageUrl) {
    }

    public record Place(String provider, String externalId, String name, String address, String longitude,
                        String latitude, String imageUrl, String telephone, String rating, String cost, String tag) {
    }
}
