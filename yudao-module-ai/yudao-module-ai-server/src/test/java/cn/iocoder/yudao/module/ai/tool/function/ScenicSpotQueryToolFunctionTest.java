package cn.iocoder.yudao.module.ai.tool.function;

import cn.iocoder.yudao.module.ai.framework.ai.config.YudaoAiProperties;
import cn.iocoder.yudao.module.ai.framework.ai.core.scenic.ScenicSpotQueryClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.scenic.ScenicSpotQueryClientFacade;
import cn.iocoder.yudao.module.ai.framework.ai.core.scenic.aliyun.AliyunScenicSpotQueryClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.scenic.gaode.GaodeScenicSpotQueryClient;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ScenicSpotQueryToolFunctionTest {

    private static final String BASE_URL = "https://example.com";
    private static final String AMAP_URL = "https://example.com/v5/place/text";

    private MockRestServiceServer server;
    private ScenicSpotQueryToolFunction function;
    private YudaoAiProperties.ScenicSpot config;
    private ConfigApi configApi;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        config = new YudaoAiProperties.ScenicSpot().setBaseUrl(BASE_URL).setAppCode("test-app-code")
                .setAmapUrl(AMAP_URL).setAmapKey("test-amap-key");
        configApi = mock(ConfigApi.class);
        when(configApi.getConfigValueByKey(ScenicSpotQueryClientFacade.QUERY_FROM_CONFIG_KEY))
                .thenReturn(success(null));
        AliyunScenicSpotQueryClient aliyunClient = new AliyunScenicSpotQueryClient(restTemplate, config);
        GaodeScenicSpotQueryClient gaodeClient = new GaodeScenicSpotQueryClient(restTemplate, config);
        function = new ScenicSpotQueryToolFunction(
                new ScenicSpotQueryClientFacade(configApi, aliyunClient, gaodeClient));
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void testQueryScenicSpots() {
        server.expect(requestTo(BASE_URL + "/area/scenic-spots"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "APPCODE test-app-code"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string(containsString("keyword=%E8%A5%BF%E6%B9%96")))
                .andExpect(content().string(containsString("page=2")))
                .andRespond(withSuccess("""
                        {"msg":"成功","code":200,"taskNo":"task-1","data":{"allNum":"1","list":[{"name":"西湖"}]}}
                        """, MediaType.APPLICATION_JSON));

        ScenicSpotQueryClient.Response response = function.apply(new ScenicSpotQueryClient.Request()
                .setType(ScenicSpotQueryClient.QueryType.SCENIC_SPOT).setKeyword("西湖").setPage(2));

        assertTrue(response.getSuccess());
        assertEquals("200", response.getCode());
        assertEquals("task-1", response.getTaskNo());
        assertEquals("西湖", response.getData().path("list").get(0).path("name").asText());
    }

    @Test
    void testQueryCities() {
        server.expect(requestTo(BASE_URL + "/area/city"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("proId=16"))
                .andRespond(withSuccess("""
                        {"msg":"成功","code":200,"data":{"list":[{"name":"南京","id":"224"}]}}
                        """, MediaType.APPLICATION_JSON));

        ScenicSpotQueryClient.Response response = function.apply(new ScenicSpotQueryClient.Request()
                .setType(ScenicSpotQueryClient.QueryType.CITY).setProId("16"));

        assertTrue(response.getSuccess());
        assertEquals("南京", response.getData().path("list").get(0).path("name").asText());
    }

    @Test
    void testQueryProvinces() {
        server.expect(requestTo(BASE_URL + "/area/province"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(""))
                .andRespond(withSuccess("""
                        {"msg":"成功","code":200,"data":{"list":[{"name":"江苏","id":"16"}]}}
                        """, MediaType.APPLICATION_JSON));

        ScenicSpotQueryClient.Response response = function.apply(new ScenicSpotQueryClient.Request()
                .setType(ScenicSpotQueryClient.QueryType.PROVINCE));

        assertTrue(response.getSuccess());
        assertEquals("江苏", response.getData().path("list").get(0).path("name").asText());
    }

    @Test
    void testQueryDistricts_providerFailed() {
        server.expect(requestTo(BASE_URL + "/area/district"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("cityId=224"))
                .andRespond(withSuccess("{\"msg\":\"参数不正确\",\"code\":400}", MediaType.APPLICATION_JSON));

        ScenicSpotQueryClient.Response response = function.apply(new ScenicSpotQueryClient.Request()
                .setType(ScenicSpotQueryClient.QueryType.DISTRICT).setCityId("224"));

        assertFalse(response.getSuccess());
        assertEquals("400", response.getCode());
        assertEquals("参数不正确", response.getMessage());
    }

    @Test
    void testQueryCitiesWithoutProvinceId() {
        ScenicSpotQueryClient.Response response = function.apply(new ScenicSpotQueryClient.Request()
                .setType(ScenicSpotQueryClient.QueryType.CITY));

        assertFalse(response.getSuccess());
        assertEquals("查询城市列表时 proId 不能为空", response.getMessage());
    }

    @Test
    void testQueryScenicSpotsByAmap() {
        when(configApi.getConfigValueByKey(ScenicSpotQueryClientFacade.QUERY_FROM_CONFIG_KEY))
                .thenReturn(success(" GaOdE "));
        server.expect(requestTo(startsWith(AMAP_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("key", "test-amap-key"))
                .andExpect(queryParam("keywords", UriUtils.encodeQueryParam("西湖", StandardCharsets.UTF_8)))
                .andExpect(queryParam("types", "110000"))
                .andExpect(queryParam("region", UriUtils.encodeQueryParam("杭州市", StandardCharsets.UTF_8)))
                .andExpect(queryParam("city_limit", "true"))
                .andExpect(queryParam("show_fields", "business,photos"))
                .andExpect(queryParam("page_size", "20"))
                .andExpect(queryParam("page_num", "2"))
                .andRespond(withSuccess("""
                        {"status":"1","info":"OK","infocode":"10000","count":"1",
                         "pois":[{"id":"B0FFGQ6XQH","name":"杭州西湖风景名胜区","location":"120.139186,30.240736"}]}
                        """, MediaType.APPLICATION_JSON));

        ScenicSpotQueryClient.Response response = function.apply(new ScenicSpotQueryClient.Request()
                .setType(ScenicSpotQueryClient.QueryType.SCENIC_SPOT).setKeyword("西湖")
                .setRegion("杭州市").setPage(2));

        assertTrue(response.getSuccess());
        assertEquals("10000", response.getCode());
        assertEquals("1", response.getData().path("allNum").asText());
        assertEquals("杭州西湖风景名胜区", response.getData().path("list").get(0).path("name").asText());
    }

    @Test
    void testQueryScenicSpots_gaodeFailureDoesNotFallback() {
        when(configApi.getConfigValueByKey(ScenicSpotQueryClientFacade.QUERY_FROM_CONFIG_KEY))
                .thenReturn(success("gaode"));
        server.expect(requestTo(startsWith(AMAP_URL)))
                .andRespond(withSuccess("{\"status\":\"0\",\"info\":\"INVALID_USER_KEY\",\"infocode\":\"10001\"}",
                        MediaType.APPLICATION_JSON));

        ScenicSpotQueryClient.Response response = function.apply(new ScenicSpotQueryClient.Request()
                .setType(ScenicSpotQueryClient.QueryType.SCENIC_SPOT).setKeyword("西湖"));

        assertFalse(response.getSuccess());
        assertEquals("10001", response.getCode());
        assertEquals("INVALID_USER_KEY", response.getMessage());
    }

}
