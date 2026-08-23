package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 旅行规划会话创建 Request VO")
@Data
public class AppTripChatConversationCreateReqVO {

    @Schema(description = "省级地区编号", example = "330000")
    private Long provinceId;

    @Schema(description = "市级地区编号", example = "330100")
    private Long cityId;

    @Schema(description = "区县级地区编号", example = "330106")
    private Long districtId;

}
