package cn.iocoder.yudao.module.gift.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 轮播图跳转页面枚举。
 */
@AllArgsConstructor
@Getter
public enum SliderItemJumpPageEnum implements ArrayValuable<String> {

    SHARE("SHARE", "分享页");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(SliderItemJumpPageEnum::getCode)
            .toArray(String[]::new);

    private final String code;
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
