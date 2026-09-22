package cn.iocoder.yudao.module.gift.dal.dataobject.itineraryitem;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 文章 DO
 *
 * @author 羔享科技
 */
@TableName("gift_itinerary_item")
@KeySequence("gift_itinerary_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 线路ID
     */
    private Long itineraryId;
    /**
     * 省ID
     */
    private Integer provinceId;
    /**
     * 市ID
     */
    private Integer cityId;
    /**
     * 区ID
     */
    private Integer districtId;
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
     * 轮播图
     */
    private String picUrls;
    /**
     * 尺寸
     */
    private String picSizes;
    /**
     * 标签
     */
    private String tags;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 位置
     */
    private String gdPosition;
    /**
     * 营业时间
     */
    private String businessTime;
    /**
     * 详细地址
     */
    private String addressDetail;
    /**
     * 电话
     */
    private String phoneNo;


}