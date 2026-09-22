# Managed Agents 旅行规划部署包

本目录只属于 gift 模块，用于在阿里云百炼 Managed Agents 中部署旅行规划 Agent；Java 运行时通过 Sessions API 调用已部署的 Agent 与 Environment。

## 控制台配置

1. 创建 Managed Agent，并将 `SYSTEM_PROMPT.md` 设为系统指令。
2. 上传 `skills/travel-planner` Skill。该 Skill 只生成粗粒度行程骨架，服务端负责最终结构校验。
3. 为 Agent 添加高德地图 MCP，仅开放 POI 文本搜索与详情查询能力。日内路线由 C 端按需调用后端批量测距接口，不应让 Agent 在生成时逐段导航。
4. 创建云端托管 Environment，确认 Agent 能使用高德 MCP。
5. 将 Agent 与 Environment 发布到同一个 Workspace。

## 服务配置

通过部署侧受保护的 YAML 注入。API Key 在 ECS 以环境变量注入，YAML 只引用该变量，禁止将密钥提交到仓库：

```yaml
yudao:
  gift:
    trip-managed-agent:
      api-key: ${YUDAO_GIFT_TRIP_MANAGED_AGENT_API_KEY:}
      workspace: ws-nryr94at12b2jm8d
      region: cn-beijing
      # 使用 Sessions API 返回的 ID，而不是控制台中显示的名称。
      agent-id: agent_01M2VYR70XKESBBK3A9TS27FJ1
      environment-id: env_ZDUyZTk1MDU1YzQ1NDFmOG
      stream-timeout: 5m
      # INTAKE 的模型请求固定为 1 次，工具/Skill 调用固定为 0 次。
      intake-stream-timeout: 45s
      intake-max-run-duration: 3m
      intake-max-total-tokens: 8000
      intake-max-output-tokens: 1000
      intake-max-output-characters: 8192
```

若专有网络需要显式地址，可改配 `base-url`，其值为
`https://ws-nryr94at12b2jm8d.cn-beijing.maas.aliyuncs.com/api/v1/agentstudio`。

应用数据库先执行 `sql/mysql/gift_trip.sql` 中的 `managed_agent_session_id` 增量语句。新入口为：

```text
POST /app-api/ai/chat/message/managed/run
Content-Type: application/json
Accept: text/event-stream
```

请求和 AG-UI 响应与统一旅行接口保持一致。新链路复用已保存的 TripState；首次生成需补齐出发地、目的地、出发日期、旅行天数、出行人数和预算。

## Session 生命周期

- 每个旅行会话只创建一个 Managed Agent Session，并记录在 `gift_trip_plan.managed_agent_session_id`。
- 后续 `INTAKE`、`GENERATE_PLAN`、`EDIT_PLAN` 均复用该 Session，使需求抽取与行程生成共享上下文。
- `INTAKE` 与 `PLAN` 使用独立预算；`INTAKE` 只允许一次模型推理，禁止工具和 Skill 调用。
- 预算或超时熔断只向当前运行发送 interrupt，保留 Session 与历史事件，后续请求继续复用。
- 权威业务状态始终是 `TripState` 与当前 itinerary 版本，而不是 Managed Agents Session 上下文。

## Intake 编辑命令协议

已有行程后的自然语言修改由 Managed Agent 的 `EXTRACT_TRIP_REQUIREMENTS` 任务输出单个
`change_command`，Java 服务端再统一执行：

```json
{
  "change_command": {
    "operation": "REPLAN_DAY",
    "day": 2,
    "values": {
      "instruction": "下午换成更适合儿童的室内地点"
    }
  }
}
```

- `operation` 使用 `TripChangeCommand.Operation` 枚举值。
- 单轮只接受一个命令；组合修改应拆成多轮，避免部分成功。
- `baseVersion` 由服务端从当前 itinerary 读取，模型输出的版本不会被信任。
- 旧 `itinerary_patch.operations` 暂时兼容：单日修改转换为 `REPLAN_DAY`，多日修改转换为 `REPLAN_TRIP`。
- Intake Role 只识别意图和目标，不直接写入 POI 编号、坐标或路线事实。
