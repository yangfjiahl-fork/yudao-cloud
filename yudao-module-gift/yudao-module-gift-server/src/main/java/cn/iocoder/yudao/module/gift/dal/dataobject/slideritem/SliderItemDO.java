package cn.iocoder.yudao.module.gift.dal.dataobject.slideritem;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 轮播图 DO
 *
 * @author 羔享科技
 */
@TableName("gift_slider_item")
@KeySequence("gift_slider_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SliderItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 轮播ID
     */
    private Long sliderId;
    /**
     * 图片地址
     */
    private String imageUrl;
    /**
     * 图片宽度
     */
    private Integer imageWidth;
    /**
     * 图片高度
     */
    private Integer imageHeight;
    /**
     * 顺序
     */
    private Integer sort;
    /**
     * 跳转页面
     */
    private String jumpPage;
    /**
     * 跳转页面ID
     */
    private Long jumpPageId;


}