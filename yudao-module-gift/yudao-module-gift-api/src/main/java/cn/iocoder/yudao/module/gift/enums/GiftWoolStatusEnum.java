/*
 * Copyright (C) 2019 ~ 2026 MeiTuan. All Rights Reserved.
 *
 */
package cn.iocoder.yudao.module.gift.enums;

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 羊毛收取状态
 *
 * @author YangFeng(calvin)
 * @version v1.0
 * @date 2026/7/11 11:45
 */
@AllArgsConstructor
@Getter
public enum GiftWoolStatusEnum {

    INIT(1, "等待收取", "等待用户收取"),

    SUCCESS(3, "已收取", "用户已收取"),

    EXPIRE(6, "已过期", "用户未收取，过期失效"),
    ;


    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;
    /**
     * 描述
     */
    private final String description;

    public static GiftWoolStatusEnum getByType(Integer type) {
        return EnumUtil.getBy(GiftWoolStatusEnum.class,
                e -> Objects.equals(type, e.getType()));
    }
}
