package cn.iocoder.yudao.module.gift.controller.app.slider;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.dal.dataobject.slideritem.SliderItemDO;
import cn.iocoder.yudao.module.gift.service.slider.SliderService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppSliderControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppSliderController controller;
    @Mock
    private SliderService sliderService;

    @Test
    void getSliderItemList_shouldReturnCurrentUserItems() {
        Long memberId = 288L;
        SliderItemDO item = SliderItemDO.builder()
                .id(11L)
                .sliderId(1L)
                .imageUrl("https://example.com/banner.png")
                .imageWidth(750)
                .imageHeight(320)
                .sort(1)
                .jumpPage("SHARE")
                .jumpPageId(10L)
                .build();
        when(sliderService.getSliderItemList(memberId, "HOME_TOP")).thenReturn(List.of(item));

        try (MockedStatic<SecurityFrameworkUtils> securityMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(memberId);

            var result = controller.getSliderItemList("HOME_TOP");

            assertEquals(1, result.getData().size());
            assertEquals(11L, result.getData().get(0).getId());
            assertEquals("https://example.com/banner.png", result.getData().get(0).getImageUrl());
            assertEquals("SHARE", result.getData().get(0).getJumpPage());
            verify(sliderService).getSliderItemList(memberId, "HOME_TOP");
        }
    }

}
