package cn.iocoder.yudao.module.gift.dal.dataobject.itineraryday;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

/**
 * 通用行程每日安排 DO
 *
 * @author 羔享科技
 */
@TableName("gift_itinerary_day")
@KeySequence("gift_itinerary_day_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryDayDO extends BaseDO {

    /**
     * 通用行程日程ID
     */
    @TableId
    private Long id;
    /**
     * 通用行程ID
     */
    private Long itineraryId;
    /**
     * 行程第几天，从1开始
     */
    private Integer day;
    /**
     * 当日城市ID
     */
    private Integer cityId;
    /**
     * 当日区县ID
     */
    private Integer districtId;
    /**
     * 当日标题
     */
    private String title;
    /**
     * 当日描述
     */
    private String description;
    /**
     * 排序值
     */
    private Integer sort;

}
