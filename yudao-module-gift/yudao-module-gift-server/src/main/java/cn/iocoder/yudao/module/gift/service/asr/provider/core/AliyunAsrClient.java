package cn.iocoder.yudao.module.gift.service.asr.provider.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.gift.service.asr.provider.config.AliyunAsrProperties;
import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** 阿里云百炼 Paraformer 短音频识别客户端。 */
public class AliyunAsrClient {

    private final AliyunAsrProperties properties;

    public AliyunAsrClient(AliyunAsrProperties properties) {
        this.properties = properties;
    }

    public Result transcribe(File audioFile, String format, int sampleRate) throws Exception {
        RecognitionParam param = RecognitionParam.builder()
                .apiKey(properties.getApiKey())
                .model(properties.getModel())
                .format(format)
                .sampleRate(sampleRate)
                .build();
        Recognition recognition = new Recognition();
        String response = recognition.call(param, audioFile);
        return new Result(extractText(response), recognition.getLastRequestId());
    }

    static String extractText(String response) {
        JsonNode root = JsonUtils.parseTree(response);
        JsonNode sentences = root == null ? null : root.path("sentences");
        if (sentences == null || !sentences.isArray()) {
            return null;
        }
        List<String> texts = new ArrayList<>();
        for (JsonNode sentence : sentences) {
            String text = sentence.path("text").asText();
            if (StrUtil.isNotBlank(text)) {
                texts.add(text.trim());
            }
        }
        return StrUtil.join("", texts);
    }

    public record Result(String text, String requestId) {
    }

}
