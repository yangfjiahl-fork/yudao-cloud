package cn.iocoder.yudao.module.gift.dal.dataobject.trip;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("gift_trip_run")
@KeySequence("gift_trip_run_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class TripRunDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long tripId;
    private String stage;
    private String model;
    private Long promptTokens;
    private Long completionTokens;
    private Long totalTokens;
    private Long durationMs;
    private Integer status;
    private String errorMessage;

}
