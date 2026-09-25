package cn.iocoder.yudao.module.gift.service.slider;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.dal.dataobject.slider.SliderDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.slideritem.SliderItemDO;
import cn.iocoder.yudao.module.gift.dal.mysql.slider.SliderMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.slideritem.SliderItemMapper;
import cn.iocoder.yudao.module.gift.service.usercity.UserCityService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SliderServiceImplTest extends BaseMockitoUnitTest {

    private static final Long MEMBER_ID = 288L;
    private static final String POSITION_CODE = "HOME_TOP";

    @InjectMocks
    private SliderServiceImpl sliderService;
    @Mock
    private SliderMapper sliderMapper;
    @Mock
    private SliderItemMapper sliderItemMapper;
    @Mock
    private UserCityService userCityService;

    @Test
    void getSliderItemList_shouldReturnCurrentCityItems() {
        SliderDO citySlider = SliderDO.builder().id(1L).cityId(330100L).positionCode(POSITION_CODE).build();
        List<SliderItemDO> expected = List.of(SliderItemDO.builder().id(11L).sliderId(1L).sort(1).build());
        when(userCityService.getUserCity(MEMBER_ID))
                .thenReturn(new UserCityService.UserCity(330100L, "杭州市"));
        when(sliderMapper.selectByPositionCodeAndCityId(POSITION_CODE, 330100L)).thenReturn(citySlider);
        when(sliderItemMapper.selectListBySliderId(1L)).thenReturn(expected);

        List<SliderItemDO> result = sliderService.getSliderItemList(MEMBER_ID, POSITION_CODE);

        assertEquals(expected, result);
        verify(sliderMapper, never()).selectByPositionCodeAndCityId(POSITION_CODE, null);
    }

    @Test
    void getSliderItemList_shouldFallbackWhenCurrentCityHasNoConfiguration() {
        SliderDO defaultSlider = SliderDO.builder().id(2L).cityId(null).positionCode(POSITION_CODE).build();
        List<SliderItemDO> expected = List.of(SliderItemDO.builder().id(21L).sliderId(2L).sort(1).build());
        when(userCityService.getUserCity(MEMBER_ID))
                .thenReturn(new UserCityService.UserCity(330100L, "杭州市"));
        when(sliderMapper.selectByPositionCodeAndCityId(POSITION_CODE, 330100L)).thenReturn(null);
        when(sliderMapper.selectByPositionCodeAndCityId(POSITION_CODE, null)).thenReturn(defaultSlider);
        when(sliderItemMapper.selectListBySliderId(2L)).thenReturn(expected);

        List<SliderItemDO> result = sliderService.getSliderItemList(MEMBER_ID, POSITION_CODE);

        assertEquals(expected, result);
        verify(sliderMapper).selectByPositionCodeAndCityId(POSITION_CODE, 330100L);
        verify(sliderMapper).selectByPositionCodeAndCityId(POSITION_CODE, null);
    }

    @Test
    void getSliderItemList_shouldFallbackWhenCurrentCityConfigurationHasNoItems() {
        SliderDO citySlider = SliderDO.builder().id(1L).cityId(330100L).positionCode(POSITION_CODE).build();
        SliderDO defaultSlider = SliderDO.builder().id(2L).cityId(null).positionCode(POSITION_CODE).build();
        List<SliderItemDO> expected = List.of(SliderItemDO.builder().id(21L).sliderId(2L).sort(1).build());
        when(userCityService.getUserCity(MEMBER_ID))
                .thenReturn(new UserCityService.UserCity(330100L, "杭州市"));
        when(sliderMapper.selectByPositionCodeAndCityId(POSITION_CODE, 330100L)).thenReturn(citySlider);
        when(sliderItemMapper.selectListBySliderId(1L)).thenReturn(List.of());
        when(sliderMapper.selectByPositionCodeAndCityId(POSITION_CODE, null)).thenReturn(defaultSlider);
        when(sliderItemMapper.selectListBySliderId(2L)).thenReturn(expected);

        List<SliderItemDO> result = sliderService.getSliderItemList(MEMBER_ID, POSITION_CODE);

        assertEquals(expected, result);
    }

    @Test
    void getSliderItemList_shouldUseDefaultConfigurationWhenCurrentCityIsUnknown() {
        SliderDO defaultSlider = SliderDO.builder().id(2L).cityId(null).positionCode(POSITION_CODE).build();
        List<SliderItemDO> expected = List.of(SliderItemDO.builder().id(21L).sliderId(2L).sort(1).build());
        when(sliderMapper.selectByPositionCodeAndCityId(POSITION_CODE, null)).thenReturn(defaultSlider);
        when(sliderItemMapper.selectListBySliderId(2L)).thenReturn(expected);

        List<SliderItemDO> result = sliderService.getSliderItemList(MEMBER_ID, POSITION_CODE);

        assertEquals(expected, result);
        verify(userCityService).getUserCity(MEMBER_ID);
        verify(sliderMapper).selectByPositionCodeAndCityId(POSITION_CODE, null);
    }

}
