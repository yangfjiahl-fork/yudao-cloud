package cn.iocoder.yudao.module.gift.service.userfeedback;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.controller.app.userfeedback.vo.AppUserFeedbackCreateReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.userfeedback.UserFeedbackDO;
import cn.iocoder.yudao.module.gift.dal.mysql.userfeedback.UserFeedbackMapper;
import cn.iocoder.yudao.module.gift.enums.UserFeedbackStatusEnum;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.USER_FEEDBACK_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserFeedbackServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private UserFeedbackServiceImpl userFeedbackService;
    @Mock
    private UserFeedbackMapper userFeedbackMapper;

    @Test
    void testCreateUserFeedbackByMember() {
        Long memberId = 288L;
        AppUserFeedbackCreateReqVO createReqVO = new AppUserFeedbackCreateReqVO();
        createReqVO.setCategory(40);
        createReqVO.setContent("营业时间有误");
        createReqVO.setPoiId("B0FFG2M8Q4");
        createReqVO.setPoiName("西湖风景名胜区");

        userFeedbackService.createUserFeedback(memberId, createReqVO);

        ArgumentCaptor<UserFeedbackDO> captor = ArgumentCaptor.forClass(UserFeedbackDO.class);
        verify(userFeedbackMapper).insert(captor.capture());
        UserFeedbackDO userFeedback = captor.getValue();
        assertEquals(memberId, userFeedback.getMemberId());
        assertEquals(createReqVO.getCategory(), userFeedback.getCategory());
        assertEquals(createReqVO.getContent(), userFeedback.getContent());
        assertEquals(createReqVO.getPoiId(), userFeedback.getPoiId());
        assertEquals(createReqVO.getPoiName(), userFeedback.getPoiName());
        assertEquals("gaode", userFeedback.getPoiProvider());
        assertEquals(UserFeedbackStatusEnum.UNPROCESSED.getStatus(), userFeedback.getStatus());
    }

    @Test
    void testProcessUserFeedback() {
        Long id = 1L;
        String processRemark = "地点营业时间已经更新";
        when(userFeedbackMapper.selectById(id)).thenReturn(UserFeedbackDO.builder().id(id).build());

        userFeedbackService.processUserFeedback(id, processRemark);

        ArgumentCaptor<UserFeedbackDO> captor = ArgumentCaptor.forClass(UserFeedbackDO.class);
        verify(userFeedbackMapper).updateById(captor.capture());
        UserFeedbackDO updateObj = captor.getValue();
        assertEquals(id, updateObj.getId());
        assertEquals(UserFeedbackStatusEnum.PROCESSED.getStatus(), updateObj.getStatus());
        assertEquals(processRemark, updateObj.getProcessRemark());
    }

    @Test
    void testProcessUserFeedback_notExists() {
        Long id = 1L;
        when(userFeedbackMapper.selectById(id)).thenReturn(null);

        assertServiceException(() -> userFeedbackService.processUserFeedback(id, "处理说明"),
                USER_FEEDBACK_NOT_EXISTS);
    }

}
