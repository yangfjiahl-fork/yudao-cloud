package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tracer.core.annotation.BizTrace;
import cn.iocoder.yudao.framework.tracer.core.util.TracerFrameworkUtils;
import cn.iocoder.yudao.module.ai.api.chat.AiChatApi;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateRespDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatGenerateStreamRespDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageCreateAssistantReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageRespDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationRespDTO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripItineraryDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripItinerarySlotDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripPlanDO;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripItineraryMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripItinerarySlotMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripPlanMapper;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentExecutionTerminatedException;
import cn.iocoder.yudao.module.gift.framework.trip.managed.ManagedAgentTerminationReason;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentResult;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentEvent;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripChangeCommand;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripItineraryRouteResult;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripItinerarySlotResult;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.system.api.area.AreaApi;
import com.alibaba.loongsuite.otel.util.genai.AgentInvocation;
import com.alibaba.loongsuite.otel.util.genai.GenAiTelemetryHandler;
import com.alibaba.loongsuite.otel.util.genai.types.InputMessage;
import com.alibaba.loongsuite.otel.util.genai.types.OutputMessage;
import com.alibaba.loongsuite.otel.util.genai.types.TextPart;
import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 旅行规划的显式编排器。LLM 负责需求交互和总览表达；状态、骨架、事实及引用均由服务端控制。
 */
@Service
@Slf4j
public class TripAgentServiceImpl implements TripAgentService {

    private static final int STATUS_ACTIVE = 1;
    private static final String INTAKE_ROLE_ID_CONFIG_KEY = "trip.agent.intakeRoleId";
    private static final String SUMMARY_ROLE_ID_CONFIG_KEY = "trip.agent.sumaryRoleId";
    private static final String QUESTION_COUNT_CONFIG_KEY = "trip.question.cnt";
    private static final int MAX_SUGGESTION_COUNT = 5;
    private static final String STATE_ITINERARY_OVERRIDES = "itineraryOverrides";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    private static final int SLOT_RESOLVE_STATUS_PENDING = 0;
    private static final int SLOT_RESOLVE_STATUS_PROCESSING = 1;
    private static final int SLOT_RESOLVE_STATUS_COMPLETED = 2;
    private static final int SLOT_RESOLVE_STATUS_FAILED = 3;
    private static final int TRIP_OVERVIEW_MIN_LENGTH = 40;
    private static final int TRIP_OVERVIEW_MAX_LENGTH = 60;
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("\\d[\\d,]*");
    private static final Set<String> DAILY_ITINERARY_SLOTS = Set.of("MORNING", "LUNCH", "AFTERNOON", "DINNER",
            "ACCOMMODATION");
    private static final Set<String> ITINERARY_SLOTS = Set.of("MORNING", "LUNCH", "AFTERNOON", "DINNER",
            "EVENING", "ACCOMMODATION", "ARRIVAL", "DEPARTURE", "TRIP_OVERVIEW", "DAY_OVERVIEW");
    @Resource
    private AiChatApi aiChatApi;
    @Resource
    private GenAiTelemetryHandler genAiTelemetryHandler;
    @Resource
    private TripPlanMapper tripPlanMapper;
    @Resource
    private TripItineraryMapper tripItineraryMapper;
    @Resource
    private TripItinerarySlotMapper tripItinerarySlotMapper;
    @Resource
    private TripRunLogService tripRunLogService;
    @Resource
    private ConfigApi configApi;
    @Resource
    private AreaApi areaApi;
    @Resource
    private TripResearchExecutor tripResearchExecutor;
    @Resource
    private TripTravelQueryService tripTravelQueryService;
    @Resource
    private TripItineraryAssembler tripItineraryAssembler;
    @Resource
    private ManagedTripPlannerService managedTripPlannerService;
    @Resource
    private ManagedTripAgentExecutor managedTripAgentExecutor;
    @Resource
    private TripItineraryVersionService tripItineraryVersionService;
    @Resource
    private TripPlanEditorService tripPlanEditorService;
    @Resource
    private TripTopicGuard tripTopicGuard;
    @Resource
    private Tracer tracer;

    @Override
    public void createTrip(Long conversationId, Long memberId) {
        createTrip(conversationId, memberId, null, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createTrip(Long conversationId, Long memberId, Long provinceId, Long cityId, Long districtId) {
        TripPlanDO existing = tripPlanMapper.selectByConversationIdAndMemberId(conversationId, memberId);
        if (existing != null) {
            return trimNullable(TripAgentFormatUtils.parseMap(existing.getStateJson()).get("departure"));
        }
        TripPlanDO trip = new TripPlanDO();
        trip.setConversationId(conversationId);
        trip.setMemberId(memberId);
        Map<String, Object> state = new LinkedHashMap<>();
        String defaultDeparture = getDefaultDeparture(provinceId, cityId, districtId);
        if (StrUtil.isNotBlank(defaultDeparture)) {
            state.put("departure", defaultDeparture);
        }
        trip.setStateJson(JsonUtils.toJsonString(state));
        trip.setMissingRequiredJson(JsonUtils.toJsonString(validateState(state)));
        trip.setStatus(STATUS_ACTIVE);
        tripPlanMapper.insert(trip);
        log.info("[createTrip][tripId({}) conversationId({}) memberId({}) 初始化成功]",
                trip.getId(), conversationId, memberId);
        return defaultDeparture;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = {"#conversationId"}, expire = 360000, acquireTimeout = 3000)
    @BizTrace(operationName = "trip.agent.handle-managed-message", type = "'ai.chat.conversation'", id = "#conversationId")
    public TripAgentResult handleManagedMessage(Long conversationId, Long memberId, String content,
                                                Consumer<TripAgentEvent> eventConsumer) {
        try {
            return doHandleManagedMessage(conversationId, memberId, content, eventConsumer);
        } catch (ManagedAgentExecutionTerminatedException e) {
            String fallback = buildManagedBudgetFallback(e.getReason());
            AiChatMessageRespDTO assistant = createTranscriptMessage(conversationId, memberId, fallback, true);
            log.warn("[handleManagedMessage][conversationId({}) memberId({}) 托管 Agent 预算熔断 reason({}) snapshot({})]",
                    conversationId, memberId, e.getReason(), e.getMetrics());
            eventConsumer.accept(TripAgentEvent.of("question", "BUDGET_GUARD", fallback)
                    .setMessageId(assistant.getId()).setMissingRequired(List.of()));
            return new TripAgentResult().setType("QUESTION").setMessageId(assistant.getId()).setContent(fallback)
                    .setMissingRequired(List.of());
        }
    }

    private static String buildManagedBudgetFallback(ManagedAgentTerminationReason reason) {
        if (reason == ManagedAgentTerminationReason.MAX_RUN_DURATION
                || reason == ManagedAgentTerminationReason.STREAM_IDLE_TIMEOUT) {
            return "这次旅行请求处理耗时较长，我已停止本次执行。你可以减少城市数量或缩短天数后再试，"
                    + "我会基于已经填写的信息继续规划。";
        }
        return "这次旅行请求处理涉及的步骤较多，我已停止本次执行。你可以减少城市数量或缩短天数后再试，"
                + "我会基于已经填写的信息继续规划。";
    }

    private TripAgentResult doHandleManagedMessage(Long conversationId, Long memberId, String content,
                                                   Consumer<TripAgentEvent> eventConsumer) {
        TripPlanDO trip = tripPlanMapper.selectByConversationIdAndMemberId(conversationId, memberId);
        if (trip == null) {
            log.info("[handleMessage][conversationId({}) memberId({}) 缺少旅行状态，开始兼容初始化]",
                    conversationId, memberId);
            createTrip(conversationId, memberId);
            trip = tripPlanMapper.selectByConversationIdAndMemberId(conversationId, memberId);
        }
        log.info("[handleMessage][tripId({}) conversationId({}) memberId({}) 开始编排]",
                trip.getId(), conversationId, memberId);
        createTranscriptMessage(conversationId, memberId, content, false);
        Map<String, Object> promptVariables = buildPromptVariables(conversationId, memberId);

        Map<String, Object> state = TripAgentFormatUtils.parseMap(trip.getStateJson());
        Map<String, Object> previousState = TripAgentFormatUtils.parseMap(JsonUtils.toJsonString(state));
        sanitizeTravelerProfile(state);
        state.remove("destinationEntityId"); // 兼容已存的旧状态，不再持久化外部实体副本
        List<String> currentMissingRequired = validateState(new LinkedHashMap<>(state));
        TripTopicGuard.Decision precheckDecision = tripTopicGuard.precheck(content, state, currentMissingRequired);
        if (!precheckDecision.allowed()) {
            return handleOutOfTopicMessage(conversationId, memberId, trip.getId(), state,
                    currentMissingRequired, precheckDecision, eventConsumer);
        }
        TripItineraryDO currentItinerary = trip.getCurrentItineraryId() == null ? null
                : tripItineraryMapper.selectById(trip.getCurrentItineraryId());
        AtomicInteger modelDeltaSequence = new AtomicInteger();
        eventConsumer.accept(TripAgentEvent.of("stage", "INTAKE", "正在提取本轮出行需求…"));
        String intakeContent = executeManagedIntake(trip, state, currentItinerary, content);
        Map<String, Object> intake = TripAgentFormatUtils.parseMap(intakeContent);
        TripTopicGuard.Decision topicDecision = tripTopicGuard.decide(
                content, state, currentMissingRequired, intake);
        if (!topicDecision.allowed()) {
            return handleOutOfTopicMessage(conversationId, memberId, trip.getId(), state,
                    currentMissingRequired, topicDecision, eventConsumer);
        }
        mergeInformationState(state, extractState(intake), content);
        TripChangeCommand changeCommand = currentItinerary == null ? null
                : extractAgentChangeCommand(intake, currentItinerary.getVersion());
        if (changeCommand == null) {
            applyItineraryPatch(state, intake.get("itinerary_patch"));
        } else {
            // 当前快照已经承载旧 override 的结果；切换到统一命令后不再让历史文本补丁污染后续事实。
            state.remove(STATE_ITINERARY_OVERRIDES);
        }
        List<String> missingRequired = validateState(state);
        boolean stateChanged = !previousState.equals(state);
        boolean generateRequested = changeCommand != null || isGenerateRequested(intake)
                || (trip.getCurrentItineraryId() != null && stateChanged);
        TripOrchestrationAction action = determineAction(missingRequired, generateRequested, stateChanged,
                trip.getCurrentItineraryId() != null);
        trip.setStateJson(JsonUtils.toJsonString(state));
        trip.setMissingRequiredJson(JsonUtils.toJsonString(missingRequired));
        tripPlanMapper.updateById(trip);
        log.info("[handleMessage][tripId({}) action({}) 状态字段({}) 缺失字段({})]",
                trip.getId(), action, state.keySet(), missingRequired);
        int questionCount = getQuestionCount();
        boolean needFollowUp = !action.requiresPlanning();
        if (needFollowUp) {
            eventConsumer.accept(TripAgentEvent.of("stage", "FOLLOW_UP", "正在整理下一步建议…"));
        }
        TripInteraction interaction = needFollowUp ? generateInteraction(conversationId, memberId, state, missingRequired,
                trip.getId(), promptVariables, questionCount, eventConsumer, modelDeltaSequence) : null;
        List<Map<String, Object>> inputCards = needFollowUp ? interaction.inputCards() : List.of();
        eventConsumer.accept(TripAgentEvent.of("intake_completed", "INTAKE", buildIntakeCompletedContent(state, missingRequired))
                .setMissingRequired(missingRequired).setInputCards(inputCards));

        if (CollUtil.isNotEmpty(missingRequired)) {
            String question = interaction.question();
            AiChatMessageRespDTO assistant = createTranscriptMessage(conversationId, memberId, question, true);
            log.info("[handleMessage][tripId({}) 返回追问 messageId({})]", trip.getId(), assistant.getId());
            TripAgentResult result = new TripAgentResult().setType("QUESTION").setMessageId(assistant.getId()).setContent(question)
                    .setMissingRequired(missingRequired);
            eventConsumer.accept(TripAgentEvent.of("question", "INTAKE", question).setMessageId(assistant.getId())
                    .setMissingRequired(missingRequired).setInputCards(inputCards));
            return result;
        }

        if (changeCommand != null) {
            eventConsumer.accept(TripAgentEvent.of("stage", "ASSEMBLE", "正在应用行程修改并校验受影响日期…"));
            TripPlanEditorService.EditResult edited = tripPlanEditorService.applyWithinExistingLock(
                    conversationId, memberId, changeCommand);
            TripItineraryVersionService.SavedItinerary saved = edited.saved();
            log.info("[handleMessage][tripId({}) command({}) affectedDays({}) version({}) Agent 编辑完成]",
                    trip.getId(), changeCommand.operation(), edited.affectedDays(), saved.version());
            TripAgentResult result = new TripAgentResult().setType("ITINERARY_SKELETON")
                    .setMessageId(saved.messageId()).setContent(saved.displayText())
                    .setItinerary(edited.itinerary()).setMissingRequired(List.of());
            eventConsumer.accept(TripAgentEvent.of("itinerary_skeleton", "ASSEMBLE", saved.displayText())
                    .setMessageId(saved.messageId()).setItinerary(edited.itinerary())
                    .setMissingRequired(List.of()));
            return result;
        }

        if (!action.requiresPlanning()) {
            String question = interaction.question();
            AiChatMessageRespDTO assistant = createTranscriptMessage(conversationId, memberId, question, true);
            log.info("[handleMessage][tripId({}) 等待用户确认生成或继续补充 messageId({})]", trip.getId(), assistant.getId());
            TripAgentResult result = new TripAgentResult().setType("QUESTION").setMessageId(assistant.getId()).setContent(question)
                    .setMissingRequired(List.of());
            eventConsumer.accept(TripAgentEvent.of("question", "INTAKE", question).setMessageId(assistant.getId())
                    .setMissingRequired(List.of()).setInputCards(inputCards));
            return result;
        }

        Span assembleSpan = tracer.spanBuilder("trip.agent.assemble")
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute("trip.agent.stage", "ASSEMBLE")
                .setAttribute("trip.id", String.valueOf(trip.getId()))
                .startSpan();
        Map<String, Object> itinerary;
        try (Scope ignored = assembleSpan.makeCurrent()) {
            itinerary = managedTripPlannerService.plan(trip, state, content,
                    progress -> eventConsumer.accept(TripAgentEvent.of("stage", "ASSEMBLE", progress)));
        } catch (RuntimeException | Error e) {
            TracerFrameworkUtils.onError(e, assembleSpan);
            throw e;
        } finally {
            assembleSpan.end();
        }
        TripItineraryVersionService.SavedItinerary saved = tripItineraryVersionService.saveGeneratedItinerary(
                trip, memberId, state, itinerary);

        log.info("[handleMessage][tripId({}) 行程 itineraryId({}) version({}) messageId({}) 引用数量({}) 已生成]",
                trip.getId(), saved.itineraryId(), saved.version(), saved.messageId(),
                ((List<?>) itinerary.get("citation_ids")).size());
        TripAgentResult result = new TripAgentResult().setType("ITINERARY_SKELETON").setMessageId(saved.messageId())
                .setContent(saved.displayText())
                .setItinerary(itinerary).setMissingRequired(List.of());
        eventConsumer.accept(TripAgentEvent.of("itinerary_skeleton", "ASSEMBLE", saved.displayText())
                .setMessageId(saved.messageId())
                .setItinerary(itinerary).setMissingRequired(List.of()));
        return result;
    }

    private TripAgentResult handleOutOfTopicMessage(Long conversationId, Long memberId, Long tripId,
                                                     Map<String, Object> state, List<String> missingRequired,
                                                     TripTopicGuard.Decision decision,
                                                     Consumer<TripAgentEvent> eventConsumer) {
        String reply = decision.reply();
        List<Map<String, String>> suggestions = buildInformationSuggestions(state, missingRequired);
        List<Map<String, Object>> inputCards = TripInputCardFactory.build(missingRequired, reply, suggestions);
        AiChatMessageRespDTO assistant = createTranscriptMessage(conversationId, memberId, reply, true);
        log.info("[handleOutOfTopicMessage][tripId({}) action({}) reason({}) messageId({})]",
                tripId, decision.action(), decision.reason(), assistant.getId());
        eventConsumer.accept(TripAgentEvent.of("question", "TOPIC_GUARD", reply)
                .setMessageId(assistant.getId()).setMissingRequired(missingRequired).setInputCards(inputCards));
        return new TripAgentResult().setType("QUESTION").setMessageId(assistant.getId()).setContent(reply)
                .setMissingRequired(missingRequired);
    }

    static TripOrchestrationAction determineAction(List<String> missingRequired, boolean generateRequested,
                                                    boolean stateChanged, boolean hasItinerary) {
        if (CollUtil.isNotEmpty(missingRequired)) {
            return TripOrchestrationAction.INTAKE;
        }
        if (generateRequested) {
            return hasItinerary ? TripOrchestrationAction.EDIT_PLAN : TripOrchestrationAction.GENERATE_PLAN;
        }
        return stateChanged ? TripOrchestrationAction.UPDATE_STATE : TripOrchestrationAction.CHAT;
    }

    enum TripOrchestrationAction {
        INTAKE,
        UPDATE_STATE,
        GENERATE_PLAN,
        EDIT_PLAN,
        CHAT;

        boolean requiresPlanning() {
            return this == GENERATE_PLAN || this == EDIT_PLAN;
        }
    }

    @Override
    public Map<Long, Map<String, Object>> getItineraryMapByMessageIds(Collection<Long> messageIds) {
        if (CollUtil.isEmpty(messageIds)) {
            return Map.of();
        }
        List<TripItineraryDO> itineraries = tripItineraryMapper.selectListByMessageIds(messageIds);
        Map<Long, Map<String, Object>> itineraryMap = new LinkedHashMap<>();
        Map<Long, Map<String, Object>> itineraryMapById = new LinkedHashMap<>();
        itineraries.forEach(itinerary -> {
            Map<String, Object> content = TripAgentFormatUtils.parseMap(itinerary.getContentJson());
            itineraryMap.put(itinerary.getMessageId(), content);
            itineraryMapById.put(itinerary.getId(), content);
        });
        tripItinerarySlotMapper.selectListByItineraryIds(itineraryMapById.keySet()).forEach(slot -> {
            if (!ObjUtil.equal(slot.getResolveStatus(), SLOT_RESOLVE_STATUS_COMPLETED)) {
                return;
            }
            Map<String, Object> itinerary = itineraryMapById.get(slot.getItineraryId());
            if (itinerary != null) {
                mergeResolvedSlot(itinerary, slot);
            }
        });
        itineraryMapById.values().forEach(itinerary -> itinerary.put("citation_ids", collectCitationIds(itinerary)));
        return itineraryMap;
    }

    @Override
    public TripItineraryRouteResult resolveItineraryRoute(Long conversationId, Long memberId, Long messageId, Integer day) {
        TripPlanDO trip = tripPlanMapper.selectByConversationIdAndMemberId(conversationId, memberId);
        TripItineraryDO itinerary = tripItineraryMapper.selectByMessageId(messageId);
        if (trip == null || itinerary == null || !trip.getId().equals(itinerary.getTripId())) {
            throw new IllegalArgumentException("行程路线不存在或不属于当前会话");
        }
        if (!itinerary.getId().equals(trip.getCurrentItineraryId())) {
            throw new IllegalArgumentException("只能解析当前生效的行程路线");
        }
        List<Map<String, Object>> segments = tripItineraryAssembler.resolveTransportSegments(
                TripAgentFormatUtils.parseMap(itinerary.getContentJson()), day);
        String status = segments.isEmpty() ? "PENDING" : segments.stream()
                .allMatch(segment -> "VERIFIED".equals(segment.get("status"))) ? "VERIFIED" : "ESTIMATED";
        return new TripItineraryRouteResult().setMessageId(messageId).setDay(day).setStatus(status)
                .setTransportSegments(segments);
    }

    @Override
    public TripItinerarySlotResult resolveItinerarySlot(Long conversationId, Long memberId, Long messageId,
                                                         Integer day, String slot) {
        TripPlanDO trip = tripPlanMapper.selectByConversationIdAndMemberId(conversationId, memberId);
        TripItineraryDO itineraryDO = tripItineraryMapper.selectByMessageId(messageId);
        if (trip == null || itineraryDO == null || !trip.getId().equals(itineraryDO.getTripId())) {
            throw new IllegalArgumentException("行程骨架不存在或不属于当前会话");
        }
        if (!itineraryDO.getId().equals(trip.getCurrentItineraryId())) {
            throw new IllegalArgumentException("只能补充当前生效的行程骨架");
        }
        Map<String, Object> state = TripAgentFormatUtils.parseMap(trip.getStateJson());
        if (StrUtil.isBlank(trimNullable(state.get("destination")))) {
            throw new IllegalStateException("行程缺少目的地");
        }
        Map<String, Object> itinerary = TripAgentFormatUtils.parseMap(itineraryDO.getContentJson());
        Map<String, Object> skeletonSlot = findSlot(itinerary, day, slot);
        TripItinerarySlotDO itinerarySlot = getOrCreateItinerarySlot(itineraryDO, day, slot, skeletonSlot);
        if (!tripItinerarySlotMapper.claimForResolve(itinerarySlot.getId(), SLOT_RESOLVE_STATUS_PENDING,
                SLOT_RESOLVE_STATUS_FAILED, SLOT_RESOLVE_STATUS_PROCESSING)) {
            TripItinerarySlotDO currentSlot = tripItinerarySlotMapper.selectById(itinerarySlot.getId());
            TripItinerarySlotResult result = toSlotResult(messageId, currentSlot != null ? currentSlot : itinerarySlot);
            if (!isOverviewSlot(slot)) {
                result = withWeather(result, resolveSlotCity(state, skeletonSlot));
            }
            return withDayTransport(result, itinerary, day);
        }
        if (isOverviewSlot(slot)) {
            return withDayTransport(resolveItineraryOverviewSlot(conversationId, memberId, messageId, trip, itineraryDO,
                    itinerary, itinerarySlot, day, slot), itinerary, day);
        }
        long start = System.currentTimeMillis();
        Long runId = tripRunLogService.create(trip.getId(), "SLOT_RESOLVE", JsonUtils.toJsonString(Map.of(
                "messageId", String.valueOf(messageId), "day", day, "slot", slot)));
        try {
            TripResearchExecutor.SlotResearchResult researchResult = tripResearchExecutor.resolveSlot(trip.getId(), state, day, slot,
                    trimNullable(skeletonSlot.get("skeleton")), trimNullable(skeletonSlot.get("poiName")),
                    trimNullable(skeletonSlot.get("area")));
            String city = resolveSlotCity(state, skeletonSlot);
            List<Map<String, Object>> candidates = bindCandidatesToSlot(itinerarySlot, researchResult.candidates());
            itinerarySlot.setStatus(researchResult.status());
            itinerarySlot.setDetail(researchResult.detail());
            itinerarySlot.setCandidatesJson(JsonUtils.toJsonString(candidates));
            itinerarySlot.setCitationIdsJson(JsonUtils.toJsonString(researchResult.citationIds()));
            itinerarySlot.setResolveStatus(SLOT_RESOLVE_STATUS_COMPLETED);
            tripItinerarySlotMapper.updateById(itinerarySlot);
            tripRunLogService.complete(runId, null, 0L, 0L, 0L, System.currentTimeMillis() - start,
                    JsonUtils.toJsonString(Map.of("toolPlans", researchResult.toolPlans(), "status", researchResult.status(),
                            "candidateCount", candidates.size(), "citationIds", researchResult.citationIds())));
            return withDayTransport(withWeather(toSlotResult(messageId, itinerarySlot), city), itinerary, day);
        } catch (RuntimeException e) {
            itinerarySlot.setStatus("PENDING");
            itinerarySlot.setResolveStatus(SLOT_RESOLVE_STATUS_FAILED);
            itinerarySlot.setDetail("节点补充失败，请重试");
            tripItinerarySlotMapper.updateById(itinerarySlot);
            tripRunLogService.fail(runId, System.currentTimeMillis() - start, e.getMessage());
            throw e;
        }
    }

    private TripItinerarySlotResult resolveItineraryOverviewSlot(Long conversationId, Long memberId, Long messageId,
                                                                   TripPlanDO trip, TripItineraryDO itineraryDO,
                                                                   Map<String, Object> itinerary,
                                                                   TripItinerarySlotDO itinerarySlot,
                                                                   Integer day, String slot) {
        try {
            AiChatGenerateRespDTO response = generateStream(conversationId, memberId,
                    buildOverviewContext(TripAgentFormatUtils.parseMap(trip.getStateJson()), itinerary, day, slot),
                    "OVERVIEW", trip.getId(), buildPromptVariables(conversationId, memberId));
            String detail = trimNullable(TripAgentFormatUtils.parseMap(response.getContent()).get("overview"));
            detail = shortenTripOverview(detail, slot);
            if (StrUtil.isBlank(detail)) {
                throw new IllegalStateException("行程总览生成结果为空");
            }
            itinerarySlot.setStatus("RESOLVED");
            itinerarySlot.setDetail(detail);
            itinerarySlot.setCandidatesJson(JsonUtils.toJsonString(List.of()));
            itinerarySlot.setCitationIdsJson(JsonUtils.toJsonString(List.of()));
            itinerarySlot.setResolveStatus(SLOT_RESOLVE_STATUS_COMPLETED);
            tripItinerarySlotMapper.updateById(itinerarySlot);
            return toSlotResult(messageId, itinerarySlot);
        } catch (RuntimeException e) {
            itinerarySlot.setStatus("PENDING");
            itinerarySlot.setResolveStatus(SLOT_RESOLVE_STATUS_FAILED);
            itinerarySlot.setDetail("总览生成失败，请重试");
            tripItinerarySlotMapper.updateById(itinerarySlot);
            throw e;
        }
    }

    static String shortenTripOverview(String detail, String slot) {
        if (!StrUtil.equalsIgnoreCase(slot, "TRIP_OVERVIEW") || detail.length() <= TRIP_OVERVIEW_MAX_LENGTH) {
            return detail;
        }
        String prefix = detail.substring(0, TRIP_OVERVIEW_MAX_LENGTH);
        for (int index = prefix.length() - 1; index >= TRIP_OVERVIEW_MIN_LENGTH - 1; index--) {
            if ("。！？；".indexOf(prefix.charAt(index)) >= 0) {
                return prefix.substring(0, index + 1);
            }
        }
        return prefix.substring(0, TRIP_OVERVIEW_MAX_LENGTH - 1).stripTrailing() + "…";
    }

    /**
     * 节点补全时一并返回当天全部相邻 POI 的交通段，前端无需再为每个 POI 单独测距。
     * 单段高德查询失败时，组装器会自动回退为本地估算，不影响节点补全结果。
     */
    private TripItinerarySlotResult withDayTransport(TripItinerarySlotResult result, Map<String, Object> itinerary, Integer day) {
        if (day == null || day <= 0) {
            return result.setTransportSegments(List.of());
        }
        try {
            return result.setTransportSegments(tripItineraryAssembler.resolveTransportSegments(itinerary, day));
        } catch (RuntimeException e) {
            log.warn("[withDayTransport][day({}) 批量测距失败，不影响节点补全]", day, e);
            return result.setTransportSegments(List.of());
        }
    }

    private AiChatGenerateRespDTO generateStream(Long conversationId, Long memberId, String content,
                                                 String stage, Long tripId) {
        return generateStream(conversationId, memberId, content, stage, tripId, Map.of(), ignored -> { });
    }

    private AiChatGenerateRespDTO generateStream(Long conversationId, Long memberId, String content,
                                                 String stage, Long tripId,
                                                 Consumer<String> contentConsumer) {
        return generateStream(conversationId, memberId, content, stage, tripId, Map.of(), contentConsumer);
    }

    private AiChatGenerateRespDTO generateStream(Long conversationId, Long memberId, String content,
                                                 String stage, Long tripId, Map<String, Object> promptVariables) {
        return generateStream(conversationId, memberId, content, stage, tripId, promptVariables, ignored -> { });
    }

    private AiChatGenerateRespDTO generateStream(Long conversationId, Long memberId, String content,
                                                 String stage, Long tripId, Map<String, Object> promptVariables,
                                                 Consumer<String> contentConsumer) {
        long start = System.currentTimeMillis();
        Long roleId = getRoleId(stage);
        Long runId = tripRunLogService.create(tripId, stage, JsonUtils.toJsonString(Map.of("content", content)));
        log.info("[generateStream][tripId({}) runId({}) stage({}) 开始调用模型]", tripId, runId, stage);
        StringBuilder output = new StringBuilder();
        AgentInvocation invocation = startAgentInvocation(conversationId, tripId, stage, content, roleId);
        try (invocation) {
            AiChatGenerateRespDTO response = aiChatApi.generateStream(new AiChatGenerateReqDTO()
                    .setConversationId(conversationId).setUserId(memberId).setUserType(UserTypeEnum.MEMBER.getValue())
                    .setRoleId(roleId).setContent(content).setPromptVariables(promptVariables), chunk -> {
                        output.append(chunk.getContent());
                        contentConsumer.accept(chunk.getContent());
                    });
            setModelResponseAttributes(invocation, response, output.toString());
            tripRunLogService.complete(runId, response.getModel(), response.getPromptTokens(), response.getCompletionTokens(),
                    response.getTotalTokens(), System.currentTimeMillis() - start,
                    JsonUtils.toJsonString(Map.of("response", output.toString())));
            log.info("[generateStream][tripId({}) runId({}) stage({}) model({}) 耗时({} ms) tokens({}) 调用成功]",
                    tripId, runId, stage, response.getModel(), System.currentTimeMillis() - start, response.getTotalTokens());
            return response;
        } catch (RuntimeException e) {
            invocation.fail(e);
            tripRunLogService.fail(runId, System.currentTimeMillis() - start, e.getMessage());
            log.error("[generate][tripId({}) runId({}) stage({}) 耗时({} ms) 调用失败]",
                    tripId, runId, stage, System.currentTimeMillis() - start, e);
            throw e;
        }
    }

    private AgentInvocation startAgentInvocation(Long conversationId, Long tripId, String stage, String content, Long roleId) {
        AgentInvocation invocation = genAiTelemetryHandler.invokeLocalAgent("dashscope", "unknown", "travel-planner");
        invocation.setAgentId(String.valueOf(tripId));
        invocation.setConversationId(String.valueOf(conversationId));
        invocation.setAttribute("trip.agent.stage", stage);
        invocation.setAttribute("trip.chat.role.id", roleId);
        invocation.setAttribute("trip.llm.stream", "true");
        if (genAiTelemetryHandler.shouldCaptureContent()) {
            invocation.setInputMessages(List.of(new InputMessage("user", List.of(new TextPart(content)))));
        }
        return invocation;
    }

    private void setModelResponseAttributes(AgentInvocation invocation, AiChatGenerateRespDTO response,
                                            String responseContent) {
        if (StrUtil.isNotBlank(response.getModel())) {
            invocation.setAttribute("gen_ai.response.model", response.getModel());
        }
        if (response.getPromptTokens() != null) {
            invocation.setInputTokens(response.getPromptTokens());
        }
        if (response.getCompletionTokens() != null) {
            invocation.setOutputTokens(response.getCompletionTokens());
        }
        if (response.getTotalTokens() != null) {
            invocation.setAttribute("gen_ai.usage.total_tokens", response.getTotalTokens());
        }
        if (genAiTelemetryHandler.shouldCaptureContent()) {
            invocation.setOutputMessages(List.of(new OutputMessage("assistant", List.of(new TextPart(responseContent)), "stop")));
        }
    }

    private Long getRoleId(String stage) {
        if (!"FOLLOW_UP".equals(stage) && !"OVERVIEW".equals(stage)) {
            throw new IllegalArgumentException("不支持的旅行模型调用阶段：" + stage);
        }
        String configKey = "OVERVIEW".equals(stage) ? SUMMARY_ROLE_ID_CONFIG_KEY : INTAKE_ROLE_ID_CONFIG_KEY;
        String configValue = configApi.getConfigValueByKey(configKey).getCheckedData();
        if (StrUtil.isBlank(configValue)) {
            throw new IllegalStateException("系统配置 " + configKey + " 未配置");
        }
        try {
            return Long.parseLong(configValue.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("系统配置 " + configKey + " 必须是角色编号", e);
        }
    }

    private int getQuestionCount() {
        try {
            String value = configApi.getConfigValueByKey(QUESTION_COUNT_CONFIG_KEY).getCheckedData();
            int questionCount = Integer.parseInt(StrUtil.blankToDefault(value, "1").trim());
            return Math.min(MAX_SUGGESTION_COUNT, Math.max(1, questionCount));
        } catch (RuntimeException e) {
            log.warn("[getQuestionCount][系统配置 {} 无效，使用默认值 1]", QUESTION_COUNT_CONFIG_KEY, e);
            return 1;
        }
    }

    private Map<String, Object> buildPromptVariables(Long conversationId, Long memberId) {
        AiChatConversationRespDTO conversation = aiChatApi.getConversation(conversationId, memberId,
                UserTypeEnum.MEMBER.getValue());
        if (conversation == null) {
            throw new IllegalStateException("旅行会话不存在");
        }
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("provinceName", getAreaName(conversation.getProvinceId()));
        variables.put("cityName", getAreaName(conversation.getCityId()));
        variables.put("districtName", getAreaName(conversation.getDistrictId()));
        return variables;
    }

    private String getAreaName(Long areaId) {
        return areaId != null ? areaApi.getAreaName(areaId).getCheckedData() : "";
    }

    private String getDefaultDeparture(Long provinceId, Long cityId, Long districtId) {
        List<String> names = new ArrayList<>();
        for (Long areaId : new Long[]{provinceId, cityId, districtId}) {
            if (areaId == null) {
                continue;
            }
            String name = getAreaName(areaId);
            if (StrUtil.isNotBlank(name) && !names.contains(name)) {
                names.add(name);
            }
        }
        return String.join("", names);
    }

    private AiChatMessageRespDTO createTranscriptMessage(Long conversationId, Long memberId, String content, boolean assistant) {
        AiChatMessageCreateAssistantReqDTO req = new AiChatMessageCreateAssistantReqDTO();
        req.setConversationId(conversationId);
        req.setUserId(memberId);
        req.setUserType(UserTypeEnum.MEMBER.getValue());
        req.setContent(content);
        return assistant ? aiChatApi.createAssistantMessage(req) : aiChatApi.createUserMessage(req);
    }

    private String executeManagedIntake(TripPlanDO trip, Map<String, Object> state,
                                        TripItineraryDO currentItinerary, String content) {
        long start = System.currentTimeMillis();
        String task = buildManagedIntakeTask(state, currentItinerary, content);
        Long runId = tripRunLogService.create(trip.getId(), "INTAKE", task);
        try {
            ManagedTripAgentExecutor.Execution execution = managedTripAgentExecutor.execute(
                    trip, state, task, ManagedTripAgentStage.INTAKE);
            tripRunLogService.complete(runId, "managed-agent", execution.result().metrics().inputTokens(),
                    execution.result().metrics().outputTokens(), execution.result().metrics().totalTokens(),
                    System.currentTimeMillis() - start, JsonUtils.toJsonString(Map.of(
                            "sessionId", execution.sessionId(), "metrics", execution.result().metrics(),
                            "response", execution.result().response())));
            return execution.result().response();
        } catch (RuntimeException e) {
            Map<String, Object> failureOutput = new LinkedHashMap<>();
            if (e instanceof ManagedAgentExecutionTerminatedException terminated) {
                failureOutput.put("terminationReason", terminated.getReason().name());
                failureOutput.put("metrics", terminated.getMetrics());
            }
            tripRunLogService.fail(runId, System.currentTimeMillis() - start, e.getMessage(),
                    failureOutput.isEmpty() ? null : JsonUtils.toJsonString(failureOutput));
            throw e;
        }
    }

    static String buildManagedIntakeTask(Map<String, Object> state, TripItineraryDO currentItinerary,
                                         String content) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("task", "EXTRACT_TRIP_REQUIREMENTS");
        request.put("schemaVersion", "1.0");
        request.put("currentTripState", state);
        request.put("informationFields", informationFields(TripInformationSchema.getFields()));
        request.put("currentEditableItinerary", editableItineraryContext(currentItinerary));
        request.put("userMessage", content);
        return JsonUtils.toJsonString(request);
    }

    static Map<String, Object> editableItineraryContext(TripItineraryDO itinerary) {
        if (itinerary == null) {
            return Map.of();
        }
        Map<String, Object> content = TripAgentFormatUtils.parseMap(itinerary.getContentJson());
        List<Map<String, Object>> items = new ArrayList<>();
        if (content.get("daily_itinerary") instanceof List<?> days) {
            for (Object rawDay : days) {
                if (!(rawDay instanceof Map<?, ?> day)
                        || !(day.get("slots") instanceof List<?> slots)) {
                    continue;
                }
                Integer dayNumber = MapUtil.getInt(day, "day");
                for (Object rawSlot : slots) {
                    if (!(rawSlot instanceof Map<?, ?> slot)) {
                        continue;
                    }
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("itemId", slot.get("itemId"));
                    item.put("day", ObjUtil.defaultIfNull(MapUtil.getInt(slot, "day"), dayNumber));
                    copyIfPresent(item, slot, "type", "timePeriod", "sort", "poiId", "poiName", "label", "locked");
                    items.add(item);
                }
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        if (itinerary.getVersion() != null) {
            result.put("version", itinerary.getVersion());
        }
        result.put("items", items);
        return result;
    }

    private static void copyIfPresent(Map<String, Object> target, Map<?, ?> source, String... fields) {
        for (String field : fields) {
            if (source.get(field) != null) {
                target.put(field, source.get(field));
            }
        }
    }

    private TripInteraction generateInteraction(Long conversationId, Long memberId, Map<String, Object> state,
                                                List<String> missingRequired, Long tripId,
                                                Map<String, Object> promptVariables, int questionCount,
                                                Consumer<TripAgentEvent> eventConsumer, AtomicInteger modelDeltaSequence) {
        List<Map<String, String>> fallbackSuggestions = buildInformationSuggestions(state, missingRequired);
        String fallbackQuestion = composeQuestions(buildFallbackQuestions(state, missingRequired, questionCount));
        try {
            AiChatGenerateRespDTO response = generateStream(conversationId, memberId,
                    buildInteractionContext(state, missingRequired, questionCount), "FOLLOW_UP", tripId, promptVariables,
                    chunk -> emitModelDelta(eventConsumer, "FOLLOW_UP", chunk, modelDeltaSequence));
            Map<String, Object> interaction = TripAgentFormatUtils.parseMap(response.getContent());
            List<String> questions = parseQuestions(interaction.get("questions"), questionCount);
            if (CollUtil.isEmpty(questions)) {
                questions = parseQuestions(interaction.get("question"), questionCount);
            }
            String question = CollUtil.isNotEmpty(questions) ? composeQuestions(questions) : fallbackQuestion;
            List<Map<String, String>> suggestions = parseSuggestions(interaction.get("suggestions"));
            suggestions = ensureGenerateSuggestion(
                    CollUtil.isNotEmpty(suggestions) ? suggestions : fallbackSuggestions, missingRequired);
            return new TripInteraction(question, TripInputCardFactory.build(missingRequired, question, suggestions));
        } catch (RuntimeException e) {
            log.warn("[generateInteraction][tripId({}) 追问文案生成失败，使用字段默认文案]", tripId, e);
            List<Map<String, String>> suggestions = ensureGenerateSuggestion(fallbackSuggestions, missingRequired);
            return new TripInteraction(fallbackQuestion,
                    TripInputCardFactory.build(missingRequired, fallbackQuestion, suggestions));
        }
    }

    /**
     * 流式模型内容只供前端渐进展示，旅行状态仍以 intake_completed、question 等已校验事件为准。
     */
    private static void emitModelDelta(Consumer<TripAgentEvent> eventConsumer, String stage, String chunk,
                                       AtomicInteger sequence) {
        if (chunk == null || chunk.isEmpty()) {
            return;
        }
        eventConsumer.accept(TripAgentEvent.of("model_delta", stage, chunk)
                .setSequence(sequence.incrementAndGet()));
    }

    private static String buildInteractionContext(Map<String, Object> state, List<String> missingRequired, int questionCount) {
        List<TripInformationSchema.Field> candidates = new ArrayList<>();
        missingRequired.stream().map(TripInformationSchema::getByMissingKey).filter(java.util.Objects::nonNull)
                .forEach(candidates::add);
        TripInformationSchema.getFields().stream().filter(field -> !hasValue(state.get(field.stateKey())))
                .filter(field -> !candidates.contains(field)).forEach(candidates::add);
        return "InteractionType: FOLLOW_UP\n\nCurrent TripState:\n" + JsonUtils.toJsonString(state) + "\n\n"
                + "MissingRequiredFields:\n" + JsonUtils.toJsonString(missingRequired) + "\n\n"
                + "QuestionCount:\n" + questionCount + "\n\n"
                + "MaximumSuggestionCount:\n" + getModelSuggestionCount(missingRequired) + "\n\n"
                + "CandidateFields:\n" + JsonUtils.toJsonString(informationFields(candidates));
    }

    @SuppressWarnings("unchecked")
    private static String buildOverviewContext(Map<String, Object> state, Map<String, Object> itinerary, Integer day, String slot) {
        Object target = itinerary;
        if ("DAY_OVERVIEW".equalsIgnoreCase(slot)) {
            Object dailyItinerary = itinerary.get("daily_itinerary");
            if (dailyItinerary instanceof List<?> days) {
                target = days.stream().filter(Map.class::isInstance).map(Map.class::cast)
                        .filter(item -> ObjUtil.equal(MapUtil.getInt(item, "day"), day)).findFirst().orElse(Map.of());
            }
        }
        String scope = "TRIP_OVERVIEW".equalsIgnoreCase(slot) ? "TRIP" : "DAY";
        return "InteractionType: ITINERARY_OVERVIEW\n\nOverviewScope: " + scope + "\n\nTripState:\n"
                + JsonUtils.toJsonString(state) + "\n\nOverviewTarget:\n" + JsonUtils.toJsonString(target);
    }

    private static int getModelSuggestionCount(List<String> missingRequired) {
        return CollUtil.isEmpty(missingRequired) ? MAX_SUGGESTION_COUNT - 1 : MAX_SUGGESTION_COUNT;
    }

    private static List<Map<String, Object>> informationFields(List<TripInformationSchema.Field> fields) {
        return fields.stream().map(field -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stateKey", field.stateKey());
            item.put("label", field.label());
            item.put("required", TripInformationSchema.getRequiredStateKeys().contains(field.stateKey()));
            item.put("questionHint", field.question());
            item.put("suggestions", field.suggestions());
            return item;
        }).toList();
    }

    private static Object extractState(Map<String, Object> intake) {
        for (String key : List.of("state", "tripState", "patch")) {
            Object value = intake.get(key);
            if (value instanceof Map<?, ?>) {
                return value;
            }
        }
        return Map.of();
    }

    private static boolean isGenerateRequested(Map<String, Object> intake) {
        return "GENERATE".equalsIgnoreCase(trimNullable(intake.get("action")));
    }

    /** 将 Agent 输出转换为服务端统一命令；baseVersion 始终取当前快照，不信任模型提供的版本。 */
    static TripChangeCommand extractAgentChangeCommand(Map<String, Object> intake, int baseVersion) {
        Object rawCommand = firstNonNull(intake.get("change_command"), intake.get("changeCommand"));
        Object rawCommands = intake.get("change_commands");
        if (rawCommand == null && rawCommands instanceof List<?> commands && !commands.isEmpty()) {
            if (commands.size() != 1) {
                throw new IllegalArgumentException("Agent 单次只能提交一个行程变更命令");
            }
            rawCommand = commands.get(0);
        }
        if (rawCommand instanceof Map<?, ?> command) {
            String operationText = StrUtil.trim(ObjUtil.toString(
                    firstNonNull(command.get("operation"), command.get("op")))).toUpperCase(Locale.ROOT);
            TripChangeCommand.Operation operation;
            try {
                operation = TripChangeCommand.Operation.valueOf(operationText);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Agent 返回了不支持的行程变更操作：" + operationText, ex);
            }
            return new TripChangeCommand(operation, baseVersion, trimNullable(command.get("itemId")),
                    MapUtil.getInt(command, "day"), trimNullable(command.get("timePeriod")),
                    MapUtil.getInt(command, "sort"), stringKeyMap(command.get("values")));
        }
        return legacyItineraryPatchCommand(intake.get("itinerary_patch"), baseVersion);
    }

    private static TripChangeCommand legacyItineraryPatchCommand(Object value, int baseVersion) {
        if (!(value instanceof Map<?, ?> patch) || !(patch.get("operations") instanceof List<?> operations)) {
            return null;
        }
        Set<Integer> affectedDays = new java.util.LinkedHashSet<>();
        List<String> instructions = new ArrayList<>();
        for (Object valueItem : operations) {
            if (!(valueItem instanceof Map<?, ?> operation)) {
                continue;
            }
            Integer day = MapUtil.getInt(operation, "day");
            String op = StrUtil.trim(ObjUtil.toString(operation.get("op"))).toUpperCase(Locale.ROOT);
            if (day == null || day <= 0 || !("SET".equals(op) || "REMOVE".equals(op))) {
                continue;
            }
            affectedDays.add(day);
            String instruction = trimNullable(operation.get("instruction"));
            if ("SET".equals(op) && StrUtil.isNotBlank(instruction)) {
                instructions.add(instruction);
            }
        }
        if (affectedDays.isEmpty()) {
            return null;
        }
        if (affectedDays.size() == 1) {
            Map<String, Object> values = instructions.isEmpty() ? Map.of()
                    : Map.of("instruction", String.join("；", instructions));
            return new TripChangeCommand(TripChangeCommand.Operation.REPLAN_DAY, baseVersion, null,
                    affectedDays.iterator().next(), null, null, values);
        }
        return new TripChangeCommand(TripChangeCommand.Operation.REPLAN_TRIP, baseVersion,
                null, null, null, null, Map.of());
    }

    private static Object firstNonNull(Object first, Object second) {
        return first != null ? first : second;
    }

    private static Map<String, Object> stringKeyMap(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, item) -> {
            if (key instanceof String text) {
                result.put(text, item);
            }
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    static void mergeInformationState(Map<String, Object> state, Object extractedState, String userMessage) {
        if (!(extractedState instanceof Map<?, ?> patch)) {
            return;
        }
        patch.forEach((key, value) -> {
            if (key instanceof String field && TripInformationSchema.supports(field) && value != null) {
                Object normalized = normalizeField(field, value);
                if (normalized != null && requiresExplicitNumber(field)
                        && !isExplicitlyMentioned(field, userMessage)) {
                    log.info("[mergeInformationState][忽略模型未被本轮消息支持的数值字段 field({})]", field);
                    return;
                }
                if (normalized == null) {
                    state.remove(field);
                } else {
                    state.put(field, normalized);
                }
            }
        });
    }

    private static boolean requiresExplicitNumber(String field) {
        return "days".equals(field) || "travelerCount".equals(field) || "budget".equals(field)
                || "hotelBudget".equals(field) || "travelerProfile".equals(field);
    }

    private static boolean isExplicitlyMentioned(String field, String userMessage) {
        if (StrUtil.isBlank(userMessage)) {
            return false;
        }
        String number = "[0-9０-９一二两三四五六七八九十百千万]+";
        return switch (field) {
            case "days" -> userMessage.matches("(?s).*" + number + "\\s*[天晚日].*");
            case "travelerCount" -> userMessage.matches("(?s).*" + number + "\\s*[人位大小].*")
                    || userMessage.matches("(?s).*一家" + number + "口.*");
            case "travelerProfile" -> userMessage.matches("(?s).*" + number + "\\s*[大小].*")
                    || userMessage.matches("(?s).*" + number + "\\s*(?:位)?(?:成人|儿童|小孩|老人).*" );
            case "budget" -> userMessage.matches("(?s).*" + number + "\\s*(?:元|块|人民币).*" )
                    || userMessage.matches("(?s).*(?:预算|花费|花).*" + number + ".*");
            case "hotelBudget" -> userMessage.matches("(?s).*(?:住宿|酒店|每晚).*" + number + ".*");
            default -> true;
        };
    }

    private static Object normalizeField(String field, Object value) {
        if ("days".equals(field) || "travelerCount".equals(field)) {
            String text = trimNullable(value);
            return StrUtil.isBlank(text) ? null : Integer.parseInt(text);
        }
        if ("hotelBudget".equals(field)) {
            Integer budget = normalizeAmount(value);
            return budget != null && budget > 0 ? budget : null;
        }
        if ("budget".equals(field)) {
            Integer budget = normalizeAmount(value);
            return budget != null && budget > 0 ? String.valueOf(budget) : null;
        }
        if ("dailyStartTime".equals(field) || "dailyEndTime".equals(field)) {
            return normalizeTime(value);
        }
        if ("travelerProfile".equals(field)) {
            return normalizeTravelerProfile(value);
        }
        if ("interests".equals(field) || "mustVisit".equals(field) || "constraints".equals(field)) {
            if (value instanceof List<?> list) {
                return list.stream().map(TripAgentServiceImpl::trimNullable).filter(StrUtil::isNotBlank).toList();
            }
            String text = trimNullable(value);
            return StrUtil.isBlank(text) ? List.of() : List.of(text);
        }
        return trimNullable(value);
    }

    private static Integer normalizeAmount(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        Matcher matcher = AMOUNT_PATTERN.matcher(trimNullable(value));
        if (!matcher.find()) {
            return null;
        }
        try {
            return Integer.valueOf(matcher.group().replace(",", ""));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String normalizeTime(Object value) {
        String text = trimNullable(value);
        if (StrUtil.isBlank(text)) {
            return null;
        }
        try {
            return LocalTime.parse(text, TIME_FORMATTER).format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static Map<String, Object> normalizeTravelerProfile(Object value) {
        if (!(value instanceof Map<?, ?> profile)) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        addProfileCount(result, profile, "adultCount");
        addProfileCount(result, profile, "childCount");
        addProfileCount(result, profile, "elderlyCount");
        return result;
    }

    private static void addProfileCount(Map<String, Object> result, Map<?, ?> profile, String key) {
        Integer value = MapUtil.getInt(profile, key);
        if (value != null && value >= 0 && value <= 20) {
            result.put(key, value);
        }
    }

    private static List<Map<String, String>> buildInformationSuggestions(Map<String, Object> state,
                                                                           List<String> missingRequired) {
        List<Map<String, String>> suggestions = new ArrayList<>();
        if (CollUtil.isEmpty(missingRequired)) {
            addSuggestion(suggestions, "立即生成行程", "请立即生成行程");
        }
        List<TripInformationSchema.Field> orderedFields = new ArrayList<>();
        missingRequired.stream().map(TripInformationSchema::getByMissingKey).filter(java.util.Objects::nonNull)
                .forEach(orderedFields::add);
        TripInformationSchema.getFields().stream().filter(field -> !hasValue(state.get(field.stateKey())))
                .filter(field -> !orderedFields.contains(field)).forEach(orderedFields::add);
        for (TripInformationSchema.Field field : orderedFields) {
            field.suggestions().forEach(suggestion -> addSuggestion(suggestions, suggestion.label(), suggestion.content()));
        }
        return suggestions;
    }

    private static List<Map<String, String>> ensureGenerateSuggestion(List<Map<String, String>> suggestions,
                                                                         List<String> missingRequired) {
        if (CollUtil.isNotEmpty(missingRequired)) {
            return suggestions;
        }
        List<Map<String, String>> result = new ArrayList<>();
        for (Map<String, String> suggestion : suggestions) {
            if ("请立即生成行程".equals(suggestion.get("content"))) {
                continue;
            }
            if (result.size() >= MAX_SUGGESTION_COUNT - 1) {
                break;
            }
            result.add(suggestion);
        }
        addSuggestion(result, "立即生成行程", "请立即生成行程");
        return result;
    }

    private static List<String> buildFallbackQuestions(Map<String, Object> state, List<String> missingRequired,
                                                       int questionCount) {
        List<TripInformationSchema.Field> fields = new ArrayList<>();
        missingRequired.stream().map(TripInformationSchema::getByMissingKey).filter(java.util.Objects::nonNull)
                .forEach(fields::add);
        TripInformationSchema.getFields().stream().filter(field -> !hasValue(state.get(field.stateKey())))
                .filter(field -> !fields.contains(field)).forEach(fields::add);
        List<String> questions = fields.stream().map(TripInformationSchema.Field::question).limit(questionCount).toList();
        return CollUtil.isNotEmpty(questions) ? questions
                : List.of("你还可以补充偏好、同行人或特殊约束，让推荐更贴合你。");
    }

    private static List<String> parseQuestions(Object value, int questionCount) {
        if (value instanceof List<?> list) {
            return list.stream().map(TripAgentServiceImpl::trimNullable).filter(StrUtil::isNotBlank)
                    .limit(questionCount).toList();
        }
        String question = trimNullable(value);
        return StrUtil.isNotBlank(question) ? List.of(question) : List.of();
    }

    private static String composeQuestions(List<String> questions) {
        if (questions.size() == 1) {
            return questions.get(0);
        }
        List<String> items = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            items.add((i + 1) + ". " + questions.get(i));
        }
        return String.join("\n", items);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, String>> parseSuggestions(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, String>> suggestions = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> suggestion)) {
                continue;
            }
            String label = trimNullable(suggestion.get("label"));
            String content = trimNullable(suggestion.get("content"));
            if (StrUtil.isNotBlank(label) && StrUtil.isNotBlank(content)) {
                addSuggestion(suggestions, label, content);
            }
        }
        return suggestions;
    }

    private static boolean hasValue(Object value) {
        if (value instanceof List<?> list) {
            return CollUtil.isNotEmpty(list);
        }
        if (value instanceof Map<?, ?> map) {
            return MapUtil.isNotEmpty(map);
        }
        return StrUtil.isNotBlank(trimNullable(value));
    }

    private static void sanitizeTravelerProfile(Map<String, Object> state) {
        Object profile = state.get("travelerProfile");
        if (profile == null) {
            return;
        }
        Map<String, Object> sanitized = normalizeTravelerProfile(profile);
        if (sanitized.isEmpty()) {
            state.remove("travelerProfile");
        } else {
            state.put("travelerProfile", sanitized);
        }
    }

    private static void addSuggestion(List<Map<String, String>> suggestions, String label, String content) {
        if (suggestions.size() < MAX_SUGGESTION_COUNT) {
            suggestions.add(Map.of("label", label, "content", content));
        }
    }

    private record TripInteraction(String question, List<Map<String, Object>> inputCards) {
    }

    @SuppressWarnings("unchecked")
    private static void applyItineraryPatch(Map<String, Object> state, Object itineraryPatchValue) {
        if (!(itineraryPatchValue instanceof Map<?, ?> itineraryPatch)
                || !(itineraryPatch.get("operations") instanceof List<?> operations)) {
            return;
        }
        Map<String, Map<String, Object>> overrides = new LinkedHashMap<>();
        Object currentValue = state.get(STATE_ITINERARY_OVERRIDES);
        if (currentValue instanceof List<?> currentOverrides) {
            currentOverrides.forEach(value -> addItineraryOverride(overrides, value));
        }
        operations.forEach(operation -> addItineraryOverride(overrides, operation));
        if (overrides.isEmpty() && !(currentValue instanceof List<?>)) {
            return;
        }
        state.put(STATE_ITINERARY_OVERRIDES, new ArrayList<>(overrides.values()));
    }

    private static void addItineraryOverride(Map<String, Map<String, Object>> overrides, Object value) {
        if (!(value instanceof Map<?, ?> operation)) {
            return;
        }
        String op = StrUtil.trim(ObjUtil.toString(operation.get("op"))).toUpperCase(Locale.ROOT);
        Integer day = MapUtil.getInt(operation, "day");
        String slot = StrUtil.trim(ObjUtil.toString(operation.get("slot"))).toUpperCase(Locale.ROOT);
        if (!ITINERARY_SLOTS.contains(slot) || day == null || !isValidOverrideTarget(day, slot)) {
            return;
        }
        String key = day + ":" + slot;
        if ("REMOVE".equals(op)) {
            overrides.remove(key);
            return;
        }
        String instruction = StrUtil.trim(ObjUtil.toString(operation.get("instruction")));
        if ("SET".equals(op) && StrUtil.isNotBlank(instruction)) {
            Map<String, Object> override = new LinkedHashMap<>();
            override.put("day", day);
            override.put("slot", slot);
            override.put("instruction", instruction);
            overrides.put(key, override);
        }
    }

    private static boolean isValidOverrideTarget(Integer day, String slot) {
        return "ARRIVAL".equals(slot) || "DEPARTURE".equals(slot)
                ? day == 0 : day >= 1 && day <= 30 && DAILY_ITINERARY_SLOTS.contains(slot);
    }

    static List<String> validateState(Map<String, Object> state) {
        List<String> missing = new ArrayList<>();
        if (StrUtil.isBlank(trimNullable(state.get("departure")))) {
            missing.add("departure");
        }
        if (StrUtil.isBlank(trimNullable(state.get("destination")))) {
            missing.add("destination");
        }
        Integer days = MapUtil.getInt(state, "days");
        if (days != null && (days < 1 || days > 30)) {
            state.remove("days");
            days = null;
        }
        String startDate = trimNullable(state.get("startDate"));
        String endDate = trimNullable(state.get("endDate"));
        LocalDate parsedStartDate = null;
        LocalDate parsedEndDate = null;
        try {
            if (StrUtil.isNotBlank(startDate)) {
                parsedStartDate = LocalDate.parse(startDate);
            }
        } catch (RuntimeException e) {
            state.remove("startDate");
            startDate = null;
        }
        try {
            if (StrUtil.isNotBlank(endDate)) {
                parsedEndDate = LocalDate.parse(endDate);
                if (parsedStartDate == null || parsedEndDate.isBefore(parsedStartDate)) {
                    state.remove("endDate");
                    endDate = null;
                    parsedEndDate = null;
                }
            }
        } catch (RuntimeException e) {
            state.remove("endDate");
            endDate = null;
            parsedEndDate = null;
        }
        if (days == null && parsedStartDate != null && parsedEndDate != null) {
            long inclusiveDays = ChronoUnit.DAYS.between(parsedStartDate, parsedEndDate) + 1;
            if (inclusiveDays >= 1 && inclusiveDays <= 30) {
                days = Math.toIntExact(inclusiveDays);
                state.put("days", days);
            }
        }
        if (days == null) {
            missing.add("days");
        }
        Integer travelers = MapUtil.getInt(state, "travelerCount");
        if (travelers != null && (travelers < 1 || travelers > 20)) {
            state.remove("travelerCount");
            travelers = null;
        }
        if (travelers == null) {
            missing.add("traveler_count");
        }
        return missing;
    }

    private static String buildIntakeCompletedContent(Map<String, Object> state, List<String> missingRequired) {
        List<String> values = new ArrayList<>();
        String destination = trimNullable(state.get("destination"));
        if (StrUtil.isNotBlank(destination)) {
            values.add(destination);
        }
        Integer days = MapUtil.getInt(state, "days");
        if (days != null) {
            values.add(days + " 天");
        }
        Integer travelerCount = MapUtil.getInt(state, "travelerCount");
        if (travelerCount != null) {
            values.add(travelerCount + " 人");
        }
        String summary = CollUtil.isEmpty(values) ? "当前需求" : String.join(" · ", values);
        return CollUtil.isEmpty(missingRequired) ? "需求已整理：" + summary : "已整理：" + summary;
    }

    private static String trimNullable(Object value) {
        if (value == null) {
            return "";
        }
        String text = StrUtil.trim(String.valueOf(value));
        return "null".equalsIgnoreCase(text) ? "" : text;
    }

    private TripItinerarySlotDO getOrCreateItinerarySlot(TripItineraryDO itinerary, Integer day, String slot,
                                                           Map<String, Object> skeletonSlot) {
        String normalizedSlot = StrUtil.trim(slot).toUpperCase(Locale.ROOT);
        TripItinerarySlotDO result = tripItinerarySlotMapper.selectByItineraryIdAndDayAndSlot(itinerary.getId(), day, normalizedSlot);
        if (result != null) {
            return result;
        }
        Map<String, Object> slotData = new LinkedHashMap<>(skeletonSlot);
        slotData.put("slot", normalizedSlot);
        createItinerarySlotIfAbsent(itinerary, day, slotData);
        result = tripItinerarySlotMapper.selectByItineraryIdAndDayAndSlot(itinerary.getId(), day, normalizedSlot);
        if (result == null) {
            throw new IllegalStateException("行程节点初始化失败");
        }
        return result;
    }

    private void createItinerarySlotIfAbsent(TripItineraryDO itinerary, Integer day, Map<String, Object> slotData) {
        String slot = StrUtil.trim(ObjUtil.toString(slotData.get("slot"))).toUpperCase(Locale.ROOT);
        if (!ITINERARY_SLOTS.contains(slot)) {
            return;
        }
        TripItinerarySlotDO itinerarySlot = new TripItinerarySlotDO();
        itinerarySlot.setTenantId(TenantContextHolder.getRequiredTenantId());
        itinerarySlot.setItineraryId(itinerary.getId());
        itinerarySlot.setDay(day);
        itinerarySlot.setSlot(slot);
        itinerarySlot.setSkeleton(StrUtil.blankToDefault(ObjUtil.toString(slotData.get("skeleton")), "待补充"));
        itinerarySlot.setPoiId(trimNullable(slotData.get("poiId")));
        itinerarySlot.setStatus(StrUtil.blankToDefault(ObjUtil.toString(slotData.get("status")), "PENDING"));
        itinerarySlot.setResolveStatus(SLOT_RESOLVE_STATUS_PENDING);
        tripItinerarySlotMapper.insertIgnore(itinerarySlot);
    }

    private static boolean isOverviewSlot(String slot) {
        return "TRIP_OVERVIEW".equalsIgnoreCase(slot) || "DAY_OVERVIEW".equalsIgnoreCase(slot);
    }

    private TripItinerarySlotResult withWeather(TripItinerarySlotResult result, String city) {
        if (StrUtil.isBlank(city)) {
            return result;
        }
        try {
            result.setWeather(tripTravelQueryService.getCurrentWeather(city));
        } catch (RuntimeException e) {
            log.warn("[resolveItinerarySlot][city({}) 天气查询失败，不影响节点结果]", city, e);
        }
        return result;
    }

    private static String resolveSlotCity(Map<String, Object> state, Map<String, Object> skeletonSlot) {
        String city = trimNullable(skeletonSlot.get("city"));
        return StrUtil.blankToDefault(city, trimNullable(state.get("destination")));
    }

    private static TripItinerarySlotResult toSlotResult(Long messageId, TripItinerarySlotDO slot) {
        String detail = slot.getDetail();
        if (ObjUtil.equal(slot.getResolveStatus(), SLOT_RESOLVE_STATUS_PROCESSING) && StrUtil.isBlank(detail)) {
            detail = "节点正在补充中";
        }
        return new TripItinerarySlotResult().setMessageId(messageId).setDay(slot.getDay()).setSlot(slot.getSlot())
                .setSlotId(slot.getId()).setStatus(slot.getStatus()).setDetail(detail)
                .setCandidates(bindCandidatesToSlot(slot, parseCandidates(slot.getCandidatesJson())))
                .setCitationIds(parseCitationIds(slot.getCitationIdsJson()));
    }

    private static void mergeResolvedSlot(Map<String, Object> itinerary, TripItinerarySlotDO slot) {
        try {
            Map<String, Object> target = findSlot(itinerary, slot.getDay(), slot.getSlot());
            target.put("slotId", String.valueOf(slot.getId()));
            target.put("status", slot.getStatus());
            target.put("detail", slot.getDetail());
            target.put("candidates", bindCandidatesToSlot(slot, parseCandidates(slot.getCandidatesJson())));
            target.put("citationIds", parseCitationIds(slot.getCitationIdsJson()));
        } catch (IllegalArgumentException e) {
            log.warn("[mergeResolvedSlot][itineraryId({}) day({}) slot({}) 节点不存在]",
                    slot.getItineraryId(), slot.getDay(), slot.getSlot());
        }
    }

    private static List<Map<String, Object>> parseCandidates(String candidatesJson) {
        if (StrUtil.isBlank(candidatesJson)) {
            return List.of();
        }
        List<Map<String, Object>> candidates = JsonUtils.parseObjectQuietly(candidatesJson,
                new TypeReference<List<Map<String, Object>>>() { });
        return candidates != null ? candidates : List.of();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> bindCandidatesToSlot(TripItinerarySlotDO slot,
                                                                    List<Map<String, Object>> candidates) {
        if (CollUtil.isEmpty(candidates)) {
            return List.of();
        }
        String slotId = String.valueOf(slot.getId());
        List<Map<String, Object>> result = new ArrayList<>(candidates.size());
        for (Map<String, Object> candidate : candidates) {
            Map<String, Object> bound = new LinkedHashMap<>(candidate);
            String poiId = StrUtil.blankToDefault(ObjUtil.toString(bound.get("poiId")),
                    ObjUtil.toString(bound.get("id")));
            bound.remove("externalId");
            bound.put("poiId", poiId);
            bound.put("id", "slot-" + slotId + "-" + poiId);
            Map<String, Object> metadata = bound.get("metadata") instanceof Map<?, ?> rawMetadata
                    ? new LinkedHashMap<>((Map<String, Object>) rawMetadata) : new LinkedHashMap<>();
            metadata.put("slotId", slotId);
            bound.put("metadata", metadata);
            result.add(bound);
        }
        return result;
    }

    private static List<String> parseCitationIds(String citationIdsJson) {
        if (StrUtil.isBlank(citationIdsJson)) {
            return List.of();
        }
        List<String> citationIds = JsonUtils.parseObjectQuietly(citationIdsJson, new TypeReference<List<String>>() { });
        return citationIds != null ? citationIds : List.of();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> findSlot(Map<String, Object> itinerary, Integer day, String slot) {
        if (day == 0 && StrUtil.equalsIgnoreCase(slot, "TRIP_OVERVIEW")) {
            Object overview = itinerary.get("overview");
            if (overview instanceof Map<?, ?> result) {
                return (Map<String, Object>) result;
            }
            throw new IllegalArgumentException("行程总览节点不存在");
        }
        if (day == 0 && (StrUtil.equalsIgnoreCase(slot, "ARRIVAL") || StrUtil.equalsIgnoreCase(slot, "DEPARTURE"))) {
            Object transport = itinerary.get("transport");
            if (transport instanceof Map<?, ?> transportMap) {
                Object transportSlot = transportMap.get(StrUtil.equalsIgnoreCase(slot, "ARRIVAL") ? "arrival" : "departure");
                if (transportSlot instanceof Map<?, ?> result) {
                    return (Map<String, Object>) result;
                }
            }
            throw new IllegalArgumentException("交通节点不存在");
        }
        Object dailyItinerary = itinerary.get("daily_itinerary");
        if (!(dailyItinerary instanceof List<?> days)) {
            throw new IllegalArgumentException("行程骨架格式错误");
        }
        for (Object item : days) {
            if (!(item instanceof Map<?, ?> rawDay) || !ObjUtil.equals(day, MapUtil.getInt((Map<?, ?>) rawDay, "day"))) {
                continue;
            }
            if (StrUtil.equalsIgnoreCase(slot, "DAY_OVERVIEW") && rawDay.get("overview") instanceof Map<?, ?> overview) {
                return (Map<String, Object>) overview;
            }
            Object slots = rawDay.get("slots");
            if (slots instanceof List<?> slotList) {
                for (Object slotItem : slotList) {
                    if (slotItem instanceof Map<?, ?> rawSlot && StrUtil.equalsIgnoreCase(slot, ObjUtil.toString(rawSlot.get("slot")))) {
                        return (Map<String, Object>) rawSlot;
                    }
                }
            }
        }
        throw new IllegalArgumentException("行程节点不存在");
    }

    @SuppressWarnings("unchecked")
    private static List<String> collectCitationIds(Map<String, Object> itinerary) {
        List<String> result = new ArrayList<>();
        Object dailyItinerary = itinerary.get("daily_itinerary");
        if (dailyItinerary instanceof List<?> days) {
            for (Object day : days) {
                if (day instanceof Map<?, ?> dayMap && dayMap.get("slots") instanceof List<?> slots) {
                    for (Object slot : slots) {
                        if (slot instanceof Map<?, ?> slotMap && slotMap.get("citationIds") instanceof List<?> ids) {
                            ids.forEach(id -> result.add(String.valueOf(id)));
                        }
                    }
                }
            }
        }
        return result.stream().distinct().toList();
    }

    private static String slotLabel(String slot) {
        return switch (StrUtil.nullToDefault(slot, "").toUpperCase(java.util.Locale.ROOT)) {
            case "MORNING" -> "上午";
            case "LUNCH" -> "午餐";
            case "AFTERNOON" -> "下午";
            case "DINNER" -> "晚餐";
            case "EVENING" -> "晚上";
            case "ACCOMMODATION" -> "住宿";
            case "TRIP_OVERVIEW" -> "行程总览";
            case "DAY_OVERVIEW" -> "每日总览";
            default -> "待补充";
        };
    }

}
