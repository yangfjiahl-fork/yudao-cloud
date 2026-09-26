package cn.iocoder.yudao.module.gift.controller.admin.useritinerary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.controller.admin.useritinerary.vo.UserItineraryPageReqVO;
import cn.iocoder.yudao.module.gift.controller.common.vo.UserItineraryPageRespVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import cn.iocoder.yudao.module.gift.service.useritinerary.UserItineraryService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserItineraryControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private UserItineraryController controller;
    @Mock
    private UserItineraryService userItineraryService;

    @Test
    void getUserItineraryPage_shouldReturnSharedCompactFields() {
        UserItineraryPageReqVO reqVO = new UserItineraryPageReqVO();
        UserItineraryDO itinerary = UserItineraryDO.builder()
                .id(1024L).memberId(288L).title("云南亲子六日游")
                .overviewDetail("详情字段不应出现在分页响应中").build();
        when(userItineraryService.getUserItineraryPage(reqVO))
                .thenReturn(new PageResult<>(List.of(itinerary), 1L));

        CommonResult<PageResult<UserItineraryPageRespVO>> result = controller.getUserItineraryPage(reqVO);

        assertEquals(0, result.getCode());
        assertEquals(1L, result.getData().getTotal());
        assertEquals(1024L, result.getData().getList().get(0).getId());
        assertEquals(288L, result.getData().getList().get(0).getMemberId());
        assertEquals("云南亲子六日游", result.getData().getList().get(0).getTitle());
        verify(userItineraryService).getUserItineraryPage(reqVO);
    }

}
