package cn.iocoder.yudao.module.trade.service.delivery;

import cn.iocoder.yudao.module.trade.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.ExpressClientFactory;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.ExpressSubscribeClient;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackNotifyRespDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackRespDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpressSubscriptionServiceTest {

    private static final String CACHE_KEY = "a".repeat(64);

    @Mock
    private ExpressClientFactory expressClientFactory;
    @Mock
    private ExpressSubscribeClient expressSubscribeClient;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;
    @Mock
    private TradeOrderLogisticsService tradeOrderLogisticsService;

    private ExpressSubscriptionService service;

    @BeforeEach
    void setUp() {
        service = new ExpressSubscriptionService();
        ReflectionTestUtils.setField(service, "expressClientFactory", expressClientFactory);
        ReflectionTestUtils.setField(service, "tradeExpressProperties", new TradeExpressProperties()
                .setAliyunKd100(new TradeExpressProperties.AliyunKd100Config().setCallbackToken("test-token")));
        ReflectionTestUtils.setField(service, "cacheManager", cacheManager);
        ReflectionTestUtils.setField(service, "tradeOrderLogisticsService", tradeOrderLogisticsService);
    }

    @Test
    void testProcessNotify_updateDatabaseThenEvictCache() {
        List<ExpressTrackRespDTO> tracks = List.of(new ExpressTrackRespDTO().setContent("已签收"));
        when(expressClientFactory.getDefaultExpressClient()).thenReturn(expressSubscribeClient);
        when(expressSubscribeClient.parseExpressTrackNotify("{}"))
                .thenReturn(new ExpressTrackNotifyRespDTO().setState("3").setTracks(tracks));
        when(cacheManager.getCache(RedisKeyConstants.EXPRESS_TRACK)).thenReturn(cache);

        service.processNotify(1L, CACHE_KEY, "test-token", "{}");

        InOrder inOrder = inOrder(tradeOrderLogisticsService, cache);
        inOrder.verify(tradeOrderLogisticsService).saveNotify(CACHE_KEY, "3", tracks);
        inOrder.verify(cache).evict(CACHE_KEY);
    }

}
