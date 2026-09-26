package cn.iocoder.yudao.module.gift.controller.app.itinerary.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "用户 APP - 高德地点搜索 Request VO")
@Data
public class AppItineraryPlaceSearchReqVO {

    @Schema(description = "搜索关键词；与 category 至少提供一个", example = "咖啡")
    @Size(max = 80, message = "搜索关键词不能超过 80 个字符")
    private String keyword;

    @Schema(description = "POI 分类：sightseeing/hotel/food/shopping；与 keyword 至少提供一个", example = "food")
    @Size(max = 20, message = "POI 分类不能超过 20 个字符")
    private String category;

    @Schema(description = "城市行政区编号；未提供经纬度时必填", example = "330100")
    @Positive(message = "城市编号必须大于 0")
    private Long cityId;

    @Schema(description = "中心点经度（高德 GCJ-02）；需与纬度同时提供", example = "120.155070")
    @DecimalMin(value = "-180", message = "经度必须在 -180 到 180 之间")
    @DecimalMax(value = "180", message = "经度必须在 -180 到 180 之间")
    @Digits(integer = 3, fraction = 6, message = "经度最多保留 6 位小数")
    private BigDecimal longitude;

    @Schema(description = "中心点纬度（高德 GCJ-02）；需与经度同时提供", example = "30.274084")
    @DecimalMin(value = "-90", message = "纬度必须在 -90 到 90 之间")
    @DecimalMax(value = "90", message = "纬度必须在 -90 到 90 之间")
    @Digits(integer = 2, fraction = 6, message = "纬度最多保留 6 位小数")
    private BigDecimal latitude;

    @Schema(description = "周边搜索半径（米）；按城市搜索时忽略", example = "5000", defaultValue = "5000")
    @Min(value = 1, message = "搜索半径不能小于 1 米")
    @Max(value = 50000, message = "搜索半径不能超过 50000 米")
    private Integer radius = 5000;

    @Schema(description = "页码", example = "1", defaultValue = "1")
    @Min(value = 1, message = "页码不能小于 1")
    @Max(value = 100, message = "页码不能超过 100")
    private Integer pageNo = 1;

    @Schema(description = "每页数量", example = "20", defaultValue = "20")
    @Min(value = 1, message = "每页数量不能小于 1")
    @Max(value = 25, message = "每页数量不能超过 25")
    private Integer pageSize = 20;

    @AssertTrue(message = "搜索关键词和 POI 分类至少提供一个")
    @Schema(hidden = true)
    public boolean isSearchConditionPresent() {
        return !isBlank(keyword) || !isBlank(category);
    }

    @AssertTrue(message = "经纬度必须同时提供")
    @Schema(hidden = true)
    public boolean isCoordinateComplete() {
        return (longitude == null) == (latitude == null);
    }

    @AssertTrue(message = "经纬度和城市编号至少提供一个")
    @Schema(hidden = true)
    public boolean isSearchScopePresent() {
        return cityId != null || longitude != null && latitude != null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
