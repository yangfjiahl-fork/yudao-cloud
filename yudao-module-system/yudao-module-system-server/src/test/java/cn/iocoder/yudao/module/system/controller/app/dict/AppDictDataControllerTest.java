package cn.iocoder.yudao.module.system.controller.app.dict;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.system.controller.app.dict.vo.AppDictDataRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictDataDO;
import cn.iocoder.yudao.module.system.service.dict.DictDataService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AppDictDataControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppDictDataController controller;
    @Mock
    private DictDataService dictDataService;

    @Test
    void getDictDataListByType_allowed() {
        String type = "gift_user_feedback_category";
        DictDataDO dictData = new DictDataDO().setDictType(type).setLabel("地点图片").setValue("20");
        when(dictDataService.getDictDataList(CommonStatusEnum.ENABLE.getStatus(), type))
                .thenReturn(List.of(dictData));

        CommonResult<List<AppDictDataRespVO>> result = controller.getDictDataListByType(type);

        assertEquals(1, result.getData().size());
        assertEquals("20", result.getData().get(0).getValue());
        verify(dictDataService).getDictDataList(CommonStatusEnum.ENABLE.getStatus(), type);
    }

    @Test
    void getDictDataListByType_notAllowed() {
        CommonResult<List<AppDictDataRespVO>> result = controller.getDictDataListByType("private_dict");

        assertTrue(result.getData().isEmpty());
        verifyNoInteractions(dictDataService);
    }

}
