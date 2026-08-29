package cn.iocoder.yudao.module.gift.framework.trip.provider.place;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/** 根据配置将旅行地点查询路由至对应供应商。 */
@Slf4j
public class TravelPlaceQueryClientFacade {

    public static final String HOTEL_QUERY_FROM_CONFIG_KEY = "hotel.query.from";
    /** 配置键沿用现有系统拼写：resturant，而不是 restaurant。 */
    public static final String RESTAURANT_QUERY_FROM_CONFIG_KEY = "resturant.query.from";
    private static final String DEFAULT_PROVIDER = "gaode";

    private final ConfigApi configApi;
    private final Map<String, TravelPlaceQueryClient> clients;

    public TravelPlaceQueryClientFacade(ConfigApi configApi, List<TravelPlaceQueryClient> clients) {
        this.configApi = configApi;
        this.clients = clients.stream().collect(toMap(client -> client.provider().toLowerCase(), Function.identity()));
    }

    public TravelPlaceQueryClient.Response query(TravelPlaceQueryClient.Request request) {
        String provider = provider(request == null ? null : request.getType());
        TravelPlaceQueryClient client = clients.get(provider.toLowerCase());
        if (client == null) {
            log.warn("[query][旅行地点查询供应商不存在，provider({}) type({}) region({})]", provider,
                    request == null ? null : request.getType(), request == null ? null : request.getRegion());
            return TravelPlaceQueryClient.Response.failure("未配置旅行地点查询供应商：" + provider);
        }
        log.info("[query][旅行地点查询路由，provider({}) type({}) region({}) limit({})]", provider,
                request == null ? null : request.getType(), request == null ? null : request.getRegion(),
                request == null ? null : request.getLimit());
        return client.query(request);
    }

    public String provider(TravelPlaceQueryClient.PlaceType type) {
        String configKey = type == TravelPlaceQueryClient.PlaceType.HOTEL
                ? HOTEL_QUERY_FROM_CONFIG_KEY : RESTAURANT_QUERY_FROM_CONFIG_KEY;
        String configuredProvider;
        try {
            configuredProvider = configApi.getConfigValueByKey(configKey).getCheckedData();
        } catch (RuntimeException ex) {
            log.error("[provider][读取旅行地点查询来源配置失败，key({})]", configKey, ex);
            configuredProvider = null;
        }
        return StrUtil.blankToDefault(StrUtil.trim(configuredProvider), DEFAULT_PROVIDER);
    }

}
