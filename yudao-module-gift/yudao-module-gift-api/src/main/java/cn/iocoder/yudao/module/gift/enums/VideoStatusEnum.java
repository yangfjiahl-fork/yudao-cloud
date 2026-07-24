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
 * 视频状态枚举
 *
 * @author YangFeng(calvin)
 * @version v1.0
 * @date 2026/7/24 22:13
 */
@AllArgsConstructor
@Getter
public enum VideoStatusEnum implements ArrayValuable<Integer> {

    ONLINE(1, "在线", "对外可访问"),

    OFFLINE(2, "下线", "对外不可访问");


    public static final Integer[] ARRAYS = Arrays.stream(values()).map(VideoStatusEnum::getType).toArray(Integer[]::new);

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

    public static VideoStatusEnum getByType(Integer type) {
        return EnumUtil.getBy(VideoStatusEnum.class,
                e -> Objects.equals(type, e.getType()));
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
