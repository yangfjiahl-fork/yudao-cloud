package cn.iocoder.yudao.module.trade.service.delivery;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderLogisticsDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderLogisticsMapper;
import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.ExpressTrackCacheKeyUtils;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单物流持久化 Service
 */
@Service
public class TradeOrderLogisticsService {

    private static final int SUBSCRIBE_MESSAGE_MAX_LENGTH = 512;
    private static final int TRACK_REFRESH_INTERVAL_HOURS = 12;

    @Resource
    private TradeOrderLogisticsMapper tradeOrderLogisticsMapper;
    @Resource
    private TradeExpressProperties tradeExpressProperties;

    public void initLogistics(Long orderId, Long logisticsId, ExpressTrackQueryReqDTO reqDTO) {
        String cacheKey = buildCacheKey(reqDTO);
        TradeOrderLogisticsDO logistics = tradeOrderLogisticsMapper.selectByOrderId(orderId);
        if (logistics == null) {
            tradeOrderLogisticsMapper.insert(new TradeOrderLogisticsDO()
                    .setOrderId(orderId).setLogisticsId(logisticsId)
                    .setExpressCode(reqDTO.getExpressCode()).setLogisticsNo(reqDTO.getLogisticsNo())
                    .setCacheKey(cacheKey).setProvider(tradeExpressProperties.getClient().getCode())
                    .setSubscribeStatus(TradeOrderLogisticsDO.SUBSCRIBE_STATUS_PENDING));
            return;
        }
        tradeOrderLogisticsMapper.updateById(new TradeOrderLogisticsDO().setId(logistics.getId())
                .setLogisticsId(logisticsId).setExpressCode(reqDTO.getExpressCode())
                .setLogisticsNo(reqDTO.getLogisticsNo()).setCacheKey(cacheKey)
                .setProvider(tradeExpressProperties.getClient().getCode())
                .setSubscribeStatus(TradeOrderLogisticsDO.SUBSCRIBE_STATUS_PENDING)
                .setSubscribeMessage(""));
    }

    /**
     * 获取未过期的落库轨迹。返回 null 表示无轨迹或轨迹已经超过刷新间隔。
     */
    public List<ExpressTrackRespDTO> getTrackList(String cacheKey) {
        TradeOrderLogisticsDO logistics = tradeOrderLogisticsMapper.selectByCacheKey(cacheKey);
        if (logistics == null || StrUtil.isBlank(logistics.getTrackData())) {
            return null;
        }
        LocalDateTime trackUpdateTime = getTrackUpdateTime(logistics);
        if (trackUpdateTime == null
                || trackUpdateTime.isBefore(LocalDateTime.now().minusHours(TRACK_REFRESH_INTERVAL_HOURS))) {
            return null;
        }
        List<ExpressTrackRespDTO> tracks = JsonUtils.parseArray(logistics.getTrackData(), ExpressTrackRespDTO.class);
        return CollUtil.isEmpty(tracks) ? null : tracks;
    }

    public void saveQueryResult(Long orderId, Long logisticsId, ExpressTrackQueryReqDTO reqDTO,
                                List<ExpressTrackRespDTO> tracks) {
        String cacheKey = buildCacheKey(reqDTO);
        TradeOrderLogisticsDO logistics = tradeOrderLogisticsMapper.selectByCacheKey(cacheKey);
        if (logistics == null) {
            tradeOrderLogisticsMapper.insert(new TradeOrderLogisticsDO()
                    .setOrderId(orderId).setLogisticsId(logisticsId)
                    .setExpressCode(reqDTO.getExpressCode()).setLogisticsNo(reqDTO.getLogisticsNo())
                    .setCacheKey(cacheKey).setProvider(tradeExpressProperties.getClient().getCode())
                    .setSubscribeStatus(TradeOrderLogisticsDO.SUBSCRIBE_STATUS_PENDING)
                    .setTrackData(JsonUtils.toJsonString(tracks)).setLastQueryTime(LocalDateTime.now()));
            return;
        }
        tradeOrderLogisticsMapper.updateById(new TradeOrderLogisticsDO().setId(logistics.getId())
                .setTrackData(JsonUtils.toJsonString(tracks)).setLastQueryTime(LocalDateTime.now()));
    }

    public void updateSubscribeResult(String cacheKey, boolean success, String message) {
        tradeOrderLogisticsMapper.updateByCacheKey(cacheKey, new TradeOrderLogisticsDO()
                .setSubscribeStatus(success ? TradeOrderLogisticsDO.SUBSCRIBE_STATUS_SUCCESS
                        : TradeOrderLogisticsDO.SUBSCRIBE_STATUS_FAILED)
                .setSubscribeMessage(StrUtil.maxLength(StrUtil.nullToEmpty(message), SUBSCRIBE_MESSAGE_MAX_LENGTH))
                .setSubscribeTime(LocalDateTime.now()));
    }

    public void saveNotify(String cacheKey, String trackState, List<ExpressTrackRespDTO> tracks) {
        TradeOrderLogisticsDO updateObj = new TradeOrderLogisticsDO().setTrackState(trackState)
                .setLastNotifyTime(LocalDateTime.now());
        if (CollUtil.isNotEmpty(tracks)) {
            updateObj.setTrackData(JsonUtils.toJsonString(tracks));
        }
        int updateCount = tradeOrderLogisticsMapper.updateByCacheKey(cacheKey, updateObj);
        if (updateCount == 0) {
            throw new IllegalStateException("物流订阅记录不存在");
        }
    }

    private static String buildCacheKey(ExpressTrackQueryReqDTO reqDTO) {
        return ExpressTrackCacheKeyUtils.build(reqDTO.getExpressCode(), reqDTO.getLogisticsNo(), reqDTO.getPhone());
    }

    private static LocalDateTime getTrackUpdateTime(TradeOrderLogisticsDO logistics) {
        LocalDateTime lastQueryTime = logistics.getLastQueryTime();
        LocalDateTime lastNotifyTime = logistics.getLastNotifyTime();
        if (lastQueryTime == null) {
            return lastNotifyTime;
        }
        if (lastNotifyTime == null) {
            return lastQueryTime;
        }
        return lastQueryTime.isAfter(lastNotifyTime) ? lastQueryTime : lastNotifyTime;
    }

}
