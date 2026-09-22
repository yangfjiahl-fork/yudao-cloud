package cn.iocoder.yudao.module.gift.service.asr;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.framework.asr.config.AliyunAsrProperties;
import cn.iocoder.yudao.module.gift.framework.asr.core.AliyunAsrClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.ASR_AUDIO_EMPTY;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.ASR_AUDIO_FORMAT_UNSUPPORTED;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.ASR_AUDIO_TOO_LARGE;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.ASR_NOT_CONFIGURED;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.ASR_RESULT_EMPTY;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.ASR_SAMPLE_RATE_UNSUPPORTED;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.ASR_SERVICE_ERROR;

@Service
@Slf4j
public class AsrServiceImpl implements AsrService {

    static final int DEFAULT_SAMPLE_RATE = 16000;
    private static final Set<Integer> SUPPORTED_SAMPLE_RATES = Set.of(8000, 16000);
    private static final Set<String> SUPPORTED_FORMATS = Set.of("pcm", "wav", "opus", "speex", "aac", "amr");
    private static final Map<String, String> FORMAT_ALIASES = Map.of(
            "wave", "wav",
            "m4a", "aac");

    @Resource
    private AliyunAsrClient aliyunAsrClient;
    @Resource
    private AliyunAsrProperties properties;

    @Override
    public Result transcribe(MultipartFile audio, Integer sampleRate) {
        validateAudio(audio);
        int resolvedSampleRate = sampleRate == null ? DEFAULT_SAMPLE_RATE : sampleRate;
        if (!SUPPORTED_SAMPLE_RATES.contains(resolvedSampleRate)) {
            throw exception(ASR_SAMPLE_RATE_UNSUPPORTED);
        }
        String format = resolveFormat(audio.getOriginalFilename(), audio.getContentType());
        if (format == null) {
            throw exception(ASR_AUDIO_FORMAT_UNSUPPORTED);
        }
        if (StrUtil.isBlank(properties.getApiKey())) {
            throw exception(ASR_NOT_CONFIGURED);
        }
        Path temporaryFile = null;
        try {
            temporaryFile = Files.createTempFile("gift-asr-", "." + format);
            audio.transferTo(temporaryFile);
            AliyunAsrClient.Result result = aliyunAsrClient.transcribe(
                    temporaryFile.toFile(), format, resolvedSampleRate);
            if (result == null || StrUtil.isBlank(result.text())) {
                throw exception(ASR_RESULT_EMPTY);
            }
            return new Result(result.text(), result.requestId());
        } catch (IOException e) {
            log.warn("[transcribe][写入临时语音文件失败]", e);
            throw exception(ASR_SERVICE_ERROR);
        } catch (Exception e) {
            if (e instanceof cn.iocoder.yudao.framework.common.exception.ServiceException serviceException) {
                throw serviceException;
            }
            log.warn("[transcribe][调用阿里云语音识别失败]", e);
            throw exception(ASR_SERVICE_ERROR);
        } finally {
            deleteTemporaryFile(temporaryFile);
        }
    }

    private void validateAudio(MultipartFile audio) {
        if (audio == null || audio.isEmpty()) {
            throw exception(ASR_AUDIO_EMPTY);
        }
        if (audio.getSize() > properties.getMaxFileSize().toBytes()) {
            throw exception(ASR_AUDIO_TOO_LARGE, properties.getMaxFileSize().toMegabytes());
        }
    }

    static String resolveFormat(String originalFilename, String contentType) {
        String extension = StrUtil.blankToDefault(FileNameUtil.extName(originalFilename), "")
                .toLowerCase(Locale.ROOT);
        String format = FORMAT_ALIASES.getOrDefault(extension, extension);
        if (SUPPORTED_FORMATS.contains(format)) {
            return format;
        }
        if (StrUtil.isBlank(contentType)) {
            return null;
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "audio/wav", "audio/x-wav", "audio/wave" -> "wav";
            case "audio/aac", "audio/mp4", "audio/x-m4a" -> "aac";
            case "audio/opus", "audio/ogg" -> "opus";
            case "audio/amr" -> "amr";
            case "audio/speex" -> "speex";
            case "audio/pcm", "audio/l16" -> "pcm";
            default -> null;
        };
    }

    private void deleteTemporaryFile(Path temporaryFile) {
        if (temporaryFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException e) {
            log.warn("[deleteTemporaryFile][删除临时语音文件失败][file={}]", temporaryFile.getFileName(), e);
        }
    }

}
