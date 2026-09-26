package cn.iocoder.yudao.module.gift.controller.app.itinerary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryChatConversationRespVO;
import cn.iocoder.yudao.module.gift.service.itinerary.ItineraryPlanningService;
import jakarta.annotation.security.PermitAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppItineraryChatConversationControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppItineraryChatConversationController controller;
    @Mock
    private ItineraryPlanningService itineraryPlanningService;

    @Test
    void getConversationListReturnsCurrentMemberPage() {
        Long memberId = 288L;
        PageParam pageReqVO = new PageParam().setPageNo(1).setPageSize(10);
        ItineraryPlanningService.Conversation conversation = new ItineraryPlanningService.Conversation(
                10L, "云南亲子游", true, 530000L, 530100L, null,
                LocalDateTime.of(2026, 9, 26, 10, 0));
        when(itineraryPlanningService.getConversations(memberId, pageReqVO))
                .thenReturn(new PageResult<>(List.of(conversation), 1L));

        try (MockedStatic<SecurityFrameworkUtils> securityMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(memberId);

            CommonResult<PageResult<AppItineraryChatConversationRespVO>> result =
                    controller.getConversationList(pageReqVO);

            assertEquals(0, result.getCode());
            assertEquals(1L, result.getData().getTotal());
            assertEquals(10L, result.getData().getList().get(0).getId());
            assertEquals("云南亲子游", result.getData().getList().get(0).getTitle());
            verify(itineraryPlanningService).getConversations(memberId, pageReqVO);
        }
    }

    @Test
    void getConversationListRequiresLoginAndKeepsPath() throws NoSuchMethodException {
        Method method = AppItineraryChatConversationController.class
                .getMethod("getConversationList", PageParam.class);

        assertFalse(method.isAnnotationPresent(PermitAll.class));
        assertArrayEquals(new String[]{"/list"}, method.getAnnotation(GetMapping.class).value());
    }

}
