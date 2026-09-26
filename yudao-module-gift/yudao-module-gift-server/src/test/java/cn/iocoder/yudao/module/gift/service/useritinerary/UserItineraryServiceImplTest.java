package cn.iocoder.yudao.module.gift.service.useritinerary;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.controller.admin.useritinerary.vo.UserItineraryPageReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import cn.iocoder.yudao.module.gift.dal.mysql.useritinerary.UserItineraryMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.USER_ITINERARY_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserItineraryServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private UserItineraryServiceImpl userItineraryService;
    @Mock
    private UserItineraryMapper userItineraryMapper;

    @Test
    void getUserItineraryPage_shouldFilterByMemberId() {
        Long memberId = 288L;
        PageParam pageReqVO = new PageParam().setPageNo(1).setPageSize(10);
        PageResult<UserItineraryDO> expected = new PageResult<>(
                List.of(UserItineraryDO.builder().id(1024L).memberId(memberId).build()), 1L);
        when(userItineraryMapper.selectPageByMemberId(pageReqVO, memberId)).thenReturn(expected);

        PageResult<UserItineraryDO> result = userItineraryService.getUserItineraryPage(memberId, pageReqVO);

        assertEquals(expected, result);
        verify(userItineraryMapper).selectPageByMemberId(pageReqVO, memberId);
    }

    @Test
    void getUserItineraryExportPage_shouldUseFullFieldQuery() {
        UserItineraryPageReqVO pageReqVO = new UserItineraryPageReqVO();
        PageResult<UserItineraryDO> expected = new PageResult<>(
                List.of(UserItineraryDO.builder().id(1024L).memberId(288L).build()), 1L);
        when(userItineraryMapper.selectExportPage(pageReqVO)).thenReturn(expected);

        PageResult<UserItineraryDO> result = userItineraryService.getUserItineraryExportPage(pageReqVO);

        assertEquals(expected, result);
        verify(userItineraryMapper).selectExportPage(pageReqVO);
    }

    @Test
    void getUserItinerary_shouldFilterByMemberId() {
        UserItineraryDO expected = UserItineraryDO.builder().id(1024L).memberId(288L).build();
        when(userItineraryMapper.selectByIdAndMemberId(1024L, 288L)).thenReturn(expected);

        UserItineraryDO result = userItineraryService.getUserItinerary(288L, 1024L);

        assertEquals(expected, result);
        verify(userItineraryMapper).selectByIdAndMemberId(1024L, 288L);
    }

    @Test
    void getUserItinerary_shouldRejectMissingOrForeignItinerary() {
        when(userItineraryMapper.selectByIdAndMemberId(1024L, 288L)).thenReturn(null);

        assertServiceException(() -> userItineraryService.getUserItinerary(288L, 1024L),
                USER_ITINERARY_NOT_EXISTS);
    }

    @Test
    void deleteUserItinerary_shouldFilterByMemberId() {
        when(userItineraryMapper.deleteByIdAndMemberId(1024L, 288L)).thenReturn(1);

        userItineraryService.deleteUserItinerary(288L, 1024L);

        verify(userItineraryMapper).deleteByIdAndMemberId(1024L, 288L);
    }

    @Test
    void deleteUserItinerary_shouldRejectMissingOrForeignItinerary() {
        when(userItineraryMapper.deleteByIdAndMemberId(1024L, 288L)).thenReturn(0);

        assertServiceException(() -> userItineraryService.deleteUserItinerary(288L, 1024L),
                USER_ITINERARY_NOT_EXISTS);
    }

}
