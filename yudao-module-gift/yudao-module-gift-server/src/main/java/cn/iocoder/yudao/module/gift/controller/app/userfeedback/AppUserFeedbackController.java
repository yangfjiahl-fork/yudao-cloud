package cn.iocoder.yudao.module.gift.controller.app.userfeedback;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.gift.controller.app.userfeedback.vo.AppUserFeedbackCreateReqVO;
import cn.iocoder.yudao.module.gift.service.userfeedback.UserFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 用户反馈")
@RestController
@RequestMapping("/gift/user-feedback")
@Validated
public class AppUserFeedbackController {

    @Resource
    private UserFeedbackService userFeedbackService;

    @PostMapping("/create")
    @Operation(summary = "创建用户反馈")
    public CommonResult<Long> createUserFeedback(
            @Valid @RequestBody AppUserFeedbackCreateReqVO createReqVO) {
        return success(userFeedbackService.createUserFeedback(getLoginUserId(), createReqVO));
    }

}
