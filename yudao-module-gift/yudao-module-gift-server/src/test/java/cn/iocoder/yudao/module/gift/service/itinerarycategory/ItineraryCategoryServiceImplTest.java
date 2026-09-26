package cn.iocoder.yudao.module.gift.service.itinerarycategory;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerarycategory.ItineraryCategoryDO;
import cn.iocoder.yudao.module.gift.dal.mysql.itinerarycategory.ItineraryCategoryMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItineraryCategoryServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ItineraryCategoryServiceImpl itineraryCategoryService;
    @Mock
    private ItineraryCategoryMapper itineraryCategoryMapper;

    @Test
    void getItineraryCategoryListReturnsEnabledCategories() {
        List<ItineraryCategoryDO> categories = List.of(ItineraryCategoryDO.builder()
                .id(1L)
                .title("亲子游")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build());
        when(itineraryCategoryMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(categories);

        List<ItineraryCategoryDO> result = itineraryCategoryService.getItineraryCategoryList();

        assertSame(categories, result);
        verify(itineraryCategoryMapper).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
