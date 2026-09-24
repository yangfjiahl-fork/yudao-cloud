package cn.iocoder.yudao.module.gift.controller.admin.userfeedback.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.gift.enums.UserFeedbackStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 用户反馈新增/修改 Request VO")
@Data
public class UserFeedbackSaveReqVO {

    @Schema(description = "用户反馈ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29247")
    private Long id;

    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25837")
    @NotNull(message = "会员ID不能为空")
    private Long memberId;

    @Schema(description = "问题分类：0未知，10地名名称，20地点图片，30地点介绍，40营业时间，50地理位置，60电话，99其他建议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "问题分类不能为空")
    private Integer category;

    @Schema(description = "处理状态：0未处理，1已处理，2已忽略", example = "0")
    @InEnum(UserFeedbackStatusEnum.class)
    private Integer status;

    @Schema(description = "反馈问题与建议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "反馈问题与建议不能为空")
    private String content;

    @Schema(description = "POI供应商地点ID", example = "20346")
    private String poiId;

    @Schema(description = "POI名称快照", example = "王五")
    private String poiName;

    @Schema(description = "POI数据供应商")
    private String poiProvider;

}
