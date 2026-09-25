package cn.iocoder.yudao.module.gift.controller.app.userfeedback;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.controller.app.userfeedback.vo.AppUserFeedbackCreateReqVO;
import cn.iocoder.yudao.module.gift.service.userfeedback.UserFeedbackService;
import jakarta.annotation.security.PermitAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppUserFeedbackControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppUserFeedbackController controller;
    @Mock
    private UserFeedbackService userFeedbackService;

    @Test
    void createUserFeedback_shouldUseLoginMember() {
        Long memberId = 288L;
        Long feedbackId = 1L;
        AppUserFeedbackCreateReqVO createReqVO = new AppUserFeedbackCreateReqVO();
        createReqVO.setCategory(40);
        createReqVO.setContent("营业时间有误");
        when(userFeedbackService.createUserFeedback(memberId, createReqVO)).thenReturn(feedbackId);

        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(memberId);

            CommonResult<Long> result = controller.createUserFeedback(createReqVO);

            assertEquals(feedbackId, result.getData());
            verify(userFeedbackService).createUserFeedback(memberId, createReqVO);
        }
    }

    @Test
    void createEndpoint_shouldRequireLogin() throws NoSuchMethodException {
        Method createMethod = AppUserFeedbackController.class
                .getMethod("createUserFeedback", AppUserFeedbackCreateReqVO.class);

        assertNull(createMethod.getAnnotation(PermitAll.class));
    }

}
