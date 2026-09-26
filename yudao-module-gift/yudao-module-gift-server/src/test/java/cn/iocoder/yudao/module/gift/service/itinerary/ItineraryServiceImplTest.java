package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.controller.admin.itinerary.vo.ItineraryPageReqVO;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryCityPageReqVO;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryPageReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerary.ItineraryDO;
import cn.iocoder.yudao.module.gift.dal.mysql.itinerary.ItineraryMapper;
import cn.iocoder.yudao.module.gift.service.usercity.UserCityService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItineraryServiceImplTest extends BaseMockitoUnitTest {

    private static final Long MEMBER_ID = 288L;
    private static final Integer CITY_ID = 310100;

    @InjectMocks
    private ItineraryServiceImpl itineraryService;
    @Mock
    private ItineraryMapper itineraryMapper;
    @Mock
    private UserCityService userCityService;

    @Test
    void getItineraryPage_shouldUseSharedAppPageQuery() {
        AppItineraryPageReqVO reqVO = new AppItineraryPageReqVO();
        reqVO.setCategoryId(1L);
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        PageResult<ItineraryDO> expected = new PageResult<>(List.of(createItinerary()), 1L);
        when(itineraryMapper.selectPage(reqVO, null, reqVO.getCategoryId())).thenReturn(expected);

        PageResult<ItineraryDO> result = itineraryService.getItineraryPage(reqVO);

        assertEquals(expected, result);
        verify(itineraryMapper).selectPage(reqVO, null, reqVO.getCategoryId());
    }

    @Test
    void getItineraryExportPage_shouldUseFullFieldQuery() {
        ItineraryPageReqVO reqVO = new ItineraryPageReqVO();
        PageResult<ItineraryDO> expected = new PageResult<>(List.of(createItinerary()), 1L);
        when(itineraryMapper.selectExportPage(reqVO)).thenReturn(expected);

        PageResult<ItineraryDO> result = itineraryService.getItineraryExportPage(reqVO);

        assertEquals(expected, result);
        verify(itineraryMapper).selectExportPage(reqVO);
    }

    @Test
    void getItineraryCityPage_shouldUseRequestedCity() {
        AppItineraryCityPageReqVO reqVO = createReqVO(CITY_ID);
        PageResult<ItineraryDO> expected = new PageResult<>(List.of(createItinerary()), 1L);
        when(itineraryMapper.selectPage(reqVO, CITY_ID, reqVO.getCategoryId())).thenReturn(expected);

        PageResult<ItineraryDO> result = itineraryService.getItineraryCityPage(MEMBER_ID, reqVO);

        assertEquals(expected, result);
        verify(userCityService, never()).getUserCity(MEMBER_ID);
    }

    @Test
    void getItineraryCityPage_shouldUseCurrentMemberCity() {
        AppItineraryCityPageReqVO reqVO = createReqVO(null);
        PageResult<ItineraryDO> expected = new PageResult<>(List.of(createItinerary()), 1L);
        when(userCityService.getUserCity(MEMBER_ID))
                .thenReturn(new UserCityService.UserCity(CITY_ID.longValue(), "上海市"));
        when(itineraryMapper.selectPage(reqVO, CITY_ID, reqVO.getCategoryId())).thenReturn(expected);

        PageResult<ItineraryDO> result = itineraryService.getItineraryCityPage(MEMBER_ID, reqVO);

        assertEquals(expected, result);
        verify(itineraryMapper).selectPage(reqVO, CITY_ID, reqVO.getCategoryId());
    }

    @Test
    void getItineraryCityPage_shouldReturnEmptyWhenCurrentCityMissing() {
        AppItineraryCityPageReqVO reqVO = createReqVO(null);

        PageResult<ItineraryDO> result = itineraryService.getItineraryCityPage(MEMBER_ID, reqVO);

        assertEquals(0L, result.getTotal());
        assertEquals(List.of(), result.getList());
        verify(itineraryMapper, never()).selectPage(
                any(PageParam.class), any(Integer.class), any(Long.class));
    }

    private static AppItineraryCityPageReqVO createReqVO(Integer cityId) {
        AppItineraryCityPageReqVO reqVO = new AppItineraryCityPageReqVO();
        reqVO.setCityId(cityId);
        reqVO.setCategoryId(1L);
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private static ItineraryDO createItinerary() {
        return ItineraryDO.builder().id(11L).cityId(CITY_ID).categoryId(1L).title("上海亲子游").build();
    }

}
