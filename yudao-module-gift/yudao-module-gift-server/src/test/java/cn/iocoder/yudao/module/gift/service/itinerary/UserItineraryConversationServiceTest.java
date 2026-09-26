package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversation.UserItineraryConversationDO;
import cn.iocoder.yudao.module.gift.dal.mysql.useritineraryconversation.UserItineraryConversationMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserItineraryConversationServiceTest extends BaseMockitoUnitTest {

    @InjectMocks
    private UserItineraryConversationService service;
    @Mock
    private UserItineraryConversationMapper mapper;

    @Test
    void getPageFiltersByMemberId() {
        Long memberId = 288L;
        PageParam pageReqVO = new PageParam().setPageNo(1).setPageSize(10);
        PageResult<UserItineraryConversationDO> expected = new PageResult<>(
                List.of(new UserItineraryConversationDO().setId(10L).setMemberId(memberId)), 1L);
        when(mapper.selectPageByMemberId(pageReqVO, memberId)).thenReturn(expected);

        PageResult<UserItineraryConversationDO> result = service.getPage(memberId, pageReqVO);

        assertEquals(expected, result);
        verify(mapper).selectPageByMemberId(pageReqVO, memberId);
    }

}
