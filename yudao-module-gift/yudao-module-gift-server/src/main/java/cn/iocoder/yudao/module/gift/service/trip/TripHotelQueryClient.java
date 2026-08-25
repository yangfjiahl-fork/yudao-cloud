package cn.iocoder.yudao.module.gift.service.trip;

/** 酒店候选查询的受控入口，后续可直接替换为供应商实现。 */
public interface TripHotelQueryClient {

    HotelCandidate queryByCity(String city);

    record HotelCandidate(String name, String imageUrl, String externalId) {
    }

}
