package cn.iocoder.yudao.module.system.enums.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用户 App 可访问的字典类型枚举。
 */
@Getter
@AllArgsConstructor
public enum AppDictTypeEnum {

    USER_FEEDBACK_CATEGORY("gift_user_feedback_category"),
    ;

    private final String type;

    public static boolean contains(String type) {
        return Arrays.stream(values()).anyMatch(item -> item.getType().equals(type));
    }

}
