package cn.iocoder.yudao.module.gift.service.itinerary.provider.place;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 旅行场景实际使用的高德 POI 大类。
 *
 * 系统字典 {@value #DICT_TYPE} 的 value 与 {@link #category} 保持一致；高德 typecode
 * 仅在此处维护，避免行程规划和探索页各自硬编码。
 */
@Getter
@AllArgsConstructor
public enum AmapPoiTypeEnum {

    SCENIC("sightseeing", "景点", "110000", 100, true),
    HOTEL("hotel", "酒店", "100000", 80, false),
    FOOD("food", "美食", "050000", 90, false),
    SHOPPING("shopping", "购物", "060000", 70, false);

    public static final String DICT_TYPE = "gift_amap_poi_type";

    /** 探索页与系统字典使用的分类值。 */
    private final String category;
    /** 系统字典标签。 */
    private final String label;
    /** 高德 POI 大类编码。 */
    private final String amapTypeCode;
    private final int priority;
    private final boolean featured;

    public String getAmapTypePrefix() {
        return amapTypeCode.substring(0, 2);
    }

    public static AmapPoiTypeEnum fromCategory(String category) {
        return Arrays.stream(values())
                .filter(type -> StrUtil.equalsIgnoreCase(type.category, StrUtil.trim(category)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("地图分类不支持"));
    }

}
