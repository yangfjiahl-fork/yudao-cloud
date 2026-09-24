package cn.iocoder.yudao.module.gift.controller.admin.useritineraryliked.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 收藏行程新增/修改 Request VO")
@Data
public class UserItineraryLikedSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "23699")
    private Long id;

    @Schema(description = "行程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12377")
    @NotNull(message = "行程ID不能为空")
    private Long itineraryId;

    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13449")
    @NotNull(message = "会员ID不能为空")
    private Long memberId;

}