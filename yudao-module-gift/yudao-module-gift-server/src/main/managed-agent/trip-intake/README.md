# Managed Agents 旅行需求收集部署包

该 Agent 负责处理 `EXTRACT_TRIP_REQUIREMENTS` 和 `GENERATE_TRIP_FOLLOW_UP`，不配置 MCP 或 Skill。
前一个任务提取状态增量，后一个任务在后端校验状态后生成追问和建议；卡片结构仍由 Java 后端确定性组装。

1. 在百炼创建独立 Managed Agent。
2. 将本目录的 `SYSTEM_PROMPT.md` 设为系统指令。
3. 创建并发布专用 Environment。
4. 将 Agent 和 Environment ID 分别注入
   `yudao.gift.trip-managed-agent.intake-agent-id` 和
   `yudao.gift.trip-managed-agent.intake-environment-id`。

该 Agent 与行程生成 Agent 使用不同的 Agent ID 和 Session。

## 行程编辑命令

已有行程后的自然语言修改由本 Agent 输出单个 `change_command`，Java 服务端再统一执行：

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

- `operation` 使用 `ItineraryChangeCommand.Operation` 枚举值。
- 单轮只接受一个命令；组合修改应拆成多轮。
- Agent 只识别意图和目标，不直接写入 POI 编号、坐标或路线事实。
