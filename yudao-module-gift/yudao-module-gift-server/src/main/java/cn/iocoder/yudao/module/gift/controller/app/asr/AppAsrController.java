package cn.iocoder.yudao.module.gift.controller.app.asr;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.gift.controller.app.asr.vo.AppAsrRespVO;
import cn.iocoder.yudao.module.gift.service.asr.AsrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 语音识别")
@RestController
@RequestMapping("/asr")
@Validated
public class AppAsrController {

    @Resource
    private AsrService asrService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "将短音频识别为文字")
    @Parameter(name = "sampleRate", description = "采样率，支持 8000 或 16000，默认 16000", example = "16000")
    public CommonResult<AppAsrRespVO> transcribe(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam(value = "sampleRate", required = false) Integer sampleRate) {
        AsrService.Result result = asrService.transcribe(audio, sampleRate);
        return success(new AppAsrRespVO().setText(result.text()).setRequestId(result.requestId()));
    }

}
