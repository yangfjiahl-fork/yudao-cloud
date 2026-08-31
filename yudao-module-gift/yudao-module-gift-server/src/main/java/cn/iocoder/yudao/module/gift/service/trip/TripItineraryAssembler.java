package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 基于已提取旅行信息的确定性行程骨架组装器。 */
@Service
public class TripItineraryAssembler {

    private static final List<String> DAILY_SLOTS = List.of("MORNING", "LUNCH", "AFTERNOON", "DINNER", "EVENING", "ACCOMMODATION");

    @Resource
    private TripTravelQueryService tripTravelQueryService;

    public Map<String, Object> assemble(Map<String, Object> state) {
        String destination = text(state.get("destination"));
        int days = MapUtil.getInt(state, "days", 1);
        List<String> interests = texts(state.get("interests"));
        List<TripTravelQueryService.ScenicSpot> scenicSpots = queryScenicSpots(destination, interests, days);
        List<TripTravelQueryService.Place> restaurants = queryPlaces(destination, false);
        List<TripTravelQueryService.Place> hotels = queryPlaces(destination, true);

        Map<String, Object> itinerary = new LinkedHashMap<>();
        itinerary.put("summary", buildSummary(state));
        itinerary.put("daily_itinerary", buildDays(state, days, destination, interests, scenicSpots, restaurants, hotels));
        itinerary.put("transport", buildTransport(destination));
        itinerary.put("citation_ids", List.of());
        applyOverrides(itinerary, state.get("itineraryOverrides"));
        return itinerary;
    }

    private List<Map<String, Object>> buildDays(Map<String, Object> state, int days, String destination,
                                                 List<String> interests, List<TripTravelQueryService.ScenicSpot> scenicSpots,
                                                 List<TripTravelQueryService.Place> restaurants,
                                                 List<TripTravelQueryService.Place> hotels) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate startDate = parseDate(text(state.get("startDate")));
        for (int day = 1; day <= days; day++) {
            Map<String, Object> dayItem = new LinkedHashMap<>();
            dayItem.put("day", day);
            if (startDate != null) {
                dayItem.put("date", startDate.plusDays(day - 1).toString());
            }
            List<Map<String, Object>> slots = new ArrayList<>();
            for (String slot : DAILY_SLOTS) {
                slots.add(buildSlot(day, slot, destination, interests, scenicSpots, restaurants, hotels));
            }
            dayItem.put("slots", slots);
            result.add(dayItem);
        }
        return result;
    }

    private Map<String, Object> buildSlot(int day, String slot, String destination, List<String> interests,
                                          List<TripTravelQueryService.ScenicSpot> scenicSpots,
                                          List<TripTravelQueryService.Place> restaurants,
                                          List<TripTravelQueryService.Place> hotels) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("slot", slot);
        result.put("label", slotLabel(slot));
        result.put("status", "PENDING");
        result.put("city", destination);
        if ("MORNING".equals(slot) || "AFTERNOON".equals(slot) || "EVENING".equals(slot)) {
            int scenicIndex = (day - 1) * 3 + scenicSlotOffset(slot);
            TripTravelQueryService.ScenicSpot spot = pick(scenicSpots, scenicIndex);
            String interest = pick(interests, scenicIndex, "城市风光");
            String poiName = spot != null ? spot.name() : interest;
            result.put("poiName", poiName);
            result.put("area", destination);
            result.put("skeleton", spot != null ? "游览" + spot.name() : "探索" + destination + "的" + interest);
            return result;
        }
        if ("LUNCH".equals(slot) || "DINNER".equals(slot)) {
            TripTravelQueryService.Place place = pick(restaurants, (day - 1) * 2 + ("DINNER".equals(slot) ? 1 : 0));
            result.put("skeleton", place != null ? "在" + place.name() + "用餐" : "在" + destination + "品尝当地美食");
            return result;
        }
        TripTravelQueryService.Place hotel = pick(hotels, day - 1);
        result.put("skeleton", hotel != null ? "入住" + hotel.name() : "在" + destination + "安排住宿");
        return result;
    }

    private static Map<String, Object> buildTransport(String destination) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("arrival", Map.of("status", "PENDING", "city", destination, "skeleton", "抵达" + destination + "的交通待确认"));
        result.put("departure", Map.of("status", "PENDING", "city", destination, "skeleton", "从" + destination + "返程的交通待确认"));
        return result;
    }

    private List<TripTravelQueryService.ScenicSpot> queryScenicSpots(String destination, List<String> interests, int days) {
        try {
            return tripTravelQueryService.queryScenicSpots(destination, pick(interests, 0, destination), Math.min(days * 3, 20));
        } catch (RuntimeException ignored) {
            return List.of();
        }
    }

    private List<TripTravelQueryService.Place> queryPlaces(String destination, boolean hotel) {
        try {
            return hotel ? tripTravelQueryService.queryHotels(destination, 2)
                    : tripTravelQueryService.queryRestaurants(destination, 4);
        } catch (RuntimeException ignored) {
            return List.of();
        }
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

    private static String buildSummary(Map<String, Object> state) {
        String destination = text(state.get("destination"));
        int days = MapUtil.getInt(state, "days", 1);
        String pace = text(state.get("pace"));
        List<String> interests = texts(state.get("interests"));
        String summary = "已根据你的信息生成" + destination + days + "天行程。";
        if (!interests.isEmpty()) {
            summary += "优先安排" + String.join("、", interests) + "体验。";
        }
        return StrUtil.isNotBlank(pace) ? summary + "行程节奏按“" + pace + "”安排。" : summary;
    }

    private static int scenicSlotOffset(String slot) {
        return "MORNING".equals(slot) ? 0 : "AFTERNOON".equals(slot) ? 1 : 2;
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

}
