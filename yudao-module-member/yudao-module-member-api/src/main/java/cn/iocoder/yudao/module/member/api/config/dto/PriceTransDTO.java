/*
 * Copyright (C) 2019 ~ 2026 MeiTuan. All Rights Reserved.
 *
 */
package cn.iocoder.yudao.module.member.api.config.dto;

import org.dromara.core.trans.vo.VO;

import lombok.Builder;
import lombok.Data;

/**
 * 金额转羊毛
 *
 * @author YangFeng(calvin)
 * @version v1.0
 * @date 2026/7/27 22:30
 */
@Data
@Builder
public class PriceTransDTO implements VO {

    private Integer price;

    private Integer woolPrice;

    @Override
    public Object getPkey() {
        return price;
    }
}
