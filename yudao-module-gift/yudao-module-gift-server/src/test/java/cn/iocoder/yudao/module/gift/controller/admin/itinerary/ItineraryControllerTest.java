package cn.iocoder.yudao.module.gift.controller.admin.itinerary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.controller.admin.itinerary.vo.ItineraryPageReqVO;
import cn.iocoder.yudao.module.gift.controller.common.vo.ItineraryPageRespVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerary.ItineraryDO;
import cn.iocoder.yudao.module.gift.service.itinerary.ItineraryService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItineraryControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ItineraryController controller;
    @Mock
    private ItineraryService itineraryService;

    @Test
    void getItineraryPage_shouldReturnSharedCompactFields() {
        ItineraryPageReqVO reqVO = new ItineraryPageReqVO();
        ItineraryDO itinerary = ItineraryDO.builder()
                .id(11L).cityId(310100).categoryId(1L).title("上海亲子游")
                .description("详情字段不应出现在分页响应中").build();
        when(itineraryService.getItineraryPage(reqVO))
                .thenReturn(new PageResult<>(List.of(itinerary), 1L));

        CommonResult<PageResult<ItineraryPageRespVO>> result = controller.getItineraryPage(reqVO);

        assertEquals(0, result.getCode());
        assertEquals(1L, result.getData().getTotal());
        assertEquals(11L, result.getData().getList().get(0).getId());
        assertEquals("上海亲子游", result.getData().getList().get(0).getTitle());
        verify(itineraryService).getItineraryPage(reqVO);
    }

}
