package cn.iocoder.yudao.module.gift.framework.trip.provider.place;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TravelPlaceQueryClientFacadeTest {

    private ConfigApi configApi;
    private TravelPlaceQueryClient gaode;
    private TravelPlaceQueryClientFacade facade;

    @BeforeEach
    void setUp() {
        configApi = mock(ConfigApi.class);
        gaode = mock(TravelPlaceQueryClient.class);
        when(gaode.provider()).thenReturn("gaode");
        facade = new TravelPlaceQueryClientFacade(configApi, List.of(gaode));
    }

    @Test
    void queryHotelUsesHotelConfigKey() {
        TravelPlaceQueryClient.Response expected = TravelPlaceQueryClient.Response.failure("expected");
        when(configApi.getConfigValueByKey(TravelPlaceQueryClientFacade.HOTEL_QUERY_FROM_CONFIG_KEY))
                .thenReturn(CommonResult.success("gaode"));
        when(gaode.query(org.mockito.ArgumentMatchers.any())).thenReturn(expected);

        TravelPlaceQueryClient.Response result = facade.query(new TravelPlaceQueryClient.Request()
                .setType(TravelPlaceQueryClient.PlaceType.HOTEL).setRegion("杭州"));

        assertSame(expected, result);
        verify(configApi).getConfigValueByKey(TravelPlaceQueryClientFacade.HOTEL_QUERY_FROM_CONFIG_KEY);
        verify(configApi, never()).getConfigValueByKey(TravelPlaceQueryClientFacade.RESTAURANT_QUERY_FROM_CONFIG_KEY);
    }

    @Test
    void queryRestaurantUsesExistingResturantConfigKey() {
        TravelPlaceQueryClient.Response expected = TravelPlaceQueryClient.Response.failure("expected");
        when(configApi.getConfigValueByKey(TravelPlaceQueryClientFacade.RESTAURANT_QUERY_FROM_CONFIG_KEY))
                .thenReturn(CommonResult.success("gaode"));
        when(gaode.query(org.mockito.ArgumentMatchers.any())).thenReturn(expected);

        TravelPlaceQueryClient.Response result = facade.query(new TravelPlaceQueryClient.Request()
                .setType(TravelPlaceQueryClient.PlaceType.RESTAURANT).setRegion("杭州"));

        assertSame(expected, result);
        verify(configApi).getConfigValueByKey(TravelPlaceQueryClientFacade.RESTAURANT_QUERY_FROM_CONFIG_KEY);
    }

    @Test
    void missingConfigDefaultsToGaode() {
        TravelPlaceQueryClient.Response expected = TravelPlaceQueryClient.Response.failure("expected");
        when(configApi.getConfigValueByKey(TravelPlaceQueryClientFacade.HOTEL_QUERY_FROM_CONFIG_KEY))
                .thenReturn(CommonResult.success(null));
        when(gaode.query(org.mockito.ArgumentMatchers.any())).thenReturn(expected);

        assertSame(expected, facade.query(new TravelPlaceQueryClient.Request()
                .setType(TravelPlaceQueryClient.PlaceType.HOTEL).setRegion("杭州")));
    }

}
