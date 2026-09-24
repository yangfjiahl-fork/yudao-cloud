package cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryliked;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 收藏行程 DO
 *
 * @author 羔享科技
 */
@TableName("gift_user_itinerary_liked")
@KeySequence("gift_user_itinerary_liked_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserItineraryLikedDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 行程ID
     */
    private Long itineraryId;
    /**
     * 会员ID
     */
    private Long memberId;


}
