package cn.iocoder.yudao.module.gift.framework.asr.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AliyunAsrClientTest {

    @Test
    void extractTextJoinsSentences() {
        String response = """
                {"sentences":[{"text":"我想去云南，"},{"text":"玩七天。"}]}
                """;

        assertEquals("我想去云南，玩七天。", AliyunAsrClient.extractText(response));
    }

    @Test
    void extractTextReturnsNullWithoutSentences() {
        assertNull(AliyunAsrClient.extractText("{}"));
    }

}
