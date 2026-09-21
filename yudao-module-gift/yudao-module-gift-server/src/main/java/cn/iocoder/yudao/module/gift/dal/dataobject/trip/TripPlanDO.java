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
     * 最近一次生成或重规划使用的百炼 Managed Agents Session，仅用于审计定位。
     * 每次 GENERATE_PLAN / EDIT_PLAN 都创建新 Session，避免继承上一次工具输出和临时上下文。
     */
    private String managedAgentSessionId;
    private String stateJson;
    private String missingRequiredJson;
    private Integer status;

}
