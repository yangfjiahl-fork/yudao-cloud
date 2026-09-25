package cn.iocoder.yudao.module.gift.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用户反馈问题分类枚举。
 */
@Getter
@AllArgsConstructor
public enum UserFeedbackCategoryEnum implements ArrayValuable<Integer> {

    UNKNOWN(0, "未知"),
    PLACE_NAME(10, "地名名称"),
    PLACE_IMAGE(20, "地点图片"),
    PLACE_INTRODUCTION(30, "地点介绍"),
    BUSINESS_HOURS(40, "营业时间"),
    LOCATION(50, "地理位置"),
    PHONE(60, "电话"),
    OTHER_SUGGESTION(99, "其他建议");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(UserFeedbackCategoryEnum::getCategory)
            .toArray(Integer[]::new);

    private final Integer category;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
