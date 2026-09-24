package cn.iocoder.yudao.module.gift.controller.admin.itinerarydayitem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalTime;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 文章新增/修改 Request VO")
@Data
public class ItineraryDayItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "12291")
    private Long id;

    @Schema(description = "线路ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "4883")
    @NotNull(message = "线路ID不能为空")
    private Long itineraryId;

    @NotNull(message = "行程日ID不能为空")
    private Long itineraryDayId;

    private String type;
    private String slot;

    @Schema(description = "省ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "16725")
    @NotNull(message = "省ID不能为空")
    private Integer provinceId;

    @Schema(description = "市ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19048")
    @NotNull(message = "市ID不能为空")
    private Integer cityId;

    @Schema(description = "区ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20888")
    @NotNull(message = "区ID不能为空")
    private Integer districtId;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "标题不能为空")
    private String title;

    @Schema(description = "副标题")
    private String subTitle;

    @Schema(description = "描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "你说的对")
    @NotEmpty(message = "描述不能为空")
    private String description;

    @Schema(description = "封面图", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @NotEmpty(message = "封面图不能为空")
    private String coverUrl;

    @Schema(description = "宽度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "宽度不能为空")
    private Integer coverWidth;

    @Schema(description = "高度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "高度不能为空")
    private Integer coverHeight;

    @Schema(description = "轮播图", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "轮播图不能为空")
    private String picUrls;

    @Schema(description = "尺寸", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "尺寸不能为空")
    private String picSizes;

    @Schema(description = "标签", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "标签不能为空")
    private String tags;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    private Integer sort;

    private LocalTime startTime;
    private Integer durationMinutes;
    private String poiId;
    private BigDecimal longitude;
    private BigDecimal latitude;

    @Schema(description = "位置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "位置不能为空")
    private String gdPosition;

    @Schema(description = "营业时间")
    private String businessTime;

    @Schema(description = "详细地址")
    private String addressDetail;

    @Schema(description = "电话")
    private String phoneNo;

}
