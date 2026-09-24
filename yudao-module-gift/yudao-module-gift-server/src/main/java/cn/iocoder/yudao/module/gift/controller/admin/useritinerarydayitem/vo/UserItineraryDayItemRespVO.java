package cn.iocoder.yudao.module.gift.controller.admin.useritinerarydayitem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 用户行程节点 Response VO")
@Data
@ExcelIgnoreUnannotated
public class UserItineraryDayItemRespVO {

    @Schema(description = "用户行程节点记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19593")
    @ExcelProperty("用户行程节点记录ID")
    private Long id;

    @Schema(description = "用户行程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12729")
    @ExcelProperty("用户行程ID")
    private Long userItineraryId;

    @Schema(description = "用户行程日程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "14303")
    @ExcelProperty("用户行程日程ID")
    private Long userItineraryDayId;

    @Schema(description = "行程节点业务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28898")
    @ExcelProperty("行程节点业务ID")
    private String itemId;

    @Schema(description = "所属行程天数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("所属行程天数")
    private Integer day;

    @Schema(description = "节点类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("节点类型")
    private String type;

    @Schema(description = "节点时段", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("节点时段")
    private String slot;

    @Schema(description = "节点展示标签")
    @ExcelProperty("节点展示标签")
    private String label;

    @Schema(description = "当日节点排序值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("当日节点排序值")
    private Integer sort;

    @Schema(description = "计划开始时间")
    @ExcelProperty("计划开始时间")
    private LocalTime startTime;

    @Schema(description = "计划结束时间")
    @ExcelProperty("计划结束时间")
    private LocalTime endTime;

    @Schema(description = "建议停留分钟数")
    @ExcelProperty("建议停留分钟数")
    private Integer durationMinutes;

    @Schema(description = "POI供应商地点ID", example = "3188")
    @ExcelProperty("POI供应商地点ID")
    private String poiId;

    @Schema(description = "POI名称", example = "张三")
    @ExcelProperty("POI名称")
    private String poiName;

    @Schema(description = "POI省级区域ID", example = "12321")
    @ExcelProperty("POI省级区域ID")
    private Integer provinceId;

    @Schema(description = "POI城市ID", example = "22808")
    @ExcelProperty("POI城市ID")
    private Integer cityId;

    @Schema(description = "POI区县ID", example = "30598")
    @ExcelProperty("POI区县ID")
    private Integer districtId;

    @Schema(description = "POI城市名称")
    @ExcelProperty("POI城市名称")
    private String city;

    @Schema(description = "POI所在区域")
    @ExcelProperty("POI所在区域")
    private String area;

    @Schema(description = "POI详细地址")
    @ExcelProperty("POI详细地址")
    private String addressDetail;

    @Schema(description = "POI经度")
    @ExcelProperty("POI经度")
    private BigDecimal longitude;

    @Schema(description = "POI纬度")
    @ExcelProperty("POI纬度")
    private BigDecimal latitude;

    @Schema(description = "坐标系")
    @ExcelProperty("坐标系")
    private String coordinateSystem;

    @Schema(description = "营业时间")
    @ExcelProperty("营业时间")
    private String businessHours;

    @Schema(description = "联系电话")
    @ExcelProperty("联系电话")
    private String phoneNo;

    @Schema(description = "封面图地址", example = "https://www.iocoder.cn")
    @ExcelProperty("封面图地址")
    private String coverUrl;

    @Schema(description = "评分")
    @ExcelProperty("评分")
    private BigDecimal rating;

    @Schema(description = "预计花费")
    @ExcelProperty("预计花费")
    private BigDecimal cost;

    @Schema(description = "标签JSON")
    @ExcelProperty("标签JSON")
    private String tagsJson;

    @Schema(description = "节点骨架文案")
    @ExcelProperty("节点骨架文案")
    private String skeleton;

    @Schema(description = "节点详细文案")
    @ExcelProperty("节点详细文案")
    private String detail;

    @Schema(description = "节点内容状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("节点内容状态")
    private String status;

    @Schema(description = "节点解析状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("节点解析状态")
    private Integer resolveStatus;

    @Schema(description = "节点排程状态", example = "2")
    @ExcelProperty("节点排程状态")
    private String planningStatus;

    @Schema(description = "POI校验状态", example = "1")
    @ExcelProperty("POI校验状态")
    private String poiVerificationStatus;

    @Schema(description = "是否必去", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否必去")
    private Boolean mustVisit;

    @Schema(description = "是否锁定", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否锁定")
    private Boolean locked;

    @Schema(description = "节点来源")
    @ExcelProperty("节点来源")
    private String source;

    @Schema(description = "POI数据供应商")
    @ExcelProperty("POI数据供应商")
    private String provider;

    @Schema(description = "POI快照JSON")
    @ExcelProperty("POI快照JSON")
    private String poiSnapshotJson;

    @Schema(description = "候选POI JSON")
    @ExcelProperty("候选POI JSON")
    private String candidatesJson;

    @Schema(description = "引用来源ID JSON")
    @ExcelProperty("引用来源ID JSON")
    private String citationIdsJson;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
