package cn.iocoder.yudao.module.gift.controller.admin.userfeedback.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - 用户反馈处理 Request VO")
@Data
public class UserFeedbackProcessReqVO {

    @Schema(description = "用户反馈ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "用户反馈ID不能为空")
    private Long id;

    @Schema(description = "处理说明", requiredMode = Schema.RequiredMode.REQUIRED, example = "地点营业时间已经更新")
    @NotBlank(message = "处理说明不能为空")
    @Size(max = 2000, message = "处理说明不能超过 2000 个字符")
    private String processRemark;

}
