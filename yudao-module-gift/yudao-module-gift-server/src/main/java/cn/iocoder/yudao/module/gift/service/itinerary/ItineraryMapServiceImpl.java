package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.TRIP_ITINERARY_NOT_EXISTS;

/** APP 地图聚合服务。 */
@Service
public class ItineraryMapServiceImpl implements ItineraryMapService {

    private static final String COORDINATE_SYSTEM = "GCJ-02";

    @Resource
    private UserItineraryQueryService userItineraryQueryService;

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
        List<MapPoint> located = days.stream().flatMap(day -> day.stops().stream())
                .filter(ItineraryStop::hasCoordinate)
                .map(stop -> new MapPoint(stop.latitude(), stop.longitude()))
                .toList();
        return new ItineraryMap(COORDINATE_SYSTEM, itineraryId, viewport(located), days);
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

    private static Viewport viewport(List<MapPoint> items) {
        if (items.isEmpty()) {
            return null;
        }
        double minLatitude = items.stream().mapToDouble(MapPoint::latitude).min().orElseThrow();
        double maxLatitude = items.stream().mapToDouble(MapPoint::latitude).max().orElseThrow();
        double minLongitude = items.stream().mapToDouble(MapPoint::longitude).min().orElseThrow();
        double maxLongitude = items.stream().mapToDouble(MapPoint::longitude).max().orElseThrow();
        return new Viewport((minLatitude + maxLatitude) / 2, (minLongitude + maxLongitude) / 2, 12, minLatitude,
                minLongitude, maxLatitude, maxLongitude);
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

    private record MapPoint(Double latitude, Double longitude) {
    }

}
