package cn.iocoder.yudao.module.gift.dal.dataobject.memberlocationhistory;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/** 会员位置变更历史。 */
@TableName("gift_member_location_history")
@KeySequence("gift_member_location_history_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberLocationHistoryDO extends BaseDO {

    @TableId
    private Long id;
    private Long memberId;
    private String sourceType;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String ip;
    private Long cityId;
    private String cityName;

}
