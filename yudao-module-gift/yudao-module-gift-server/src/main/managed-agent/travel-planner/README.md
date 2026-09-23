# Managed Agents 行程生成部署包

本目录只属于 gift 模块，用于部署根据已校验 `TripState` 生成宏观路线的 Agent。
需求收集 Agent 的部署包位于相邻的 `trip-intake` 目录。

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
      intake-agent-id: ${YUDAO_GIFT_TRIP_INTAKE_AGENT_ID:}
      intake-environment-id: ${YUDAO_GIFT_TRIP_INTAKE_ENVIRONMENT_ID:}
      plan-agent-id: ${YUDAO_GIFT_TRIP_PLAN_AGENT_ID:}
      plan-environment-id: ${YUDAO_GIFT_TRIP_PLAN_ENVIRONMENT_ID:}
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

应用数据库先执行 `sql/mysql/gift_trip.sql` 中的 Session 列增量语句：它会删除旧的
`managed_agent_session_id`，并新增 `intake_agent_session_id` 和 `plan_agent_session_id`。新入口为：

```text
POST /app-api/ai/chat/message/managed/run
Content-Type: application/json
Accept: text/event-stream
```

请求和 AG-UI 响应与统一旅行接口保持一致。新链路复用已保存的 TripState；首次生成需补齐出发地、目的地和出行人数，并在“完整的开始/结束日期”与“旅行天数”中至少提供一项。日期区间按首尾日期都计入自动换算天数；未提供预算时按每人每天 500 元且不含往返交通处理。

## Session 生命周期

- 每个旅行会话分别创建需求收集和行程生成 Session，记录在
  `gift_trip_plan.intake_agent_session_id` 和 `gift_trip_plan.plan_agent_session_id`。
- `INTAKE` 只能使用需求收集 Agent，`PLAN` 只能使用行程生成 Agent，两者不共享 Session 历史。
- 两个 Agent 通过服务端已校验的 `TripState` 交接，不依赖对方的上下文。
- `INTAKE` 只允许一次模型推理，禁止工具和 Skill 调用。
- 预算或超时熔断只向当前运行发送 interrupt，保留 Session 与历史事件，后续请求继续复用。
- 权威业务状态始终是 `TripState` 与当前 itinerary 版本，而不是 Managed Agents Session 上下文。
