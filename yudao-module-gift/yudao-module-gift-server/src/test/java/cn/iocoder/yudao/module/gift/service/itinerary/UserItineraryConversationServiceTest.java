package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.dal.mysql.useritinerary.UserItineraryMapper;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversation.UserItineraryConversationDO;
import cn.iocoder.yudao.module.gift.dal.mysql.useritineraryconversation.UserItineraryConversationMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserItineraryConversationServiceTest extends BaseMockitoUnitTest {

    @InjectMocks
    private UserItineraryConversationService service;
    @Mock
    private UserItineraryConversationMapper mapper;
    @Mock
    private UserItineraryMapper userItineraryMapper;

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

    @Test
    void deleteClearsUserItineraryConversationIdBeforeDeletingConversation() {
        Long conversationId = 10L;
        Long memberId = 288L;
        when(mapper.selectByIdAndMemberId(conversationId, memberId))
                .thenReturn(new UserItineraryConversationDO().setId(conversationId).setMemberId(memberId));

        service.delete(conversationId, memberId);

        InOrder inOrder = inOrder(mapper, userItineraryMapper);
        inOrder.verify(mapper).selectByIdAndMemberId(conversationId, memberId);
        inOrder.verify(userItineraryMapper).clearConversationId(conversationId, memberId);
        inOrder.verify(mapper).deleteById(conversationId);
    }

}
