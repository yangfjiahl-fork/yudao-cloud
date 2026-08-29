package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tracer.core.util.TracerFrameworkUtils;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripFactDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripSourceDO;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripFactMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripSourceMapper;
import jakarta.annotation.Resource;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/** 旅行检索的确定性执行器；不接受 LLM 自行指定的工具、参数或供应商。 */
@Service
@Slf4j
public class TripResearchExecutor {

    private static final int STATUS_VERIFIED = 1;
    private static final int MAX_SCENIC_SPOT_CANDIDATES = 2;
    private static final int MAX_TRAVEL_PLACE_CANDIDATES = 2;
    private static final int SCENIC_SPOT_QUERY_LIMIT = 8;
    private static final String FACT_KEY_POI_CANDIDATES_PREFIX = "poi.candidates.";
    private static final String FACT_KEY_TRAVEL_PLACE_CANDIDATES_PREFIX = "travel.place.candidates.";
    private static final ReentrantLock[] SCENIC_QUERY_LOCKS = createScenicQueryLocks();

    @Resource
    private TripTravelQueryService tripTravelQueryService;
    @Resource
    private TripFactMapper tripFactMapper;
    @Resource
    private TripSourceMapper tripSourceMapper;
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
        String factKey = travelPlaceFactKey(destination, day, slot, type);
        ReentrantLock lock = scenicQueryLock(tripId);
        lock.lock();
        try {
            TripFactDO cachedFact = tripFactMapper.selectActiveByTripIdAndFactKey(tripId, factKey);
            if (isFactForDestination(cachedFact, destination)) {
                List<Map<String, Object>> candidates = getCandidateList(JsonUtils.parseMap(cachedFact.getValueJson()).get("candidates"));
                return candidates.isEmpty()
                        ? pending(slot, type + "_QUERY", "未查询到" + travelPlaceLabel(type) + "候选")
                        : resolved(slot, travelPlaceLabel(type) + "候选已获取", candidates,
                        List.of(String.valueOf(cachedFact.getId())), toolPlan(type + "_QUERY", "CACHED", "当前日程节点候选未过期"));
            }
            List<TripTravelQueryService.Place> places = "HOTEL".equals(type)
                    ? tripTravelQueryService.queryHotels(destination, MAX_TRAVEL_PLACE_CANDIDATES)
                    : tripTravelQueryService.queryRestaurants(destination, MAX_TRAVEL_PLACE_CANDIDATES);
            List<Map<String, Object>> candidates = places.stream().limit(MAX_TRAVEL_PLACE_CANDIDATES)
                    .map(place -> toTravelPlaceCandidate(place, type)).toList();
            LocalDateTime now = LocalDateTime.now();
            TripSourceDO source = insertSource(tripId, places.isEmpty() ? "provider" : places.get(0).provider(),
                    travelPlaceLabel(type) + "查询（" + destination + "·第" + day + "天·" + slot + "）",
                    travelPlaceSourceUrl(places), now, now.plusDays(30));
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("city", destination);
            value.put("type", type);
            value.put("candidates", candidates);
            TripFactDO fact = upsertFact(tripId, cachedFact, factKey, value, source.getId(), now, now.plusDays(30));
            if (candidates.isEmpty()) {
                return new SlotResearchResult(slot, "PENDING", "未查询到" + travelPlaceLabel(type) + "候选", List.of(),
                        List.of(String.valueOf(fact.getId())), List.of(toolPlan(type + "_QUERY", "EXECUTED", "未查询到候选")));
            }
            return resolved(slot, "已获取 " + candidates.size() + " 个" + travelPlaceLabel(type) + "候选", candidates,
                    List.of(String.valueOf(fact.getId())), toolPlan(type + "_QUERY", "EXECUTED", "已获取候选"));
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
        String factKey = scenicFactKey(destination, day, slot, keyword, area);
        ReentrantLock lock = scenicQueryLock(tripId);
        lock.lock();
        try {
            TripFactDO cachedFact = tripFactMapper.selectActiveByTripIdAndFactKey(tripId, factKey);
            if (isFactForDestination(cachedFact, destination)) {
                Map<String, Object> value = JsonUtils.parseMap(cachedFact.getValueJson());
                List<Map<String, Object>> candidates = getCandidateList(value.get("candidates"));
                return resolved(slot, "景点候选已获取", candidates, List.of(String.valueOf(cachedFact.getId())),
                        toolPlan("SCENIC_SPOT_QUERY", "CACHED", "当前日程节点的景点候选未过期"));
            }
            Set<String> usedCandidateIds = usedScenicCandidateIds(tripId, factKey);
            List<TripTravelQueryService.ScenicSpot> spots = tripTravelQueryService.queryScenicSpots(destination, keyword,
                    SCENIC_SPOT_QUERY_LIMIT);
            List<Map<String, Object>> candidates = spots.stream()
                    .filter(spot -> usedCandidateIds.add(scenicCandidateIdentity(spot)))
                    .limit(MAX_SCENIC_SPOT_CANDIDATES)
                    .map(TripResearchExecutor::toScenicCandidate)
                    .toList();
            LocalDateTime now = LocalDateTime.now();
            TripSourceDO source = insertSource(tripId, spots.isEmpty() ? "provider" : spots.get(0).provider(),
                    "景点查询（" + destination + "·第" + day + "天·" + slot + "）", scenicSourceUrl(spots), now, now.plusDays(30));
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("city", destination);
            value.put("keyword", keyword);
            value.put("area", StrUtil.trim(area));
            value.put("candidates", candidates);
            TripFactDO fact = upsertFact(tripId, cachedFact, factKey, value, source.getId(), now, now.plusDays(30));
            if (candidates.isEmpty()) {
                return new SlotResearchResult(slot, "PENDING", "未查询到与其他日程节点不同的景点候选", List.of(),
                        List.of(String.valueOf(fact.getId())), List.of(toolPlan("SCENIC_SPOT_QUERY", "EXECUTED",
                        "未查询到与其他日程节点不同的景点候选")));
            }
            return resolved(slot, "已获取 " + candidates.size() + " 个景点候选", candidates, List.of(String.valueOf(fact.getId())),
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

    private TripSourceDO insertSource(Long tripId, String provider, String name, String url,
                                      LocalDateTime retrievedAt, LocalDateTime expiresAt) {
        TripSourceDO source = new TripSourceDO();
        source.setTripId(tripId);
        source.setSourceType("provider");
        source.setSourceName(StrUtil.blankToDefault(provider, "provider") + "：" + name);
        source.setSourceUrl(url);
        source.setRetrievedAt(retrievedAt);
        source.setExpiresAt(expiresAt);
        source.setStatus(STATUS_VERIFIED);
        tripSourceMapper.insert(source);
        return source;
    }

    private TripFactDO upsertFact(Long tripId, TripFactDO fact, String key, Map<String, Object> value, Long sourceId,
                                  LocalDateTime retrievedAt, LocalDateTime expiresAt) {
        if (fact == null) {
            fact = new TripFactDO();
            fact.setTripId(tripId);
            fact.setFactKey(key);
        }
        fact.setValueJson(JsonUtils.toJsonString(value));
        fact.setSourceId(sourceId);
        fact.setRetrievedAt(retrievedAt);
        fact.setExpiresAt(expiresAt);
        fact.setConfidence(1D);
        fact.setStatus(STATUS_VERIFIED);
        if (fact.getId() == null) {
            tripFactMapper.insert(fact);
        } else {
            tripFactMapper.updateById(fact);
        }
        return fact;
    }

    private static boolean isFactForDestination(TripFactDO fact, String destination) {
        return fact != null && StrUtil.equals(destination, ObjUtil.toString(JsonUtils.parseMap(fact.getValueJson()).get("city")));
    }

    private Set<String> usedScenicCandidateIds(Long tripId, String currentFactKey) {
        Set<String> result = new HashSet<>();
        tripFactMapper.selectActiveByTripIdAndFactKeyPrefix(tripId, FACT_KEY_POI_CANDIDATES_PREFIX).forEach(fact -> {
            if (StrUtil.equals(fact.getFactKey(), currentFactKey)) {
                return;
            }
            Map<String, Object> value = JsonUtils.parseMap(fact.getValueJson());
            getCandidateList(value.get("candidates")).forEach(candidate ->
                    result.add(StrUtil.blankToDefault(ObjUtil.toString(candidate.get("externalId")),
                            ObjUtil.toString(candidate.get("id")))));
        });
        return result;
    }

    private static String scenicFactKey(String destination, Integer day, String slot, String poiName, String area) {
        return FACT_KEY_POI_CANDIDATES_PREFIX + DigestUtil.md5Hex(StrUtil.join("|", destination, day, slot, poiName, area));
    }

    private static String travelPlaceFactKey(String destination, Integer day, String slot, String type) {
        return FACT_KEY_TRAVEL_PLACE_CANDIDATES_PREFIX + DigestUtil.md5Hex(StrUtil.join("|", destination, day, slot, type));
    }

    private static String scenicCandidateIdentity(TripTravelQueryService.ScenicSpot spot) {
        return StrUtil.blankToDefault(StrUtil.trim(spot.externalId()), StrUtil.trim(spot.name()));
    }

    private static String pendingDetail(String detail, String skeleton) {
        String context = StrUtil.trim(skeleton);
        return StrUtil.isBlank(context) ? detail : detail + "（当前安排：" + StrUtil.maxLength(context, 60) + "）";
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

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> getCandidateList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> candidates = new ArrayList<>();
        list.forEach(item -> {
            if (item instanceof Map<?, ?> candidate) {
                candidates.add((Map<String, Object>) candidate);
            }
        });
        return candidates.stream().limit(MAX_SCENIC_SPOT_CANDIDATES).toList();
    }

    private static Map<String, Object> toScenicCandidate(TripTravelQueryService.ScenicSpot spot) {
        Map<String, Object> candidate = new LinkedHashMap<>();
        candidate.put("id", spot.externalId());
        candidate.put("type", "POI");
        candidate.put("name", spot.name());
        candidate.put("provider", spot.provider());
        candidate.put("externalId", spot.externalId());
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("address", spot.address());
        metadata.put("imageUrl", spot.imageUrl());
        metadata.put("longitude", spot.longitude());
        metadata.put("latitude", spot.latitude());
        candidate.put("metadata", metadata);
        return candidate;
    }

    private static Map<String, Object> toTravelPlaceCandidate(TripTravelQueryService.Place place, String type) {
        Map<String, Object> candidate = new LinkedHashMap<>();
        candidate.put("id", place.externalId());
        candidate.put("type", type);
        candidate.put("name", place.name());
        candidate.put("provider", place.provider());
        candidate.put("externalId", place.externalId());
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

    private static String scenicSourceUrl(List<TripTravelQueryService.ScenicSpot> spots) {
        return !spots.isEmpty() && "gaode".equalsIgnoreCase(spots.get(0).provider())
                ? "https://lbs.amap.com/api/webservice/guide/api-advanced/newpoisearch"
                : "https://market.aliyun.com/";
    }

    private static String travelPlaceSourceUrl(List<TripTravelQueryService.Place> places) {
        return !places.isEmpty() && "gaode".equalsIgnoreCase(places.get(0).provider())
                ? "https://lbs.amap.com/api/webservice/guide/api-advanced/newpoisearch" : "";
    }

    private static String travelPlaceLabel(String type) {
        return "HOTEL".equals(type) ? "酒店" : "餐厅";
    }

    public record SlotResearchResult(String slot, String status, String detail, List<Map<String, Object>> candidates,
                                     List<String> citationIds, List<Map<String, Object>> toolPlans) {
    }

}
