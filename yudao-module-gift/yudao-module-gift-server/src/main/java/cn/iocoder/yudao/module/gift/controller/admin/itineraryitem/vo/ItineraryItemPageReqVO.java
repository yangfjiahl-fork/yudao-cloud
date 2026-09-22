package cn.iocoder.yudao.module.gift.controller.admin.itineraryitem.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 文章分页 Request VO")
@Data
public class ItineraryItemPageReqVO extends PageParam {

    @Schema(description = "线路ID", example = "4883")
    private Long itineraryId;

    @Schema(description = "省ID", example = "16725")
    private Integer provinceId;

    @Schema(description = "市ID", example = "19048")
    private Integer cityId;

    @Schema(description = "区ID", example = "20888")
    private Integer districtId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "副标题")
    private String subTitle;

    @Schema(description = "描述", example = "你说的对")
    private String description;

    @Schema(description = "封面图", example = "https://www.iocoder.cn")
    private String coverUrl;

    @Schema(description = "宽度")
    private Integer coverWidth;

    @Schema(description = "高度")
    private Integer coverHeight;

    @Schema(description = "轮播图")
    private String picUrls;

    @Schema(description = "尺寸")
    private String picSizes;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "位置")
    private String gdPosition;

    @Schema(description = "营业时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private String[] businessTime;

    @Schema(description = "详细地址")
    private String addressDetail;

    @Schema(description = "电话")
    private String phoneNo;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}