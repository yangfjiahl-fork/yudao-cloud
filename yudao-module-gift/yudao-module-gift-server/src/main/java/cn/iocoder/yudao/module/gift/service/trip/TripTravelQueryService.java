package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.framework.trip.provider.place.TravelPlaceQueryClient;
import cn.iocoder.yudao.module.gift.framework.trip.provider.place.TravelPlaceQueryClientFacade;
import cn.iocoder.yudao.module.gift.framework.trip.provider.route.amap.AmapRouteQueryClient;
import cn.iocoder.yudao.module.gift.framework.trip.provider.scenic.ScenicSpotQueryClient;
import cn.iocoder.yudao.module.gift.framework.trip.provider.scenic.ScenicSpotQueryClientFacade;
import cn.iocoder.yudao.module.gift.framework.trip.provider.weather.WeatherClient;
import cn.iocoder.yudao.module.gift.framework.trip.provider.weather.WeatherClientFacade;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 旅行业务使用的统一查询服务；供应商选择和协议细节由 framework 层负责。 */
@Service
public class TripTravelQueryService {

    private static final int POI_PAGE_SIZE = 25;

    @Resource
    private WeatherClientFacade weatherClientFacade;
    @Resource
    private ScenicSpotQueryClientFacade scenicSpotQueryClientFacade;
    @Resource
    private TravelPlaceQueryClientFacade travelPlaceQueryClientFacade;
    @Resource
    private AmapRouteQueryClient amapRouteQueryClient;

    /** 查询城市当前天气。 */
    public Weather getCurrentWeather(String city) {
        WeatherClient.CurrentWeather weather = weatherClientFacade.getCurrentWeather(city);
        return new Weather(weatherClientFacade.provider(), weather.city(), weather.temperature(), weather.condition(),
                weather.humidity(), weather.windDirection(), weather.windPower(), weather.queryTime());
    }

    public List<ScenicSpot> queryScenicSpots(String city, String keyword, int limit) {
        String provider = scenicSpotQueryClientFacade.provider();
        int candidateLimit = Math.max(1, limit);
        Map<String, ScenicSpot> result = new LinkedHashMap<>();
        for (int page = 1; result.size() < candidateLimit; page++) {
            ScenicSpotQueryClient.Response response = scenicSpotQueryClientFacade.query(new ScenicSpotQueryClient.Request()
                    .setType(ScenicSpotQueryClient.QueryType.SCENIC_SPOT).setRegion(city).setKeyword(keyword).setPage(page));
            if (!Boolean.TRUE.equals(response.getSuccess())) {
                if (page == 1) {
                    throw new IllegalStateException(StrUtil.blankToDefault(response.getMessage(), "景点查询失败"));
                }
                break; // 已有前页结果时允许以部分数据降级。
            }
            JsonNode list = response.getData() == null ? null : response.getData().path("list");
            if (list == null || !list.isArray() || list.isEmpty()) {
                break;
            }
            int before = result.size();
            for (JsonNode item : list) {
                String name = text(item, "name", "scenicName", "title");
                if (StrUtil.isBlank(name)) {
                    continue;
                }
                String location = text(item, "location");
                String[] coordinate = StrUtil.splitToArray(location, ',');
                ScenicSpot spot = new ScenicSpot(provider, text(item, "id", "scenicId", "code"), name,
                        text(item, "address", "addr"), coordinate.length > 0 ? coordinate[0] : text(item, "longitude", "lng"),
                        coordinate.length > 1 ? coordinate[1] : text(item, "latitude", "lat"), firstImage(item),
                        businessHours(item), text(item.path("business"), "rating"), text(item.path("business"), "cost"));
                result.putIfAbsent(scenicIdentity(spot), spot);
                if (result.size() >= candidateLimit) {
                    break;
                }
            }
            if (list.size() < POI_PAGE_SIZE || result.size() == before) {
                break;
            }
        }
        return new ArrayList<>(result.values());
    }

    public List<Place> queryHotels(String city, int limit) {
        return queryPlaces(TravelPlaceQueryClient.PlaceType.HOTEL, city, limit);
    }

    public List<Place> queryRestaurants(String city, int limit) {
        return queryPlaces(TravelPlaceQueryClient.PlaceType.RESTAURANT, city, limit);
    }

    /** 以已选景点为中心查询 5km 内餐厅；坐标必须是高德 GCJ-02。 */
    public List<Place> queryRestaurantsAround(String city, String longitude, String latitude, int limit) {
        return queryPlacesAround(TravelPlaceQueryClient.PlaceType.RESTAURANT, city, longitude, latitude, 5_000, limit);
    }

    /** 以当日最后一个景点为中心查询 10km 内酒店；坐标必须是高德 GCJ-02。 */
    public List<Place> queryHotelsAround(String city, String longitude, String latitude, int limit) {
        return queryPlacesAround(TravelPlaceQueryClient.PlaceType.HOTEL, city, longitude, latitude, 10_000, limit);
    }

    private List<Place> queryPlacesAround(TravelPlaceQueryClient.PlaceType type, String city, String longitude,
                                          String latitude, int radius, int limit) {
        return queryPlaces(type, city, longitude + ',' + latitude, radius, limit, "周边地点查询失败");
    }

    public boolean isRouteAvailable() {
        return amapRouteQueryClient.isConfigured();
    }

    /** 查询同城单段路径；调用方决定是否在失败时回退为本地估算。 */
    public Route queryRoute(String city, String origin, String destination, RouteMode mode) {
        AmapRouteQueryClient.Mode amapMode = mode == RouteMode.WALKING
                ? AmapRouteQueryClient.Mode.WALKING : AmapRouteQueryClient.Mode.TRANSIT;
        AmapRouteQueryClient.Route route = amapRouteQueryClient.query(new AmapRouteQueryClient.Request(city, origin, destination, amapMode));
        List<RoutePoint> routePoints = route.routePoints().stream()
                .map(point -> new RoutePoint(point.longitude(), point.latitude())).toList();
        return new Route("gaode", mode, route.distanceMeters(), route.durationSeconds(), routePoints);
    }

    private List<Place> queryPlaces(TravelPlaceQueryClient.PlaceType type, String city, int limit) {
        return queryPlaces(type, city, null, null, limit, "旅行地点查询失败");
    }

    private List<Place> queryPlaces(TravelPlaceQueryClient.PlaceType type, String city, String location, Integer radius,
                                    int limit, String failureMessage) {
        String provider = travelPlaceQueryClientFacade.provider(type);
        int candidateLimit = Math.max(1, limit);
        Map<String, Place> result = new LinkedHashMap<>();
        for (int page = 1; result.size() < candidateLimit; page++) {
            int pageLimit = Math.min(POI_PAGE_SIZE, candidateLimit - result.size());
            TravelPlaceQueryClient.Response response = travelPlaceQueryClientFacade.query(new TravelPlaceQueryClient.Request()
                    .setType(type).setRegion(city).setLocation(location).setRadius(radius).setLimit(pageLimit).setPage(page));
            if (!Boolean.TRUE.equals(response.getSuccess())) {
                if (page == 1) {
                    throw new IllegalStateException(StrUtil.blankToDefault(response.getMessage(), failureMessage));
                }
                break;
            }
            List<TravelPlaceQueryClient.Place> places = response.getPlaces() == null ? List.of() : response.getPlaces();
            if (places.isEmpty()) {
                break;
            }
            int before = result.size();
            for (TravelPlaceQueryClient.Place place : places) {
                Place converted = new Place(provider, place.getExternalId(), place.getName(), place.getAddress(),
                        place.getLongitude(), place.getLatitude(), place.getImageUrl(), place.getTelephone(),
                        place.getRating(), place.getCost(), place.getTag(), place.getBusinessHours());
                result.putIfAbsent(placeIdentity(converted), converted);
                if (result.size() >= candidateLimit) {
                    break;
                }
            }
            if (places.size() < pageLimit || result.size() == before) {
                break;
            }
        }
        return new ArrayList<>(result.values());
    }

    private static String scenicIdentity(ScenicSpot spot) {
        return StrUtil.blankToDefault(spot.externalId(), spot.name());
    }

    private static String placeIdentity(Place place) {
        return StrUtil.blankToDefault(place.externalId(), place.name());
    }

    private static String firstImage(JsonNode item) {
        String imageUrl = text(item, "imageUrl", "image", "imgUrl", "cover");
        if (StrUtil.isNotBlank(imageUrl)) {
            return imageUrl;
        }
        JsonNode photos = item.path("photos");
        return photos.isArray() && !photos.isEmpty() ? text(photos.get(0), "url", "imageUrl") : "";
    }

    private static String businessHours(JsonNode item) {
        JsonNode business = item.path("business");
        for (String name : List.of("business_time", "opentime_week", "open_time")) {
            String value = text(business, name);
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return "";
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
                             String latitude, String imageUrl, String businessHours, String rating, String cost) {

        public ScenicSpot(String provider, String externalId, String name, String address, String longitude,
                          String latitude, String imageUrl) {
            this(provider, externalId, name, address, longitude, latitude, imageUrl, "", "", "");
        }
    }

    public record Place(String provider, String externalId, String name, String address, String longitude,
                        String latitude, String imageUrl, String telephone, String rating, String cost, String tag,
                        String businessHours) {

        public Place(String provider, String externalId, String name, String address, String longitude,
                     String latitude, String imageUrl, String telephone, String rating, String cost, String tag) {
            this(provider, externalId, name, address, longitude, latitude, imageUrl, telephone, rating, cost, tag, "");
        }
    }

    public enum RouteMode {
        WALKING,
        TRANSIT
    }

    public record Route(String provider, RouteMode mode, int distanceMeters, int durationSeconds, List<RoutePoint> routePoints) {
    }

    /** 路线点为高德 GCJ-02 坐标，供 uni-app、Android 与 iOS 地图 SDK 绘制折线。 */
    public record RoutePoint(double longitude, double latitude) {
    }
}
