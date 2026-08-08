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
 * 文章状态
 *
 * @author YangFeng(calvin)
 * @version v1.0
 * @date 2026/8/7 21:40
 */
@AllArgsConstructor
@Getter
public enum ArticleStatusEnum implements ArrayValuable<Integer> {

    DRAFT(1, "草稿", "草稿"),

    OFFLINE(3, "已下线", "已下线"),

    ONLINE(5, "已发布", "已发布"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ArticleStatusEnum::getType).toArray(Integer[]::new);

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

    public static ArticleStatusEnum getByType(Integer type) {
        return EnumUtil.getBy(ArticleStatusEnum.class,
                e -> Objects.equals(type, e.getType()));
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
