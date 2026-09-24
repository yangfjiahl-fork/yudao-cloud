package cn.iocoder.yudao.module.gift.service.itinerary;

import org.springframework.stereotype.Component;

/** 首版占位酒店查询；结果不表示真实价格、库存或可预订性。 */
@Component
public class MockItineraryHotelQueryClient implements ItineraryHotelQueryClient {

    @Override
    public HotelCandidate queryByCity(String city) {
        return new HotelCandidate("示例酒店（待接入供应商）", "", "mock-hotel-1");
    }

}
