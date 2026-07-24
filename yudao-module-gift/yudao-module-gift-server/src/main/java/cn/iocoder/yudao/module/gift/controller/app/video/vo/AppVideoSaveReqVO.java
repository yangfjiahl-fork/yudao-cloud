package cn.iocoder.yudao.module.gift.controller.app.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "用户 APP - 视频新增/修改 Request VO")
@Data
public class AppVideoSaveReqVO {

    @Schema(description = "视频标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "视频标题不能为空")
    private String title;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态不能为空")
    private Integer status;

}