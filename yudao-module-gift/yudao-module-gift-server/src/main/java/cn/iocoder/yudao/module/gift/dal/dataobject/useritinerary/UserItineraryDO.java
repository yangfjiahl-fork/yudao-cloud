package cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary;

import lombok.*;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 用户行程 DO
 *
 * @author 羔享科技
 */
@TableName("gift_user_itinerary")
@KeySequence("gift_user_itinerary_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserItineraryDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 会员ID
     */
    private Long memberId;
    /**
     * 标题
     */
    private String title;
    /**
     * 封面图
     */
    private String coverUrl;
    /**
     * 宽度
     */
    private Integer coverWidth;
    /**
     * 高度
     */
    private Integer coverHeight;
    /**
     * 开始日期
     */
    private LocalDate startDate;
    /**
     * 完成日期
     */
    private LocalDate endDate;
    /**
     * 天数
     */
    private Integer dayCnt;
    /**
     * 城市ID
     */
    private Integer cityId;
    /**
     * 城市ID
     */
    private Integer nextCityId;
    /**
     * 偏好
     */
    private String preference;


}
