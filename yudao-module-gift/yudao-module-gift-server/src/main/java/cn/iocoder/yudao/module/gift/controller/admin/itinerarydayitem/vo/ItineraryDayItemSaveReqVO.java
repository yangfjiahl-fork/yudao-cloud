package cn.iocoder.yudao.module.gift.controller.admin.itinerarydayitem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalTime;

@Schema(description = "管理后台 - 通用行程节点新增/修改 Request VO")
@Data
public class ItineraryDayItemSaveReqVO {

    @Schema(description = "通用行程节点ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "14712")
    private Long id;

    @Schema(description = "通用行程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26903")
    @NotNull(message = "通用行程ID不能为空")
    private Long itineraryId;

    @Schema(description = "通用行程日程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6501")
    @NotNull(message = "通用行程日程ID不能为空")
    private Long itineraryDayId;

    @Schema(description = "节点类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "节点类型不能为空")
    private String type;

    @Schema(description = "节点时段")
    private String slot;

    @Schema(description = "节点标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "节点标题不能为空")
    private String title;

    @Schema(description = "节点副标题")
    private String subTitle;

    @Schema(description = "节点描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "你说的对")
    @NotEmpty(message = "节点描述不能为空")
    private String description;

    @Schema(description = "当日节点排序值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "当日节点排序值不能为空")
    private Integer sort;

    @Schema(description = "计划开始时间")
    private LocalTime startTime;

    @Schema(description = "建议停留分钟数")
    private Integer durationMinutes;

    @Schema(description = "POI供应商地点ID", example = "21698")
    private String poiId;

    @Schema(description = "POI省级区域ID", example = "8932")
    private Integer provinceId;

    @Schema(description = "POI城市ID", example = "10294")
    private Integer cityId;

    @Schema(description = "POI区县ID", example = "16850")
    private Integer districtId;

    @Schema(description = "POI经度")
    private BigDecimal longitude;

    @Schema(description = "POI纬度")
    private BigDecimal latitude;

    @Schema(description = "封面图地址", example = "https://www.iocoder.cn")
    private String coverUrl;

    @Schema(description = "封面图宽度")
    private Integer coverWidth;

    @Schema(description = "封面图高度")
    private Integer coverHeight;

    @Schema(description = "图片地址集合")
    private String picUrls;

    @Schema(description = "图片尺寸集合")
    private String picSizes;

    @Schema(description = "标签集合")
    private String tags;

    @Schema(description = "营业时间")
    private String businessTime;

    @Schema(description = "详细地址")
    private String addressDetail;

    @Schema(description = "联系电话")
    private String phoneNo;

}
