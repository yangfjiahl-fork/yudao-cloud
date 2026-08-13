package cn.iocoder.yudao.module.trade.service.delivery;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderLogisticsDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderLogisticsMapper;
import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.ExpressTrackCacheKeyUtils;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackRespDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.enums.ExpressClientEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeOrderLogisticsServiceTest {

    @Mock
    private TradeOrderLogisticsMapper tradeOrderLogisticsMapper;

    private TradeOrderLogisticsService service;

    @BeforeEach
    void setUp() {
        service = new TradeOrderLogisticsService();
        ReflectionTestUtils.setField(service, "tradeOrderLogisticsMapper", tradeOrderLogisticsMapper);
        ReflectionTestUtils.setField(service, "tradeExpressProperties", new TradeExpressProperties()
                .setClient(ExpressClientEnum.ALIYUN_KD_100));
    }

    @Test
    void testInitLogistics_insert() {
        ExpressTrackQueryReqDTO reqDTO = createReqDTO();
        service.initLogistics(100L, 10L, reqDTO);

        ArgumentCaptor<TradeOrderLogisticsDO> captor = ArgumentCaptor.forClass(TradeOrderLogisticsDO.class);
        verify(tradeOrderLogisticsMapper).insert(captor.capture());
        TradeOrderLogisticsDO logistics = captor.getValue();
        assertEquals(100L, logistics.getOrderId());
        assertEquals(10L, logistics.getLogisticsId());
        assertEquals(ExpressClientEnum.ALIYUN_KD_100.getCode(), logistics.getProvider());
        assertEquals(TradeOrderLogisticsDO.SUBSCRIBE_STATUS_PENDING, logistics.getSubscribeStatus());
        assertEquals(ExpressTrackCacheKeyUtils.build("zhongtong", "735587678765", "13800138000"),
                logistics.getCacheKey());
    }

    @Test
    void testGetTrackList() {
        ExpressTrackRespDTO track = new ExpressTrackRespDTO()
                .setTime(LocalDateTime.of(2025, 6, 16, 7, 34, 28)).setContent("快件已发往转运中心");
        when(tradeOrderLogisticsMapper.selectByCacheKey("cache-key"))
                .thenReturn(new TradeOrderLogisticsDO().setTrackData(JsonUtils.toJsonString(List.of(track)))
                        .setLastNotifyTime(LocalDateTime.now().minusHours(1)));

        List<ExpressTrackRespDTO> result = service.getTrackList("cache-key");

        assertEquals(1, result.size());
        assertEquals(track.getTime(), result.get(0).getTime());
        assertEquals(track.getContent(), result.get(0).getContent());
    }

    @Test
    void testGetTrackList_expired() {
        ExpressTrackRespDTO track = new ExpressTrackRespDTO().setContent("旧物流轨迹");
        when(tradeOrderLogisticsMapper.selectByCacheKey("cache-key"))
                .thenReturn(new TradeOrderLogisticsDO().setTrackData(JsonUtils.toJsonString(List.of(track)))
                        .setLastQueryTime(LocalDateTime.now().minusHours(13)));

        List<ExpressTrackRespDTO> result = service.getTrackList("cache-key");

        assertNull(result);
    }

    @Test
    void testSaveNotify_recordNotFound() {
        when(tradeOrderLogisticsMapper.updateByCacheKey(any(), any())).thenReturn(0);

        assertThrows(IllegalStateException.class,
                () -> service.saveNotify("cache-key", "3", List.of()));
    }

    private static ExpressTrackQueryReqDTO createReqDTO() {
        return new ExpressTrackQueryReqDTO().setExpressCode("zhongtong")
                .setLogisticsNo("735587678765").setPhone("13800138000");
    }

}
