package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.enums.AreaTypeEnum;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import cn.iocoder.yudao.module.gift.framework.geo.core.AmapGeocodingClient;
import cn.iocoder.yudao.module.gift.framework.trip.provider.place.AmapPoiTypeEnum;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.TRIP_ITINERARY_NOT_EXISTS;

/** APP 地图聚合服务。 */
@Service
public class TripMapServiceImpl implements TripMapService {

    private static final String COORDINATE_SYSTEM = "GCJ-02";
    private static final String RECOMMENDED_CATEGORY = "recommended";
    private static final ResolvedCity DEFAULT_CITY = new ResolvedCity(310000L, "上海市", "310000");
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    @Resource
    private TripTravelQueryService tripTravelQueryService;
    @Resource
    private AmapGeocodingClient amapGeocodingClient;
    @Resource
    private UserItineraryQueryService userItineraryQueryService;

    @Override
    public ExploreMap getExploreMap(ExploreMapRequest request) {
        validateCoordinate(request.latitude(), request.longitude());
        int pageNo = request.pageNo() == null ? 1 : Math.max(1, request.pageNo());
        int pageSize = request.pageSize() == null ? DEFAULT_PAGE_SIZE
                : Math.min(MAX_PAGE_SIZE, Math.max(1, request.pageSize()));
        ResolvedCity city = resolveCity(request.cityId(), request.latitude(), request.longitude());
        List<ExploreItem> items = queryExploreItems(city.name(), request.keyword(), normalizeCategory(request.category()),
                pageNo, pageSize, request.latitude(), request.longitude());
        AmapGeocodingClient.Point cityPoint = items.isEmpty()
                ? request.latitude() != null && request.cityId() == null
                    ? new AmapGeocodingClient.Point(BigDecimal.valueOf(request.longitude()), BigDecimal.valueOf(request.latitude()))
                    : amapGeocodingClient.geocodeCity(city.name())
                : null;
        return new ExploreMap(COORDINATE_SYSTEM, city, viewport(items, cityPoint), items);
    }

    @Override
    public FootprintMap getFootprintMap(Long memberId, Integer year) {
        // 当前没有已到访记录模型；不能将规划行程、设备位置或默认城市误报为用户足迹。
        return new FootprintMap(COORDINATE_SYSTEM, new FootprintSummary(0), null, List.of());
    }

    @Override
    public ItineraryMap getItineraryMap(Long memberId, Long itineraryId) {
        UserItineraryDO itinerary = userItineraryQueryService.getById(itineraryId, null);
        if (itinerary == null || !Objects.equals(itinerary.getMemberId(), memberId)) {
            throw exception(TRIP_ITINERARY_NOT_EXISTS);
        }
        List<ItineraryDay> days = extractItineraryDays(userItineraryQueryService.toMap(itinerary));
        List<ExploreItem> located = days.stream().flatMap(day -> day.stops().stream())
                .filter(ItineraryStop::hasCoordinate)
                .map(stop -> new ExploreItem(stop.poiId(), stop.poiId(), stop.name(), "itinerary", stop.latitude(),
                        stop.longitude(), stop.address(), null, null, List.of(), null, null, null, null, null))
                .toList();
        return new ItineraryMap(COORDINATE_SYSTEM, itineraryId, viewport(located, null), days);
    }

    private ResolvedCity resolveCity(Long cityId, Double latitude, Double longitude) {
        if (cityId != null) {
            Area area = AreaUtils.getArea(cityId.intValue());
            if (area == null) {
                throw new IllegalArgumentException("城市编号不存在");
            }
            Area city = findAncestor(area, AreaTypeEnum.CITY);
            Area resolved = city == null ? area : city;
            return new ResolvedCity(resolved.getId().longValue(), resolved.getName(), String.valueOf(resolved.getId()));
        }
        if (latitude != null) {
            AmapGeocodingClient.Location location = amapGeocodingClient.reverseGeocode(BigDecimal.valueOf(longitude),
                    BigDecimal.valueOf(latitude));
            long districtId = Long.parseLong(location.adcode());
            long resolvedCityId = location.city().equals(location.district()) ? districtId : districtId / 100 * 100;
            return new ResolvedCity(resolvedCityId, location.city(), location.adcode());
        }
        return DEFAULT_CITY;
    }

    private List<ExploreItem> queryExploreItems(String city, String keyword, String category, int pageNo, int pageSize,
                                                 Double latitude, Double longitude) {
        List<ExploreItem> result = new ArrayList<>();
        List<AmapPoiTypeEnum> poiTypes = RECOMMENDED_CATEGORY.equals(category)
                ? List.of(AmapPoiTypeEnum.SCENIC, AmapPoiTypeEnum.FOOD, AmapPoiTypeEnum.HOTEL)
                : List.of(AmapPoiTypeEnum.fromCategory(category));
        int perCategory = RECOMMENDED_CATEGORY.equals(category) ? Math.max(1, (int) Math.ceil(pageSize / 3D)) : pageSize;
        for (AmapPoiTypeEnum poiType : poiTypes) {
            try {
                switch (poiType) {
                    case SCENIC -> tripTravelQueryService.queryScenicSpots(city, keyword, pageNo, perCategory).forEach(spot ->
                            result.add(toExploreItem(poiType, spot.poiId(), spot.name(), spot.longitude(), spot.latitude(),
                                    spot.address(), spot.imageUrl(), spot.rating(), latitude, longitude)));
                    case FOOD -> tripTravelQueryService.queryRestaurants(city, keyword, pageNo, perCategory).forEach(place ->
                            result.add(toExploreItem(poiType, place.poiId(), place.name(), place.longitude(), place.latitude(),
                                    place.address(), place.imageUrl(), place.rating(), latitude, longitude)));
                    case HOTEL -> tripTravelQueryService.queryHotels(city, keyword, pageNo, perCategory).forEach(place ->
                            result.add(toExploreItem(poiType, place.poiId(), place.name(), place.longitude(), place.latitude(),
                                    place.address(), place.imageUrl(), place.rating(), latitude, longitude)));
                    case SHOPPING -> tripTravelQueryService.queryShopping(city, keyword, pageNo, perCategory).forEach(place ->
                            result.add(toExploreItem(poiType, place.poiId(), place.name(), place.longitude(), place.latitude(),
                                    place.address(), place.imageUrl(), place.rating(), latitude, longitude)));
                }
            } catch (RuntimeException ignored) {
                // 一个类别的供应商失败不阻断其他类别，也不返回伪造地点。
            }
        }
        return result.stream().filter(Objects::nonNull).limit(pageSize).toList();
    }

    private static ExploreItem toExploreItem(AmapPoiTypeEnum poiType, String poiId, String name, String longitude, String latitude,
                                              String address, String coverUrl, String rating,
                                              Double currentLatitude, Double currentLongitude) {
        Double parsedLatitude = toDouble(latitude);
        Double parsedLongitude = toDouble(longitude);
        if (StrUtil.isBlank(poiId) || StrUtil.isBlank(name) || parsedLatitude == null || parsedLongitude == null) {
            return null;
        }
        Double score = toDouble(rating);
        Double distance = currentLatitude == null ? null
                : haversineMeters(currentLatitude, currentLongitude, parsedLatitude, parsedLongitude);
        return new ExploreItem(poiType.getCategory() + ':' + poiId, poiId, name, poiType.getCategory(), parsedLatitude,
                parsedLongitude, address, coverUrl, StrUtil.blankToDefault(address, name), List.of(poiType.getLabel()),
                distance, score, poiType.getPriority(), name, poiType.isFeatured());
    }

    private static List<ItineraryDay> extractItineraryDays(Map<String, Object> itinerary) {
        Object rawDays = itinerary.get("daily_itinerary");
        if (!(rawDays instanceof List<?> dayList)) {
            return List.of();
        }
        List<ItineraryDay> result = new ArrayList<>();
        for (Object rawDay : dayList) {
            if (!(rawDay instanceof Map<?, ?> dayMap)) {
                continue;
            }
            Integer day = toInteger(dayMap.get("day"));
            Object rawSlots = dayMap.get("slots");
            if (day == null || !(rawSlots instanceof List<?> slots)) {
                continue;
            }
            List<ItineraryStop> stops = new ArrayList<>();
            for (int index = 0; index < slots.size(); index++) {
                if (slots.get(index) instanceof Map<?, ?> slot) {
                    stops.add(toItineraryStop(slot, index + 1));
                }
            }
            result.add(new ItineraryDay(day, stops));
        }
        return result.stream().sorted(Comparator.comparing(ItineraryDay::day)).toList();
    }

    private static ItineraryStop toItineraryStop(Map<?, ?> slot, int order) {
        Double latitude = toDouble(slot.get("latitude"));
        Double longitude = toDouble(slot.get("longitude"));
        boolean hasCoordinate = latitude != null && longitude != null;
        String arrival = text(slot.get("plannedStartTime"));
        String departure = text(slot.get("plannedEndTime"));
        String timeLabel = StrUtil.isAllBlank(arrival, departure) ? null
                : StrUtil.blankToDefault(arrival, "") + (StrUtil.isNotBlank(arrival) && StrUtil.isNotBlank(departure) ? "-" : "")
                + StrUtil.blankToDefault(departure, "");
        return new ItineraryStop(text(slot.get("slot")), order, text(slot.get("poiId")), text(slot.get("poiName")),
                hasCoordinate ? latitude : null, hasCoordinate ? longitude : null,
                StrUtil.blankToDefault(text(slot.get("address")), text(slot.get("area"))), arrival, departure, timeLabel,
                hasCoordinate);
    }

    private static Viewport viewport(List<ExploreItem> items, AmapGeocodingClient.Point fallback) {
        if (items.isEmpty()) {
            return fallback == null ? null : new Viewport(fallback.latitude().doubleValue(), fallback.longitude().doubleValue(),
                    12, fallback.latitude().doubleValue(), fallback.longitude().doubleValue(), fallback.latitude().doubleValue(),
                    fallback.longitude().doubleValue());
        }
        double minLatitude = items.stream().mapToDouble(ExploreItem::latitude).min().orElseThrow();
        double maxLatitude = items.stream().mapToDouble(ExploreItem::latitude).max().orElseThrow();
        double minLongitude = items.stream().mapToDouble(ExploreItem::longitude).min().orElseThrow();
        double maxLongitude = items.stream().mapToDouble(ExploreItem::longitude).max().orElseThrow();
        return new Viewport((minLatitude + maxLatitude) / 2, (minLongitude + maxLongitude) / 2, 12, minLatitude,
                minLongitude, maxLatitude, maxLongitude);
    }

    private static String normalizeCategory(String category) {
        String value = StrUtil.blankToDefault(StrUtil.trim(category), RECOMMENDED_CATEGORY).toLowerCase();
        return RECOMMENDED_CATEGORY.equals(value) ? value : AmapPoiTypeEnum.fromCategory(value).getCategory();
    }

    private static Area findAncestor(Area area, AreaTypeEnum type) {
        for (Area current = area; current != null; current = current.getParent()) {
            if (type.getType().equals(current.getType())) {
                return current;
            }
        }
        return null;
    }

    private static void validateCoordinate(Double latitude, Double longitude) {
        if ((latitude == null) != (longitude == null) || (latitude != null
                && (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180))) {
            throw new IllegalArgumentException("经纬度必须同时提供且在有效范围内");
        }
    }

    private static String text(Object value) {
        return value == null ? null : StrUtil.trim(value.toString());
    }

    private static Double toDouble(Object value) {
        try {
            String text = text(value);
            return StrUtil.isBlank(text) ? null : Double.valueOf(text);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static Integer toInteger(Object value) {
        try {
            String text = text(value);
            return StrUtil.isBlank(text) ? null : Integer.valueOf(text);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static double haversineMeters(double latitude1, double longitude1, double latitude2, double longitude2) {
        double latitudeDelta = Math.toRadians(latitude2 - latitude1);
        double longitudeDelta = Math.toRadians(longitude2 - longitude1);
        double a = Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2)
                + Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2))
                * Math.sin(longitudeDelta / 2) * Math.sin(longitudeDelta / 2);
        return 6_371_000D * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

}
