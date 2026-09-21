package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripItineraryDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripItineraryMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripPlanMapper;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripChangeCommand;
import com.baomidou.lock.annotation.Lock4j;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 在当前不可变快照上应用统一编辑命令，并将结果保存为新版本。 */
@Service
public class TripPlanEditorService {

    private static final Set<String> UPDATE_FIELDS = Set.of("startTime", "durationMinutes", "timePeriod");

    @Resource
    private TripPlanMapper tripPlanMapper;
    @Resource
    private TripItineraryMapper tripItineraryMapper;
    @Resource
    private TripItineraryVersionService tripItineraryVersionService;

    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = {"#conversationId"}, expire = 360000, acquireTimeout = 3000)
    public EditResult apply(Long conversationId, Long memberId, TripChangeCommand command) {
        TripPlanDO trip = tripPlanMapper.selectByConversationIdAndMemberId(conversationId, memberId);
        if (trip == null || trip.getCurrentItineraryId() == null) {
            throw new IllegalArgumentException("当前旅行尚未生成可编辑行程");
        }
        TripItineraryDO current = tripItineraryMapper.selectById(trip.getCurrentItineraryId());
        if (current == null) {
            throw new IllegalArgumentException("当前行程版本不存在");
        }
        if (!Integer.valueOf(command.baseVersion()).equals(current.getVersion())) {
            throw new IllegalStateException("行程版本已更新，请刷新后重试");
        }

        Map<String, Object> itinerary = TripAgentFormatUtils.parseMap(current.getContentJson());
        LinkedHashSet<Integer> affectedDays = applyCommand(itinerary, command);
        itinerary.put("last_change", Map.of(
                "operation", command.operation().name(),
                "baseVersion", command.baseVersion(),
                "affectedDays", List.copyOf(affectedDays)));
        Map<String, Object> state = TripAgentFormatUtils.parseMap(trip.getStateJson());
        TripItineraryVersionService.SavedItinerary saved = tripItineraryVersionService.saveGeneratedItinerary(
                trip, memberId, state, itinerary);
        return new EditResult(saved, List.copyOf(affectedDays), itinerary);
    }

    private static LinkedHashSet<Integer> applyCommand(Map<String, Object> itinerary, TripChangeCommand command) {
        return switch (command.operation()) {
            case REMOVE_ITEM -> removeItem(itinerary, command.itemId());
            case MOVE_ITEM -> moveItem(itinerary, command);
            case UPDATE_ITEM -> updateItem(itinerary, command);
            case LOCK_ITEM -> setLocked(itinerary, command.itemId(), true);
            case UNLOCK_ITEM -> setLocked(itinerary, command.itemId(), false);
            case ADD_ITEM, REPLACE_ITEM, REPLAN_DAY, REPLAN_TRIP ->
                    throw new UnsupportedOperationException("该操作需要 POI 核验或重新规划，尚未接入：" + command.operation());
        };
    }

    private static LinkedHashSet<Integer> removeItem(Map<String, Object> itinerary, String itemId) {
        ItemLocation location = requireItem(itinerary, itemId);
        location.slots().remove(location.index());
        reindex(location.day(), location.slots());
        return affected(location.day());
    }

    private static LinkedHashSet<Integer> moveItem(Map<String, Object> itinerary, TripChangeCommand command) {
        ItemLocation source = requireItem(itinerary, command.itemId());
        if (command.day() == null) {
            throw new IllegalArgumentException("MOVE_ITEM 缺少目标 day");
        }
        DayLocation target = requireDay(itinerary, command.day());
        Map<String, Object> item = source.slots().remove(source.index());
        if (StrUtil.isNotBlank(command.timePeriod())) {
            item.put("timePeriod", command.timePeriod());
            item.put("slot", command.timePeriod());
        }
        int targetSort = command.sort() == null ? target.slots().size()
                : Math.max(0, Math.min(command.sort(), target.slots().size()));
        target.slots().add(targetSort, item);
        reindex(source.day(), source.slots());
        reindex(target.day(), target.slots());
        return affected(source.day(), target.day());
    }

    private static LinkedHashSet<Integer> updateItem(Map<String, Object> itinerary, TripChangeCommand command) {
        ItemLocation location = requireItem(itinerary, command.itemId());
        command.values().forEach((key, value) -> {
            if (!UPDATE_FIELDS.contains(key)) {
                throw new IllegalArgumentException("不允许直接更新行程字段：" + key);
            }
            location.item().put(key, value);
            if ("timePeriod".equals(key)) {
                location.item().put("slot", value);
            }
        });
        reindex(location.day(), location.slots());
        return affected(location.day());
    }

    private static LinkedHashSet<Integer> setLocked(Map<String, Object> itinerary, String itemId, boolean locked) {
        ItemLocation location = requireItem(itinerary, itemId);
        location.item().put("locked", locked);
        return affected(location.day());
    }

    private static LinkedHashSet<Integer> affected(Integer... days) {
        LinkedHashSet<Integer> result = new LinkedHashSet<>();
        for (Integer day : days) {
            if (day != null) {
                result.add(day);
            }
        }
        return result;
    }

    private static ItemLocation requireItem(Map<String, Object> itinerary, String itemId) {
        if (StrUtil.isBlank(itemId)) {
            throw new IllegalArgumentException("行程编辑缺少 itemId");
        }
        for (DayLocation day : days(itinerary)) {
            for (int index = 0; index < day.slots().size(); index++) {
                Map<String, Object> item = day.slots().get(index);
                if (itemId.equals(text(item.get("itemId")))) {
                    return new ItemLocation(day.day(), day.slots(), index, item);
                }
            }
        }
        throw new IllegalArgumentException("行程节点不存在：" + itemId);
    }

    private static DayLocation requireDay(Map<String, Object> itinerary, int targetDay) {
        return days(itinerary).stream().filter(day -> day.day() == targetDay).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("目标行程天数不存在：" + targetDay));
    }

    @SuppressWarnings("unchecked")
    private static List<DayLocation> days(Map<String, Object> itinerary) {
        if (!(itinerary.get("daily_itinerary") instanceof List<?> rawDays)) {
            throw new IllegalArgumentException("行程缺少 daily_itinerary");
        }
        List<DayLocation> result = new ArrayList<>();
        for (Object rawDay : rawDays) {
            if (!(rawDay instanceof Map<?, ?> dayMap)) {
                continue;
            }
            Map<String, Object> day = (Map<String, Object>) dayMap;
            Integer dayNumber = MapUtil.getInt(day, "day");
            if (dayNumber == null || !(day.get("slots") instanceof List<?> rawSlots)) {
                continue;
            }
            List<Map<String, Object>> slots = (List<Map<String, Object>>) rawSlots;
            result.add(new DayLocation(dayNumber, slots));
        }
        result.sort(Comparator.comparingInt(DayLocation::day));
        return result;
    }

    private static void reindex(int day, List<Map<String, Object>> slots) {
        for (int index = 0; index < slots.size(); index++) {
            Map<String, Object> item = slots.get(index);
            item.put("day", day);
            item.put("sort", index);
        }
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    public record EditResult(TripItineraryVersionService.SavedItinerary saved, List<Integer> affectedDays,
                             Map<String, Object> itinerary) {
    }

    private record DayLocation(int day, List<Map<String, Object>> slots) {
    }

    private record ItemLocation(int day, List<Map<String, Object>> slots, int index, Map<String, Object> item) {
    }

}
