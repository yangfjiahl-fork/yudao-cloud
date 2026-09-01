package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 基于已提取旅行信息的确定性行程骨架组装器。 */
@Service
public class TripItineraryAssembler {

    private static final int MAX_POI_QUERY_LIMIT = 20;
    private static final int MIN_SCENIC_QUERY_LIMIT = 8;
    private static final int MIN_PLACE_QUERY_LIMIT = 8;
    private static final double WALKING_DISTANCE_METERS = 1_500D;
    private static final double EARTH_RADIUS_METERS = 6_371_000D;

    @Resource
    private TripTravelQueryService tripTravelQueryService;

    public Map<String, Object> assemble(Map<String, Object> state) {
        String destination = text(state.get("destination"));
        int days = MapUtil.getInt(state, "days", 1);
        List<String> interests = texts(state.get("interests"));
        int scenicCountPerDay = scenicCountPerDay();
        List<ScenicCandidate> scenicCandidates = collectScenicCandidates(destination, interests, days, scenicCountPerDay);
        List<List<ScenicCandidate>> dailyScenicPlans = allocateDays(scenicCandidates, days, scenicCountPerDay);
        List<TripTravelQueryService.Place> restaurants = queryPlaces(destination, false, Math.max(days * 2, MIN_PLACE_QUERY_LIMIT));
        List<TripTravelQueryService.Place> hotels = queryPlaces(destination, true, MIN_PLACE_QUERY_LIMIT);

        Map<String, Object> itinerary = new LinkedHashMap<>();
        List<Map<String, Object>> dailyItinerary = buildDays(state, days, destination, dailyScenicPlans, restaurants, hotels);
        itinerary.put("overview", buildOverviewSlot(0, destination));
        itinerary.put("daily_itinerary", dailyItinerary);
        itinerary.put("transport", buildTransport(destination));
        itinerary.put("citation_ids", List.of());
        applyOverrides(itinerary, state.get("itineraryOverrides"));
        return itinerary;
    }

    private List<Map<String, Object>> buildDays(Map<String, Object> state, int days, String destination,
                                                 List<List<ScenicCandidate>> dailyScenicPlans,
                                                 List<TripTravelQueryService.Place> restaurants,
                                                 List<TripTravelQueryService.Place> fallbackHotels) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate startDate = parseDate(text(state.get("startDate")));
        Set<String> usedRestaurantIds = new HashSet<>();
        for (int day = 1; day <= days; day++) {
            Map<String, Object> dayItem = new LinkedHashMap<>();
            dayItem.put("day", day);
            if (startDate != null) {
                dayItem.put("date", startDate.plusDays(day - 1).toString());
            }
            List<ScenicCandidate> scenicPlan = dailyScenicPlans.get(day - 1);
            List<Map<String, Object>> slots = buildSlots(destination, scenicPlan, restaurants,
                    selectHotel(destination, last(scenicPlan), fallbackHotels), usedRestaurantIds);
            dayItem.put("overview", buildOverviewSlot(day, destination));
            dayItem.put("slots", slots);
            result.add(dayItem);
        }
        return result;
    }

    /** 按需查询某一天的交通段，避免行程骨架默认携带大量路线点。 */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> resolveTransportSegments(Map<String, Object> itinerary, int day) {
        Object dailyItinerary = itinerary.get("daily_itinerary");
        if (!(dailyItinerary instanceof List<?> days)) {
            throw new IllegalArgumentException("行程骨架格式错误");
        }
        for (Object item : days) {
            if (!(item instanceof Map<?, ?> dayItem) || !ObjUtil.equal(MapUtil.getInt(dayItem, "day"), day)) {
                continue;
            }
            Object rawSlots = dayItem.get("slots");
            if (!(rawSlots instanceof List<?> slotList)) {
                throw new IllegalArgumentException("行程节点不存在");
            }
            List<Map<String, Object>> slots = slotList.stream().filter(Map.class::isInstance)
                    .map(slot -> (Map<String, Object>) slot).toList();
            Map<String, Object> accommodation = slots.stream()
                    .filter(slot -> "ACCOMMODATION".equals(text(slot.get("slot")))).findFirst().orElse(null);
            String city = slots.stream().map(slot -> text(slot.get("city"))).filter(StrUtil::isNotBlank)
                    .findFirst().orElse("");
            return buildTransportSegments(city, slots, accommodation == null ? null : point(accommodation));
        }
        throw new IllegalArgumentException("行程天数不存在");
    }

    private List<Map<String, Object>> buildSlots(String destination, List<ScenicCandidate> scenicPlan,
                                                  List<TripTravelQueryService.Place> restaurants,
                                                  TripTravelQueryService.Place hotel, Set<String> usedRestaurantIds) {
        ScenicCandidate morning = pick(scenicPlan, 0);
        ScenicCandidate afternoon = pick(scenicPlan, 1);
        TripTravelQueryService.Place lunch = selectMeal(destination, morning, restaurants, usedRestaurantIds);
        TripTravelQueryService.Place dinner = selectMeal(destination,
                afternoon != null ? afternoon : morning, restaurants, usedRestaurantIds);
        return List.of(
                buildScenicSlot("MORNING", destination, morning),
                buildMealSlot("LUNCH", destination, lunch),
                buildScenicSlot("AFTERNOON", destination, afternoon),
                buildMealSlot("DINNER", destination, dinner),
                buildAccommodationSlot(destination, hotel)
        );
    }

    private static Map<String, Object> buildScenicSlot(String slot, String destination, ScenicCandidate candidate) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("slot", slot);
        result.put("label", slotLabel(slot));
        result.put("status", "PENDING");
        result.put("city", destination);
        if (candidate == null) {
            result.put("skeleton", "待补充游览地点");
            return result;
        }
        result.put("poiId", candidate.spot().externalId());
        result.put("poiName", candidate.spot().name());
        result.put("area", destination);
        result.put("longitude", candidate.spot().longitude());
        result.put("latitude", candidate.spot().latitude());
        result.put("suggestedStayMinutes", 150);
        result.put("skeleton", "游览" + candidate.spot().name());
        return result;
    }

    private static Map<String, Object> buildMealSlot(String slot, String destination, TripTravelQueryService.Place place) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("slot", slot);
        result.put("label", slotLabel(slot));
        result.put("status", "PENDING");
        result.put("city", destination);
        if (place == null) {
            result.put("skeleton", "在" + destination + "品尝当地美食");
            return result;
        }
        result.put("poiId", place.externalId());
        result.put("poiName", place.name());
        result.put("longitude", place.longitude());
        result.put("latitude", place.latitude());
        result.put("suggestedStayMinutes", 60);
        result.put("skeleton", "在" + place.name() + "用餐");
        return result;
    }

    private static Map<String, Object> buildAccommodationSlot(String destination, TripTravelQueryService.Place hotel) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("slot", "ACCOMMODATION");
        result.put("label", slotLabel("ACCOMMODATION"));
        result.put("status", "PENDING");
        result.put("city", destination);
        if (hotel == null) {
            result.put("skeleton", "在" + destination + "安排住宿");
            return result;
        }
        result.put("poiId", hotel.externalId());
        result.put("poiName", hotel.name());
        result.put("longitude", hotel.longitude());
        result.put("latitude", hotel.latitude());
        result.put("cost", hotel.cost());
        result.put("rating", hotel.rating());
        result.put("tag", hotel.tag());
        result.put("skeleton", "入住" + hotel.name());
        return result;
    }

    private static Map<String, Object> buildTransport(String destination) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("arrival", Map.of("status", "PENDING", "city", destination, "skeleton", "抵达" + destination + "的交通待确认"));
        result.put("departure", Map.of("status", "PENDING", "city", destination, "skeleton", "从" + destination + "返程的交通待确认"));
        return result;
    }

    private List<ScenicCandidate> collectScenicCandidates(String destination, List<String> interests, int days, int scenicCountPerDay) {
        int queryLimit = Math.min(MAX_POI_QUERY_LIMIT, Math.max(days * scenicCountPerDay * 2, MIN_SCENIC_QUERY_LIMIT));
        List<String> keywords = interests.isEmpty() ? List.of(destination) : interests;
        Map<String, ScenicCandidate> candidates = new LinkedHashMap<>();
        for (int index = 0; index < keywords.size(); index++) {
            String keyword = keywords.get(index);
            for (TripTravelQueryService.ScenicSpot spot : queryScenicSpots(destination, keyword, queryLimit)) {
                String identity = StrUtil.blankToDefault(spot.externalId(), spot.name());
                candidates.merge(identity, new ScenicCandidate(spot, preferenceScore(index)),
                        (left, right) -> left.preferenceScore() >= right.preferenceScore() ? left : right);
            }
        }
        if (candidates.size() < days * scenicCountPerDay && !interests.isEmpty()) {
            for (TripTravelQueryService.ScenicSpot spot : queryScenicSpots(destination, destination, queryLimit)) {
                String identity = StrUtil.blankToDefault(spot.externalId(), spot.name());
                candidates.putIfAbsent(identity, new ScenicCandidate(spot, 0));
            }
        }
        return candidates.values().stream().sorted(Comparator.comparingInt(ScenicCandidate::preferenceScore).reversed()).toList();
    }

    private List<TripTravelQueryService.ScenicSpot> queryScenicSpots(String destination, String keyword, int limit) {
        try {
            return tripTravelQueryService.queryScenicSpots(destination, keyword, limit);
        } catch (RuntimeException ignored) {
            return List.of();
        }
    }

    private List<TripTravelQueryService.Place> queryPlaces(String destination, boolean hotel, int limit) {
        try {
            return hotel ? tripTravelQueryService.queryHotels(destination, limit)
                    : tripTravelQueryService.queryRestaurants(destination, limit);
        } catch (RuntimeException ignored) {
            return List.of();
        }
    }

    private static List<List<ScenicCandidate>> allocateDays(List<ScenicCandidate> candidates, int days, int scenicCountPerDay) {
        List<ScenicCandidate> remaining = new ArrayList<>(candidates);
        List<List<ScenicCandidate>> result = new ArrayList<>();
        for (int day = 0; day < days; day++) {
            List<ScenicCandidate> dayPlan = new ArrayList<>();
            while (dayPlan.size() < scenicCountPerDay && !remaining.isEmpty()) {
                ScenicCandidate selected = dayPlan.isEmpty() ? remaining.get(0)
                        : remaining.stream().min(Comparator.<ScenicCandidate>comparingDouble(candidate -> distanceToPlan(candidate, dayPlan))
                        .thenComparing(Comparator.comparingInt(ScenicCandidate::preferenceScore).reversed())).orElseThrow();
                dayPlan.add(selected);
                remaining.remove(selected);
            }
            result.add(dayPlan);
        }
        return result;
    }

    private TripTravelQueryService.Place selectHotel(String city, ScenicCandidate anchor,
                                                      List<TripTravelQueryService.Place> fallbackHotels) {
        if (anchor != null) {
            try {
                List<TripTravelQueryService.Place> nearbyHotels = tripTravelQueryService.queryHotelsAround(city,
                        anchor.spot().longitude(), anchor.spot().latitude(), 10);
                if (nearbyHotels != null && !nearbyHotels.isEmpty()) {
                    return selectHotelByPreference(nearbyHotels);
                }
            } catch (RuntimeException ignored) {
                // 城市级酒店候选已在组装前获取，周边检索失败时无需中断整份行程。
            }
        }
        return selectHotelByPreference(fallbackHotels);
    }

    private static TripTravelQueryService.Place selectHotelByPreference(List<TripTravelQueryService.Place> hotels) {
        return hotels.stream().min(Comparator.comparingDouble(TripItineraryAssembler::cost)
                .thenComparing(Comparator.comparingInt(TripItineraryAssembler::starLevel).reversed())
                .thenComparing(Comparator.comparingDouble(TripItineraryAssembler::rating).reversed())).orElse(null);
    }

    private TripTravelQueryService.Place selectMeal(String city, ScenicCandidate anchor,
                                                     List<TripTravelQueryService.Place> fallbackPlaces,
                                                     Set<String> usedPlaceIds) {
        if (anchor == null) {
            return null;
        }
        try {
            List<TripTravelQueryService.Place> nearbyPlaces = tripTravelQueryService.queryRestaurantsAround(city,
                    anchor.spot().longitude(), anchor.spot().latitude(), 5);
            if (nearbyPlaces != null && !nearbyPlaces.isEmpty()) {
                TripTravelQueryService.Place nearby = selectNearestPlace(nearbyPlaces, anchor, usedPlaceIds);
                if (nearby != null) {
                    return nearby;
                }
            }
        } catch (RuntimeException ignored) {
            // 城市级候选已在组装前获取，周边检索失败时无需中断整份行程。
        }
        return selectNearestPlace(fallbackPlaces, anchor, usedPlaceIds);
    }

    private static TripTravelQueryService.Place selectNearestPlace(List<TripTravelQueryService.Place> places,
                                                                     ScenicCandidate anchor, Set<String> usedPlaceIds) {
        if (anchor == null) {
            return null;
        }
        return places.stream().filter(place -> !usedPlaceIds.contains(placeIdentity(place)))
                .min(Comparator.<TripTravelQueryService.Place>comparingDouble(place -> distance(anchor.spot().longitude(), anchor.spot().latitude(),
                        place.longitude(), place.latitude())).thenComparing(Comparator.comparingDouble(TripItineraryAssembler::rating).reversed()))
                .map(place -> {
                    usedPlaceIds.add(placeIdentity(place));
                    return place;
                }).orElse(null);
    }

    private List<Map<String, Object>> buildTransportSegments(String city, List<Map<String, Object>> slots,
                                                              Map<String, Object> initialPoint) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> previous = initialPoint;
        for (Map<String, Object> slot : slots) {
            Map<String, Object> current = point(slot);
            if (previous != null && current != null) {
                double distance = distance(text(previous.get("longitude")), text(previous.get("latitude")),
                        text(current.get("longitude")), text(current.get("latitude")));
                if (distance >= 0) {
                    Map<String, Object> segment = new LinkedHashMap<>();
                    segment.put("fromPoiId", previous.get("poiId"));
                    segment.put("fromPoiName", previous.get("poiName"));
                    segment.put("fromLongitude", previous.get("longitude"));
                    segment.put("fromLatitude", previous.get("latitude"));
                    segment.put("toPoiId", current.get("poiId"));
                    segment.put("toPoiName", current.get("poiName"));
                    segment.put("toLongitude", current.get("longitude"));
                    segment.put("toLatitude", current.get("latitude"));
                    enrichRouteSegment(segment, city, text(previous.get("longitude")), text(previous.get("latitude")),
                            text(current.get("longitude")), text(current.get("latitude")), distance);
                    result.add(segment);
                }
            }
            if (current != null) {
                previous = current;
            }
        }
        return result;
    }

    private void enrichRouteSegment(Map<String, Object> segment, String city, String originLongitude, String originLatitude,
                                    String destinationLongitude, String destinationLatitude, double estimatedDistance) {
        TripTravelQueryService.RouteMode mode = estimatedDistance <= WALKING_DISTANCE_METERS
                ? TripTravelQueryService.RouteMode.WALKING : TripTravelQueryService.RouteMode.TRANSIT;
        if (tripTravelQueryService.isRouteAvailable()) {
            try {
                TripTravelQueryService.Route route = tripTravelQueryService.queryRoute(city,
                        originLongitude + ',' + originLatitude, destinationLongitude + ',' + destinationLatitude, mode);
                segment.put("distanceMeters", route.distanceMeters());
                segment.put("mode", route.mode().name());
                segment.put("durationMinutes", Math.max(1, (int) Math.ceil(route.durationSeconds() / 60D)));
                segment.put("provider", route.provider());
                segment.put("routePoints", buildRoutePoints(route.routePoints(), originLongitude, originLatitude,
                        destinationLongitude, destinationLatitude));
                segment.put("status", "VERIFIED");
                return;
            } catch (RuntimeException ignored) {
                // 高德单段路径偶发不可用时，使用本地估算以确保行程骨架仍可生成。
            }
        }
        segment.put("distanceMeters", Math.round(estimatedDistance));
        segment.put("mode", mode.name());
        segment.put("durationMinutes", estimateDurationMinutes(estimatedDistance));
        segment.put("routePoints", buildRoutePoints(List.of(), originLongitude, originLatitude,
                destinationLongitude, destinationLatitude));
        segment.put("status", "ESTIMATED");
    }

    private static List<Map<String, Double>> buildRoutePoints(List<TripTravelQueryService.RoutePoint> routePoints,
                                                               String originLongitude, String originLatitude,
                                                               String destinationLongitude, String destinationLatitude) {
        List<Map<String, Double>> result = new ArrayList<>();
        addRoutePoint(result, originLongitude, originLatitude);
        for (TripTravelQueryService.RoutePoint point : routePoints) {
            addRoutePoint(result, point.longitude(), point.latitude());
        }
        addRoutePoint(result, destinationLongitude, destinationLatitude);
        return result;
    }

    private static void addRoutePoint(List<Map<String, Double>> points, String longitude, String latitude) {
        try {
            addRoutePoint(points, Double.parseDouble(longitude), Double.parseDouble(latitude));
        } catch (NumberFormatException ignored) {
            // 起终点已在组装阶段做过坐标校验，异常时仅忽略坏点。
        }
    }

    private static void addRoutePoint(List<Map<String, Double>> points, double longitude, double latitude) {
        Map<String, Double> point = Map.of("longitude", longitude, "latitude", latitude);
        // List#getLast is unavailable on the project's Java 17 runtime.
        if (points.isEmpty() || !points.get(points.size() - 1).equals(point)) {
            points.add(point);
        }
    }

    private static Map<String, Object> point(Map<String, Object> slot) {
        return slot.containsKey("poiId") ? placePoint(text(slot.get("poiId")), text(slot.get("poiName")),
                text(slot.get("longitude")), text(slot.get("latitude"))) : null;
    }

    private static Map<String, Object> placePoint(String poiId, String poiName, String longitude, String latitude) {
        if (distance(longitude, latitude, longitude, latitude) < 0) {
            return null;
        }
        return Map.of("poiId", poiId, "poiName", poiName, "longitude", longitude, "latitude", latitude);
    }

    private static double distanceToPlan(ScenicCandidate candidate, List<ScenicCandidate> dayPlan) {
        return dayPlan.stream().mapToDouble(item -> distance(candidate.spot().longitude(), candidate.spot().latitude(),
                item.spot().longitude(), item.spot().latitude())).filter(distance -> distance >= 0).min().orElse(Double.MAX_VALUE);
    }

    private static double distance(String longitude1, String latitude1, String longitude2, String latitude2) {
        try {
            double lat1 = Math.toRadians(Double.parseDouble(latitude1));
            double lat2 = Math.toRadians(Double.parseDouble(latitude2));
            double longitudeDelta = Math.toRadians(Double.parseDouble(longitude2) - Double.parseDouble(longitude1));
            double latitudeDelta = lat2 - lat1;
            double haversine = Math.pow(Math.sin(latitudeDelta / 2), 2)
                    + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(longitudeDelta / 2), 2);
            return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
        } catch (RuntimeException ignored) {
            return -1D;
        }
    }

    private static int estimateDurationMinutes(double distanceMeters) {
        if (distanceMeters <= WALKING_DISTANCE_METERS) {
            return Math.max(1, (int) Math.ceil(distanceMeters / 75D));
        }
        return 8 + Math.max(1, (int) Math.ceil(distanceMeters / 333D));
    }

    private static int scenicCountPerDay() {
        return 2;
    }

    private static int preferenceScore(int index) {
        return Math.max(1, 100 - index);
    }

    private static String placeIdentity(TripTravelQueryService.Place place) {
        return StrUtil.blankToDefault(place.externalId(), place.name());
    }

    private static double rating(TripTravelQueryService.Place place) {
        try {
            return Double.parseDouble(place.rating());
        } catch (RuntimeException ignored) {
            return 0D;
        }
    }

    private static double cost(TripTravelQueryService.Place place) {
        try {
            return Double.parseDouble(StrUtil.blankToDefault(place.cost(), "").replaceAll("[^0-9.]", ""));
        } catch (RuntimeException ignored) {
            return Double.MAX_VALUE;
        }
    }

    private static int starLevel(TripTravelQueryService.Place place) {
        String tag = text(place.tag());
        if (tag.contains("5星") || tag.contains("五星")) {
            return 5;
        }
        if (tag.contains("4星") || tag.contains("四星")) {
            return 4;
        }
        if (tag.contains("3星") || tag.contains("三星")) {
            return 3;
        }
        if (tag.contains("2星") || tag.contains("二星")) {
            return 2;
        }
        if (tag.contains("1星") || tag.contains("一星")) {
            return 1;
        }
        if (tag.contains("豪华")) {
            return 5;
        }
        if (tag.contains("高档")) {
            return 4;
        }
        if (tag.contains("舒适")) {
            return 3;
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    private static void applyOverrides(Map<String, Object> itinerary, Object value) {
        if (!(value instanceof List<?> overrides)) {
            return;
        }
        for (Object item : overrides) {
            if (!(item instanceof Map<?, ?> override)) {
                continue;
            }
            Integer day = MapUtil.getInt(override, "day");
            String slot = text(override.get("slot")).toUpperCase();
            String instruction = text(override.get("instruction"));
            if (day == null || StrUtil.isBlank(slot) || StrUtil.isBlank(instruction)) {
                continue;
            }
            Map<String, Object> target = findSlot(itinerary, day, slot);
            if (target != null) {
                target.put("skeleton", instruction);
                if ("MORNING".equals(slot) || "AFTERNOON".equals(slot) || "EVENING".equals(slot)) {
                    target.put("poiName", instruction);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> findSlot(Map<String, Object> itinerary, int day, String slot) {
        if (day == 0 && ("ARRIVAL".equals(slot) || "DEPARTURE".equals(slot))) {
            Object transport = itinerary.get("transport");
            return transport instanceof Map<?, ?> map ? (Map<String, Object>) map.get("ARRIVAL".equals(slot) ? "arrival" : "departure") : null;
        }
        Object dailyItinerary = itinerary.get("daily_itinerary");
        if (!(dailyItinerary instanceof List<?> days)) {
            return null;
        }
        for (Object item : days) {
            if (!(item instanceof Map<?, ?> dayItem) || !ObjUtil.equal(MapUtil.getInt(dayItem, "day"), day)) {
                continue;
            }
            Object slots = dayItem.get("slots");
            if (slots instanceof List<?> slotList) {
                for (Object candidate : slotList) {
                    if (candidate instanceof Map<?, ?> slotItem && slot.equals(text(slotItem.get("slot")).toUpperCase())) {
                        return (Map<String, Object>) slotItem;
                    }
                }
            }
        }
        return null;
    }

    private static Map<String, Object> buildOverviewSlot(int day, String destination) {
        String slot = day == 0 ? "TRIP_OVERVIEW" : "DAY_OVERVIEW";
        String label = day == 0 ? "行程总览" : "每日总览";
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("slot", slot);
        result.put("label", label);
        result.put("status", "PENDING");
        result.put("city", destination);
        result.put("skeleton", label + "待生成");
        return result;
    }

    private static String slotLabel(String slot) {
        return switch (slot) {
            case "MORNING" -> "上午";
            case "LUNCH" -> "午餐";
            case "AFTERNOON" -> "下午";
            case "DINNER" -> "晚餐";
            case "EVENING" -> "晚上";
            case "ACCOMMODATION" -> "住宿";
            default -> slot;
        };
    }

    private static <T> T pick(List<T> values, int index) {
        return values.isEmpty() ? null : values.get(Math.floorMod(index, values.size()));
    }

    private static <T> T last(List<T> values) {
        return values.isEmpty() ? null : values.get(values.size() - 1);
    }

    private static String pick(List<String> values, int index, String fallback) {
        String value = pick(values, index);
        return StrUtil.blankToDefault(value, fallback);
    }

    private static List<String> texts(Object value) {
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        return values.stream().map(TripItineraryAssembler::text).filter(StrUtil::isNotBlank).toList();
    }

    private static LocalDate parseDate(String value) {
        try {
            return StrUtil.isBlank(value) ? null : LocalDate.parse(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static String text(Object value) {
        return value == null ? "" : StrUtil.trim(String.valueOf(value));
    }

    private record ScenicCandidate(TripTravelQueryService.ScenicSpot spot, int preferenceScore) {
    }

}
