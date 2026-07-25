/*
 * Copyright (C) 2019 ~ 2026 MeiTuan. All Rights Reserved.
 *
 */
package cn.iocoder.yudao.module.gift.controller.app.wool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 首页进度条
 *
 * @author YangFeng(calvin)
 * @version v1.0
 * @date 2026/7/25 21:45
 */
@Schema(description = "用户 APP - 羊毛 首页进度条配置 VO")
@Data
public class ProgressIconRespVO {

    /**
     * 羊毛数量
     */
    @Schema(description = "羊毛数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "800")
    private int woolCnt;

    /**
     * Image图片地址
     */
    @Schema(description = "Image图片地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "http://")
    private String imgUrl;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "蓝牙耳机")
    private String productName;
}
