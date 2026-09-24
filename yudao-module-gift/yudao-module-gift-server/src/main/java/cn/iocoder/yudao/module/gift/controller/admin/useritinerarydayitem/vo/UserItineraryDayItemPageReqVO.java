package cn.iocoder.yudao.module.gift.controller.admin.useritinerarydayitem.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用户行程节点分页 Request VO")
@Data
public class UserItineraryDayItemPageReqVO extends PageParam {

    @Schema(description = "用户行程ID", example = "12729")
    private Long userItineraryId;

    @Schema(description = "用户行程日程ID", example = "14303")
    private Long userItineraryDayId;

    @Schema(description = "行程节点业务ID", example = "28898")
    private String itemId;

    @Schema(description = "所属行程天数")
    private Integer day;

    @Schema(description = "节点类型", example = "2")
    private String type;

    @Schema(description = "节点时段")
    private String slot;

    @Schema(description = "节点展示标签")
    private String label;

    @Schema(description = "当日节点排序值")
    private Integer sort;

    @Schema(description = "计划开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalTime[] startTime;

    @Schema(description = "计划结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalTime[] endTime;

    @Schema(description = "建议停留分钟数")
    private Integer durationMinutes;

    @Schema(description = "POI供应商地点ID", example = "3188")
    private String poiId;

    @Schema(description = "POI名称", example = "张三")
    private String poiName;

    @Schema(description = "POI省级区域ID", example = "12321")
    private Integer provinceId;

    @Schema(description = "POI城市ID", example = "22808")
    private Integer cityId;

    @Schema(description = "POI区县ID", example = "30598")
    private Integer districtId;

    @Schema(description = "POI城市名称")
    private String city;

    @Schema(description = "POI所在区域")
    private String area;

    @Schema(description = "POI详细地址")
    private String addressDetail;

    @Schema(description = "POI经度")
    private BigDecimal longitude;

    @Schema(description = "POI纬度")
    private BigDecimal latitude;

    @Schema(description = "坐标系")
    private String coordinateSystem;

    @Schema(description = "营业时间")
    private String businessHours;

    @Schema(description = "联系电话")
    private String phoneNo;

    @Schema(description = "封面图地址", example = "https://www.iocoder.cn")
    private String coverUrl;

    @Schema(description = "评分")
    private BigDecimal rating;

    @Schema(description = "预计花费")
    private BigDecimal cost;

    @Schema(description = "标签JSON")
    private String tagsJson;

    @Schema(description = "节点骨架文案")
    private String skeleton;

    @Schema(description = "节点详细文案")
    private String detail;

    @Schema(description = "节点内容状态", example = "1")
    private String status;

    @Schema(description = "节点解析状态", example = "1")
    private Integer resolveStatus;

    @Schema(description = "节点排程状态", example = "2")
    private String planningStatus;

    @Schema(description = "POI校验状态", example = "1")
    private String poiVerificationStatus;

    @Schema(description = "是否必去")
    private Boolean mustVisit;

    @Schema(description = "是否锁定")
    private Boolean locked;

    @Schema(description = "节点来源")
    private String source;

    @Schema(description = "POI数据供应商")
    private String provider;

    @Schema(description = "POI快照JSON")
    private String poiSnapshotJson;

    @Schema(description = "候选POI JSON")
    private String candidatesJson;

    @Schema(description = "引用来源ID JSON")
    private String citationIdsJson;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
