package cn.iocoder.yudao.module.gift.controller.app.itinerary.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/** AG-UI RunAgentInput 的旅行规划入口；会话历史仍由服务端保存和校验。 */
@Data
public class AppItineraryAgUiRunReqVO {

    @NotEmpty(message = "会话标识不能为空")
    @Size(max = 20, message = "会话标识长度不能超过 20")
    private String threadId;

    @NotEmpty(message = "运行标识不能为空")
    @Size(max = 128, message = "运行标识长度不能超过 128")
    private String runId;

    @NotNull(message = "消息不能为空")
    @Size(min = 1, max = 1, message = "旅行规划每次只能提交一条用户消息")
    @Valid
    private List<AppItineraryAgUiMessageReqVO> messages;

}
