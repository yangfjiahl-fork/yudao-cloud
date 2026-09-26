package cn.iocoder.yudao.module.gift.dal.dataobject.itinerary;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 行程 DO
 *
 * @author 羔享科技
 */
@TableName("gift_itinerary")
@KeySequence("gift_itinerary_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 城市ID
     */
    private Integer cityId;
    /**
     * 类别ID
     */
    private Long categoryId;
    /**
     * 标题
     */
    private String title;
    /**
     * 副标题
     */
    private String subTitle;
    /**
     * 描述
     */
    private String description;
    /**
     * 图标
     */
    private String icon;
    /**
     * 多图片
     */
    private String picUrls;
    /**
     * 图片尺寸
     */
    private String picSizes;
    /**
     * 标签
     */
    private String tags;
    /**
     * 封面图
     */
    private String coverUrl;
    /**
     * 封面宽度
     */
    private Integer coverWidth;
    /**
     * 封面高度
     */
    private Integer coverHeight;
    /**
     * 第二城市ID
     */
    private Integer nextCityId;
    /**
     * 浏览数
     */
    private Long viewCnt;
    /**
     * 点赞数
     */
    private Long likeCnt;
    /**
     * 排序
     */
    private Integer sort;


}
