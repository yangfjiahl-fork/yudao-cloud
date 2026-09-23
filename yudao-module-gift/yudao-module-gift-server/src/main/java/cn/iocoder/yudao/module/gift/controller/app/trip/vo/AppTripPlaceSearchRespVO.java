package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import cn.iocoder.yudao.module.gift.framework.geo.core.AmapPlaceSearchClient;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "用户 APP - 高德地点搜索 Response VO")
@Data
public class AppTripPlaceSearchRespVO {

    @Schema(description = "坐标系", example = "GCJ-02")
    private String coordinateSystem;

    @Schema(description = "结果总数", example = "86")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Integer pageNo;

    @Schema(description = "每页数量", example = "20")
    private Integer pageSize;

    @Schema(description = "地点列表")
    private List<Place> list;

    public static AppTripPlaceSearchRespVO from(AmapPlaceSearchClient.SearchResult result,
                                                 Integer pageNo, Integer pageSize) {
        return new AppTripPlaceSearchRespVO().setCoordinateSystem("GCJ-02").setTotal(result.total())
                .setPageNo(pageNo).setPageSize(pageSize)
                .setList(result.places().stream().map(Place::from).toList());
    }

    @Schema(description = "用户 APP - 高德地点信息")
    @Data
    public static class Place {

        @Schema(description = "高德 POI 编号", example = "B0FFG2URKG")
        private String poiId;

        @Schema(description = "地点名称", example = "杭州西湖风景名胜区")
        private String name;

        @Schema(description = "地址", example = "浙江省杭州市西湖区龙井路1号")
        private String address;

        @Schema(description = "经度（高德 GCJ-02）", example = "120.155070")
        private BigDecimal longitude;

        @Schema(description = "纬度（高德 GCJ-02）", example = "30.274084")
        private BigDecimal latitude;

        @Schema(description = "高德地点类型", example = "风景名胜;风景名胜;世界遗产")
        private String type;

        @Schema(description = "高德地点类型编码", example = "110202")
        private String typeCode;

        @Schema(description = "省份", example = "浙江省")
        private String province;

        @Schema(description = "城市", example = "杭州市")
        private String city;

        @Schema(description = "区县", example = "西湖区")
        private String district;

        @Schema(description = "高德行政区编码", example = "330106")
        private String adcode;

        @Schema(description = "距中心点距离（米）；非周边搜索时为空", example = "328")
        private Long distanceMeters;

        @Schema(description = "联系电话", example = "0571-12345678")
        private String telephone;

        @Schema(description = "首张地点图片")
        private String photoUrl;

        private static Place from(AmapPlaceSearchClient.Place source) {
            return new Place().setPoiId(source.poiId()).setName(source.name()).setAddress(source.address())
                    .setLongitude(source.longitude()).setLatitude(source.latitude()).setType(source.type())
                    .setTypeCode(source.typeCode()).setProvince(source.province()).setCity(source.city())
                    .setDistrict(source.district()).setAdcode(source.adcode())
                    .setDistanceMeters(source.distanceMeters()).setTelephone(source.telephone())
                    .setPhotoUrl(source.photoUrl());
        }
    }

}
