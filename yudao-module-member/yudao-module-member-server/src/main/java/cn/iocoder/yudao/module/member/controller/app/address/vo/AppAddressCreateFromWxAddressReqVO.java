package cn.iocoder.yudao.module.member.controller.app.address.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "用户 APP - 根据微信地址创建用户收件地址 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppAddressCreateFromWxAddressReqVO extends AppAddressBaseVO {

    @Schema(description = "省名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "北京市")
//    @NotBlank(message = "省名称不能为空")
    private String provinceName;

    @Schema(description = "市名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "北京市")
//    @NotBlank(message = "市名称不能为空")
    private String cityName;

    @Schema(description = "区名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "朝阳区")
    @NotBlank(message = "区名称不能为空")
    private String districtName;

}
