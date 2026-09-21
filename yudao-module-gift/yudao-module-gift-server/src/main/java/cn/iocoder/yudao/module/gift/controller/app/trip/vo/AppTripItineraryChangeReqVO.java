package cn.iocoder.yudao.module.gift.controller.app.trip.vo;

import cn.iocoder.yudao.module.gift.service.trip.bo.TripChangeCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/** App 手动编辑行程请求。 */
@Data
public class AppTripItineraryChangeReqVO {

    @NotNull(message = "会话编号不能为空")
    private Long conversationId;

    @NotNull(message = "编辑操作不能为空")
    private TripChangeCommand.Operation operation;

    @Min(value = 1, message = "基础版本必须大于 0")
    private int baseVersion;

    @Size(max = 128, message = "行程节点编号长度不能超过 128")
    private String itemId;

    @Min(value = 1, message = "行程天数必须大于 0")
    private Integer day;

    @Size(max = 32, message = "时间段长度不能超过 32")
    private String timePeriod;

    @Min(value = 0, message = "排序位置不能小于 0")
    private Integer sort;

    @Size(max = 10, message = "单次更新字段不能超过 10 个")
    private Map<String, Object> values;

}
