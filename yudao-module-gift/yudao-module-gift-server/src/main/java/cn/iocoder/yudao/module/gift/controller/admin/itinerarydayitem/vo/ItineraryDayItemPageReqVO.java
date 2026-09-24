package cn.iocoder.yudao.module.gift.controller.admin.itinerarydayitem.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 通用行程节点分页 Request VO")
@Data
public class ItineraryDayItemPageReqVO extends PageParam {

    @Schema(description = "通用行程ID", example = "26903")
    private Long itineraryId;

    @Schema(description = "通用行程日程ID", example = "6501")
    private Long itineraryDayId;

    @Schema(description = "节点类型", example = "2")
    private String type;

    @Schema(description = "节点时段")
    private String slot;

    @Schema(description = "节点标题")
    private String title;

    @Schema(description = "节点副标题")
    private String subTitle;

    @Schema(description = "节点描述", example = "你说的对")
    private String description;

    @Schema(description = "当日节点排序值")
    private Integer sort;

    @Schema(description = "计划开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalTime[] startTime;

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

    @Schema(description = "高德地图坐标")
    private String gdPosition;

    @Schema(description = "营业时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private String[] businessTime;

    @Schema(description = "详细地址")
    private String addressDetail;

    @Schema(description = "联系电话")
    private String phoneNo;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
