package cn.iocoder.yudao.module.gift.framework.trip.provider.scenic;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 景点查询客户端
 */
public interface ScenicSpotQueryClient {

    /**
     * 使用统一入参和出参查询景点或行政区划信息
     */
    Response query(Request request);

    enum QueryType {
        SCENIC_SPOT,
        PROVINCE,
        CITY,
        DISTRICT
    }

    @Data
    @JsonClassDescription("查询全国景点或查询省份、城市、区县列表")
    class Request {

        @JsonProperty(required = true, value = "type")
        @JsonPropertyDescription("查询类型：SCENIC_SPOT=景点，PROVINCE=省份列表，CITY=城市列表，DISTRICT=区县列表")
        private QueryType type;

        @JsonProperty(value = "keyword")
        @JsonPropertyDescription("景点名称，例如：西湖；仅 SCENIC_SPOT 可用")
        private String keyword;

        @JsonProperty(value = "proId")
        @JsonPropertyDescription("省份 ID；查询 CITY 时必填，查询 SCENIC_SPOT 时可选")
        private String proId;

        @JsonProperty(value = "cityId")
        @JsonPropertyDescription("城市 ID；查询 DISTRICT 时必填，查询 SCENIC_SPOT 时可选")
        private String cityId;

        @JsonProperty(value = "areaId")
        @JsonPropertyDescription("区县 ID；仅 SCENIC_SPOT 可用")
        private String areaId;

        @JsonProperty(value = "region")
        @JsonPropertyDescription("搜索区域，可填写城市中文名、citycode 或 adcode，例如：杭州市、330100")
        private String region;

        @JsonProperty(value = "page")
        @JsonPropertyDescription("景点结果页码；仅 SCENIC_SPOT 可用")
        private Integer page;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class Response {

        private Boolean success;
        private QueryType type;
        private String code;
        private String message;
        private String taskNo;
        private JsonNode data;

        public static Response failure(QueryType type, String message) {
            return new Response(false, type, null, message, null, null);
        }

    }

}
