package cn.iocoder.yudao.module.gift.controller.admin.userfeedback.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用户反馈分页 Request VO")
@Data
public class UserFeedbackPageReqVO extends PageParam {

    @Schema(description = "会员ID", example = "25837")
    private Long memberId;

    @Schema(description = "问题分类：0未知，10地名名称，20地点图片，30地点介绍，40营业时间，50地理位置，60电话，99其他建议")
    private Integer category;

    @Schema(description = "处理状态：0未处理，1已处理，2已忽略", example = "0")
    private Integer status;

    @Schema(description = "反馈问题与建议")
    private String content;

    @Schema(description = "POI供应商地点ID", example = "20346")
    private String poiId;

    @Schema(description = "POI名称快照", example = "王五")
    private String poiName;

    @Schema(description = "POI数据供应商")
    private String poiProvider;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
