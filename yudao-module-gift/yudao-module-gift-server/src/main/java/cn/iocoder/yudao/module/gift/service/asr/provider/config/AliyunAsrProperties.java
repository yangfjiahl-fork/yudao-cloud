package cn.iocoder.yudao.module.gift.service.asr.provider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "yudao.gift.asr")
@Data
public class AliyunAsrProperties {

    /** 百炼 API Key，只允许由部署环境注入。 */
    private String apiKey;

    /** Paraformer 实时语音识别模型。 */
    private String model = "paraformer-realtime-v2";

    /** 单个短音频文件大小上限。 */
    private DataSize maxFileSize = DataSize.ofMegabytes(10);

}
