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
    private String stateJson;
    private String missingRequiredJson;
    private Integer status;

}
