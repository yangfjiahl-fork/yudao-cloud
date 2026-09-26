package cn.iocoder.yudao.module.gift.controller.admin.itinerarydayitem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 通用行程节点 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ItineraryDayItemRespVO {

    @Schema(description = "通用行程节点ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "14712")
    @ExcelProperty("通用行程节点ID")
    private Long id;

    @Schema(description = "通用行程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26903")
    @ExcelProperty("通用行程ID")
    private Long itineraryId;

    @Schema(description = "通用行程日程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6501")
    @ExcelProperty("通用行程日程ID")
    private Long itineraryDayId;

    @Schema(description = "节点类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("节点类型")
    private String type;

    @Schema(description = "节点时段")
    @ExcelProperty("节点时段")
    private String slot;

    @Schema(description = "节点标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("节点标题")
    private String title;

    @Schema(description = "节点副标题")
    @ExcelProperty("节点副标题")
    private String subTitle;

    @Schema(description = "节点描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "你说的对")
    @ExcelProperty("节点描述")
    private String description;

    @Schema(description = "当日节点排序值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("当日节点排序值")
    private Integer sort;

    @Schema(description = "计划开始时间")
    @ExcelProperty("计划开始时间")
    private LocalTime startTime;

    @Schema(description = "建议停留分钟数")
    @ExcelProperty("建议停留分钟数")
    private Integer durationMinutes;

    @Schema(description = "POI供应商地点ID", example = "21698")
    @ExcelProperty("POI供应商地点ID")
    private String poiId;

    @Schema(description = "POI省级区域ID", example = "8932")
    @ExcelProperty("POI省级区域ID")
    private Integer provinceId;

    @Schema(description = "POI城市ID", example = "10294")
    @ExcelProperty("POI城市ID")
    private Integer cityId;

    @Schema(description = "POI区县ID", example = "16850")
    @ExcelProperty("POI区县ID")
    private Integer districtId;

    @Schema(description = "POI经度")
    @ExcelProperty("POI经度")
    private BigDecimal longitude;

    @Schema(description = "POI纬度")
    @ExcelProperty("POI纬度")
    private BigDecimal latitude;

    @Schema(description = "封面图地址", example = "https://www.iocoder.cn")
    @ExcelProperty("封面图地址")
    private String coverUrl;

    @Schema(description = "封面图宽度")
    @ExcelProperty("封面图宽度")
    private Integer coverWidth;

    @Schema(description = "封面图高度")
    @ExcelProperty("封面图高度")
    private Integer coverHeight;

    @Schema(description = "图片地址集合")
    @ExcelProperty("图片地址集合")
    private String picUrls;

    @Schema(description = "图片尺寸集合")
    @ExcelProperty("图片尺寸集合")
    private String picSizes;

    @Schema(description = "标签集合")
    @ExcelProperty("标签集合")
    private String tags;

    @Schema(description = "营业时间")
    @ExcelProperty("营业时间")
    private String businessTime;

    @Schema(description = "详细地址")
    @ExcelProperty("详细地址")
    private String addressDetail;

    @Schema(description = "联系电话")
    @ExcelProperty("联系电话")
    private String phoneNo;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
