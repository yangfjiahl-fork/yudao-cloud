package cn.iocoder.yudao.module.gift.controller.app.itinerary.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "用户 APP - 通用行程分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppItineraryPageReqVO extends PageParam {

    @Schema(description = "行程类别编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "行程类别编号不能为空")
    private Long categoryId;

}
