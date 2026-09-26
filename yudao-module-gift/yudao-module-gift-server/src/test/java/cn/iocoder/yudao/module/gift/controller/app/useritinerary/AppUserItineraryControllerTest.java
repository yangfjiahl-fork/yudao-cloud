package cn.iocoder.yudao.module.gift.controller.app.useritinerary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.controller.app.useritinerary.vo.AppUserItineraryRespVO;
import cn.iocoder.yudao.module.gift.controller.common.vo.UserItineraryPageRespVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import cn.iocoder.yudao.module.gift.service.useritinerary.UserItineraryService;
import jakarta.annotation.security.PermitAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppUserItineraryControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppUserItineraryController controller;
    @Mock
    private UserItineraryService userItineraryService;

    @Test
    void getUserItineraryPage_shouldUseCurrentMember() {
        Long memberId = 288L;
        PageParam pageReqVO = new PageParam().setPageNo(1).setPageSize(10);
        UserItineraryDO itinerary = UserItineraryDO.builder()
                .id(1024L)
                .memberId(memberId)
                .conversationId(2048L)
                .title("云南亲子六日游")
                .startDate(LocalDate.of(2026, 10, 1))
                .dayCnt(6)
                .build();
        when(userItineraryService.getUserItineraryPage(memberId, pageReqVO))
                .thenReturn(new PageResult<>(List.of(itinerary), 1L));

        try (MockedStatic<SecurityFrameworkUtils> securityMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(memberId);

            CommonResult<PageResult<UserItineraryPageRespVO>> result = controller.getUserItineraryPage(pageReqVO);

            assertEquals(0, result.getCode());
            assertEquals(1L, result.getData().getTotal());
            assertEquals(1024L, result.getData().getList().get(0).getId());
            assertEquals(memberId, result.getData().getList().get(0).getMemberId());
            assertEquals("云南亲子六日游", result.getData().getList().get(0).getTitle());
            verify(userItineraryService).getUserItineraryPage(memberId, pageReqVO);
        }
    }

    @Test
    void getUserItinerary_shouldUseCurrentMemberAndReturnDetail() {
        Long memberId = 288L;
        UserItineraryDO itinerary = UserItineraryDO.builder()
                .id(1024L).memberId(memberId).title("云南亲子六日游")
                .travelerCount(4).budget(20_000).hotelBudget(5_000).build();
        when(userItineraryService.getUserItinerary(memberId, 1024L)).thenReturn(itinerary);

        try (MockedStatic<SecurityFrameworkUtils> securityMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(memberId);

            CommonResult<AppUserItineraryRespVO> result = controller.getUserItinerary(1024L);

            assertEquals(0, result.getCode());
            assertEquals("云南亲子六日游", result.getData().getTitle());
            assertEquals(4, result.getData().getTravelerCount());
            assertEquals(20_000, result.getData().getBudget());
            verify(userItineraryService).getUserItinerary(memberId, 1024L);
        }
    }

    @Test
    void getUserItineraryPage_shouldRequireLogin() throws NoSuchMethodException {
        Method pageMethod = AppUserItineraryController.class.getMethod("getUserItineraryPage", PageParam.class);
        Method detailMethod = AppUserItineraryController.class.getMethod("getUserItinerary", Long.class);

        assertFalse(pageMethod.isAnnotationPresent(PermitAll.class));
        assertFalse(detailMethod.isAnnotationPresent(PermitAll.class));
    }

    @Test
    void deleteUserItinerary_shouldUseCurrentMember() {
        Long memberId = 288L;
        Long itineraryId = 1024L;

        try (MockedStatic<SecurityFrameworkUtils> securityMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(memberId);

            CommonResult<Boolean> result = controller.deleteUserItinerary(itineraryId);

            assertEquals(0, result.getCode());
            assertEquals(Boolean.TRUE, result.getData());
            verify(userItineraryService).deleteUserItinerary(memberId, itineraryId);
        }
    }

    @Test
    void deleteUserItinerary_shouldRequireLogin() throws NoSuchMethodException {
        Method method = AppUserItineraryController.class.getMethod("deleteUserItinerary", Long.class);

        assertFalse(method.isAnnotationPresent(PermitAll.class));
    }

}
