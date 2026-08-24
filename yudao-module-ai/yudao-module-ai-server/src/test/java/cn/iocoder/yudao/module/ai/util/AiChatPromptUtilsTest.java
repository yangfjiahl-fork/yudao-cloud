package cn.iocoder.yudao.module.ai.util;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiChatPromptUtilsTest {

    @Test
    void testBuildSystemMessage() {
        String result = AiChatPromptUtils.buildSystemMessage("地区：{{ provinceName }}{{cityName}}{{districtName}}；未传：{{unknown}}",
                Map.of("provinceName", "浙江省", "cityName", "杭州市", "districtName", "西湖区"));

        assertTrue(result.startsWith("地区：浙江省杭州市西湖区；未传：{{unknown}}"));
        assertTrue(result.matches("(?s).*当前系统时间：\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}（Asia/Shanghai）。"));
    }

    @Test
    void testRenderTemplateWithNullValue() {
        assertEquals("地区：", AiChatPromptUtils.renderTemplate("地区：{{provinceName}}",
                Collections.singletonMap("provinceName", null)));
    }

}
