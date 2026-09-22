package cn.iocoder.yudao.module.gift.service.asr;

import org.springframework.web.multipart.MultipartFile;

public interface AsrService {

    Result transcribe(MultipartFile audio, Integer sampleRate);

    record Result(String text, String requestId) {
    }

}
