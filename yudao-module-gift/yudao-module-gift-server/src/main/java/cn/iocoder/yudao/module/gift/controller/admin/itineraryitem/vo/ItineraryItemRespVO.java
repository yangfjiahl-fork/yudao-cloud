package cn.iocoder.yudao.module.gift.controller.admin.itineraryitem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 文章 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ItineraryItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "12291")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "线路ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "4883")
    @ExcelProperty("线路ID")
    private Long itineraryId;

    @Schema(description = "省ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "16725")
    @ExcelProperty("省ID")
    private Integer provinceId;

    @Schema(description = "市ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19048")
    @ExcelProperty("市ID")
    private Integer cityId;

    @Schema(description = "区ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20888")
    @ExcelProperty("区ID")
    private Integer districtId;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("标题")
    private String title;

    @Schema(description = "副标题")
    @ExcelProperty("副标题")
    private String subTitle;

    @Schema(description = "描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "你说的对")
    @ExcelProperty("描述")
    private String description;

    @Schema(description = "封面图", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @ExcelProperty("封面图")
    private String coverUrl;

    @Schema(description = "宽度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("宽度")
    private Integer coverWidth;

    @Schema(description = "高度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("高度")
    private Integer coverHeight;

    @Schema(description = "轮播图", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("轮播图")
    private String picUrls;

    @Schema(description = "尺寸", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("尺寸")
    private String picSizes;

    @Schema(description = "标签", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("标签")
    private String tags;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排序")
    private Integer sort;

    @Schema(description = "位置", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("位置")
    private String gdPosition;

    @Schema(description = "营业时间")
    @ExcelProperty("营业时间")
    private String businessTime;

    @Schema(description = "详细地址")
    @ExcelProperty("详细地址")
    private String addressDetail;

    @Schema(description = "电话")
    @ExcelProperty("电话")
    private String phoneNo;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}