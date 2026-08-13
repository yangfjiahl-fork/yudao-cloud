package cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl.aliyunkd100;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.ExpressTrackCacheKeyUtils;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackNotifyRespDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackRespDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.enums.ExpressClientEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AliyunKd100ExpressClientTest {

    private static final String QUERY_URL = "https://example.com/query";
    private static final String SUBSCRIBE_URL = "https://example.com/subscribe";

    private MockRestServiceServer server;
    private AliyunKd100ExpressClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        TradeExpressProperties.AliyunKd100Config config = new TradeExpressProperties.AliyunKd100Config()
                .setAppCode("test-app-code")
                .setQueryUrl(QUERY_URL)
                .setSubscribeUrl(SUBSCRIBE_URL)
                .setCallbackUrl("https://example.com/admin-api/trade/express/aliyun-kd100/notify")
                .setCallbackToken("test-token")
                .setResultV2("4");
        client = new AliyunKd100ExpressClient(restTemplate, config);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
        server.verify();
    }

    @Test
    void testGetExpressTrackList() {
        server.expect(requestTo(QUERY_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "APPCODE test-app-code"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string(containsString("com=zhongtong")))
                .andExpect(content().string(containsString("num=735587678765")))
                .andExpect(content().string(containsString("phone=13800138000")))
                .andRespond(withSuccess("""
                        {"message":"ok","nu":"735587678765","com":"zhongtong","status":"200",
                         "data":[{"time":"2025-06-16 07:34:28","context":"快件已发往转运中心"}],"state":"0"}
                        """, MediaType.APPLICATION_JSON));

        List<ExpressTrackRespDTO> tracks = client.getExpressTrackList(new ExpressTrackQueryReqDTO()
                .setExpressCode("ZHONGTONG").setLogisticsNo("735587678765").setPhone("13800138000"));

        assertEquals(1, tracks.size());
        assertEquals(LocalDateTime.of(2025, 6, 16, 7, 34, 28), tracks.get(0).getTime());
        assertEquals("快件已发往转运中心", tracks.get(0).getContent());
    }

    @Test
    void testGetExpressTrackList_statusFailed() {
        server.expect(requestTo(QUERY_URL))
                .andRespond(withSuccess("{\"message\":\"参数错误\",\"state\":\"0\",\"status\":\"400\"}",
                        MediaType.APPLICATION_JSON));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> client.getExpressTrackList(new ExpressTrackQueryReqDTO().setExpressCode("zhongtong")
                        .setLogisticsNo("735587678765")));
        assertEquals("快递查询返回失败，原因：参数错误", exception.getMessage());
    }

    @Test
    void testSubscribeExpressTrack() {
        TenantContextHolder.setTenantId(1L);
        String cacheKey = ExpressTrackCacheKeyUtils.build("zhongtong", "735587678765", "13800138000");
        server.expect(requestTo(SUBSCRIBE_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "APPCODE test-app-code"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"company":"zhongtong","number":"735587678765","parameters":{
                          "callbackurl":"https://example.com/admin-api/trade/express/aliyun-kd100/notify?tenant-id=1&cache-key=%s&token=test-token",
                          "resultv2":"4","phone":"13800138000"}}
                        """.formatted(cacheKey)))
                .andRespond(withSuccess("{\"result\":true,\"returnCode\":\"200\",\"message\":\"提交成功\"}",
                        MediaType.APPLICATION_JSON));

        client.subscribeExpressTrack(new ExpressTrackQueryReqDTO().setExpressCode("zhongtong")
                .setLogisticsNo("735587678765").setPhone("13800138000"));
    }

    @Test
    void testParseExpressTrackNotify() {
        ExpressTrackNotifyRespDTO notify = client.parseExpressTrackNotify("""
                {"status":"polling","message":"寄件","lastResult":{"message":"ok",
                 "nu":"735587678765","com":"zhongtong","status":"200","state":"0",
                 "data":[{"time":"2025-06-16 07:34:28","context":"快件已发往转运中心"}]}}
                """);

        assertEquals("zhongtong", notify.getExpressCode());
        assertEquals("735587678765", notify.getLogisticsNo());
        assertEquals("0", notify.getState());
        assertEquals(1, notify.getTracks().size());
        assertEquals("快件已发往转运中心", notify.getTracks().get(0).getContent());
    }

    @Test
    void testBindExpressClientEnum() {
        Binder binder = new Binder(new MapConfigurationPropertySource(
                Map.of("yudao.trade.express.client", "aliyun_kd100")));

        ExpressClientEnum clientEnum = binder.bind("yudao.trade.express.client", ExpressClientEnum.class).get();

        assertEquals(ExpressClientEnum.ALIYUN_KD_100, clientEnum);
    }

}
