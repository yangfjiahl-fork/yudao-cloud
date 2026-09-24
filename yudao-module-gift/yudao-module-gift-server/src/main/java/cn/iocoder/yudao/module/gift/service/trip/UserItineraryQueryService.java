package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDayDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDayItemDO;
import cn.iocoder.yudao.module.gift.dal.mysql.useritinerary.UserItineraryDayMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.useritinerary.UserItineraryDayItemMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.useritinerary.UserItineraryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserItineraryQueryService {

    @Resource
    private UserItineraryMapper userItineraryMapper;
    @Resource
    private UserItineraryDayMapper userItineraryDayMapper;
    @Resource
    private UserItineraryDayItemMapper userItineraryDayItemMapper;

    public UserItineraryDO getById(Long id, Long conversationId) {
        return userItineraryMapper.selectByIdAndConversationId(id, conversationId);
    }

    public UserItineraryDO getByConversationId(Long conversationId) {
        return userItineraryMapper.selectByConversationId(conversationId);
    }

    public UserItineraryDO getByResultEventId(Long eventId) {
        return userItineraryMapper.selectByResultEventId(eventId);
    }

    public Map<Long, Map<String, Object>> getByResultEventIds(Collection<Long> eventIds) {
        Map<Long, Map<String, Object>> result = new LinkedHashMap<>();
        userItineraryMapper.selectListByResultEventIds(eventIds)
                .forEach(itinerary -> result.put(itinerary.getResultEventId(), toMap(itinerary)));
        return result;
    }

    public Map<String, Object> toMap(UserItineraryDO itinerary) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", itinerary.getId());
        result.put("summary", itinerary.getTitle());
        result.put("overview", mapOf("status", itinerary.getOverviewStatus(), "skeleton",
                itinerary.getOverviewSkeleton(), "detail", itinerary.getOverviewDetail(), "slot", "TRIP_OVERVIEW"));
        result.put("planner", mapOf("type", itinerary.getPlannerType(), "validation", itinerary.getPlannerValidation()));
        List<UserItineraryDayDO> days = userItineraryDayMapper.selectListByUserItineraryId(itinerary.getId());
        List<UserItineraryDayItemDO> items = userItineraryDayItemMapper.selectListByUserItineraryId(itinerary.getId());
        List<Map<String, Object>> daily = new ArrayList<>();
        List<Map<String, Object>> macroDays = new ArrayList<>();
        for (UserItineraryDayDO day : days) {
            Map<String, Object> dayMap = new LinkedHashMap<>();
            dayMap.put("day", day.getDay());
            dayMap.put("date", day.getDate());
            dayMap.put("city", day.getCity());
            dayMap.put("area", day.getArea());
            dayMap.put("theme", day.getTheme());
            dayMap.put("overview", mapOf("status", day.getOverviewStatus(), "skeleton",
                    day.getOverviewSkeleton(), "detail", day.getOverviewDetail(), "slot", "DAY_OVERVIEW"));
            dayMap.put("planning", mapOf("solver", day.getPlanner(), "status", day.getPlanningStatus(),
                    "selectionStatus", day.getSelectionStatus(), "budgetStatus", day.getBudgetStatus()));
            List<Map<String, Object>> slots = new ArrayList<>();
            items.stream().filter(item -> day.getDay().equals(item.getDay()))
                    .forEach(item -> slots.add(toMap(item)));
            dayMap.put("slots", slots);
            daily.add(dayMap);
            macroDays.add(mapOf("day", day.getDay(), "city", day.getCity(), "area", day.getArea(),
                    "theme", day.getTheme(), "anchorPoiNames", json(day.getAnchorPoiNamesJson())));
        }
        result.put("daily_itinerary", daily);
        result.put("macro_skeleton", mapOf("days", macroDays));
        return result;
    }

    private static Map<String, Object> toMap(UserItineraryDayItemDO item) {
        return mapOf("itemId", item.getItemId(), "day", item.getDay(), "type", item.getType(),
                "slot", item.getSlot(), "label", item.getLabel(), "sort", item.getSort(),
                "startTime", item.getStartTime(), "endTime", item.getEndTime(),
                "durationMinutes", item.getDurationMinutes(), "poiId", item.getPoiId(),
                "poiName", item.getPoiName(), "city", item.getCity(), "area", item.getArea(),
                "longitude", item.getLongitude(), "latitude", item.getLatitude(),
                "skeleton", item.getSkeleton(), "detail", item.getDetail(), "status", item.getStatus(),
                "locked", item.getLocked(), "source", item.getSource(),
                "candidates", json(item.getCandidatesJson()), "citationIds", json(item.getCitationIdsJson()));
    }

    private static Map<String, Object> mapOf(Object... values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            if (values[index + 1] != null) {
                result.put(String.valueOf(values[index]), values[index + 1]);
            }
        }
        return result;
    }

    private static Object json(String value) {
        return value == null ? null : JsonUtils.parseObject(value, Object.class);
    }

}
