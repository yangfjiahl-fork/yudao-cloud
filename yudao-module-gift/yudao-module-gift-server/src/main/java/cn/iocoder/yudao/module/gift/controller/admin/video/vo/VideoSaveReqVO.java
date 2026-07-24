package cn.iocoder.yudao.module.gift.controller.admin.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 视频新增/修改 Request VO")
@Data
public class VideoSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13660")
    private Long id;

    @Schema(description = "视频标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "视频标题不能为空")
    private String title;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
