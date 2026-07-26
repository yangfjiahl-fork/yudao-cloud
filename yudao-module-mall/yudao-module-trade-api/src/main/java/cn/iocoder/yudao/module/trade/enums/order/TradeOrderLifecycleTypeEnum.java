package cn.iocoder.yudao.module.trade.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 交易订单生命周期节点
 *
 * @author calvin
 */
@RequiredArgsConstructor
@Getter
public enum TradeOrderLifecycleTypeEnum implements ArrayValuable<Integer> {

    CREATED(1, "订单创建"),
    PAID(2, "订单支付"),
    CANCELED(3, "订单取消"),
    DELIVERED(4, "订单发货"),
    RECEIVED(5, "订单收货");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(TradeOrderLifecycleTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 节点类型
     */
    private final Integer type;

    /**
     * 节点名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
