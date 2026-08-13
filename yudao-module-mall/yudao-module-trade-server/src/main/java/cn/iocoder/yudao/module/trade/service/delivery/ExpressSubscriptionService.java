package cn.iocoder.yudao.module.trade.service.delivery;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.trade.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.ExpressClient;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.ExpressClientFactory;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.ExpressSubscribeClient;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.ExpressTrackCacheKeyUtils;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackNotifyRespDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackQueryReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 快递轨迹订阅 Service
 */
@Service
@Slf4j
public class ExpressSubscriptionService {

    private static final String CACHE_KEY_PATTERN = "^[0-9a-f]{64}$";

    @Resource
    private ExpressClientFactory expressClientFactory;
    @Resource
    private TradeExpressProperties tradeExpressProperties;
    @Resource
    private CacheManager cacheManager;
    @Resource
    private TradeOrderLogisticsService tradeOrderLogisticsService;

    /**
     * 在当前事务提交后异步订阅物流动态。
     */
    public void subscribeAfterCommit(Long orderId, Long logisticsId, ExpressTrackQueryReqDTO reqDTO) {
        tradeOrderLogisticsService.initLogistics(orderId, logisticsId, reqDTO);
        ExpressClient expressClient = expressClientFactory.getDefaultExpressClient();
        if (!(expressClient instanceof ExpressSubscribeClient)) {
            return;
        }
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        Runnable subscribeTask = () -> getSelf().subscribe(tenantId, reqDTO);
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            subscribeTask.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                subscribeTask.run();
            }
        });
    }

    @Async
    public void subscribe(Long tenantId, ExpressTrackQueryReqDTO reqDTO) {
        TenantUtils.execute(tenantId, () -> {
            String cacheKey = ExpressTrackCacheKeyUtils.build(reqDTO.getExpressCode(), reqDTO.getLogisticsNo(),
                    reqDTO.getPhone());
            try {
                ((ExpressSubscribeClient) expressClientFactory.getDefaultExpressClient()).subscribeExpressTrack(reqDTO);
                tradeOrderLogisticsService.updateSubscribeResult(cacheKey, true, "提交成功");
            } catch (Throwable ex) {
                tradeOrderLogisticsService.updateSubscribeResult(cacheKey, false, ex.getMessage());
                // 订阅失败不影响订单发货，后续仍可使用实时查询接口。
                log.error("[subscribe][订阅物流动态失败，tenantId({}) expressCode({}) logisticsNo({})]",
                        tenantId, reqDTO.getExpressCode(), reqDTO.getLogisticsNo(), ex);
            }
        });
    }

    /**
     * 处理阿里云快递 100 动态通知。先更新数据库，再删除实时查询缓存。
     */
    public void processNotify(Long tenantId, String cacheKey, String token, String payload) {
        validateNotify(token, cacheKey, payload);
        ExpressClient expressClient = expressClientFactory.getDefaultExpressClient();
        if (!(expressClient instanceof ExpressSubscribeClient subscribeClient)) {
            throw new IllegalStateException("当前快递客户端不支持物流动态通知");
        }
        ExpressTrackNotifyRespDTO notify = subscribeClient.parseExpressTrackNotify(payload);
        if (notify == null) {
            return;
        }
        TenantUtils.execute(tenantId, () -> {
            tradeOrderLogisticsService.saveNotify(cacheKey, notify.getState(), notify.getTracks());
            Cache cache = cacheManager.getCache(RedisKeyConstants.EXPRESS_TRACK);
            if (cache == null) {
                throw new IllegalStateException("快递轨迹缓存不存在");
            }
            cache.evict(cacheKey);
        });
    }

    private void validateNotify(String token, String cacheKey, String payload) {
        TradeExpressProperties.AliyunKd100Config config = tradeExpressProperties.getAliyunKd100();
        if (config == null || StrUtil.isBlank(config.getCallbackToken())) {
            throw new IllegalStateException("阿里云快递 100 callback-token 未配置");
        }
        if (!MessageDigest.isEqual(config.getCallbackToken().getBytes(StandardCharsets.UTF_8),
                StrUtil.nullToEmpty(token).getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("阿里云快递 100 回调令牌错误");
        }
        if (!ReUtil.isMatch(CACHE_KEY_PATTERN, cacheKey) || !JsonUtils.isJsonObject(payload)) {
            throw new IllegalArgumentException("阿里云快递 100 回调参数错误");
        }
    }

    private ExpressSubscriptionService getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
