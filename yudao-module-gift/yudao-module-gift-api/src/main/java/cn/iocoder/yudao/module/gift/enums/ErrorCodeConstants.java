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
}
