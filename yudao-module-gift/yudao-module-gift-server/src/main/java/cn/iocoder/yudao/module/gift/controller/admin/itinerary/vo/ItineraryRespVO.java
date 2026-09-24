package cn.iocoder.yudao.module.gift.controller.admin.itinerary.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 行程 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ItineraryRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10691")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "城市ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7848")
    @ExcelProperty("城市ID")
    private Integer cityId;

    @Schema(description = "类别ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1668")
    @ExcelProperty("类别ID")
    private Long categoryId;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("标题")
    private String title;

    @Schema(description = "副标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("副标题")
    private String subTitle;

    @Schema(description = "描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "随便")
    @ExcelProperty("描述")
    private String description;

    @Schema(description = "图标")
    @ExcelProperty("图标")
    private String icon;

    @Schema(description = "多图片", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("多图片")
    private String picUrls;

    @Schema(description = "图片尺寸")
    @ExcelProperty("图片尺寸")
    private String picSizes;

    @Schema(description = "标签", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("标签")
    private String tags;

    @Schema(description = "封面图", example = "https://www.iocoder.cn")
    @ExcelProperty("封面图")
    private String coverUrl;

    @Schema(description = "封面宽度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("封面宽度")
    private Integer coverWidth;

    @Schema(description = "封面高度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("封面高度")
    private Integer coverHeight;

    @Schema(description = "首图封面", example = "https://www.iocoder.cn")
    @ExcelProperty("首图封面")
    private String firstCoverUrl;

    @Schema(description = "首图高度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("首图高度")
    private Integer firstCoverHeight;

    @Schema(description = "首图宽度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("首图宽度")
    private Integer firstCoverWidth;

    @Schema(description = "城市ID", example = "23770")
    @ExcelProperty("城市ID")
    private Integer nextCityId;

    @Schema(description = "浏览数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("浏览数")
    private Long viewCnt;

    @Schema(description = "点赞数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("点赞数")
    private Long likeCnt;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排序")
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
