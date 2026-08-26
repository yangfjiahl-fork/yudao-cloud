package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppTripItinerarySlotResolveReqVO {

    @NotNull(message = "对话编号不能为空")
    private Long conversationId;
    @NotNull(message = "行程消息编号不能为空")
    private Long messageId;
    @NotNull(message = "行程天数不能为空")
    @Min(value = 0, message = "行程天数不能小于 0")
    private Integer day;
    @NotBlank(message = "行程节点不能为空")
    private String slot;

}
