package cn.iocoder.yudao.module.gift.dal.dataobject.trip;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("gift_trip_plan")
@KeySequence("gift_trip_plan_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class TripPlanDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long conversationId;
    private Long memberId;
    /**
     * 当前生效的行程快照。历史行程仍通过关联的聊天消息保留。
     */
    private Long currentItineraryId;
    /**
     * 百炼 Managed Agents 会话。一个旅行会话只创建一次，后续重规划继续复用其沙箱与上下文。
     */
    private String managedAgentSessionId;
    private String stateJson;
    private String missingRequiredJson;
    private Integer status;

}
