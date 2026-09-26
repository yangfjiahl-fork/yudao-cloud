package cn.iocoder.yudao.module.gift.dal.dataobject.itinerarydayitem;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 通用行程节点 DO
 *
 * @author 羔享科技
 */
@TableName("gift_itinerary_day_item")
@KeySequence("gift_itinerary_day_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryDayItemDO extends BaseDO {

    /**
     * 通用行程节点ID
     */
    @TableId
    private Long id;
    /**
     * 通用行程ID
     */
    private Long itineraryId;
    /**
     * 通用行程日程ID
     */
    private Long itineraryDayId;
    /**
     * 节点类型
     */
    private String type;
    /**
     * 节点时段
     */
    private String slot;
    /**
     * 节点标题
     */
    private String title;
    /**
     * 节点副标题
     */
    private String subTitle;
    /**
     * 节点描述
     */
    private String description;
    /**
     * 当日节点排序值
     */
    private Integer sort;
    /**
     * 计划开始时间
     */
    private LocalTime startTime;
    /**
     * 建议停留分钟数
     */
    private Integer durationMinutes;
    /**
     * POI供应商地点ID
     */
    private String poiId;
    /**
     * POI省级区域ID
     */
    private Integer provinceId;
    /**
     * POI城市ID
     */
    private Integer cityId;
    /**
     * POI区县ID
     */
    private Integer districtId;
    /**
     * POI经度
     */
    private BigDecimal longitude;
    /**
     * POI纬度
     */
    private BigDecimal latitude;
    /**
     * 封面图地址
     */
    private String coverUrl;
    /**
     * 封面图宽度
     */
    private Integer coverWidth;
    /**
     * 封面图高度
     */
    private Integer coverHeight;
    /**
     * 图片地址集合
     */
    private String picUrls;
    /**
     * 图片尺寸集合
     */
    private String picSizes;
    /**
     * 标签集合
     */
    private String tags;
    /**
     * 营业时间
     */
    private String businessTime;
    /**
     * 详细地址
     */
    private String addressDetail;
    /**
     * 联系电话
     */
    private String phoneNo;

}
