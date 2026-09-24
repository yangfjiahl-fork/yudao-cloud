package cn.iocoder.yudao.module.gift.controller.admin.slideritem.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.gift.enums.SliderItemJumpPageEnum;
import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 轮播图分页 Request VO")
@Data
public class SliderItemPageReqVO extends PageParam {

    @Schema(description = "轮播ID", example = "17759")
    private Long sliderId;

    @Schema(description = "图片地址", example = "https://www.iocoder.cn")
    private String imageUrl;

    @Schema(description = "图片宽度")
    private Integer imageWidth;

    @Schema(description = "图片高度")
    private Integer imageHeight;

    @Schema(description = "顺序")
    private Integer sort;

    @Schema(description = "跳转页面", allowableValues = "SHARE", example = "SHARE")
    @InEnum(value = SliderItemJumpPageEnum.class, message = "跳转页面必须是 {value}")
    private String jumpPage;

    @Schema(description = "跳转页面ID", example = "32220")
    private Long jumpPageId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
