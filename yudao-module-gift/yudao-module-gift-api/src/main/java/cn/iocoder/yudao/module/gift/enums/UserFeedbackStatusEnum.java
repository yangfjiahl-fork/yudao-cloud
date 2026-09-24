package cn.iocoder.yudao.module.gift.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用户反馈处理状态枚举。
 */
@Getter
@AllArgsConstructor
public enum UserFeedbackStatusEnum implements ArrayValuable<Integer> {

    UNPROCESSED(0, "未处理"),
    PROCESSED(1, "已处理"),
    IGNORED(2, "已忽略");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(UserFeedbackStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
