/*
 * Copyright (C) 2019 ~ 2026 MeiTuan. All Rights Reserved.
 *
 */
package cn.iocoder.yudao.module.gift.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Gift 错误码枚举
 *
 * @author YangFeng(calvin)
 * @version v1.0
 * @date 2026/7/11 12:13
 */
public interface ErrorCodeConstants {

    ErrorCode WOOL_NOT_EXISTS = new ErrorCode(1_041_000_000, "羊毛不存在");
    ErrorCode WOOL_NEW_USER_AMOUNT_CONFIG_INVALID = new ErrorCode(1_041_000_001,
            "新用户羊毛数量配置不存在或格式错误");
    ErrorCode WOOL_NOT_WAIT_RECEIVE = new ErrorCode(1_041_000_002, "你已收取了此羊毛");
    ErrorCode WOOL_RECOMMEND_USER_AMOUNT_CONFIG_INVALID = new ErrorCode(1_041_000_003,
            "推荐用户羊毛数量配置不存在或格式错误");

    ErrorCode VIDEO_NOT_EXISTS = new ErrorCode(1_042_000_001, "视频不存在");

    ErrorCode ARTICLE_NOT_EXISTS = new ErrorCode(1_043_000_001, "文章不存在");

    // ========== 文章分类相关 1-043-001-000 ==========
    ErrorCode ARTICLE_CATEGORY_NOT_EXISTS = new ErrorCode(1_043_001_000, "文章分类不存在");
    ErrorCode ARTICLE_CATEGORY_PARENT_NOT_EXISTS = new ErrorCode(1_043_001_001, "父分类不存在");
    ErrorCode ARTICLE_CATEGORY_PARENT_NOT_FIRST_LEVEL = new ErrorCode(1_043_001_002, "父分类不能是二级分类");
    ErrorCode ARTICLE_CATEGORY_EXISTS_CHILDREN = new ErrorCode(1_043_001_003, "存在子分类，无法删除");
    ErrorCode ARTICLE_CATEGORY_DISABLED = new ErrorCode(1_043_001_004, "文章分类({})已禁用，无法使用");
    ErrorCode ARTICLE_CATEGORY_HAVE_BIND_ARTICLES = new ErrorCode(1_043_001_005, "分类下存在文章，无法删除");
    ErrorCode ARTICLE_SAVE_FAIL_CATEGORY_LEVEL_ERROR = new ErrorCode(1_043_001_006,
            "文章分类不正确，原因：必须使用第二级的文章分类及以下");
    ErrorCode ARTICLE_CATEGORY_NAME_DUPLICATE = new ErrorCode(1_043_001_007, "已经存在该分类名称的文章分类");
    ErrorCode ARTICLE_CATEGORY_PARENT_IS_CHILD = new ErrorCode(1_043_001_008, "不能设置自己的子ArticlesCategory" +
            "为父ArticlesCategory");
    ErrorCode ARTICLE_CATEGORY_PARENT_ERROR = new ErrorCode(1_043_001_009, "不能设置自己为父文章分类");

    ErrorCode ARTICLE_SUFFIX_NOT_EXISTS = new ErrorCode(1_044_001_001, "文章后缀不存在");

    ErrorCode TRIP_ITINERARY_NOT_EXISTS = new ErrorCode(1_045_000_001, "旅行行程不存在");

    // ========== 语音识别相关 1-046-000-000 ==========
    ErrorCode ASR_AUDIO_EMPTY = new ErrorCode(1_046_000_000, "语音文件不能为空");
    ErrorCode ASR_AUDIO_TOO_LARGE = new ErrorCode(1_046_000_001, "语音文件不能超过 {} MB");
    ErrorCode ASR_AUDIO_FORMAT_UNSUPPORTED = new ErrorCode(1_046_000_002, "不支持的语音格式");
    ErrorCode ASR_SAMPLE_RATE_UNSUPPORTED = new ErrorCode(1_046_000_003, "仅支持 8000 或 16000 Hz 采样率");
    ErrorCode ASR_NOT_CONFIGURED = new ErrorCode(1_046_000_004, "语音识别服务未配置");
    ErrorCode ASR_SERVICE_ERROR = new ErrorCode(1_046_000_005, "语音识别失败，请稍后重试");
    ErrorCode ASR_RESULT_EMPTY = new ErrorCode(1_046_000_006, "未识别到有效语音");

    // ========== 行程与轮播管理 1-047-000-000 ==========
    ErrorCode ITINERARY_NOT_EXISTS = new ErrorCode(1_047_000_001, "行程不存在");
    ErrorCode ITINERARY_CATEGORY_NOT_EXISTS = new ErrorCode(1_047_000_002, "行程类别不存在");
    ErrorCode ITINERARY_DAY_ITEM_NOT_EXISTS = new ErrorCode(1_047_000_003, "行程内容不存在");
    ErrorCode SLIDER_NOT_EXISTS = new ErrorCode(1_047_000_004, "轮播不存在");
    ErrorCode SLIDER_ITEM_NOT_EXISTS = new ErrorCode(1_047_000_005, "轮播图不存在");
    ErrorCode USER_ITINERARY_NOT_EXISTS = new ErrorCode(1_047_000_006, "用户行程不存在");

}
