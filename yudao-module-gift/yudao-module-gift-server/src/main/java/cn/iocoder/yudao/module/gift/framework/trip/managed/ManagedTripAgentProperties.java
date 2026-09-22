package cn.iocoder.yudao.module.gift.framework.trip.managed;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** 旅行 Managed Agent 的入口标识与分阶段执行策略。 */
@ConfigurationProperties(prefix = "yudao.gift.trip-managed-agent")
@Data
public class ManagedTripAgentProperties {

    /** 控制台预先创建并配置好高德 MCP、旅行 Skill 的 Agent 编号。 */
    private String agentId;
    /** Agent 使用的百炼云端托管环境编号。 */
    private String environmentId;
    /** PLAN 相邻事件的最大空闲等待时长。 */
    private Duration streamTimeout = Duration.ofSeconds(45);
    /** PLAN 单次运行的绝对时长上限。 */
    private Duration maxRunDuration = Duration.ofMinutes(3);
    /** INTAKE 相邻事件的最大空闲等待时长。 */
    private Duration intakeStreamTimeout = Duration.ofSeconds(45);
    /** INTAKE 单次运行的绝对时长上限。 */
    private Duration intakeMaxRunDuration = Duration.ofMinutes(3);
    /** PLAN 单次运行允许发起的模型推理次数。 */
    private int maxModelRequests = 4;
    /** PLAN 单次运行允许执行的工具调用次数。 */
    private int maxToolCalls = 2;
    /** PLAN 单次运行允许消耗的总 Token。 */
    private long maxTotalTokens = 30_000;
    /** PLAN 单次运行允许产生的输出 Token。 */
    private long maxOutputTokens = 4_000;
    /** PLAN 单次运行允许收集的助手文本字符数。 */
    private int maxOutputCharacters = 32_768;
    /** INTAKE 单次运行允许消耗的总 Token。 */
    private long intakeMaxTotalTokens = 8_000;
    /** INTAKE 单次运行允许产生的输出 Token。 */
    private long intakeMaxOutputTokens = 1_000;
    /** INTAKE 单次运行允许收集的助手文本字符数。 */
    private int intakeMaxOutputCharacters = 8_192;

}
