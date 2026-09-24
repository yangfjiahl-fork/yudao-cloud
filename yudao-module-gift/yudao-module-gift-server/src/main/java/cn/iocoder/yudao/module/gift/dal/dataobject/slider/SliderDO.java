package cn.iocoder.yudao.module.gift.dal.dataobject.slider;

import cn.iocoder.yudao.module.gift.enums.DictTypeConstants;
import cn.iocoder.yudao.module.gift.enums.SliderPositionEnum;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 轮播 DO
 *
 * @author 羔享科技
 */
@TableName("gift_slider")
@KeySequence("gift_slider_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SliderDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 轮播位置
     *
     * 枚举 {@link SliderPositionEnum}
     * 字典 {@link DictTypeConstants#SLIDER_POSITION}
     */
    private String positionCode;
    /**
     * 城市ID（可选）
     */
    private Long cityId;


}
