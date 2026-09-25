package cn.iocoder.yudao.module.gift.controller.app.userfeedback.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.gift.enums.UserFeedbackCategoryEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 APP - 用户反馈创建 Request VO")
@Data
public class AppUserFeedbackCreateReqVO {

    @Schema(description = "问题分类：0未知，10地名名称，20地点图片，30地点介绍，40营业时间，50地理位置，60电话，99其他建议",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "问题分类不能为空")
    @InEnum(UserFeedbackCategoryEnum.class)
    private Integer category;

    @Schema(description = "反馈问题与建议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "反馈问题与建议不能为空")
    private String content;

    @Schema(description = "POI供应商地点ID", example = "B0FFG2M8Q4")
    private String poiId;

    @Schema(description = "POI名称快照", example = "西湖风景名胜区")
    private String poiName;

}
