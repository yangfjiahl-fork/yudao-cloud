package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.api.travel.AiTravelQueryApi;
import cn.iocoder.yudao.module.ai.api.travel.dto.AiTravelScenicSpotRespDTO;
import cn.iocoder.yudao.module.ai.api.travel.dto.AiTravelWeatherRespDTO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripEntityDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripFactDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripSourceDO;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripEntityMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripFactMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripSourceMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private static final int ENTITY_STATUS_RESOLVED = 1;
    private static final String ENTITY_TYPE_POI = "POI";
    private static final String ENTITY_TYPE_HOTEL = "HOTEL";

    @Resource
    private AiTravelQueryApi aiTravelQueryApi;
    @Resource
    private TripHotelQueryClient tripHotelQueryClient;
    @Resource
    private TripEntityMapper tripEntityMapper;
    @Resource
    private TripFactMapper tripFactMapper;
    @Resource
    private TripSourceMapper tripSourceMapper;

    public ResearchResult execute(Long tripId, Map<String, Object> state, TripEntityDO destinationEntity) {
        List<Map<String, Object>> toolPlans = new ArrayList<>();
        String destination = ObjUtil.toString(state.get("destination"));
        upsertMockHotel(tripId, destination, destinationEntity.getId(), toolPlans);
        queryScenicSpotsIfNeeded(tripId, state, destination, destinationEntity, toolPlans);
        queryCurrentWeatherIfNeeded(tripId, state, destination, destinationEntity, toolPlans);
        int executed = (int) toolPlans.stream().filter(plan -> "EXECUTED".equals(plan.get("status"))).count();
        return new ResearchResult(toolPlans, executed);
    }

    private void upsertMockHotel(Long tripId, String destination, Long cityEntityId, List<Map<String, Object>> toolPlans) {
        TripHotelQueryClient.HotelCandidate hotel = tripHotelQueryClient.queryByCity(destination);
        TripEntityDO entity = tripEntityMapper.selectByTripIdTypeAndName(tripId, ENTITY_TYPE_HOTEL, hotel.name());
        if (entity == null) {
            entity = new TripEntityDO();
            entity.setTripId(tripId);
            entity.setEntityType(ENTITY_TYPE_HOTEL);
            entity.setName(hotel.name());
            entity.setProvider("mock_hotel");
            entity.setExternalId(hotel.externalId());
            entity.setParentEntityId(cityEntityId);
            entity.setMetadataJson(JsonUtils.toJsonString(Map.of("imageUrl", hotel.imageUrl(), "placeholder", true)));
            entity.setStatus(ENTITY_STATUS_RESOLVED);
            tripEntityMapper.insert(entity);
        }
        toolPlans.add(toolPlan("HOTEL_CANDIDATE", "EXECUTED", entity.getId(),
                "占位酒店候选；不代表价格、库存或可预订性"));
    }

    private void queryScenicSpotsIfNeeded(Long tripId, Map<String, Object> state, String destination,
                                           TripEntityDO destinationEntity, List<Map<String, Object>> toolPlans) {
        if (tripFactMapper.selectActiveByTripIdAndFactKey(tripId, "poi.candidates") != null) {
            toolPlans.add(toolPlan("SCENIC_SPOT_QUERY", "CACHED", destinationEntity.getId(), "景点候选事实未过期"));
            return;
        }
        try {
            String keyword = firstInterest(state);
            List<AiTravelScenicSpotRespDTO> spots = aiTravelQueryApi.queryScenicSpots(destination, keyword, 5);
            LocalDateTime now = LocalDateTime.now();
            TripSourceDO source = insertSource(tripId, spots.isEmpty() ? "provider" : spots.get(0).getProvider(),
                    "景点查询（" + destination + "）", scenicSourceUrl(spots), now, now.plusDays(30));
            List<String> entityIds = new ArrayList<>();
            for (AiTravelScenicSpotRespDTO spot : spots) {
                TripEntityDO entity = upsertScenicEntity(tripId, destinationEntity.getId(), spot);
                entityIds.add(String.valueOf(entity.getId()));
            }
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("city", destination);
            value.put("entityIds", entityIds);
            value.put("keyword", keyword);
            insertFact(tripId, destinationEntity.getId(), "poi.candidates", value, source.getId(), now, now.plusDays(30));
            toolPlans.add(toolPlan("SCENIC_SPOT_QUERY", "EXECUTED", destinationEntity.getId(), "已获取 " + spots.size() + " 个景点候选"));
        } catch (RuntimeException e) {
            log.warn("[queryScenicSpotsIfNeeded][tripId({}) destination({}) 景点查询失败]", tripId, destination, e);
            toolPlans.add(toolPlan("SCENIC_SPOT_QUERY", "FAILED", destinationEntity.getId(), "景点服务暂不可用"));
        }
    }

    private void queryCurrentWeatherIfNeeded(Long tripId, Map<String, Object> state, String destination,
                                             TripEntityDO destinationEntity, List<Map<String, Object>> toolPlans) {
        if (!isCurrentWeatherApplicable(state)) {
            toolPlans.add(toolPlan("CURRENT_WEATHER", "SKIPPED", destinationEntity.getId(),
                    "当前天气不用于两天以后的出行日期"));
            return;
        }
        if (tripFactMapper.selectActiveByTripIdAndFactKey(tripId, "weather.current") != null) {
            toolPlans.add(toolPlan("CURRENT_WEATHER", "CACHED", destinationEntity.getId(), "当前天气事实未过期"));
            return;
        }
        try {
            AiTravelWeatherRespDTO weather = aiTravelQueryApi.getCurrentWeather(destination);
            LocalDateTime now = LocalDateTime.now();
            TripSourceDO source = insertSource(tripId, weather.getProvider(), "当前天气（" + weather.getCity() + "）",
                    weatherSourceUrl(weather.getProvider()), now, now.plusHours(6));
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("city", weather.getCity());
            value.put("temperature", weather.getTemperature());
            value.put("condition", weather.getCondition());
            value.put("humidity", weather.getHumidity());
            value.put("windDirection", weather.getWindDirection());
            value.put("windPower", weather.getWindPower());
            value.put("queryTime", weather.getQueryTime());
            insertFact(tripId, destinationEntity.getId(), "weather.current", value, source.getId(), now, now.plusHours(6));
            toolPlans.add(toolPlan("CURRENT_WEATHER", "EXECUTED", destinationEntity.getId(), "已获取当前天气"));
        } catch (RuntimeException e) {
            log.warn("[queryCurrentWeatherIfNeeded][tripId({}) destination({}) 天气查询失败]", tripId, destination, e);
            toolPlans.add(toolPlan("CURRENT_WEATHER", "FAILED", destinationEntity.getId(), "天气服务暂不可用"));
        }
    }

    private TripEntityDO upsertScenicEntity(Long tripId, Long cityEntityId, AiTravelScenicSpotRespDTO spot) {
        TripEntityDO entity = tripEntityMapper.selectByTripIdTypeAndName(tripId, ENTITY_TYPE_POI, spot.getName());
        if (entity == null) {
            entity = new TripEntityDO();
            entity.setTripId(tripId);
            entity.setEntityType(ENTITY_TYPE_POI);
            entity.setName(spot.getName());
            entity.setProvider(spot.getProvider());
            entity.setExternalId(spot.getExternalId());
            entity.setParentEntityId(cityEntityId);
            entity.setLongitude(parseDecimal(spot.getLongitude()));
            entity.setLatitude(parseDecimal(spot.getLatitude()));
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("address", spot.getAddress());
            metadata.put("imageUrl", spot.getImageUrl());
            entity.setMetadataJson(JsonUtils.toJsonString(metadata));
            entity.setStatus(ENTITY_STATUS_RESOLVED);
            tripEntityMapper.insert(entity);
        }
        return entity;
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

    private void insertFact(Long tripId, Long entityId, String key, Map<String, Object> value, Long sourceId,
                            LocalDateTime retrievedAt, LocalDateTime expiresAt) {
        TripFactDO fact = new TripFactDO();
        fact.setTripId(tripId);
        fact.setEntityId(entityId);
        fact.setFactKey(key);
        fact.setValueJson(JsonUtils.toJsonString(value));
        fact.setSourceId(sourceId);
        fact.setRetrievedAt(retrievedAt);
        fact.setExpiresAt(expiresAt);
        fact.setConfidence(1D);
        fact.setStatus(STATUS_VERIFIED);
        tripFactMapper.insert(fact);
    }

    private static boolean isCurrentWeatherApplicable(Map<String, Object> state) {
        String startDate = ObjUtil.toString(state.get("startDate"));
        if (StrUtil.isBlank(startDate)) {
            return false;
        }
        try {
            LocalDate date = LocalDate.parse(startDate);
            return !date.isBefore(LocalDate.now()) && !date.isAfter(LocalDate.now().plusDays(1));
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static String firstInterest(Map<String, Object> state) {
        Object value = state.get("interests");
        if (value instanceof List<?> list && !list.isEmpty()) {
            return StrUtil.trim(ObjUtil.toString(list.get(0)));
        }
        return "景点";
    }

    private static BigDecimal parseDecimal(String value) {
        try {
            return StrUtil.isBlank(value) ? null : new BigDecimal(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Map<String, Object> toolPlan(String type, String status, Long entityId, String detail) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", type);
        result.put("status", status);
        result.put("entityId", entityId == null ? null : String.valueOf(entityId));
        result.put("detail", detail);
        return result;
    }

    private static String scenicSourceUrl(List<AiTravelScenicSpotRespDTO> spots) {
        return !spots.isEmpty() && "gaode".equalsIgnoreCase(spots.get(0).getProvider())
                ? "https://lbs.amap.com/api/webservice/guide/api-advanced/newpoisearch"
                : "https://market.aliyun.com/";
    }

    private static String weatherSourceUrl(String provider) {
        return "gaode".equalsIgnoreCase(provider)
                ? "https://lbs.amap.com/api/webservice/guide/api/weatherinfo"
                : "https://market.aliyun.com/";
    }

    public record ResearchResult(List<Map<String, Object>> toolPlans, int executedToolCount) {
    }

}
