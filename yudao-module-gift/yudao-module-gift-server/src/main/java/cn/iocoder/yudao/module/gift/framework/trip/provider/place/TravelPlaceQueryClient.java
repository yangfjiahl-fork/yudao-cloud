package cn.iocoder.yudao.module.gift.framework.trip.provider.place;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 旅行地点查询的供应商协议。
 *
 * 酒店、餐厅复用同一地点搜索协议；新供应商只需实现本接口并注册为 Bean。
 */
public interface TravelPlaceQueryClient {

    String provider();

    Response query(Request request);

    enum PlaceType {
        HOTEL,
        RESTAURANT
    }

    @Data
    class Request {

        private PlaceType type;
        private String region;
        private Integer limit;
        /** 高德坐标系中心点，经度在前纬度在后；为空时使用城市文本搜索。 */
        private String location;
        /** 周边搜索半径，单位米。 */
        private Integer radius;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class Response {

        private Boolean success;
        private String code;
        private String message;
        private List<Place> places;

        public static Response failure(String message) {
            return new Response(false, null, message, List.of());
        }

    }

    @Data
    class Place {

        private String externalId;
        private String name;
        private String address;
        private String longitude;
        private String latitude;
        private String imageUrl;
        private String telephone;
        private String rating;
        private String cost;
        private String tag;

    }

}
