package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.gift.dal.dataobject.itineraryconversation.ItineraryConversationDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversationevent.UserItineraryConversationEventDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripChangeCommand;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripMacroSkeleton;
import com.baomidou.lock.annotation.Lock4j;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 在当前行程上应用统一编辑命令，并覆盖保存结果。 */
@Service
public class TripPlanEditorService {

    private static final Set<String> UPDATE_FIELDS = Set.of("startTime", "durationMinutes", "timePeriod");

    @Resource
    private ItineraryConversationService conversationService;
    @Resource
    private UserItineraryQueryService userItineraryQueryService;
    @Resource
    private TripItinerarySaveService tripItinerarySaveService;
    @Resource
    private TripItineraryAssembler tripItineraryAssembler;

    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = {"#conversationId"}, expire = 360000, acquireTimeout = 3000)
    public EditResult apply(Long conversationId, Long memberId, TripChangeCommand command) {
        return applyInternal(conversationId, memberId, command, ChangeContext.manual());
    }

    /** 供已经持有同一 conversation 锁的 Agent 编排事务调用，避免重复获取分布式锁。 */
    EditResult applyWithinExistingLock(Long conversationId, Long memberId, TripChangeCommand command,
                                       String runId, Long requestEventId) {
        return applyInternal(conversationId, memberId, command, ChangeContext.ai(runId, requestEventId));
    }

    private EditResult applyInternal(Long conversationId, Long memberId, TripChangeCommand command,
                                     ChangeContext context) {
        ItineraryConversationDO conversation = conversationService.getRequired(conversationId, memberId);
        UserItineraryDO current = userItineraryQueryService.getByConversationId(conversationId);
        if (current == null) {
            throw new IllegalArgumentException("当前旅行尚未生成可编辑行程");
        }

        Long requestEventId = context.requestEventId();
        if (context.source() == ChangeSource.MANUAL) {
            UserItineraryConversationEventDO requestEvent = conversationService.createEvent(conversationId, null, null,
                    "USER_ACTION", "user", "EDIT", manualChangeDescription(command),
                    JsonUtils.toJsonString(command));
            requestEventId = requestEvent.getId();
        }

        Map<String, Object> itinerary = userItineraryQueryService.toMap(current);
        Map<String, Object> state = TripAgentFormatUtils.parseMap(conversation.getStateJson());
        LinkedHashSet<Integer> affectedDays = isReplan(command)
                ? replan(itinerary, state, command) : applyLocalCommand(itinerary, command);
        itinerary.put("last_change", Map.of(
                "operation", command.operation().name(),
                "source", context.source().name(),
                "affectedDays", List.copyOf(affectedDays)));
        TripItinerarySaveService.SavedItinerary saved = tripItinerarySaveService.saveGeneratedItinerary(
                conversation, memberId, context.runId(), requestEventId, state, itinerary);
        return new EditResult(saved, List.copyOf(affectedDays), itinerary);
    }

    private static String manualChangeDescription(TripChangeCommand command) {
        return "手动调整行程：" + switch (command.operation()) {
            case ADD_ITEM -> "添加行程节点";
            case REMOVE_ITEM -> "删除行程节点";
            case REPLACE_ITEM -> "替换行程节点";
            case MOVE_ITEM -> "移动行程节点";
            case UPDATE_ITEM -> "修改行程节点";
            case LOCK_ITEM -> "锁定行程节点";
            case UNLOCK_ITEM -> "解锁行程节点";
            case REPLAN_DAY -> "重新规划当天行程";
            case REPLAN_TRIP -> "重新规划全部行程";
        };
    }

    private static boolean isReplan(TripChangeCommand command) {
        return command.operation() == TripChangeCommand.Operation.REPLAN_DAY
                || command.operation() == TripChangeCommand.Operation.REPLAN_TRIP;
    }

    private static LinkedHashSet<Integer> applyLocalCommand(Map<String, Object> itinerary, TripChangeCommand command) {
        return switch (command.operation()) {
            case REMOVE_ITEM -> removeItem(itinerary, command.itemId());
            case MOVE_ITEM -> moveItem(itinerary, command);
            case UPDATE_ITEM -> updateItem(itinerary, command);
            case LOCK_ITEM -> setLocked(itinerary, command.itemId(), true);
            case UNLOCK_ITEM -> setLocked(itinerary, command.itemId(), false);
            case ADD_ITEM, REPLACE_ITEM ->
                    throw new UnsupportedOperationException("该操作需要 POI 核验或重新规划，尚未接入：" + command.operation());
            case REPLAN_DAY, REPLAN_TRIP -> throw new IllegalStateException("重排命令应进入重排流程");
        };
    }

    private LinkedHashSet<Integer> replan(Map<String, Object> itinerary, Map<String, Object> state,
                                           TripChangeCommand command) {
        TripMacroSkeleton macroSkeleton = withReplanInstruction(macroSkeleton(itinerary), command);
        LinkedHashSet<Integer> affectedDays = new LinkedHashSet<>();
        if (command.operation() == TripChangeCommand.Operation.REPLAN_DAY) {
            if (command.day() == null) {
                throw new IllegalArgumentException("REPLAN_DAY 缺少目标 day");
            }
            affectedDays.add(command.day());
        } else {
            macroSkeleton.days().stream().map(TripMacroSkeleton.Day::day).sorted().forEach(affectedDays::add);
        }
        List<Map<String, Object>> replannedDays = tripItineraryAssembler.replanDays(
                state, macroSkeleton, affectedDays, ignored -> {
                });
        replannedDays.forEach(replanned -> mergeReplannedDay(itinerary, replanned));
        return affectedDays;
    }

    private static TripMacroSkeleton withReplanInstruction(TripMacroSkeleton macroSkeleton,
                                                            TripChangeCommand command) {
        String instruction = text(command.values().get("instruction"));
        if (command.operation() != TripChangeCommand.Operation.REPLAN_DAY || command.day() == null
                || StrUtil.isBlank(instruction)) {
            return macroSkeleton;
        }
        List<TripMacroSkeleton.Day> days = macroSkeleton.days().stream().map(day -> {
            if (day.day() != command.day()) {
                return day;
            }
            LinkedHashSet<String> anchors = new LinkedHashSet<>();
            anchors.add(instruction);
            anchors.addAll(day.anchorPoiNames());
            return new TripMacroSkeleton.Day(day.day(), day.city(), day.area(), day.theme(),
                    anchors.stream().limit(2).toList());
        }).toList();
        return new TripMacroSkeleton(days);
    }

    private static void mergeReplannedDay(Map<String, Object> itinerary, Map<String, Object> replanned) {
        Integer dayNumber = MapUtil.getInt(replanned, "day");
        if (dayNumber == null) {
            throw new IllegalArgumentException("重排结果缺少 day");
        }
        DayLocation current = requireDay(itinerary, dayNumber);
        List<Map<String, Object>> lockedItems = current.slots().stream()
                .filter(item -> MapUtil.getBool(item, "locked", false))
                .sorted(Comparator.comparingInt(item -> MapUtil.getInt(item, "sort", Integer.MAX_VALUE))).toList();
        List<Map<String, Object>> mergedSlots = new ArrayList<>(slots(replanned));
        for (Map<String, Object> lockedItem : lockedItems) {
            mergedSlots.removeIf(candidate -> sameItem(candidate, lockedItem));
            int lockedSort = MapUtil.getInt(lockedItem, "sort", mergedSlots.size());
            mergedSlots.add(Math.max(0, Math.min(lockedSort, mergedSlots.size())), lockedItem);
        }
        replanned.put("slots", mergedSlots);
        reindex(dayNumber, mergedSlots);
        if (!lockedItems.isEmpty()) {
            Map<String, Object> planning = replanned.get("planning") instanceof Map<?, ?> rawPlanning
                    ? new LinkedHashMap<>(castMap(rawPlanning)) : new LinkedHashMap<>();
            planning.put("status", "PENDING");
            planning.put("reason", "保留锁定节点后需重新核验日内路线");
            replanned.put("planning", planning);
        }
        current.days().set(current.index(), replanned);
    }

    private static boolean sameItem(Map<String, Object> left, Map<String, Object> right) {
        String leftItemId = text(left.get("itemId"));
        String rightItemId = text(right.get("itemId"));
        if (StrUtil.isNotBlank(leftItemId) && leftItemId.equals(rightItemId)) {
            return true;
        }
        String leftPoiId = text(left.get("poiId"));
        return StrUtil.isNotBlank(leftPoiId) && leftPoiId.equals(text(right.get("poiId")));
    }

    @SuppressWarnings("unchecked")
    private static TripMacroSkeleton macroSkeleton(Map<String, Object> itinerary) {
        if (!(itinerary.get("macro_skeleton") instanceof Map<?, ?> rawMacro)
                || !(rawMacro.get("days") instanceof List<?> rawDays)) {
            throw new IllegalArgumentException("当前行程缺少可用于局部重排的宏观骨架");
        }
        List<TripMacroSkeleton.Day> macroDays = new ArrayList<>();
        for (Object rawDay : rawDays) {
            if (!(rawDay instanceof Map<?, ?> day)) {
                continue;
            }
            Integer dayNumber = MapUtil.getInt(day, "day");
            if (dayNumber == null) {
                continue;
            }
            List<String> anchors = day.get("anchorPoiNames") instanceof List<?> values
                    ? values.stream().map(TripPlanEditorService::text).filter(StrUtil::isNotBlank).toList() : List.of();
            macroDays.add(new TripMacroSkeleton.Day(dayNumber, text(day.get("city")), text(day.get("area")),
                    text(day.get("theme")), anchors));
        }
        if (macroDays.isEmpty()) {
            throw new IllegalArgumentException("当前行程的宏观骨架为空");
        }
        return new TripMacroSkeleton(macroDays);
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
        List<Map<String, Object>> mutableDays = (List<Map<String, Object>>) rawDays;
        for (int index = 0; index < mutableDays.size(); index++) {
            Object rawDay = mutableDays.get(index);
            if (!(rawDay instanceof Map<?, ?> dayMap)) {
                continue;
            }
            Map<String, Object> day = (Map<String, Object>) dayMap;
            Integer dayNumber = MapUtil.getInt(day, "day");
            if (dayNumber == null || !(day.get("slots") instanceof List<?> rawSlots)) {
                continue;
            }
            List<Map<String, Object>> slots = (List<Map<String, Object>>) rawSlots;
            result.add(new DayLocation(dayNumber, mutableDays, index, slots));
        }
        result.sort(Comparator.comparingInt(DayLocation::day));
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> slots(Map<String, Object> day) {
        if (!(day.get("slots") instanceof List<?> rawSlots)) {
            throw new IllegalArgumentException("重排结果缺少 slots");
        }
        return (List<Map<String, Object>>) rawSlots;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<?, ?> value) {
        return (Map<String, Object>) value;
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

    public record EditResult(TripItinerarySaveService.SavedItinerary saved, List<Integer> affectedDays,
                             Map<String, Object> itinerary) {
    }

    private enum ChangeSource {
        MANUAL,
        AI
    }

    private record ChangeContext(ChangeSource source, String runId, Long requestEventId) {

        private static ChangeContext manual() {
            return new ChangeContext(ChangeSource.MANUAL, null, null);
        }

        private static ChangeContext ai(String runId, Long requestEventId) {
            if (requestEventId == null) {
                throw new IllegalArgumentException("AI 行程修改缺少请求事件");
            }
            return new ChangeContext(ChangeSource.AI, runId, requestEventId);
        }
    }

    private record DayLocation(int day, List<Map<String, Object>> days, int index, List<Map<String, Object>> slots) {
    }

    private record ItemLocation(int day, List<Map<String, Object>> slots, int index, Map<String, Object> item) {
    }

}
