package cn.iocoder.yudao.module.product.api.message.spu;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商品 SPU 创建消息
 *
 * @author 芋道源码
 */
@Data
public class ProductSpuCreateMessage {

    /**
     * 商品 SPU 编号
     */
    @NotNull(message = "商品 SPU 编号不能为空")
    private Long spuId;

}
