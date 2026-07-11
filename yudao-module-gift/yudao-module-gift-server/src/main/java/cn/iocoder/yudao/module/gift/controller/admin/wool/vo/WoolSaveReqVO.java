package cn.iocoder.yudao.module.gift.controller.admin.wool.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.gift.enums.WoolStatusEnum;
import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 羊毛新增/修改 Request VO")
@Data
public class WoolSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13660")
    private Long id;

    @Schema(description = "业务场景", requiredMode = Schema.RequiredMode.REQUIRED, example = "REGISTER")
    @NotEmpty(message = "业务场景不能为空")
    @InEnum(MemberPointBizTypeEnum.class)
    private Integer bizType;

    @Schema(description = "业务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27723")
    @NotEmpty(message = "业务编号不能为空")
    private String bizId;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数量不能为空")
    @Min(0)
    private Integer amount;

    @Schema(description = "备注", example = "新用户注册赠送羊毛")
    private String remark;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "INIT")
    @NotEmpty(message = "状态不能为空")
    @InEnum(WoolStatusEnum.class)
    private Integer status;

    @Schema(description = "会员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "289")
    @NotNull(message = "会员编号不能为空")
    private Long memberId;

}
