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
 * 视频清晰度枚举
 *
 * @author YangFeng(calvin)
 * @version v1.0
 * @date 2026/7/24 22:13
 */
@AllArgsConstructor
@Getter
public enum VideoQualityEnum implements ArrayValuable<Integer> {

    LD(1, "标清", "标清"),

    SD(2, "高清", "高清"),

    HD(3, "超清", "超清"),
    ;


    public static final Integer[] ARRAYS = Arrays.stream(values()).map(VideoQualityEnum::getType).toArray(Integer[]::new);

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

    public static VideoQualityEnum getByType(Integer type) {
        return EnumUtil.getBy(VideoQualityEnum.class,
                e -> Objects.equals(type, e.getType()));
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
