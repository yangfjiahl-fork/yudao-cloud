package cn.iocoder.yudao.module.gift.service.itinerary.managed;

import java.util.Map;

/** 创建 Managed Agent Session 所需的通用参数。 */
public record ManagedAgentSessionCreateRequest(
        String agentId,
        String environmentId,
        String title,
        Map<String, String> metadata) {
}
