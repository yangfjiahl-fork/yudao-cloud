package cn.iocoder.yudao.module.gift.controller.app.itinerary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryChatConversationCreateReqVO;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryChatConversationRespVO;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryChatConversationUpdateReqVO;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryChatCreateStreamRespVO;
import cn.iocoder.yudao.module.gift.service.itinerary.ItineraryPlanningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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
public class AppItineraryChatConversationController {

    @Resource
    private ItineraryPlanningService itineraryPlanningService;

    @PostMapping(value = "/create", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "创建旅行规划会话并流式返回引导语")
    public Flux<CommonResult<AppItineraryChatCreateStreamRespVO>> createConversation(
            @Valid @RequestBody(required = false) AppItineraryChatConversationCreateReqVO reqVO) {
        Long userId = getLoginUserId();
        ItineraryPlanningService.ConversationCreated created = itineraryPlanningService.createConversation(userId,
                reqVO != null ? reqVO.getProvinceId() : null, reqVO != null ? reqVO.getCityId() : null,
                reqVO != null ? reqVO.getDistrictId() : null);
        return Flux.just(success(new AppItineraryChatCreateStreamRespVO().setEvent("created")
                .setConversationId(created.conversationId()).setMessageId(created.messageId())
                .setContent(created.content())));
    }

    @PutMapping("/update")
    @Operation(summary = "更新旅行规划会话")
    public CommonResult<Boolean> updateConversation(@Valid @RequestBody AppItineraryChatConversationUpdateReqVO reqVO) {
        itineraryPlanningService.updateConversation(
                reqVO.getId(), getLoginUserId(), reqVO.getTitle(), reqVO.getPinned());
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得我的旅行规划会话列表")
    public CommonResult<List<AppItineraryChatConversationRespVO>> getConversationList() {
        return success(itineraryPlanningService.getConversations(getLoginUserId()).stream()
                .map(AppItineraryChatConversationController::toConversation).toList());
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除旅行规划会话")
    @Parameter(name = "id", required = true, description = "对话编号", example = "1024")
    public CommonResult<Boolean> deleteConversation(@RequestParam("id") Long id) {
        itineraryPlanningService.deleteConversation(id, getLoginUserId());
        return success(true);
    }

    private static AppItineraryChatConversationRespVO toConversation(ItineraryPlanningService.Conversation source) {
        AppItineraryChatConversationRespVO result = new AppItineraryChatConversationRespVO();
        result.setId(source.id());
        result.setTitle(source.title());
        result.setPinned(source.pinned());
        result.setProvinceId(source.provinceId());
        result.setCityId(source.cityId());
        result.setDistrictId(source.districtId());
        result.setCreateTime(source.createTime());
        return result;
    }

}
