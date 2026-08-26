package cn.iocoder.yudao.module.gift.service.trip;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TripAgentFormatUtilsTest {

    @Test
    void parseMap_shouldNormalizeMarkdownAndMalformedObjectKey() {
        String content = """
                ```json
                {"summary":"三日行程","daily_itinerary":[{"day":1,activities":["抵达"]}]}
                ```
                """;

        Map<String, Object> result = TripAgentFormatUtils.parseMap(content);

        assertEquals("三日行程", result.get("summary"));
        List<?> dailyItinerary = (List<?>) result.get("daily_itinerary");
        assertEquals(List.of("抵达"), ((Map<?, ?>) dailyItinerary.getFirst()).get("activities"));
    }

    @Test
    void parseMap_shouldExtractJsonFromLeadingAndTrailingText() {
        Map<String, Object> result = TripAgentFormatUtils.parseMap("以下是结果：{\"summary\":\"行程\"} 请查收");

        assertEquals("行程", result.get("summary"));
    }

    @Test
    void streamParser_shouldEmitSummaryDeltaAndCompletedDay() {
        List<String> events = new ArrayList<>();
        TripAgentStreamParser parser = new TripAgentStreamParser(event -> events.add(event.getEvent() + ":" + event.getContent()
                + ":" + event.getItemType()));

        parser.append("{\"summary\":\"杭州三");
        parser.append("日游\",\"daily_itinerary\":[{\"day\":1,\"title\":\"西湖\",\"activities\":[\"漫步\"]}");
        parser.append("]}");

        assertEquals(List.of("assistant_delta:杭州三:null", "assistant_delta:日游:null",
                "itinerary_day_started:正在规划第 1 天…:daily_itinerary",
                "itinerary_item:null:daily_itinerary"), events);
    }

    @Test
    void streamParser_shouldWaitForCompleteDayNumber() {
        List<String> events = new ArrayList<>();
        TripAgentStreamParser parser = new TripAgentStreamParser(event -> events.add(event.getEvent() + ":" + event.getContent()));

        parser.append("{\"daily_itinerary\":[{\"day\":1");
        parser.append("0,\"title\":\"返程\"}");

        assertEquals(List.of("itinerary_day_started:正在规划第 10 天…", "itinerary_item:null"), events);
    }

}
