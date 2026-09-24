package cn.iocoder.yudao.module.gift.service.asr;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.gift.service.asr.provider.config.AliyunAsrProperties;
import cn.iocoder.yudao.module.gift.service.asr.provider.core.AliyunAsrClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;

import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.ASR_AUDIO_EMPTY;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.ASR_AUDIO_FORMAT_UNSUPPORTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AsrServiceImplTest {

    private AliyunAsrClient aliyunAsrClient;
    private AsrServiceImpl service;

    @BeforeEach
    void setUp() {
        AliyunAsrProperties properties = new AliyunAsrProperties().setApiKey("test-key");
        aliyunAsrClient = mock(AliyunAsrClient.class);
        service = new AsrServiceImpl();
        ReflectionTestUtils.setField(service, "aliyunAsrClient", aliyunAsrClient);
        ReflectionTestUtils.setField(service, "properties", properties);
    }

    @Test
    void transcribeUsesDefaultSampleRateAndDeletesTemporaryFile() throws Exception {
        MockMultipartFile audio = new MockMultipartFile(
                "audio", "voice.wav", "audio/wav", new byte[]{1, 2, 3});
        when(aliyunAsrClient.transcribe(isA(File.class), eq("wav"), eq(16000)))
                .thenAnswer(invocation -> {
                    File temporaryFile = invocation.getArgument(0);
                    assertFalse(temporaryFile.length() == 0);
                    return new AliyunAsrClient.Result("去云南玩七天", "request-1");
                });

        AsrService.Result result = service.transcribe(audio, null);

        assertEquals("去云南玩七天", result.text());
        assertEquals("request-1", result.requestId());
        verify(aliyunAsrClient).transcribe(isA(File.class), eq("wav"), eq(16000));
    }

    @Test
    void transcribeInfersM4aAsAac() throws Exception {
        MockMultipartFile audio = new MockMultipartFile(
                "audio", "voice.m4a", "audio/mp4", new byte[]{1});
        when(aliyunAsrClient.transcribe(isA(File.class), eq("aac"), eq(8000)))
                .thenReturn(new AliyunAsrClient.Result("测试", "request-2"));

        service.transcribe(audio, 8000);

        verify(aliyunAsrClient).transcribe(isA(File.class), eq("aac"), eq(8000));
    }

    @Test
    void transcribeRejectsEmptyAudio() {
        MockMultipartFile audio = new MockMultipartFile(
                "audio", "voice.wav", "audio/wav", new byte[0]);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.transcribe(audio, null));

        assertEquals(ASR_AUDIO_EMPTY.getCode(), exception.getCode());
    }

    @Test
    void transcribeRejectsUnsupportedFormat() {
        MockMultipartFile audio = new MockMultipartFile(
                "audio", "voice.mp3", "audio/mpeg", new byte[]{1});

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.transcribe(audio, null));

        assertEquals(ASR_AUDIO_FORMAT_UNSUPPORTED.getCode(), exception.getCode());
    }

}
