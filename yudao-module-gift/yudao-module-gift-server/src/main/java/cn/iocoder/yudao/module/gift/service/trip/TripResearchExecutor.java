package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tracer.core.util.TracerFrameworkUtils;
import cn.iocoder.yudao.module.ai.api.travel.AiTravelQueryApi;
import cn.iocoder.yudao.module.ai.api.travel.dto.AiTravelScenicSpotRespDTO;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 旅行检索的确定性执行器；不接受 LLM 自行指定的工具、参数或供应商。 */
@Service
@Slf4j
public class TripResearchExecutor {

    private static final int STATUS_VERIFIED = 1;
    private static final int MAX_SCENIC_SPOT_CANDIDATES = 2;
    private static final String FACT_KEY_POI_CANDIDATES = "poi.candidates";

    @Resource
    private AiTravelQueryApi aiTravelQueryApi;
    @Resource
    private TripHotelQueryClient tripHotelQueryClient;
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
    public SlotResearchResult resolveSlot(Long tripId, Map<String, Object> state, String slot) {
        String normalizedSlot = StrUtil.trim(slot).toUpperCase(java.util.Locale.ROOT);
        Span span = tracer.spanBuilder("trip.agent.slot.resolve")
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute("trip.id", String.valueOf(tripId))
                .setAttribute("trip.itinerary.slot", normalizedSlot)
                .startSpan();
        try (Scope ignored = span.makeCurrent()) {
            String destination = StrUtil.trim(ObjUtil.toString(state.get("destination")));
            SlotResearchResult result = switch (normalizedSlot) {
                case "MORNING", "AFTERNOON", "EVENING" -> resolveScenicSpotSlot(tripId, state, destination, normalizedSlot);
                case "ACCOMMODATION" -> resolveHotelSlot(destination, normalizedSlot);
                case "LUNCH", "DINNER" -> pending(normalizedSlot, "RESTAURANT_QUERY", "餐饮供应商尚未接入，保留为待确认");
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

    private SlotResearchResult resolveScenicSpotSlot(Long tripId, Map<String, Object> state, String destination, String slot) {
        TripFactDO cachedFact = tripFactMapper.selectActiveByTripIdAndFactKey(tripId, FACT_KEY_POI_CANDIDATES);
        if (isFactForDestination(cachedFact, destination)) {
            Map<String, Object> value = JsonUtils.parseMap(cachedFact.getValueJson());
            List<Map<String, Object>> candidates = getCandidateList(value.get("candidates"));
            return resolved(slot, "景点候选已获取", candidates, List.of(String.valueOf(cachedFact.getId())),
                    toolPlan("SCENIC_SPOT_QUERY", "CACHED", "景点候选事实未过期"));
        }
        try {
            String keyword = firstInterest(state);
            List<AiTravelScenicSpotRespDTO> spots = aiTravelQueryApi.queryScenicSpots(destination, keyword,
                    MAX_SCENIC_SPOT_CANDIDATES);
            List<Map<String, Object>> candidates = spots.stream().map(TripResearchExecutor::toScenicCandidate).toList();
            LocalDateTime now = LocalDateTime.now();
            TripSourceDO source = insertSource(tripId, spots.isEmpty() ? "provider" : spots.get(0).getProvider(),
                    "景点查询（" + destination + "）", scenicSourceUrl(spots), now, now.plusDays(30));
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("city", destination);
            value.put("keyword", keyword);
            value.put("candidates", candidates);
            TripFactDO fact = upsertFact(tripId, cachedFact, FACT_KEY_POI_CANDIDATES, value, source.getId(), now, now.plusDays(30));
            return resolved(slot, "已获取 " + candidates.size() + " 个景点候选", candidates, List.of(String.valueOf(fact.getId())),
                    toolPlan("SCENIC_SPOT_QUERY", "EXECUTED", "已获取 " + candidates.size() + " 个景点候选"));
        } catch (RuntimeException e) {
            log.warn("[resolveScenicSpotSlot][tripId({}) destination({}) 景点查询失败]", tripId, destination, e);
            return pending(slot, "SCENIC_SPOT_QUERY", "景点服务暂不可用");
        }
    }

    private SlotResearchResult resolveHotelSlot(String destination, String slot) {
        TripHotelQueryClient.HotelCandidate hotel = tripHotelQueryClient.queryByCity(destination);
        Map<String, Object> candidate = new LinkedHashMap<>();
        candidate.put("id", hotel.externalId());
        candidate.put("type", "HOTEL");
        candidate.put("name", hotel.name());
        candidate.put("provider", "mock_hotel");
        candidate.put("externalId", hotel.externalId());
        candidate.put("metadata", Map.of("imageUrl", hotel.imageUrl(), "placeholder", true));
        return resolved(slot, "占位酒店候选；不代表价格、库存或可预订性", List.of(candidate), List.of(),
                toolPlan("HOTEL_CANDIDATE", "EXECUTED", "占位酒店候选；不代表价格、库存或可预订性"));
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

    private static Map<String, Object> toScenicCandidate(AiTravelScenicSpotRespDTO spot) {
        Map<String, Object> candidate = new LinkedHashMap<>();
        candidate.put("id", spot.getExternalId());
        candidate.put("type", "POI");
        candidate.put("name", spot.getName());
        candidate.put("provider", spot.getProvider());
        candidate.put("externalId", spot.getExternalId());
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("address", spot.getAddress());
        metadata.put("imageUrl", spot.getImageUrl());
        metadata.put("longitude", spot.getLongitude());
        metadata.put("latitude", spot.getLatitude());
        candidate.put("metadata", metadata);
        return candidate;
    }

    private static String firstInterest(Map<String, Object> state) {
        Object value = state.get("interests");
        if (value instanceof List<?> list && !list.isEmpty()) {
            return StrUtil.trim(ObjUtil.toString(list.get(0)));
        }
        return "景点";
    }

    private static Map<String, Object> toolPlan(String type, String status, String detail) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", type);
        result.put("status", status);
        result.put("detail", detail);
        return result;
    }

    private static String scenicSourceUrl(List<AiTravelScenicSpotRespDTO> spots) {
        return !spots.isEmpty() && "gaode".equalsIgnoreCase(spots.get(0).getProvider())
                ? "https://lbs.amap.com/api/webservice/guide/api-advanced/newpoisearch"
                : "https://market.aliyun.com/";
    }

    public record SlotResearchResult(String slot, String status, String detail, List<Map<String, Object>> candidates,
                                     List<String> citationIds, List<Map<String, Object>> toolPlans) {
    }

}
