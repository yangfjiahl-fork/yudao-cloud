package cn.iocoder.yudao.module.gift.framework.trip.provider.weather.amap;

import lombok.Data;

import java.util.List;

/**
 * 高德地理编码响应 DTO
 */
@Data
class AmapGeocodeRespDTO {

    private String status;

    private String info;

    private String infocode;

    private List<Geocode> geocodes;

    @Data
    static class Geocode {

        private String adcode;

    }

}
