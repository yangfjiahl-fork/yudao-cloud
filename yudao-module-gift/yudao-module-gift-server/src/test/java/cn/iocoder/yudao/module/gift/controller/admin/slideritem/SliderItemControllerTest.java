package cn.iocoder.yudao.module.gift.controller.admin.slideritem;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.controller.admin.slideritem.vo.SliderItemPageReqVO;
import cn.iocoder.yudao.module.gift.controller.admin.slideritem.vo.SliderItemRespVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.slider.SliderDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.slideritem.SliderItemDO;
import cn.iocoder.yudao.module.gift.service.slider.SliderService;
import cn.iocoder.yudao.module.gift.service.slideritem.SliderItemService;
import cn.iocoder.yudao.module.system.api.area.AreaApi;
import cn.iocoder.yudao.module.system.api.area.dto.AreaRespDTO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SliderItemControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SliderItemController controller;
    @Mock
    private SliderItemService sliderItemService;
    @Mock
    private SliderService sliderService;
    @Mock
    private AreaApi areaApi;

    @Test
    void getSliderItemPage_shouldFillSliderAndAreaFields() {
        SliderItemPageReqVO pageReqVO = new SliderItemPageReqVO();
        SliderItemDO sliderItem = new SliderItemDO();
        sliderItem.setId(1L);
        sliderItem.setSliderId(150L);
        when(sliderItemService.getSliderItemPage(pageReqVO))
                .thenReturn(new PageResult<>(List.of(sliderItem), 1L));

        SliderDO slider = new SliderDO();
        slider.setId(150L);
        slider.setPositionCode("HOME_TOP");
        slider.setCityId(110100L);
        when(sliderService.getSliderList(Set.of(150L))).thenReturn(List.of(slider));

        AreaRespDTO area = new AreaRespDTO().setId(110100L)
                .setProvinceName("北京市").setCityName("北京市");
        when(areaApi.getAreaList(Set.of(110100L))).thenReturn(success(List.of(area)));

        CommonResult<PageResult<SliderItemRespVO>> result = controller.getSliderItemPage(pageReqVO);

        SliderItemRespVO item = result.getData().getList().get(0);
        assertEquals("HOME_TOP", item.getPositionCode());
        assertEquals(110100L, item.getCityId());
        assertEquals("北京市", item.getProvinceName());
        assertEquals("北京市", item.getCityName());
        verify(sliderService).getSliderList(Set.of(150L));
        verify(areaApi).getAreaList(Set.of(110100L));
    }

}
