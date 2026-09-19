# Managed Agents 旅行规划部署包

本目录只属于 gift 模块，用于在阿里云百炼 Managed Agents 中部署旅行规划 Agent；Java 运行时通过 Sessions API 调用已部署的 Agent 与 Environment。

## 控制台配置

1. 创建 Managed Agent，并将 `SYSTEM_PROMPT.md` 设为系统指令。
2. 上传 `skills/travel-planner` Skill，保留其中的 `scripts/validate_plan.py`。
3. 为 Agent 添加高德地图 MCP，至少开放 POI 搜索、详情、路线规划与距离/耗时查询能力。
4. 创建云端托管 Environment，确认 Agent 能使用高德 MCP 和沙箱。
5. 将 Agent 与 Environment 发布到同一个 Workspace。

## 服务配置

通过部署侧受保护的 Nacos YAML 注入（不要使用环境变量，也不要将 API Key 提交到仓库）：

```yaml
yudao:
  gift:
    trip-managed-agent:
      api-key: <百炼 API Key>
      workspace: ws-nryr94at12b2jm8d
      region: cn-beijing
      agent-id: TripPlanAgent_qw37plus
      environment-id: TripPlanAgent_Env
      stream-timeout: 5m
```

若专有网络需要显式地址，可改配 `base-url`，其值为
`https://ws-nryr94at12b2jm8d.cn-beijing.maas.aliyuncs.com/api/v1/agentstudio`。

应用数据库先执行 `sql/mysql/gift_trip.sql` 中的 `managed_agent_session_id` 增量语句。新入口为：

```text
POST /ai/chat/message/managed/run
Content-Type: application/json
Accept: text/event-stream
```

请求和 AG-UI 响应与原 `/ai/chat/message/run` 保持一致。新链路复用已保存的 TripState；首次生成至少需要目的地、出发日期、旅行天数和出行人数，出发地与预算可选。
