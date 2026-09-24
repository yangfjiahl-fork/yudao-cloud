package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversation.UserItineraryConversationDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDayDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDayItemDO;
import cn.iocoder.yudao.module.gift.dal.mysql.useritinerary.UserItineraryDayMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.useritinerary.UserItineraryDayItemMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.useritinerary.UserItineraryMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 将当前行程 JSON 覆盖写入可查询、可编辑的用户行程结构。 */
@Service
public class ItineraryStructuredPersistenceService {

    private static final int RESOLVE_STATUS_PENDING = 0;
    private static final int RESOLVE_STATUS_COMPLETED = 2;
    private static final String COORDINATE_SYSTEM_GCJ02 = "GCJ02";

    @Resource
    private UserItineraryMapper userItineraryMapper;
    @Resource
    private UserItineraryDayMapper userItineraryDayMapper;
    @Resource
    private UserItineraryDayItemMapper userItineraryDayItemMapper;

    @Transactional(rollbackFor = Exception.class)
    public Long persist(UserItineraryConversationDO conversation, Long requestEventId, Long resultEventId, Long memberId,
                        Map<String, Object> state, Map<String, Object> itinerary) {
        UserItineraryDO existing = userItineraryMapper.selectByConversationId(conversation.getId());
        if (existing != null && resultEventId.equals(existing.getResultEventId())) {
            return existing.getId();
        }
        UserItineraryDO userItinerary = buildUserItinerary(conversation, requestEventId, resultEventId, memberId,
                state, itinerary);
        if (existing == null) {
            userItineraryMapper.insert(userItinerary);
        } else {
            userItinerary.setId(existing.getId());
            userItineraryDayItemMapper.deletePhysicallyByUserItineraryId(existing.getId());
            userItineraryDayMapper.deletePhysicallyByUserItineraryId(existing.getId());
            updateUserItinerary(userItinerary);
        }
        persistDays(userItinerary.getId(), itinerary);
        return userItinerary.getId();
    }

    private static UserItineraryDO buildUserItinerary(UserItineraryConversationDO conversation, Long requestEventId,
                                                        Long resultEventId, Long memberId, Map<String, Object> state,
                                                        Map<String, Object> itinerary) {
        UserItineraryDO result = new UserItineraryDO();
        result.setMemberId(memberId);
        result.setConversationId(conversation.getId());
        result.setRequestEventId(requestEventId);
        result.setResultEventId(resultEventId);
        result.setStatus(1);
        result.setTitle(StrUtil.blankToDefault(text(itinerary.get("summary")), "旅行方案"));
        result.setCoverUrl(findCoverUrl(itinerary));
        result.setCoverWidth(0);
        result.setCoverHeight(0);
        LocalDate startDate = date(state.get("startDate"));
        Integer dayCnt = integer(state.get("days"));
        result.setStartDate(startDate);
        result.setEndDate(endDate(state, startDate, dayCnt));
        result.setDayCnt(dayCnt == null ? dayCount(itinerary) : dayCnt);
        result.setDeparture(nullableText(state.get("departure")));
        result.setDestination(nullableText(state.get("destination")));
        result.setTravelerCount(integer(state.get("travelerCount")));
        result.setBudget(integer(state.get("budget")));
        result.setHotelBudget(integer(state.get("hotelBudget")));
        result.setTravelerProfileJson(json(state.get("travelerProfile")));
        result.setInterestsJson(json(state.get("interests")));
        result.setPace(nullableText(state.get("pace")));
        result.setMustVisitJson(json(state.get("mustVisit")));
        result.setConstraintsJson(json(state.get("constraints")));
        result.setDailyStartTime(time(state.get("dailyStartTime")));
        result.setDailyEndTime(time(state.get("dailyEndTime")));
        Map<String, Object> overview = map(itinerary.get("overview"));
        result.setOverviewStatus(nullableText(overview.get("status")));
        result.setOverviewSkeleton(nullableText(overview.get("skeleton")));
        result.setOverviewDetail(nullableText(overview.get("detail")));
        Map<String, Object> planner = map(itinerary.get("planner"));
        result.setPlannerType(nullableText(planner.get("type")));
        result.setPlannerValidation(nullableText(planner.get("validation")));
        return result;
    }

    private void updateUserItinerary(UserItineraryDO itinerary) {
        userItineraryMapper.update(null, new UpdateWrapper<UserItineraryDO>()
                .eq("id", itinerary.getId())
                .set("member_id", itinerary.getMemberId())
                .set("conversation_id", itinerary.getConversationId())
                .set("request_event_id", itinerary.getRequestEventId())
                .set("result_event_id", itinerary.getResultEventId())
                .set("status", itinerary.getStatus())
                .set("title", itinerary.getTitle())
                .set("cover_url", itinerary.getCoverUrl())
                .set("cover_width", itinerary.getCoverWidth())
                .set("cover_height", itinerary.getCoverHeight())
                .set("start_date", itinerary.getStartDate())
                .set("end_date", itinerary.getEndDate())
                .set("day_cnt", itinerary.getDayCnt())
                .set("departure", itinerary.getDeparture())
                .set("destination", itinerary.getDestination())
                .set("traveler_count", itinerary.getTravelerCount())
                .set("budget", itinerary.getBudget())
                .set("hotel_budget", itinerary.getHotelBudget())
                .set("traveler_profile_json", itinerary.getTravelerProfileJson())
                .set("interests_json", itinerary.getInterestsJson())
                .set("pace", itinerary.getPace())
                .set("must_visit_json", itinerary.getMustVisitJson())
                .set("constraints_json", itinerary.getConstraintsJson())
                .set("daily_start_time", itinerary.getDailyStartTime())
                .set("daily_end_time", itinerary.getDailyEndTime())
                .set("overview_status", itinerary.getOverviewStatus())
                .set("overview_skeleton", itinerary.getOverviewSkeleton())
                .set("overview_detail", itinerary.getOverviewDetail())
                .set("planner_type", itinerary.getPlannerType())
                .set("planner_validation", itinerary.getPlannerValidation()));
    }

    private void persistDays(Long userItineraryId, Map<String, Object> itinerary) {
        List<?> days = list(itinerary.get("daily_itinerary"));
        for (int index = 0; index < days.size(); index++) {
            Map<String, Object> day = map(days.get(index));
            if (day.isEmpty()) {
                continue;
            }
            UserItineraryDayDO dayDO = buildDay(userItineraryId, day, index);
            userItineraryDayMapper.insert(dayDO);
            persistItems(userItineraryId, dayDO, day);
        }
    }

    private static UserItineraryDayDO buildDay(Long userItineraryId, Map<String, Object> day, int index) {
        UserItineraryDayDO result = new UserItineraryDayDO();
        result.setTenantId(TenantContextHolder.getRequiredTenantId());
        result.setUserItineraryId(userItineraryId);
        result.setDay(integer(day.get("day")));
        result.setDate(date(day.get("date")));
        result.setProvinceId(integer(day.get("provinceId")));
        result.setCityId(integer(day.get("cityId")));
        result.setDistrictId(integer(day.get("districtId")));
        result.setCity(nullableText(day.get("city")));
        result.setArea(nullableText(day.get("area")));
        result.setTheme(nullableText(day.get("theme")));
        result.setAnchorPoiNamesJson(json(day.get("anchorPoiNames")));
        result.setSort(integer(day.get("sort"), index));
        Map<String, Object> overview = map(day.get("overview"));
        result.setOverviewStatus(nullableText(overview.get("status")));
        result.setOverviewSkeleton(nullableText(overview.get("skeleton")));
        result.setOverviewDetail(nullableText(overview.get("detail")));
        Map<String, Object> planning = map(day.get("planning"));
        result.setPlanner(nullableText(planning.get("solver")));
        result.setPlanningStatus(nullableText(planning.get("status")));
        result.setMacroSource(nullableText(planning.get("macroSource")));
        result.setSelectionStatus(nullableText(planning.get("selectionStatus")));
        result.setBudgetStatus(nullableText(planning.get("budgetStatus")));
        result.setRequestedScenicCount(integer(planning.get("requestedScenicCount")));
        result.setSelectedScenicCount(integer(planning.get("selectedScenicCount")));
        result.setDayStartTime(time(planning.get("dayStartTime")));
        result.setDayEndTime(time(planning.get("dayEndTime")));
        result.setDroppedNodeIdsJson(json(planning.get("droppedNodeIds")));
        result.setCandidateCountsJson(json(planning.get("candidateCounts")));
        return result;
    }

    private void persistItems(Long userItineraryId, UserItineraryDayDO dayDO, Map<String, Object> day) {
        List<?> slots = list(day.get("slots"));
        List<UserItineraryDayItemDO> items = new ArrayList<>();
        for (int index = 0; index < slots.size(); index++) {
            Map<String, Object> slot = map(slots.get(index));
            if (!slot.isEmpty()) {
                items.add(buildItem(userItineraryId, dayDO.getId(), dayDO.getDay(), slot, index));
            }
        }
        if (!items.isEmpty()) {
            userItineraryDayItemMapper.insertBatch(items);
        }
    }

    private static UserItineraryDayItemDO buildItem(Long userItineraryId, Long userItineraryDayId, Integer day,
                                                   Map<String, Object> slot, int index) {
        Map<String, Object> snapshot = map(slot.get("poiSnapshot"));
        UserItineraryDayItemDO result = new UserItineraryDayItemDO();
        result.setTenantId(TenantContextHolder.getRequiredTenantId());
        result.setUserItineraryId(userItineraryId);
        result.setUserItineraryDayId(userItineraryDayId);
        result.setItemId(nullableText(slot.get("itemId")));
        result.setDay(day);
        result.setType(nullableText(slot.get("type")));
        result.setSlot(nullableText(slot.get("slot")));
        result.setLabel(nullableText(slot.get("label")));
        result.setSort(integer(slot.get("sort"), index));
        result.setStartTime(time(first(slot, "startTime", "plannedStartTime")));
        result.setEndTime(time(first(slot, "endTime", "plannedEndTime")));
        result.setDurationMinutes(integer(slot.get("durationMinutes")));
        result.setPoiId(nullableText(first(slot, snapshot, "poiId")));
        result.setPoiName(nullableText(first(slot, snapshot, "poiName")));
        result.setProvinceId(integer(slot.get("provinceId")));
        result.setCityId(integer(slot.get("cityId")));
        result.setDistrictId(integer(slot.get("districtId")));
        result.setCity(nullableText(slot.get("city")));
        result.setArea(nullableText(slot.get("area")));
        result.setAddressDetail(nullableText(first(slot, snapshot, "addressDetail", "address")));
        result.setLongitude(decimal(first(slot, snapshot, "longitude")));
        result.setLatitude(decimal(first(slot, snapshot, "latitude")));
        if (result.getLongitude() != null && result.getLatitude() != null) {
            result.setCoordinateSystem(COORDINATE_SYSTEM_GCJ02);
        }
        result.setBusinessHours(nullableText(first(slot, snapshot, "businessHours")));
        result.setPhoneNo(nullableText(first(slot, snapshot, "phoneNo", "telephone")));
        result.setCoverUrl(nullableText(first(slot, snapshot, "coverUrl", "imageUrl")));
        result.setRating(decimal(first(slot, snapshot, "rating")));
        result.setCost(decimal(first(slot, snapshot, "cost")));
        result.setTagsJson(json(first(slot, snapshot, "tags", "tag")));
        result.setSkeleton(nullableText(slot.get("skeleton")));
        result.setDetail(nullableText(slot.get("detail")));
        result.setStatus(StrUtil.blankToDefault(text(slot.get("status")), "PENDING"));
        result.setResolveStatus("RESOLVED".equalsIgnoreCase(result.getStatus())
                ? RESOLVE_STATUS_COMPLETED : RESOLVE_STATUS_PENDING);
        result.setPlanningStatus(nullableText(slot.get("planningStatus")));
        result.setPoiVerificationStatus(nullableText(slot.get("poiVerificationStatus")));
        result.setMustVisit(bool(slot.get("mustVisit"), false));
        result.setLocked(bool(slot.get("locked"), false));
        result.setSource(nullableText(slot.get("source")));
        result.setProvider(nullableText(first(slot, snapshot, "provider")));
        result.setPoiSnapshotJson(snapshot.isEmpty() ? null : JsonUtils.toJsonString(snapshot));
        result.setCandidatesJson(json(slot.get("candidates")));
        result.setCitationIdsJson(json(slot.get("citationIds")));
        return result;
    }

    private static String findCoverUrl(Map<String, Object> itinerary) {
        for (Object dayValue : list(itinerary.get("daily_itinerary"))) {
            for (Object slotValue : list(map(dayValue).get("slots"))) {
                Map<String, Object> slot = map(slotValue);
                String coverUrl = nullableText(first(slot, map(slot.get("poiSnapshot")), "coverUrl", "imageUrl"));
                if (coverUrl != null) {
                    return coverUrl;
                }
            }
        }
        return "";
    }

    private static LocalDate endDate(Map<String, Object> state, LocalDate startDate, Integer days) {
        LocalDate result = date(state.get("endDate"));
        return result != null || startDate == null || days == null || days <= 0
                ? result : startDate.plusDays(days - 1L);
    }

    private static int dayCount(Map<String, Object> itinerary) {
        return list(itinerary.get("daily_itinerary")).size();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> raw ? new LinkedHashMap<>((Map<String, Object>) raw) : new LinkedHashMap<>();
    }

    private static List<?> list(Object value) {
        return value instanceof List<?> result ? result : List.of();
    }

    private static Object first(Map<String, Object> source, String... keys) {
        return first(source, Map.of(), keys);
    }

    private static Object first(Map<String, Object> source, Map<String, Object> fallback, String... keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (value != null && StrUtil.isNotBlank(text(value))) {
                return value;
            }
            value = fallback.get(key);
            if (value != null && StrUtil.isNotBlank(text(value))) {
                return value;
            }
        }
        return null;
    }

    private static String json(Object value) {
        return value == null ? null : JsonUtils.toJsonString(value);
    }

    private static String nullableText(Object value) {
        String result = text(value);
        return StrUtil.isBlank(result) || "null".equalsIgnoreCase(result) ? null : result;
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static Integer integer(Object value) {
        return integer(value, null);
    }

    private static Integer integer(Object value, Integer fallback) {
        try {
            if (value == null || StrUtil.isBlank(text(value))) {
                return fallback;
            }
            return new BigDecimal(text(value)).intValue();
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static BigDecimal decimal(Object value) {
        try {
            return value == null || StrUtil.isBlank(text(value)) ? null : new BigDecimal(text(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Boolean bool(Object value, boolean fallback) {
        return value == null ? fallback : Boolean.parseBoolean(text(value));
    }

    private static LocalDate date(Object value) {
        try {
            return StrUtil.isBlank(text(value)) ? null : LocalDate.parse(text(value));
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static LocalTime time(Object value) {
        try {
            return StrUtil.isBlank(text(value)) ? null : LocalTime.parse(text(value));
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

}
