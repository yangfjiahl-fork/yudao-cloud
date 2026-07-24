package cn.iocoder.yudao.module.gift.controller.app.video.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "用户 APP - 视频分页 Request VO")
@Data
public class AppVideoPageReqVO extends PageParam {

    @Schema(description = "阿里云 VOD VideoId", example = "6925")
    private String vodVideoId;

    @Schema(description = "状态", example = "2")
    private Integer status;

    @Schema(description = "清晰度")
    private Integer quality;

}