package cn.iocoder.yudao.module.gift.controller.app.trip;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.api.chat.AiChatApi;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationCreateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatConversationUpdateReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageCreateAssistantReqDTO;
import cn.iocoder.yudao.module.ai.api.chat.dto.AiChatMessageRespDTO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripChatConversationCreateReqVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripChatConversationRespVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripChatConversationUpdateReqVO;
import cn.iocoder.yudao.module.gift.controller.app.trip.vo.AppTripChatCreateStreamRespVO;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
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
import java.util.concurrent.ThreadLocalRandom;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 旅行规划会话")
@RestController
@RequestMapping("/ai/chat/conversation")
@Validated
@Slf4j
public class AppTripChatConversationController {

    private static final String ENTRY_ROLE_ID_CONFIG_KEY = "trip.agent.composerRoleId";
    private static final String[] TRAVEL_GUIDE_MESSAGES = {
            "告诉我目的地、出行日期和同行人数，我来帮你规划旅程。",
            "还没想好去哪里也没关系，告诉我出发地、时间和偏好即可。",
            "说说你想玩几天、预算多少，我来帮你安排一段舒心的旅行。",
            "想看风景、吃美食还是轻松度假？告诉我你的旅行期待。",
            "把出发城市、旅行天数和同行人告诉我，我们开始定制行程吧。"
    };

    @Resource
    private AiChatApi aiChatApi;
    @Resource
    private ConfigApi configApi;
    @Resource
    private TripAgentService tripAgentService;

    @PostMapping(value = "/create", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "创建旅行规划会话并流式返回引导语")
    public Flux<CommonResult<AppTripChatCreateStreamRespVO>> createConversation(
            @Valid @RequestBody(required = false) AppTripChatConversationCreateReqVO reqVO) {
        Long userId = getLoginUserId();
        Integer userType = UserTypeEnum.MEMBER.getValue();
        AiChatConversationCreateReqDTO createReqDTO = new AiChatConversationCreateReqDTO();
        createReqDTO.setUserId(userId);
        createReqDTO.setUserType(userType);
        Long roleId = getEntryRoleId();
        createReqDTO.setRoleId(roleId);
        if (reqVO != null) {
            createReqDTO.setProvinceId(reqVO.getProvinceId());
            createReqDTO.setCityId(reqVO.getCityId());
            createReqDTO.setDistrictId(reqVO.getDistrictId());
        }
        Long conversationId = aiChatApi.createConversation(createReqDTO);
        tripAgentService.createTrip(conversationId, userId);
        String content = TRAVEL_GUIDE_MESSAGES[ThreadLocalRandom.current().nextInt(TRAVEL_GUIDE_MESSAGES.length)];
        AiChatMessageCreateAssistantReqDTO messageReqDTO = new AiChatMessageCreateAssistantReqDTO();
        messageReqDTO.setConversationId(conversationId);
        messageReqDTO.setUserId(userId);
        messageReqDTO.setUserType(userType);
        messageReqDTO.setContent(content);
        AiChatMessageRespDTO message = aiChatApi.createAssistantMessage(messageReqDTO);
        log.info("[createConversation][conversationId({}) memberId({}) roleId({}) guideMessageId({}) 创建成功]",
                conversationId, userId, roleId, message.getId());
        return Flux.just(success(new AppTripChatCreateStreamRespVO().setEvent("created").setConversationId(conversationId)
                .setMessageId(message.getId()).setContent(content)));
    }

    @PutMapping("/update")
    @Operation(summary = "更新旅行规划会话")
    public CommonResult<Boolean> updateConversation(@Valid @RequestBody AppTripChatConversationUpdateReqVO reqVO) {
        AiChatConversationUpdateReqDTO reqDTO = BeanUtils.toBean(reqVO, AiChatConversationUpdateReqDTO.class);
        reqDTO.setUserId(getLoginUserId());
        reqDTO.setUserType(UserTypeEnum.MEMBER.getValue());
        aiChatApi.updateConversation(reqDTO);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得我的旅行规划会话列表")
    public CommonResult<List<AppTripChatConversationRespVO>> getConversationList() {
        return success(BeanUtils.toBean(aiChatApi.getConversationList(getLoginUserId(), UserTypeEnum.MEMBER.getValue()),
                AppTripChatConversationRespVO.class));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除旅行规划会话")
    @Parameter(name = "id", required = true, description = "对话编号", example = "1024")
    public CommonResult<Boolean> deleteConversation(@RequestParam("id") Long id) {
        aiChatApi.deleteConversation(id, getLoginUserId(), UserTypeEnum.MEMBER.getValue());
        return success(true);
    }

    private Long getEntryRoleId() {
        String configValue = configApi.getConfigValueByKey(ENTRY_ROLE_ID_CONFIG_KEY).getCheckedData();
        if (StrUtil.isBlank(configValue)) {
            throw new IllegalStateException("系统配置 trip.agent.composerRoleId 未配置");
        }
        try {
            return Long.parseLong(configValue.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("系统配置 trip.agent.composerRoleId 必须是角色编号", e);
        }
    }

}
