package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.tracer.core.util.TracerFrameworkUtils;
import jakarta.annotation.Resource;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/** 旅行检索的确定性执行器；不接受 LLM 自行指定的工具、参数或供应商。 */
@Service
@Slf4j
public class ItineraryResearchExecutor {

    private static final int MAX_SCENIC_SPOT_CANDIDATES = 2;
    private static final int MAX_TRAVEL_PLACE_CANDIDATES = 2;
    private static final int SCENIC_SPOT_QUERY_LIMIT = 8;
    private static final ReentrantLock[] SCENIC_QUERY_LOCKS = createScenicQueryLocks();

    @Resource
    private ItineraryTravelQueryService itineraryTravelQueryService;
    @Resource
    private Tracer tracer;

    /**
     * 只补充一个骨架节点，供前端并行调用；绝不为生成骨架而提前阻塞工具请求。
     * 外部候选直接返回并写入所属行程 JSON，不建立本地实体副本。
     */
    public SlotResearchResult resolveSlot(Long tripId, Map<String, Object> state, Integer day, String slot, String skeleton,
                                          String poiName, String area) {
        String normalizedSlot = StrUtil.trim(slot).toUpperCase(java.util.Locale.ROOT);
        Span span = tracer.spanBuilder("trip.agent.slot.resolve")
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute("trip.id", String.valueOf(tripId))
                .setAttribute("trip.itinerary.slot", normalizedSlot)
                .startSpan();
        try (Scope ignored = span.makeCurrent()) {
            String destination = StrUtil.trim(ObjUtil.toString(state.get("destination")));
            SlotResearchResult result = switch (normalizedSlot) {
                case "MORNING", "AFTERNOON", "EVENING" -> resolveScenicSpotSlot(tripId, destination, day, normalizedSlot,
                        skeleton, poiName, area);
                case "ACCOMMODATION" -> resolveTravelPlaceSlot(tripId, destination, day, normalizedSlot, "HOTEL");
                case "LUNCH", "DINNER" -> resolveTravelPlaceSlot(tripId, destination, day, normalizedSlot, "RESTAURANT");
                case "ARRIVAL", "DEPARTURE" -> pending(normalizedSlot, "TRANSPORT_QUERY", "交通供应商尚未接入，保留为待确认");
                default -> throw new IllegalArgumentException("不支持的行程节点：" + slot);
            };
            span.setAttribute("trip.tool.status", result.status());
            span.setAttribute("trip.tool.candidate_count", result.candidates().size());
            span.setAttribute("trip.tool.plan_count", result.toolPlans().size());
            return result;
        } catch (RuntimeException e) {
            TracerFrameworkUtils.onError(e, span);
            throw e;
        } finally {
            span.end();
        }
    }

    private SlotResearchResult resolveTravelPlaceSlot(Long tripId, String destination, Integer day, String slot, String type) {
        ReentrantLock lock = scenicQueryLock(tripId);
        lock.lock();
        try {
            List<ItineraryTravelQueryService.Place> places = "HOTEL".equals(type)
                    ? itineraryTravelQueryService.queryHotels(destination, MAX_TRAVEL_PLACE_CANDIDATES)
                    : itineraryTravelQueryService.queryRestaurants(destination, MAX_TRAVEL_PLACE_CANDIDATES);
            List<Map<String, Object>> candidates = places.stream().limit(MAX_TRAVEL_PLACE_CANDIDATES)
                    .map(place -> toTravelPlaceCandidate(place, type)).toList();
            if (candidates.isEmpty()) {
                return new SlotResearchResult(slot, "PENDING", "未查询到" + travelPlaceLabel(type) + "候选", List.of(),
                        List.of(), List.of(toolPlan(type + "_QUERY", "EXECUTED", "未查询到候选")));
            }
            return resolved(slot, "已获取 " + candidates.size() + " 个" + travelPlaceLabel(type) + "候选", candidates,
                    List.of(), toolPlan(type + "_QUERY", "EXECUTED", "已获取候选"));
        } catch (RuntimeException e) {
            log.warn("[resolveTravelPlaceSlot][tripId({}) day({}) slot({}) type({}) destination({}) 查询失败]",
                    tripId, day, slot, type, destination, e);
            return pending(slot, type + "_QUERY", travelPlaceLabel(type) + "服务暂不可用");
        } finally {
            lock.unlock();
        }
    }

    private SlotResearchResult resolveScenicSpotSlot(Long tripId, String destination, Integer day, String slot,
                                                      String skeleton, String poiName, String area) {
        String keyword = StrUtil.trim(poiName);
        if (StrUtil.isBlank(keyword)) {
            return pending(slot, "SCENIC_SPOT_QUERY", "行程骨架缺少景点名称，无法查询候选");
        }
        ReentrantLock lock = scenicQueryLock(tripId);
        lock.lock();
        try {
            List<ItineraryTravelQueryService.ScenicSpot> spots = itineraryTravelQueryService.queryScenicSpots(destination, keyword,
                    SCENIC_SPOT_QUERY_LIMIT);
            List<Map<String, Object>> candidates = spots.stream()
                    .limit(MAX_SCENIC_SPOT_CANDIDATES)
                    .map(ItineraryResearchExecutor::toScenicCandidate)
                    .toList();
            if (candidates.isEmpty()) {
                return new SlotResearchResult(slot, "PENDING", "未查询到景点候选", List.of(), List.of(),
                        List.of(toolPlan("SCENIC_SPOT_QUERY", "EXECUTED", "未查询到景点候选")));
            }
            return resolved(slot, "已获取 " + candidates.size() + " 个景点候选", candidates, List.of(),
                    toolPlan("SCENIC_SPOT_QUERY", "EXECUTED", "已获取 " + candidates.size() + " 个景点候选"));
        } catch (RuntimeException e) {
            log.warn("[resolveScenicSpotSlot][tripId({}) day({}) slot({}) destination({}) 景点查询失败]",
                    tripId, day, slot, destination, e);
            return pending(slot, "SCENIC_SPOT_QUERY", "景点服务暂不可用");
        } finally {
            lock.unlock();
        }
    }

    private static SlotResearchResult pending(String slot, String toolType, String detail) {
        return new SlotResearchResult(slot, "PENDING", detail, List.of(), List.of(), List.of(toolPlan(toolType, "SKIPPED", detail)));
    }

    private static SlotResearchResult resolved(String slot, String detail, List<Map<String, Object>> candidates,
                                                List<String> citationIds, Map<String, Object> toolPlan) {
        return new SlotResearchResult(slot, "RESOLVED", detail, candidates, citationIds, List.of(toolPlan));
    }

    private static ReentrantLock scenicQueryLock(Long tripId) {
        return SCENIC_QUERY_LOCKS[Math.floorMod(tripId.hashCode(), SCENIC_QUERY_LOCKS.length)];
    }

    private static ReentrantLock[] createScenicQueryLocks() {
        ReentrantLock[] locks = new ReentrantLock[64];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ReentrantLock();
        }
        return locks;
    }

    private static Map<String, Object> toScenicCandidate(ItineraryTravelQueryService.ScenicSpot spot) {
        Map<String, Object> candidate = new LinkedHashMap<>();
        candidate.put("id", spot.poiId());
        candidate.put("type", "POI");
        candidate.put("name", spot.name());
        candidate.put("provider", spot.provider());
        candidate.put("poiId", spot.poiId());
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("address", spot.address());
        metadata.put("imageUrl", spot.imageUrl());
        metadata.put("longitude", spot.longitude());
        metadata.put("latitude", spot.latitude());
        candidate.put("metadata", metadata);
        return candidate;
    }

    private static Map<String, Object> toTravelPlaceCandidate(ItineraryTravelQueryService.Place place, String type) {
        Map<String, Object> candidate = new LinkedHashMap<>();
        candidate.put("id", place.poiId());
        candidate.put("type", type);
        candidate.put("name", place.name());
        candidate.put("provider", place.provider());
        candidate.put("poiId", place.poiId());
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("address", place.address());
        metadata.put("imageUrl", place.imageUrl());
        metadata.put("longitude", place.longitude());
        metadata.put("latitude", place.latitude());
        metadata.put("telephone", place.telephone());
        metadata.put("rating", place.rating());
        metadata.put("cost", place.cost());
        metadata.put("tag", place.tag());
        candidate.put("metadata", metadata);
        return candidate;
    }

    private static Map<String, Object> toolPlan(String type, String status, String detail) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", type);
        result.put("status", status);
        result.put("detail", detail);
        return result;
    }

    private static String travelPlaceLabel(String type) {
        return "HOTEL".equals(type) ? "酒店" : "餐厅";
    }

    public record SlotResearchResult(String slot, String status, String detail, List<Map<String, Object>> candidates,
                                     List<String> citationIds, List<Map<String, Object>> toolPlans) {
    }

}
