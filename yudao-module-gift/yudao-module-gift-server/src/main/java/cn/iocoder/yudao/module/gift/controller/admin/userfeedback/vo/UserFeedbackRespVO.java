package cn.iocoder.yudao.module.gift.controller.admin.userfeedback.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.gift.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 用户反馈 Response VO")
@Data
@ExcelIgnoreUnannotated
public class UserFeedbackRespVO {

    @Schema(description = "用户反馈ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29247")
    @ExcelProperty("用户反馈ID")
    private Long id;

    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25837")
    @ExcelProperty("会员ID")
    private Long memberId;

    @Schema(description = "问题分类：0未知，10地名名称，20地点图片，30地点介绍，40营业时间，50地理位置，60电话，99其他建议", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("问题分类：0未知，10地名名称，20地点图片，30地点介绍，40营业时间，50地理位置，60电话，99其他建议")
    private Integer category;

    @Schema(description = "处理状态：0未处理，1已处理，2已忽略", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty(value = "处理状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.USER_FEEDBACK_STATUS)
    private Integer status;

    @Schema(description = "反馈问题与建议", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("反馈问题与建议")
    private String content;

    @Schema(description = "处理说明")
    @ExcelProperty("处理说明")
    private String processRemark;

    @Schema(description = "POI供应商地点ID", example = "20346")
    @ExcelProperty("POI供应商地点ID")
    private String poiId;

    @Schema(description = "POI名称快照", example = "王五")
    @ExcelProperty("POI名称快照")
    private String poiName;

    @Schema(description = "POI数据供应商")
    @ExcelProperty("POI数据供应商")
    private String poiProvider;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
