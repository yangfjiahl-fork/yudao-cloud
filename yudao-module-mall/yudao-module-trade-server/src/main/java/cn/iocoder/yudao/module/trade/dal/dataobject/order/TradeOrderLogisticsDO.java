package cn.iocoder.yudao.module.trade.dal.dataobject.order;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 订单物流 DO
 */
@TableName("trade_order_logistics")
@KeySequence("trade_order_logistics_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TradeOrderLogisticsDO extends BaseDO {

    public static final int SUBSCRIBE_STATUS_PENDING = 0;
    public static final int SUBSCRIBE_STATUS_SUCCESS = 10;
    public static final int SUBSCRIBE_STATUS_FAILED = 20;

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 订单编号
     */
    private Long orderId;
    /**
     * 物流公司编号
     */
    private Long logisticsId;
    /**
     * 物流公司编码
     */
    private String expressCode;
    /**
     * 物流单号
     */
    private String logisticsNo;
    /**
     * 缓存及回调关联键
     */
    private String cacheKey;
    /**
     * 查询服务商
     */
    private String provider;
    /**
     * 订阅状态
     */
    private Integer subscribeStatus;
    /**
     * 订阅结果信息
     */
    private String subscribeMessage;
    /**
     * 最近订阅时间
     */
    private LocalDateTime subscribeTime;
    /**
     * 快递当前状态
     */
    private String trackState;
    /**
     * 物流轨迹 JSON 数组
     */
    private String trackData;
    /**
     * 最近实时查询时间
     */
    private LocalDateTime lastQueryTime;
    /**
     * 最近动态通知时间
     */
    private LocalDateTime lastNotifyTime;

}
