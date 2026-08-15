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

}
