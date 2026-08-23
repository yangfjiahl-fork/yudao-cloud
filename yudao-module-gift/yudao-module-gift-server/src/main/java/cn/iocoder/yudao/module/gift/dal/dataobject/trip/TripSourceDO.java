package cn.iocoder.yudao.module.gift.dal.dataobject.trip;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 可核验旅行事实的来源快照。 */
@TableName("gift_trip_source")
@KeySequence("gift_trip_source_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class TripSourceDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long tripId;
    private String sourceType;
    private String sourceName;
    private String sourceUrl;
    private LocalDateTime retrievedAt;
    private LocalDateTime expiresAt;
    private Integer status;

}
