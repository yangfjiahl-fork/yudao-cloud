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

- `INTAKE`、`UPDATE_STATE` 和 `CHAT` 不创建 Managed Agents Session。
- 每次 `GENERATE_PLAN` 或 `EDIT_PLAN` 创建一个新 Session，避免继承上一轮高德工具输出和临时上下文。
- `gift_trip_plan.managed_agent_session_id` 只保存最近一次规划 Session ID，用于运行审计和问题定位，不作为下次规划的复用依据。
- 权威业务状态始终是 `TripState` 与当前 itinerary 版本，而不是 Managed Agents Session 上下文。
