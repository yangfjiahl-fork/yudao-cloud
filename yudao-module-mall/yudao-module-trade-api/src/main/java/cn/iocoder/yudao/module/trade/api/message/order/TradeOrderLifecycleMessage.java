package cn.iocoder.yudao.module.trade.api.message.order;

import cn.iocoder.yudao.module.trade.enums.order.TradeOrderLifecycleTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 交易订单生命周期消息
 *
 * @author calvin
 */
@Data
public class TradeOrderLifecycleMessage {

    /**
     * 订单编号
     */
    @NotNull(message = "订单编号不能为空")
    private Long orderId;

    /**
     * 订单号
     */
    @NotNull(message = "订单号不能为空")
    private String orderNo;

    /**
     * 会员编号
     */
    @NotNull(message = "会员编号不能为空")
    private Long userId;

    /**
     * 商品名称
     */
    @NotNull(message = "商品名称不能为空")
    private String productName;

    /**
     * 使用的积分
     */
    @NotNull(message = "使用积分不能为空")
    private Integer usePoint;

    /**
     * 订单类型
     */
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    /**
     * 配送类型
     */
    @NotNull(message = "配送类型不能为空")
    private Integer deliveryType;

    /**
     * 是否已支付
     */
    @NotNull(message = "支付状态不能为空")
    private Boolean payStatus;

    /**
     * 生命周期节点，枚举 {@link TradeOrderLifecycleTypeEnum}
     */
    @NotNull(message = "生命周期节点不能为空")
    private Integer lifecycleType;

}
