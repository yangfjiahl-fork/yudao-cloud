package cn.iocoder.yudao.module.gift.framework.trip.managed;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** 百炼 Managed Agents 旅行规划器配置。 */
@ConfigurationProperties(prefix = "yudao.gift.trip-managed-agent")
@Data
public class ManagedAgentProperties {

    /** 百炼工作空间编号，例如 ws_xxx。 */
    private String workspace;
    /** 当前 Managed Agents 仅支持 cn-beijing。 */
    private String region = "cn-beijing";
    /** 可选的完整 API 地址；为空时由 workspace 与 region 生成。 */
    private String baseUrl;
    /** 百炼 API Key；仅通过部署侧受保护的 YAML/Nacos 配置注入。 */
    private String apiKey;
    /** 控制台预先创建并配置好高德 MCP、旅行 Skill 的 Agent 编号。 */
    private String agentId;
    /** Agent 使用的百炼云端托管环境编号。 */
    private String environmentId;
    /** 单次规划事件流等待上限。 */
    private Duration streamTimeout = Duration.ofMinutes(5);

}
