package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 APP - 旅行规划会话 Response VO")
@Data
public class AppTripChatConversationRespVO {

    private Long id;
    private String title;
    private Boolean pinned;
    private Long provinceId;
    private Long cityId;
    private Long districtId;
    private LocalDateTime createTime;

}
