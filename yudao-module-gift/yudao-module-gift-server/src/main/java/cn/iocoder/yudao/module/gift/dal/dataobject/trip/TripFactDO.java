package cn.iocoder.yudao.module.gift.dal.dataobject.trip;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@TableName("gift_trip_fact")
@KeySequence("gift_trip_fact_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class TripFactDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long tripId;
    private String factKey;
    private String valueJson;
    private Long sourceId;
    private LocalDateTime retrievedAt;
    private LocalDateTime expiresAt;
    private Double confidence;
    /** 0=待确认，1=已验证，2=冲突，3=已过期。 */
    private Integer status;

}
