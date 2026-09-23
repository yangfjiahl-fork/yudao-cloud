package cn.iocoder.yudao.module.gift.controller.app.itinerary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryPageReqVO;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryRespVO;
import cn.iocoder.yudao.module.gift.controller.app.itinerarycategory.AppItineraryCategoryController;
import cn.iocoder.yudao.module.gift.controller.app.itinerarycategory.vo.AppItineraryCategoryRespVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerary.ItineraryDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerarycategory.ItineraryCategoryDO;
import cn.iocoder.yudao.module.gift.service.itinerary.ItineraryService;
import cn.iocoder.yudao.module.gift.service.itinerarycategory.ItineraryCategoryService;
import jakarta.annotation.security.PermitAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppItineraryControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppItineraryController itineraryController;
    @InjectMocks
    private AppItineraryCategoryController itineraryCategoryController;

    @Mock
    private ItineraryService itineraryService;
    @Mock
    private ItineraryCategoryService itineraryCategoryService;

    @Test
    void getItineraryCategoryList_shouldReturnCategories() {
        ItineraryCategoryDO category = ItineraryCategoryDO.builder()
                .id(1L).title("亲子游").icon("family").sort(10).build();
        when(itineraryCategoryService.getItineraryCategoryList()).thenReturn(List.of(category));

        CommonResult<List<AppItineraryCategoryRespVO>> result =
                itineraryCategoryController.getItineraryCategoryList();

        assertEquals(0, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals(category.getId(), result.getData().get(0).getId());
        assertEquals(category.getTitle(), result.getData().get(0).getTitle());
        verify(itineraryCategoryService).getItineraryCategoryList();
    }

    @Test
    void getItineraryPage_shouldFilterByCategoryAndKeepPagination() {
        AppItineraryPageReqVO reqVO = new AppItineraryPageReqVO();
        reqVO.setCategoryId(1L);
        reqVO.setPageNo(2);
        reqVO.setPageSize(10);
        ItineraryDO itinerary = ItineraryDO.builder()
                .id(11L).categoryId(1L).title("上海亲子游").sort(20).build();
        when(itineraryService.getItineraryPage(reqVO))
                .thenReturn(new PageResult<>(List.of(itinerary), 11L));

        CommonResult<PageResult<AppItineraryRespVO>> result = itineraryController.getItineraryPage(reqVO);

        assertEquals(0, result.getCode());
        assertEquals(11L, result.getData().getTotal());
        assertEquals(itinerary.getId(), result.getData().getList().get(0).getId());
        assertEquals(itinerary.getCategoryId(), result.getData().getList().get(0).getCategoryId());
        verify(itineraryService).getItineraryPage(reqVO);
    }

    @Test
    void endpointAuthentication_shouldMatchContract() throws NoSuchMethodException {
        Method categoryListMethod = AppItineraryCategoryController.class
                .getMethod("getItineraryCategoryList");
        Method itineraryPageMethod = AppItineraryController.class
                .getMethod("getItineraryPage", AppItineraryPageReqVO.class);

        assertNotNull(categoryListMethod.getAnnotation(PermitAll.class));
        assertTrue(categoryListMethod.isAnnotationPresent(PermitAll.class));
        assertFalse(itineraryPageMethod.isAnnotationPresent(PermitAll.class));
    }

}
