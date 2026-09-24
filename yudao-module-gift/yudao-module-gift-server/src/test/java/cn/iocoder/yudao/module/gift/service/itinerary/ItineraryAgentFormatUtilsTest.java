package cn.iocoder.yudao.module.gift.service.itinerary;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItineraryAgentFormatUtilsTest {

    @Test
    void parseMap_shouldNormalizeMarkdownAndMalformedObjectKey() {
        String content = """
                ```json
                {"summary":"三日行程","daily_itinerary":[{"day":1,activities":["抵达"]}]}
                ```
                """;

        Map<String, Object> result = ItineraryAgentFormatUtils.parseMap(content);

        assertEquals("三日行程", result.get("summary"));
        List<?> dailyItinerary = (List<?>) result.get("daily_itinerary");
        assertEquals(List.of("抵达"), ((Map<?, ?>) dailyItinerary.get(0)).get("activities"));
    }

    @Test
    void parseMap_shouldExtractJsonFromLeadingAndTrailingText() {
        Map<String, Object> result = ItineraryAgentFormatUtils.parseMap("以下是结果：{\"summary\":\"行程\"} 请查收");

        assertEquals("行程", result.get("summary"));
    }

}
