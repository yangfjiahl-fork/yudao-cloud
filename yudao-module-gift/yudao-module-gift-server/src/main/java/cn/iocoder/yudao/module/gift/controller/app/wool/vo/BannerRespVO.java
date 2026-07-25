/*
 * Copyright (C) 2019 ~ 2026 MeiTuan. All Rights Reserved.
 *
 */
package cn.iocoder.yudao.module.gift.controller.app.wool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 首页banner配置
 *
 * @author YangFeng(calvin)
 * @version v1.0
 * @date 2026/7/25 21:22
 */
@Schema(description = "用户 APP - 羊毛 首页banner配置 VO")
@Data
public class BannerRespVO {

    /**
     * 链接内容，productId、pageLink
     */
    @Schema(description = "链接内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "13660")
    private String jumpPage;

    /**
     * Image图片地址
     */
    @Schema(description = "Image图片地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "http://")
    private String imgUrl;
}
