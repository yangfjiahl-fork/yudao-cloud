package cn.iocoder.yudao.module.gift.dal.dataobject.wool;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.gift.enums.GiftWoolStatusEnum;
import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 羊毛 DO
 *
 * @author calvin
 */
@TableName("gift_wool")
@KeySequence("gift_wool_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WoolDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 业务场景
     *
     * 枚举 {@link MemberPointBizTypeEnum}
     */
    private String bizType;
    /**
     * 业务编号
     */
    private String bizId;
    /**
     * 数量
     */
    private Integer amount;
    /**
     * 备注
     */
    private String remark;
    /**
     * 状态
     *
     * 枚举 {@link GiftWoolStatusEnum}
     */
    private String status;
    /**
     * 会员编号
     */
    private Long memberId;


}
