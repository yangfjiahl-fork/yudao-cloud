package cn.iocoder.yudao.module.gift.controller.app.trip;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripChatConversationCreateReqVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripChatConversationRespVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripChatConversationUpdateReqVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripChatCreateStreamRespVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversationevent.UserItineraryConversationEventDO;
import cn.iocoder.yudao.module.gift.service.trip.ItineraryConversationService;
import cn.iocoder.yudao.module.gift.service.trip.TripAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 旅行规划会话")
@RestController
@RequestMapping("/ai/chat/conversation")
@Validated
@Slf4j
public class AppTripChatConversationController {

    private static final String TRAVEL_GUIDE_MESSAGE = "请告诉我出发地、目的地、出发日期、旅行天数、同行人数和预算，我来帮你规划旅程。";

    @Resource
    private ItineraryConversationService conversationService;
    @Resource
    private TripAgentService tripAgentService;

    @PostMapping(value = "/create", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "创建旅行规划会话并流式返回引导语")
    public Flux<CommonResult<AppTripChatCreateStreamRespVO>> createConversation(
            @Valid @RequestBody(required = false) AppTripChatConversationCreateReqVO reqVO) {
        Long userId = getLoginUserId();
        Long conversationId = conversationService.create(userId,
                reqVO != null ? reqVO.getProvinceId() : null, reqVO != null ? reqVO.getCityId() : null,
                reqVO != null ? reqVO.getDistrictId() : null);
        String defaultDeparture = tripAgentService.createTrip(conversationId, userId,
                reqVO != null ? reqVO.getProvinceId() : null, reqVO != null ? reqVO.getCityId() : null,
                reqVO != null ? reqVO.getDistrictId() : null);
        String content = StrUtil.isNotBlank(defaultDeparture)
                ? "已根据你所在位置暂定从" + defaultDeparture + "出发；如需修改可直接告诉我。" + TRAVEL_GUIDE_MESSAGE
                : TRAVEL_GUIDE_MESSAGE;
        UserItineraryConversationEventDO message = conversationService.createEvent(conversationId, null, null,
                "ASSISTANT_MESSAGE", "assistant", "WELCOME", content);
        log.info("[createConversation][conversationId({}) memberId({}) guideMessageId({}) 创建成功]",
                conversationId, userId, message.getId());
        return Flux.just(success(new AppTripChatCreateStreamRespVO().setEvent("created").setConversationId(conversationId)
                .setMessageId(message.getId()).setContent(content)));
    }

    @PutMapping("/update")
    @Operation(summary = "更新旅行规划会话")
    public CommonResult<Boolean> updateConversation(@Valid @RequestBody AppTripChatConversationUpdateReqVO reqVO) {
        conversationService.update(reqVO.getId(), getLoginUserId(), reqVO.getTitle(), reqVO.getPinned());
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得我的旅行规划会话列表")
    public CommonResult<List<AppTripChatConversationRespVO>> getConversationList() {
        return success(BeanUtils.toBean(conversationService.getList(getLoginUserId()),
                AppTripChatConversationRespVO.class));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除旅行规划会话")
    @Parameter(name = "id", required = true, description = "对话编号", example = "1024")
    public CommonResult<Boolean> deleteConversation(@RequestParam("id") Long id) {
        conversationService.delete(id, getLoginUserId());
        return success(true);
    }

}
