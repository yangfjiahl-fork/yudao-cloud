package cn.iocoder.yudao.module.gift.controller.admin.itinerary.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 行程分页 Request VO")
@Data
public class ItineraryPageReqVO extends PageParam {

    @Schema(description = "城市ID", example = "7848")
    private Integer cityId;

    @Schema(description = "类别ID", example = "1668")
    private Long categoryId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "副标题")
    private String subTitle;

    @Schema(description = "描述", example = "随便")
    private String description;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "多图片")
    private String picUrls;

    @Schema(description = "图片尺寸")
    private String picSizes;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "封面图", example = "https://www.iocoder.cn")
    private String coverUrl;

    @Schema(description = "封面宽度")
    private Integer coverWidth;

    @Schema(description = "封面高度")
    private Integer coverHeight;

    @Schema(description = "第二城市ID", example = "23770")
    private Integer nextCityId;

    @Schema(description = "浏览数")
    private Long viewCnt;

    @Schema(description = "点赞数")
    private Long likeCnt;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
