package cn.iocoder.yudao.module.gift.controller.app.asr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "用户 APP - 语音识别响应")
@Data
@Accessors(chain = true)
public class AppAsrRespVO {

    @Schema(description = "识别文字", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "我想去云南玩七天")
    private String text;

    @Schema(description = "阿里云请求编号，用于问题排查", example = "b7786f59-xxxx")
    private String requestId;

}
