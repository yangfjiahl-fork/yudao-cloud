package cn.iocoder.yudao.module.gift.service.itinerary.managed;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Managed Agent 云端连接配置，不包含具体业务入口和执行策略。 */
@ConfigurationProperties(prefix = "yudao.gift.trip-managed-agent")
@Data
public class ManagedAgentClientProperties {

    /** 百炼工作空间编号，例如 ws_xxx。 */
    private String workspace;
    /** 当前 Managed Agents 仅支持 cn-beijing。 */
    private String region = "cn-beijing";
    /** 可选的完整 API 地址；为空时由 workspace 与 region 生成。 */
    private String baseUrl;
    /** 百炼 API Key；仅通过部署侧受保护的 YAML/Nacos 配置注入。 */
    private String apiKey;

}
