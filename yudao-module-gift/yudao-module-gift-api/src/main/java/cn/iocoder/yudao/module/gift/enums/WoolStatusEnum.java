/*
 * Copyright (C) 2019 ~ 2026 MeiTuan. All Rights Reserved.
 *
 */
package cn.iocoder.yudao.module.gift.enums;

import cn.hutool.core.util.EnumUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
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
public enum WoolStatusEnum implements ArrayValuable<Integer> {

    INIT(1, "等待收取", "等待用户收取"),

    SUCCESS(3, "已收取", "用户已收取"),

    EXPIRE(6, "已过期", "用户未收取（过期失效）"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(WoolStatusEnum::getType).toArray(Integer[]::new);

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

    public static WoolStatusEnum getByType(Integer type) {
        return EnumUtil.getBy(WoolStatusEnum.class,
                e -> Objects.equals(type, e.getType()));
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
