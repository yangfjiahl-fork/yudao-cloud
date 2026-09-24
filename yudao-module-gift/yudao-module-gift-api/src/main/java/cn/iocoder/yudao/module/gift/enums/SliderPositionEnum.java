package cn.iocoder.yudao.module.gift.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 轮播位置枚举。
 */
@AllArgsConstructor
@Getter
public enum SliderPositionEnum implements ArrayValuable<String> {

    HOME_TOP("HOME_TOP", "首页顶部");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(SliderPositionEnum::getCode)
            .toArray(String[]::new);

    private final String code;
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
