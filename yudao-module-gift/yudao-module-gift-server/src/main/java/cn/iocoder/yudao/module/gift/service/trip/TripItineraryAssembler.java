package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 基于已提取旅行信息的确定性行程骨架组装器。 */
@Service
@Slf4j
public class TripItineraryAssembler {

    private static final String ITINERARY_ASSEMBLE_METRIC_NAME = "yudao.gift.trip.itinerary.assemble";
    private static final int MAX_CATEGORY_CANDIDATE_POOL_SIZE = 200;
    private static final int CANDIDATES_PER_DAY = 25;
    private static final int DAILY_SCENIC_CANDIDATE_LIMIT = 30;
    private static final int DAILY_MEAL_CANDIDATE_LIMIT = 10;
    private static final int DAILY_HOTEL_CANDIDATE_LIMIT = 10;
    private static final double WALKING_DISTANCE_METERS = 1_500D;
    private static final double EARTH_RADIUS_METERS = 6_371_000D;
    private static final int DEFAULT_DAY_START_MINUTES = 9 * 60;
    private static final int DEFAULT_DAY_END_MINUTES = 20 * 60;
    private static final int LUNCH_START_MINUTES = 11 * 60 + 30;
    private static final int LUNCH_END_MINUTES = 13 * 60 + 30;
    private static final int DINNER_START_MINUTES = 17 * 60 + 30;
    private static final int DINNER_END_MINUTES = 19 * 60 + 30;
    private static final int SCENIC_BUFFER_MINUTES = 30;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Pattern TIME_RANGE_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})\\s*[-~至]\\s*(\\d{1,2}):(\\d{2})");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("\\d[\\d,]*");

    @Resource
    private TripTravelQueryService tripTravelQueryService;
    @Resource
    private MeterRegistry meterRegistry;

    public Map<String, Object> assemble(Map<String, Object> state) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";
        long start = System.currentTimeMillis();
        try {
            String destination = text(state.get("destination"));
            int days = MapUtil.getInt(state, "days", 1);
            List<String> interests = texts(state.get("interests"));
            List<String> mustVisit = texts(state.get("mustVisit"));
            int candidateLimit = candidateLimit(days);
            Map<String, String> mdcContext = MDC.getCopyOfContextMap();
            log.info("[assemble][destination({}) days({}) candidateLimit({}) 开始组装行程骨架]", destination, days, candidateLimit);
            CompletableFuture<List<ScenicCandidate>> scenicFuture = CompletableFuture.supplyAsync(
                    withMdcContext(mdcContext,
                            () -> measureCandidateQuery("scenic", () -> collectScenicCandidates(destination, interests, mustVisit, candidateLimit))));
            CompletableFuture<List<TripTravelQueryService.Place>> restaurantFuture = CompletableFuture.supplyAsync(
                    withMdcContext(mdcContext, () -> measureCandidateQuery("restaurant", () -> queryPlaces(destination, false, candidateLimit))));
            CompletableFuture<List<TripTravelQueryService.Place>> hotelFuture = CompletableFuture.supplyAsync(
                    withMdcContext(mdcContext, () -> measureCandidateQuery("hotel", () -> queryPlaces(destination, true, candidateLimit))));
            List<ScenicCandidate> scenicCandidates = scenicFuture.join();
            List<TripTravelQueryService.Place> restaurants = restaurantFuture.join();
            List<TripTravelQueryService.Place> hotels = hotelFuture.join();

            Map<String, Object> itinerary = new LinkedHashMap<>();
            List<Map<String, Object>> dailyItinerary = buildDays(state, days, destination, scenicCandidates, restaurants, hotels);
            itinerary.put("overview", buildOverviewSlot(0, destination));
            itinerary.put("daily_itinerary", dailyItinerary);
            itinerary.put("transport", buildTransport(destination));
            itinerary.put("citation_ids", List.of());
            applyOverrides(itinerary, state.get("itineraryOverrides"));
            log.info("[assemble][destination({}) days({}) scenicCandidates({}) restaurants({}) hotels({}) 耗时({}ms) 骨架组装完成]",
                    destination, days, scenicCandidates.size(), restaurants.size(), hotels.size(), System.currentTimeMillis() - start);
            return itinerary;
        } catch (RuntimeException | Error ex) {
            outcome = "error";
            throw ex;
        } finally {
            sample.stop(itineraryTimer("assemble", "all", outcome));
        }
    }

    private List<Map<String, Object>> buildDays(Map<String, Object> state, int days, String destination,
                                                 List<ScenicCandidate> scenicCandidates,
                                                 List<TripTravelQueryService.Place> restaurants,
                                                 List<TripTravelQueryService.Place> hotels) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";
        long start = System.currentTimeMillis();
        try {
            LocalDate startDate = parseDate(text(state.get("startDate")));
            int hotelBudget = ObjUtil.defaultIfNull(normalizeAmount(state.get("hotelBudget")), 300);
            List<List<ScenicCandidate>> scenicCandidatesByDay = allocateScenicCandidates(days, scenicCandidates);
            List<TripTravelQueryService.Place> dayMealCandidates = rankMealCandidates(restaurants);
            List<TripTravelQueryService.Place> dayHotelCandidates = rankHotelCandidates(hotels, hotelBudget);
            Map<String, String> mdcContext = MDC.getCopyOfContextMap();
            List<CompletableFuture<Map<String, Object>>> dayFutures = new ArrayList<>();
            for (int day = 1; day <= days; day++) {
                final int currentDay = day;
                final List<ScenicCandidate> dayScenicCandidates = scenicCandidatesByDay.get(day - 1);
                dayFutures.add(CompletableFuture.supplyAsync(withMdcContext(mdcContext,
                        () -> buildDay(state, destination, startDate, currentDay,
                                dayScenicCandidates, dayMealCandidates, dayHotelCandidates))));
            }
            List<Map<String, Object>> result = dayFutures.stream().map(CompletableFuture::join).toList();
            log.info("[buildDays][destination({}) days({}) scenicPerDay({}) mealCandidates({}) hotelCandidates({}) 耗时({}ms)]",
                    destination, days, scenicCandidatesByDay.stream().mapToInt(List::size).boxed().toList(),
                    dayMealCandidates.size(), dayHotelCandidates.size(), System.currentTimeMillis() - start);
            return result;
        } catch (RuntimeException | Error ex) {
            outcome = "error";
            throw ex;
        } finally {
            sample.stop(itineraryTimer("daily_schedule", "all", outcome));
        }
    }

    private Map<String, Object> buildDay(Map<String, Object> state, String destination, LocalDate startDate, int day,
                                         List<ScenicCandidate> scenicCandidates,
                                         List<TripTravelQueryService.Place> mealCandidates,
                                         List<TripTravelQueryService.Place> hotelCandidates) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";
        long start = System.currentTimeMillis();
        try {
            DaySchedule schedule = optimizeDayCandidates(state, destination, scenicCandidates, mealCandidates, hotelCandidates);
            Map<String, Object> dayItem = new LinkedHashMap<>();
            dayItem.put("day", day);
            if (startDate != null) {
                dayItem.put("date", startDate.plusDays(day - 1).toString());
            }
            dayItem.put("overview", buildOverviewSlot(day, destination));
            dayItem.put("slots", schedule.slots());
            dayItem.put("planning", schedule.metadata());
            log.info("[buildDay][day({}) scenicCandidates({}) mealCandidates({}) hotelCandidates({}) status({}) 耗时({}ms)]",
                    day, scenicCandidates.size(), mealCandidates.size(), hotelCandidates.size(),
                    schedule.metadata().get("status"), System.currentTimeMillis() - start);
            return dayItem;
        } catch (RuntimeException | Error ex) {
            outcome = "error";
            throw ex;
        } finally {
            sample.stop(itineraryTimer("daily", "all", outcome));
        }
    }

    private static List<List<ScenicCandidate>> allocateScenicCandidates(int days, List<ScenicCandidate> candidates) {
        List<ScenicCandidate> ranked = rankScenicCandidates(candidates, Set.of());
        List<List<ScenicCandidate>> result = new ArrayList<>();
        for (int day = 0; day < days; day++) {
            result.add(new ArrayList<>());
        }
        if (ranked.isEmpty()) {
            return result;
        }
        int perDayLimit = Math.max(1, (int) Math.ceil((double) ranked.size() / days));
        int nextCandidateIndex = 0;
        int minimumPerDay = Math.min(2, perDayLimit);
        for (List<ScenicCandidate> dayCandidates : result) {
            for (int count = 0; count < minimumPerDay && nextCandidateIndex < ranked.size(); count++) {
                dayCandidates.add(ranked.get(nextCandidateIndex++));
            }
        }
        while (nextCandidateIndex < ranked.size()) {
            boolean allocated = false;
            for (List<ScenicCandidate> dayCandidates : result) {
                if (nextCandidateIndex >= ranked.size() || dayCandidates.size() >= perDayLimit) {
                    continue;
                }
                dayCandidates.add(ranked.get(nextCandidateIndex++));
                allocated = true;
            }
            if (!allocated) {
                break;
            }
        }
        return result;
    }

    private <T extends List<?>> T measureCandidateQuery(String category, Supplier<T> supplier) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";
        long start = System.currentTimeMillis();
        try {
            T result = supplier.get();
            log.info("[assemble][category({}) candidateCount({}) 耗时({}ms)]", category, result.size(),
                    System.currentTimeMillis() - start);
            return result;
        } catch (RuntimeException | Error e) {
            outcome = "error";
            log.warn("[assemble][category({}) 耗时({}ms) 查询失败]", category, System.currentTimeMillis() - start, e);
            throw e;
        } finally {
            sample.stop(itineraryTimer("candidate_query", category, outcome));
        }
    }

    private Timer itineraryTimer(String phase, String category, String outcome) {
        return Timer.builder(ITINERARY_ASSEMBLE_METRIC_NAME)
                .tags("phase", phase, "category", category, "outcome", outcome)
                .register(meterRegistry);
    }

    private static <T> Supplier<T> withMdcContext(Map<String, String> mdcContext, Supplier<T> supplier) {
        return () -> {
            Map<String, String> previousMdcContext = MDC.getCopyOfContextMap();
            setMdcContext(mdcContext);
            try {
                return supplier.get();
            } finally {
                setMdcContext(previousMdcContext);
            }
        };
    }

    private static void setMdcContext(Map<String, String> mdcContext) {
        if (mdcContext == null) {
            MDC.clear();
            return;
        }
        MDC.setContextMap(mdcContext);
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

    /** 在每类 200 个原始候选中粗筛后，将每天的候选交给求解器选址和排程。 */
    private DaySchedule optimizeDayCandidates(Map<String, Object> state, String city,
                                              List<ScenicCandidate> scenicCandidates,
                                              List<TripTravelQueryService.Place> mealCandidates,
                                              List<TripTravelQueryService.Place> hotelCandidates) {
        if (scenicCandidates.isEmpty() || mealCandidates.isEmpty() || hotelCandidates.isEmpty()) {
            return pendingDaySchedule(fallbackSlots(city, scenicCandidates, mealCandidates, hotelCandidates),
                    "缺少可用于时空求解的景点、餐饮或住宿候选");
        }
        ScheduleCandidate best = null;
        for (TripTravelQueryService.Place hotel : hotelCandidates) {
            ScheduleCandidate candidate = solveForHotel(state, city, scenicCandidates, mealCandidates, hotel);
            if (candidate != null && (best == null || candidate.compareTo(best) > 0)) {
                best = candidate;
            }
        }
        if (best != null) {
            return best.schedule();
        }
        return pendingDaySchedule(fallbackSlots(city, scenicCandidates, mealCandidates, hotelCandidates),
                "在营业时间、餐饮时段和每日截止时间约束下无法形成可行路线");
    }

    private ScheduleCandidate solveForHotel(Map<String, Object> state, String city,
                                            List<ScenicCandidate> scenicCandidates,
                                            List<TripTravelQueryService.Place> mealCandidates,
                                            TripTravelQueryService.Place hotel) {
        int dayStart = dayMinute(state.get("dailyStartTime"), DEFAULT_DAY_START_MINUTES);
        int dayEnd = dayMinute(state.get("dailyEndTime"), DEFAULT_DAY_END_MINUTES);
        Map<String, Object> accommodation = buildAccommodationSlot(city, hotel);
        if (point(accommodation) == null || dayStart >= dayEnd) {
            return null;
        }
        List<Map<String, Object>> activities = new ArrayList<>();
        scenicCandidates.forEach(candidate -> activities.add(buildScenicSlot("MORNING", city, candidate)));
        mealCandidates.forEach(place -> activities.add(buildMealSlot("LUNCH", city, place)));
        mealCandidates.forEach(place -> activities.add(buildMealSlot("DINNER", city, place)));
        List<Map<String, Object>> routeNodes = new ArrayList<>();
        routeNodes.add(accommodation);
        routeNodes.addAll(activities);
        RouteMatrix routeMatrix = buildRouteMatrix(routeNodes);
        List<NodeBinding> bindings = new ArrayList<>();
        bindings.add(new NodeBinding("depot", accommodation, 0, 0, false, TripOrToolsRouteOptimizer.SelectionGroup.NONE));
        int requiredScenicCount = 0;
        for (int index = 0; index < activities.size(); index++) {
            Map<String, Object> slot = activities.get(index);
            int serviceMinutes = MapUtil.getInt(slot, "suggestedStayMinutes", 60)
                    + (isScenicSlot(slot) ? SCENIC_BUFFER_MINUTES : 0);
            TimeWindow timeWindow = planningTimeWindow(slot, dayStart, dayEnd, serviceMinutes);
            if (point(slot) == null || timeWindow == null) {
                if (Boolean.TRUE.equals(slot.get("mustVisit"))) {
                    return null;
                }
                continue;
            }
            TripOrToolsRouteOptimizer.SelectionGroup group = selectionGroup(slot);
            if (group == TripOrToolsRouteOptimizer.SelectionGroup.SCENIC && Boolean.TRUE.equals(slot.get("mustVisit"))) {
                requiredScenicCount++;
            }
            bindings.add(new NodeBinding("node-" + index + '-' + StrUtil.blankToDefault(text(slot.get("poiId")), text(slot.get("slot"))),
                    slot, index + 1, bindings.size(), !Boolean.TRUE.equals(slot.get("mustVisit")), group));
        }
        int scenicCandidateCount = (int) bindings.stream()
                .filter(binding -> binding.selectionGroup() == TripOrToolsRouteOptimizer.SelectionGroup.SCENIC).count();
        int requestedScenicCount = Math.max(scenicCountPerDay(), requiredScenicCount);
        if (scenicCandidateCount == 0 || scenicCandidateCount < requiredScenicCount
                || bindings.stream().noneMatch(binding -> binding.selectionGroup() == TripOrToolsRouteOptimizer.SelectionGroup.LUNCH)
                || bindings.stream().noneMatch(binding -> binding.selectionGroup() == TripOrToolsRouteOptimizer.SelectionGroup.DINNER)) {
            return null;
        }
        RouteMatrix selectedMatrix = routeMatrix.select(bindings);
        Long budgetCapacity = dailyBudgetCapacity(state, accommodation, bindings);
        TripOrToolsRouteOptimizer.Result solved = null;
        String budgetStatus = "UNKNOWN";
        int selectedScenicCount = 0;
        for (int scenicSelectionCount = Math.min(requestedScenicCount, scenicCandidateCount);
             scenicSelectionCount >= Math.max(1, requiredScenicCount); scenicSelectionCount--) {
            Map<TripOrToolsRouteOptimizer.SelectionGroup, Integer> selectionCounts = Map.of(
                    TripOrToolsRouteOptimizer.SelectionGroup.SCENIC, scenicSelectionCount,
                    TripOrToolsRouteOptimizer.SelectionGroup.LUNCH, 1,
                    TripOrToolsRouteOptimizer.SelectionGroup.DINNER, 1);
            solved = solveDay(bindings, selectedMatrix, dayStart, dayEnd, budgetCapacity, selectionCounts);
            budgetStatus = budgetCapacity == null ? "UNKNOWN" : "WITHIN_BUDGET";
            if (solved == null && budgetCapacity != null) {
                solved = solveDay(bindings, selectedMatrix, dayStart, dayEnd, null, selectionCounts);
                budgetStatus = solved == null ? "UNKNOWN" : "EXCEEDED";
            }
            if (solved != null) {
                selectedScenicCount = scenicSelectionCount;
                break;
            }
        }
        if (solved == null) {
            return null;
        }
        Map<String, NodeBinding> bindingsById = new LinkedHashMap<>();
        bindings.forEach(binding -> bindingsById.put(binding.id(), binding));
        List<Map<String, Object>> orderedSlots = new ArrayList<>();
        int scenicCount = 0;
        int previousNodeIndex = 0;
        int utilityScore = 0;
        for (TripOrToolsRouteOptimizer.Visit visit : solved.visits()) {
            NodeBinding binding = bindingsById.get(visit.nodeId());
            if (binding == null) {
                continue;
            }
            Map<String, Object> slot = binding.slot();
            if (isScenicSlot(slot)) {
                String slotName = scenicSlotName(visit.startMinutes(), scenicCount++);
                slot.put("slot", slotName);
                slot.put("label", slotLabel(slotName));
                slot.put("bufferMinutesAfter", SCENIC_BUFFER_MINUTES);
            }
            slot.put("plannedStartTime", formatMinute(visit.startMinutes()));
            slot.put("plannedEndTime", formatMinute(visit.endMinutes()));
            slot.put("travelMinutesFromPrevious", visit.travelMinutesFromPrevious());
            slot.put("routeStatus", selectedMatrix.status(previousNodeIndex, binding.solverNodeIndex()));
            slot.put("planningStatus", "VALIDATED");
            utilityScore += MapUtil.getInt(slot, "utilityScore", 0);
            orderedSlots.add(slot);
            previousNodeIndex = binding.solverNodeIndex();
        }
        accommodation.put("plannedStartTime", formatMinute(solved.endMinutes()));
        accommodation.put("routeStatus", selectedMatrix.allVerified() ? "VERIFIED" : "ESTIMATED");
        accommodation.put("planningStatus", "VALIDATED");
        orderedSlots.add(accommodation);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("solver", "OR_TOOLS_TSPTW");
        // 候选池中未入选的节点会被正常移除；只要分组数量、时间窗和预算约束均已满足，即为可行解。
        metadata.put("status", "FEASIBLE");
        metadata.put("selectionStatus", selectedScenicCount < requestedScenicCount ? "DEGRADED" : "FULL");
        metadata.put("requestedScenicCount", requestedScenicCount);
        metadata.put("selectedScenicCount", selectedScenicCount);
        metadata.put("routeDataStatus", selectedMatrix.allVerified() ? "VERIFIED" : "ESTIMATED");
        metadata.put("budgetStatus", budgetStatus);
        metadata.put("droppedNodeIds", new ArrayList<>(solved.droppedNodeIds()));
        metadata.put("dayStartTime", formatMinute(dayStart));
        metadata.put("dayEndTime", formatMinute(dayEnd));
        metadata.put("candidateCounts", Map.of("scenic", scenicCandidates.size(), "meal", mealCandidates.size(), "hotel", 1));
        return new ScheduleCandidate(new DaySchedule(orderedSlots, metadata), utilityScore, solved.endMinutes());
    }

    private TripOrToolsRouteOptimizer.Result solveDay(List<NodeBinding> bindings, RouteMatrix routeMatrix,
                                                       int dayStart, int dayEnd, Long budgetCapacity,
                                                       Map<TripOrToolsRouteOptimizer.SelectionGroup, Integer> selectionCounts) {
        List<TripOrToolsRouteOptimizer.Node> nodes = new ArrayList<>();
        nodes.add(new TripOrToolsRouteOptimizer.Node("depot", 0, dayStart, dayEnd, 0, 0, false));
        for (int index = 1; index < bindings.size(); index++) {
            NodeBinding binding = bindings.get(index);
            Map<String, Object> slot = binding.slot();
            int serviceMinutes = MapUtil.getInt(slot, "suggestedStayMinutes", 60)
                    + (isScenicSlot(slot) ? SCENIC_BUFFER_MINUTES : 0);
            TimeWindow timeWindow = planningTimeWindow(slot, dayStart, dayEnd, serviceMinutes);
            nodes.add(new TripOrToolsRouteOptimizer.Node(binding.id(), serviceMinutes, timeWindow.startMinutes(),
                    timeWindow.endMinutes(), Math.max(0, nodeCost(slot)), MapUtil.getInt(slot, "utilityScore", 0), binding.optional(),
                    binding.selectionGroup(), StrUtil.blankToDefault(text(slot.get("poiId")), binding.id())));
        }
        return TripOrToolsRouteOptimizer.solve(nodes, routeMatrix.minutes(), dayStart, dayEnd, budgetCapacity, selectionCounts);
    }

    /** 候选阶段只计算本地估时；精确高德路线在用户打开地图时再按需查询。 */
    private RouteMatrix buildRouteMatrix(List<Map<String, Object>> nodes) {
        int size = nodes.size();
        long[][] minutes = new long[size][size];
        String[][] statuses = new String[size][size];
        boolean allVerified = false;
        for (int from = 0; from < size; from++) {
            for (int to = 0; to < size; to++) {
                if (from == to) {
                    statuses[from][to] = "VERIFIED";
                    continue;
                }
                Map<String, Object> origin = point(nodes.get(from));
                Map<String, Object> destination = point(nodes.get(to));
                double distance = distance(text(origin.get("longitude")), text(origin.get("latitude")),
                        text(destination.get("longitude")), text(destination.get("latitude")));
                minutes[from][to] = estimateDurationMinutes(distance);
                statuses[from][to] = "ESTIMATED";
            }
        }
        return new RouteMatrix(minutes, statuses, allVerified);
    }

    private static TimeWindow planningTimeWindow(Map<String, Object> slot, int dayStart, int dayEnd, int serviceMinutes) {
        int start = dayStart;
        int end = dayEnd;
        String slotName = text(slot.get("slot"));
        if ("LUNCH".equals(slotName)) {
            start = Math.max(start, LUNCH_START_MINUTES);
            end = Math.min(end, LUNCH_END_MINUTES);
        } else if ("DINNER".equals(slotName)) {
            start = Math.max(start, DINNER_START_MINUTES);
            end = Math.min(end, DINNER_END_MINUTES);
        }
        TimeWindow businessWindow = businessTimeWindow(text(slot.get("businessHours")), start, end, serviceMinutes);
        if (businessWindow != null) {
            return businessWindow;
        }
        return StrUtil.isBlank(text(slot.get("businessHours"))) && start + serviceMinutes <= end
                ? new TimeWindow(start, end) : null;
    }

    private static TimeWindow businessTimeWindow(String rawBusinessHours, int requestedStart, int requestedEnd,
                                                 int serviceMinutes) {
        Matcher matcher = TIME_RANGE_PATTERN.matcher(rawBusinessHours);
        while (matcher.find()) {
            int start = toMinute(matcher.group(1), matcher.group(2));
            int end = toMinute(matcher.group(3), matcher.group(4));
            if (end <= start) {
                continue;
            }
            int intersectStart = Math.max(start, requestedStart);
            int intersectEnd = Math.min(end, requestedEnd);
            if (intersectStart + serviceMinutes <= intersectEnd) {
                return new TimeWindow(intersectStart, intersectEnd);
            }
        }
        return null;
    }

    private static DaySchedule pendingDaySchedule(List<Map<String, Object>> slots, String reason) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("solver", "OR_TOOLS_TSPTW");
        metadata.put("status", "PENDING");
        metadata.put("reason", reason);
        return new DaySchedule(slots, metadata);
    }

    private static Long dailyBudgetCapacity(Map<String, Object> state, Map<String, Object> accommodation,
                                            List<NodeBinding> bindings) {
        Integer budget = normalizeAmount(state.get("budget"));
        Integer travelerCount = MapUtil.getInt(state, "travelerCount");
        Integer days = MapUtil.getInt(state, "days");
        long hotelCost = nodeCost(accommodation);
        if (budget == null || budget <= 0 || travelerCount == null || travelerCount <= 0 || days == null || days <= 0
                || hotelCost < 0) {
            return null;
        }
        for (int index = 1; index < bindings.size(); index++) {
            Map<String, Object> slot = bindings.get(index).slot();
            if (("LUNCH".equals(text(slot.get("slot"))) || "DINNER".equals(text(slot.get("slot")))) && nodeCost(slot) < 0) {
                return null;
            }
        }
        return Math.max(0L, (long) budget / days - hotelCost / travelerCount);
    }

    private static long nodeCost(Map<String, Object> slot) {
        String rawCost = text(slot.get("cost")).replaceAll("[^0-9.]", "");
        if (StrUtil.isBlank(rawCost)) {
            return -1L;
        }
        try {
            return Math.round(Double.parseDouble(rawCost));
        } catch (NumberFormatException ignored) {
            return -1L;
        }
    }

    private static Integer normalizeAmount(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        Matcher matcher = AMOUNT_PATTERN.matcher(text(value));
        if (!matcher.find()) {
            return null;
        }
        try {
            return Integer.valueOf(matcher.group().replace(",", ""));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static boolean isScenicSlot(Map<String, Object> slot) {
        String name = text(slot.get("slot"));
        return "MORNING".equals(name) || "AFTERNOON".equals(name) || "EVENING".equals(name);
    }

    private static String scenicSlotName(int startMinutes, int scenicIndex) {
        if (startMinutes < LUNCH_START_MINUTES && scenicIndex == 0) {
            return "MORNING";
        }
        if (startMinutes < DINNER_START_MINUTES) {
            return "AFTERNOON";
        }
        return "EVENING";
    }

    private static int dayMinute(Object value, int fallback) {
        String text = text(value);
        try {
            if (StrUtil.isBlank(text)) {
                return fallback;
            }
            LocalTime time = LocalTime.parse(text, TIME_FORMATTER);
            return time.getHour() * 60 + time.getMinute();
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static int toMinute(String hour, String minute) {
        int hourValue = Integer.parseInt(hour);
        int minuteValue = Integer.parseInt(minute);
        if (hourValue == 24 && minuteValue == 0) {
            return 24 * 60;
        }
        return hourValue >= 0 && hourValue < 24 && minuteValue >= 0 && minuteValue < 60 ? hourValue * 60 + minuteValue : -1;
    }

    private static String formatMinute(int minute) {
        return String.format("%02d:%02d", minute / 60, minute % 60);
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
        result.put("businessHours", candidate.spot().businessHours());
        result.put("rating", candidate.spot().rating());
        result.put("cost", candidate.spot().cost());
        result.put("utilityScore", candidate.preferenceScore());
        result.put("mustVisit", candidate.mustVisit());
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
        result.put("cost", place.cost());
        result.put("rating", place.rating());
        result.put("businessHours", place.businessHours());
        result.put("utilityScore", Math.max(1, (int) Math.round(rating(place) * 10)));
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
        result.put("businessHours", hotel.businessHours());
        result.put("skeleton", "入住" + hotel.name());
        return result;
    }

    private static Map<String, Object> buildTransport(String destination) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("arrival", Map.of("status", "PENDING", "city", destination, "skeleton", "抵达" + destination + "的交通待确认"));
        result.put("departure", Map.of("status", "PENDING", "city", destination, "skeleton", "从" + destination + "返程的交通待确认"));
        return result;
    }

    private List<ScenicCandidate> collectScenicCandidates(String destination, List<String> interests, List<String> mustVisit,
                                                           int candidateLimit) {
        List<String> keywords = new ArrayList<>(mustVisit);
        keywords.addAll(interests);
        if (keywords.isEmpty()) {
            keywords.add(destination);
        }
        Map<String, ScenicCandidate> candidates = new LinkedHashMap<>();
        for (int index = 0; index < keywords.size(); index++) {
            String keyword = keywords.get(index);
            boolean required = index < mustVisit.size();
            for (TripTravelQueryService.ScenicSpot spot : queryScenicSpots(destination, keyword, candidateLimit)) {
                String identity = StrUtil.blankToDefault(spot.externalId(), spot.name());
                ScenicCandidate candidate = new ScenicCandidate(spot, preferenceScore(index),
                        required && StrUtil.containsIgnoreCase(spot.name(), keyword));
                candidates.merge(identity, candidate, (left, right) -> left.mustVisit() ? left
                        : right.mustVisit() ? right : right.preferenceScore() >= left.preferenceScore() ? right : left);
            }
        }
        if (candidates.size() < candidateLimit && !interests.isEmpty()) {
            for (TripTravelQueryService.ScenicSpot spot : queryScenicSpots(destination, destination, candidateLimit)) {
                String identity = StrUtil.blankToDefault(spot.externalId(), spot.name());
                candidates.putIfAbsent(identity, new ScenicCandidate(spot, 0, false));
            }
        }
        return candidates.values().stream().sorted(Comparator.<ScenicCandidate>comparingInt(candidate -> candidate.mustVisit() ? 1 : 0).reversed()
                .thenComparing(Comparator.comparingInt(ScenicCandidate::preferenceScore).reversed())
                .thenComparing(Comparator.comparingDouble((ScenicCandidate candidate) -> scenicRating(candidate.spot())).reversed()))
                .limit(candidateLimit).toList();
    }

    private static int candidateLimit(int days) {
        return Math.min(MAX_CATEGORY_CANDIDATE_POOL_SIZE, Math.max(CANDIDATES_PER_DAY, days * CANDIDATES_PER_DAY));
    }

    private static List<ScenicCandidate> rankScenicCandidates(List<ScenicCandidate> candidates, Set<String> usedScenicIds) {
        return candidates.stream()
                .filter(candidate -> !usedScenicIds.contains(scenicIdentity(candidate)))
                .filter(candidate -> hasCoordinate(candidate.spot().longitude(), candidate.spot().latitude()))
                .sorted(Comparator.<ScenicCandidate>comparingInt(candidate -> candidate.mustVisit() ? 1 : 0).reversed()
                        .thenComparing(Comparator.comparingInt(ScenicCandidate::preferenceScore).reversed())
                        .thenComparing(Comparator.comparingDouble((ScenicCandidate candidate) -> scenicRating(candidate.spot())).reversed()))
                .limit(DAILY_SCENIC_CANDIDATE_LIMIT)
                .toList();
    }

    private static List<TripTravelQueryService.Place> rankMealCandidates(List<TripTravelQueryService.Place> candidates) {
        return candidates.stream()
                .filter(place -> hasCoordinate(place.longitude(), place.latitude()))
                .sorted(Comparator.comparingDouble(TripItineraryAssembler::rating).reversed()
                        .thenComparingDouble(TripItineraryAssembler::cost)
                        .thenComparing(TripTravelQueryService.Place::name))
                .limit(DAILY_MEAL_CANDIDATE_LIMIT)
                .toList();
    }

    private static List<TripTravelQueryService.Place> rankHotelCandidates(List<TripTravelQueryService.Place> candidates,
                                                                            int hotelBudget) {
        return candidates.stream()
                .filter(place -> hasCoordinate(place.longitude(), place.latitude()))
                .sorted(Comparator.comparingDouble((TripTravelQueryService.Place hotel) -> Math.abs(cost(hotel) - hotelBudget))
                        .thenComparing(Comparator.comparingInt(TripItineraryAssembler::starLevel).reversed())
                        .thenComparing(Comparator.comparingDouble(TripItineraryAssembler::rating).reversed())
                        .thenComparing(TripTravelQueryService.Place::name))
                .limit(DAILY_HOTEL_CANDIDATE_LIMIT)
                .toList();
    }

    private static List<Map<String, Object>> fallbackSlots(String city, List<ScenicCandidate> scenicCandidates,
                                                            List<TripTravelQueryService.Place> mealCandidates,
                                                            List<TripTravelQueryService.Place> hotelCandidates) {
        List<Map<String, Object>> slots = new ArrayList<>();
        slots.add(buildScenicSlot("MORNING", city, pick(scenicCandidates, 0)));
        slots.add(buildMealSlot("LUNCH", city, pick(mealCandidates, 0)));
        slots.add(buildScenicSlot("AFTERNOON", city, pick(scenicCandidates, 1)));
        slots.add(buildMealSlot("DINNER", city, pick(mealCandidates, mealCandidates.size() > 1 ? 1 : 0)));
        slots.add(buildAccommodationSlot(city, pick(hotelCandidates, 0)));
        return slots;
    }

    private static String scenicIdentity(ScenicCandidate candidate) {
        return StrUtil.blankToDefault(candidate.spot().externalId(), candidate.spot().name());
    }

    private static double scenicRating(TripTravelQueryService.ScenicSpot spot) {
        try {
            return Double.parseDouble(spot.rating());
        } catch (RuntimeException ignored) {
            return 0D;
        }
    }

    private static boolean hasCoordinate(String longitude, String latitude) {
        return distance(longitude, latitude, longitude, latitude) >= 0D;
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

    private static TripOrToolsRouteOptimizer.SelectionGroup selectionGroup(Map<String, Object> slot) {
        return switch (text(slot.get("slot"))) {
            case "LUNCH" -> TripOrToolsRouteOptimizer.SelectionGroup.LUNCH;
            case "DINNER" -> TripOrToolsRouteOptimizer.SelectionGroup.DINNER;
            default -> TripOrToolsRouteOptimizer.SelectionGroup.SCENIC;
        };
    }

    private static int preferenceScore(int index) {
        return Math.max(1, 100 - index);
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
        return index < 0 || index >= values.size() ? null : values.get(index);
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

    private record ScenicCandidate(TripTravelQueryService.ScenicSpot spot, int preferenceScore, boolean mustVisit) {
    }

    private record NodeBinding(String id, Map<String, Object> slot, int sourceNodeIndex, int solverNodeIndex,
                               boolean optional, TripOrToolsRouteOptimizer.SelectionGroup selectionGroup) {
    }

    private record TimeWindow(int startMinutes, int endMinutes) {
    }

    private record DaySchedule(List<Map<String, Object>> slots, Map<String, Object> metadata) {
    }

    private record ScheduleCandidate(DaySchedule schedule, int utilityScore, int endMinutes)
            implements Comparable<ScheduleCandidate> {

        @Override
        public int compareTo(ScheduleCandidate other) {
            int utilityComparison = Integer.compare(utilityScore, other.utilityScore);
            return utilityComparison != 0 ? utilityComparison : Integer.compare(other.endMinutes, endMinutes);
        }
    }

    private record RouteMatrix(long[][] minutes, String[][] statuses, boolean allVerified) {

        private RouteMatrix select(List<NodeBinding> bindings) {
            int size = bindings.size();
            long[][] selectedMinutes = new long[size][size];
            String[][] selectedStatuses = new String[size][size];
            boolean selectedAllVerified = true;
            for (int from = 0; from < size; from++) {
                for (int to = 0; to < size; to++) {
                    int sourceFrom = bindings.get(from).sourceNodeIndex();
                    int sourceTo = bindings.get(to).sourceNodeIndex();
                    selectedMinutes[from][to] = minutes[sourceFrom][sourceTo];
                    selectedStatuses[from][to] = statuses[sourceFrom][sourceTo];
                    selectedAllVerified &= "VERIFIED".equals(selectedStatuses[from][to]);
                }
            }
            return new RouteMatrix(selectedMinutes, selectedStatuses, selectedAllVerified);
        }

        private String status(int from, int to) {
            return statuses[from][to];
        }
    }

}
