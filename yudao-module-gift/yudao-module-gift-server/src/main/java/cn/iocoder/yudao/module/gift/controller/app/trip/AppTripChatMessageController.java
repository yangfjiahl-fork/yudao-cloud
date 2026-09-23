package cn.iocoder.yudao.module.gift.controller.app.trip;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tracer.core.util.MdcContextUtils;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversationevent.UserItineraryConversationEventDO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripChatMessageRespVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripAgUiMessageReqVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripAgUiRunReqVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripItineraryChangeReqVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripItineraryChangeRespVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripItineraryRouteResolveReqVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripItineraryRouteResolveRespVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripItinerarySlotResolveReqVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripItinerarySlotResolveRespVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripWeatherRespVO;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.gift.service.trip.TripAgentService;
import cn.iocoder.yudao.module.gift.service.trip.ItineraryConversationService;
import cn.iocoder.yudao.module.gift.service.trip.TripPlanEditorService;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripAgentEvent;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripChangeCommand;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripItineraryRouteResult;
import cn.iocoder.yudao.module.gift.service.trip.bo.TripItinerarySlotResult;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 旅行规划消息")
@RestController
@RequestMapping("/ai/chat/message")
@Validated
@Slf4j
public class AppTripChatMessageController {

    private static final int TEXT_DELTA_MAX_CODE_POINTS = 12;

    @Resource
    private ItineraryConversationService conversationService;
    @Resource
    private TripAgentService tripAgentService;
    @Resource
    private TripPlanEditorService tripPlanEditorService;

    @GetMapping("/list-by-conversation-id")
    @Operation(summary = "获得旅行规划消息列表")
    @Parameter(name = "conversationId", required = true, description = "对话编号", example = "1024")
    public CommonResult<List<AppTripChatMessageRespVO>> getMessageList(@RequestParam("conversationId") Long conversationId) {
        List<AppTripChatMessageRespVO> messages = conversationService.getEvents(conversationId, getLoginUserId())
                .stream().map(AppTripChatMessageController::toMessage).toList();
        Collection<Long> messageIds = messages.stream().map(AppTripChatMessageRespVO::getId).toList();
        Map<Long, Map<String, Object>> itineraryMap = tripAgentService.getItineraryMapByMessageIds(messageIds);
        messages.forEach(message -> message.setItinerary(itineraryMap.get(message.getId())));
        return success(messages);
    }

    /**
     * AG-UI RunAgentInput 入口。旅行会话的持久化历史在服务端维护，当前仅接受一条 user text message，
     * 以免客户端传入的历史消息越过既有成员与会话校验。
     */
    @PostMapping(value = "/managed/run", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "运行托管旅行规划 Agent（AG-UI）")
    public Flux<CommonResult<Map<String, Object>>> runManagedAgUi(@Valid @RequestBody AppTripAgUiRunReqVO reqVO) {
        Long conversationId = parseConversationId(reqVO.getThreadId());
        AppTripAgUiMessageReqVO message = reqVO.getMessages().get(0);
        if (!StrUtil.equalsIgnoreCase("user", message.getRole())) {
            throw new IllegalArgumentException("旅行规划仅接受 user 消息");
        }
        Long memberId = getLoginUserId();
        conversationService.getRequired(conversationId, memberId);
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        log.info("[runManagedAgUi][conversationId({}) runId({}) memberId({}) tenantId({}) 创建 AG-UI SSE 流]",
                conversationId, reqVO.getRunId(), memberId, tenantId);
        Flux<Map<String, Object>> execution = executeTrip(conversationId, memberId, tenantId, reqVO.getRunId(),
                        message.getContent())
                .concatMap(event -> Flux.fromIterable(toAgUiEvents(event, reqVO.getRunId())));
        return MdcContextUtils.withReactorContext(Flux.concat(
                        Flux.just(agUiRunStarted(reqVO.getThreadId(), reqVO.getRunId())), execution,
                        Flux.just(agUiRunFinished(reqVO.getThreadId(), reqVO.getRunId(), conversationId)))
                .map(event -> success(event))
                .onErrorResume(e -> {
                    log.error("[runManagedAgUi][conversationId({}) runId({}) 生成旅行方案失败]",
                            conversationId, reqVO.getRunId(), e);
                    return Flux.just(success(agUiRunError(reqVO.getThreadId(), reqVO.getRunId())));
                })
                .doOnSubscribe(subscription -> log.info("[runManagedAgUi][conversationId({}) runId({}) AG-UI SSE 已订阅]",
                        conversationId, reqVO.getRunId()))
                .doOnCancel(() -> log.info("[runManagedAgUi][conversationId({}) runId({}) 客户端取消 AG-UI SSE]",
                        conversationId, reqVO.getRunId()))
                .doOnComplete(() -> log.info("[runManagedAgUi][conversationId({}) runId({}) AG-UI SSE 完成]",
                        conversationId, reqVO.getRunId())));
    }

    @PostMapping("/itinerary/slot/resolve")
    @Operation(summary = "并行补充旅行行程骨架节点")
    public CommonResult<AppTripItinerarySlotResolveRespVO> resolveItinerarySlot(
            @Valid @RequestBody AppTripItinerarySlotResolveReqVO reqVO) {
        Long memberId = getLoginUserId();
        conversationService.getRequired(reqVO.getConversationId(), memberId);
        TripItinerarySlotResult result = tripAgentService.resolveItinerarySlot(reqVO.getConversationId(), memberId,
                reqVO.getMessageId(), reqVO.getDay(), reqVO.getSlot());
        AppTripItinerarySlotResolveRespVO response = new AppTripItinerarySlotResolveRespVO();
        response.setMessageId(result.getMessageId());
        response.setSlotId(result.getSlotId());
        response.setDay(result.getDay());
        response.setSlot(result.getSlot());
        response.setStatus(result.getStatus());
        response.setDetail(result.getDetail());
        response.setCandidates(result.getCandidates());
        response.setCitationIds(result.getCitationIds());
        response.setTransportSegments(result.getTransportSegments());
        if (result.getWeather() != null) {
            response.setWeather(new AppTripWeatherRespVO()
                    .setCity(result.getWeather().city())
                    .setTemperature(result.getWeather().temperature())
                    .setCondition(result.getWeather().condition())
                    .setHumidity(result.getWeather().humidity())
                    .setWindDirection(result.getWeather().windDirection())
                    .setWindPower(result.getWeather().windPower())
                    .setQueryTime(result.getWeather().queryTime()));
        }
        return success(response);
    }

    @PostMapping("/itinerary/route/resolve")
    @Operation(summary = "按需解析某一天的交通路线")
    public CommonResult<AppTripItineraryRouteResolveRespVO> resolveItineraryRoute(
            @Valid @RequestBody AppTripItineraryRouteResolveReqVO reqVO) {
        Long memberId = getLoginUserId();
        conversationService.getRequired(reqVO.getConversationId(), memberId);
        TripItineraryRouteResult result = tripAgentService.resolveItineraryRoute(reqVO.getConversationId(), memberId,
                reqVO.getMessageId(), reqVO.getDay());
        AppTripItineraryRouteResolveRespVO response = new AppTripItineraryRouteResolveRespVO();
        response.setMessageId(result.getMessageId());
        response.setDay(result.getDay());
        response.setStatus(result.getStatus());
        response.setTransportSegments(result.getTransportSegments());
        return success(response);
    }

    @PostMapping("/itinerary/change")
    @Operation(summary = "使用统一命令编辑当前旅行行程")
    public CommonResult<AppTripItineraryChangeRespVO> changeItinerary(
            @Valid @RequestBody AppTripItineraryChangeReqVO reqVO) {
        Long memberId = getLoginUserId();
        conversationService.getRequired(reqVO.getConversationId(), memberId);
        TripChangeCommand command = new TripChangeCommand(reqVO.getOperation(), reqVO.getItemId(), reqVO.getDay(),
                reqVO.getTimePeriod(), reqVO.getSort(), reqVO.getValues());
        TripPlanEditorService.EditResult result = tripPlanEditorService.apply(
                reqVO.getConversationId(), memberId, command);
        AppTripItineraryChangeRespVO response = new AppTripItineraryChangeRespVO();
        response.setItineraryId(result.saved().itineraryId());
        response.setMessageId(result.saved().messageId());
        response.setContent(result.saved().displayText());
        response.setAffectedDays(result.affectedDays());
        response.setItinerary(result.itinerary());
        return success(response);
    }

    private Flux<TripAgentEvent> executeTrip(Long conversationId, Long memberId, Long tenantId, String runId,
                                             String content) {
        // SSE 在 boundedElastic 线程执行，显式保留 HTTP 请求的 OTel 上下文，保证 Agent 和 LLM Span 归属同一条调用链。
        Context parentOtelContext = Context.current();
        return Flux.deferContextual(context -> {
            @SuppressWarnings("unchecked")
            Map<String, String> mdcContext = context.getOrDefault(MdcContextUtils.REACTOR_CONTEXT_MDC_KEY, Map.of());
            return Flux.<TripAgentEvent>create(sink -> {
                try (Scope ignored = parentOtelContext.makeCurrent()) {
                    MdcContextUtils.runWithContext(mdcContext, () -> {
                        try {
                            TenantUtils.execute(tenantId, () -> {
                                Consumer<TripAgentEvent> eventConsumer = sink::next;
                                tripAgentService.handleManagedMessage(conversationId, memberId, runId, content,
                                        eventConsumer);
                            });
                            sink.complete();
                        } catch (Exception e) {
                            sink.error(e);
                        }
                    });
                }
            }).subscribeOn(Schedulers.boundedElastic());
        });
    }

    private static Long parseConversationId(String threadId) {
        try {
            return Long.valueOf(threadId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("threadId 必须是旅行会话编号");
        }
    }

    private static List<Map<String, Object>> toAgUiEvents(TripAgentEvent event, String runId) {
        return switch (event.getEvent()) {
            case "stage" -> List.of(agUiActivitySnapshot(event, runId), agUiProgressCard(event, runId, "active"));
            // 模型增量是内部 JSON，不得作为用户可见文本或卡片数据透出。
            case "model_delta" -> List.of();
            case "question", "itinerary_skeleton" -> assistantMessageEvents(event, runId);
            default -> List.of(agUiCustom("trip_" + event.getEvent(), tripEventValue(event)));
        };
    }

    private static List<Map<String, Object>> assistantMessageEvents(TripAgentEvent event, String runId) {
        String messageId = event.getMessageId() == null ? "assistant-" + runId : String.valueOf(event.getMessageId());
        List<Map<String, Object>> result = new ArrayList<>();
        result.add(Map.of("type", "TEXT_MESSAGE_START", "messageId", messageId, "role", "assistant"));
        splitTextDeltas(event.getContent()).forEach(delta ->
                result.add(Map.of("type", "TEXT_MESSAGE_CONTENT", "messageId", messageId, "delta", delta)));
        result.add(Map.of("type", "TEXT_MESSAGE_END", "messageId", messageId));
        result.add(agUiCard(event, messageId));
        result.add(agUiCustom("trip_" + event.getEvent(), tripEventValue(event)));
        result.add(agUiProgressCard(event, runId, "completed"));
        return result;
    }

    private static Map<String, Object> agUiActivitySnapshot(TripAgentEvent event, String runId) {
        return Map.of("type", "ACTIVITY_SNAPSHOT", "messageId", "trip-progress-" + runId,
                "activityType", "TRIP_PROGRESS", "content", tripEventValue(event));
    }

    private static Map<String, Object> agUiProgressCard(TripAgentEvent event, String runId, String state) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("cardId", "trip-progress-" + runId);
        value.put("messageId", "trip-progress-" + runId);
        value.put("stage", event.getStage());
        value.put("content", event.getContent());
        value.put("state", state);
        return agUiCustom("trip_progress_card", value);
    }

    private static Map<String, Object> agUiCard(TripAgentEvent event, String messageId) {
        Map<String, Object> value = new LinkedHashMap<>(tripEventValue(event));
        String cardType = "question".equals(event.getEvent()) ? "trip_question_card" : "trip_itinerary_card";
        String cardIdPrefix = "question".equals(event.getEvent()) ? "trip-question-" : "trip-itinerary-";
        value.put("cardId", cardIdPrefix + messageId);
        value.put("messageId", messageId);
        return agUiCustom(cardType, value);
    }

    private static List<String> splitTextDeltas(String content) {
        if (StrUtil.isBlank(content)) {
            return List.of();
        }
        int[] codePoints = content.codePoints().toArray();
        List<String> result = new ArrayList<>((codePoints.length + TEXT_DELTA_MAX_CODE_POINTS - 1)
                / TEXT_DELTA_MAX_CODE_POINTS);
        for (int start = 0; start < codePoints.length; start += TEXT_DELTA_MAX_CODE_POINTS) {
            int length = Math.min(TEXT_DELTA_MAX_CODE_POINTS, codePoints.length - start);
            result.add(new String(codePoints, start, length));
        }
        return result;
    }

    private static Map<String, Object> agUiCustom(String name, Map<String, Object> value) {
        return Map.of("type", "CUSTOM", "name", name, "value", value);
    }

    private static Map<String, Object> tripEventValue(TripAgentEvent event) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stage", event.getStage());
        putIfNotNull(result, "messageId", event.getMessageId());
        putIfNotNull(result, "content", event.getContent());
        putIfNotNull(result, "sequence", event.getSequence());
        putIfNotNull(result, "itemType", event.getItemType());
        putIfNotNull(result, "item", event.getItem());
        putIfNotNull(result, "itinerary", event.getItinerary());
        putIfNotNull(result, "missingRequired", event.getMissingRequired());
        putIfNotNull(result, "inputCards", event.getInputCards());
        return result;
    }

    private static void putIfNotNull(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    private static AppTripChatMessageRespVO toMessage(UserItineraryConversationEventDO event) {
        AppTripChatMessageRespVO result = new AppTripChatMessageRespVO();
        result.setId(event.getId());
        result.setReplyId(event.getReplyEventId());
        result.setType(event.getRole());
        result.setContent(event.getContent());
        result.setCreateTime(event.getCreateTime());
        return result;
    }

    private static Map<String, Object> agUiRunStarted(String threadId, String runId) {
        return Map.of("type", "RUN_STARTED", "threadId", threadId, "runId", runId);
    }

    private static Map<String, Object> agUiRunFinished(String threadId, String runId, Long conversationId) {
        return Map.of("type", "RUN_FINISHED", "threadId", threadId, "runId", runId,
                "outcome", Map.of("type", "success"), "result", Map.of("conversationId", String.valueOf(conversationId)));
    }

    private static Map<String, Object> agUiRunError(String threadId, String runId) {
        return Map.of("type", "RUN_ERROR", "threadId", threadId, "runId", runId,
                "code", "TRIP_GENERATION_FAILED", "message", "暂时无法生成旅行方案，请稍后重试。");
    }

}
