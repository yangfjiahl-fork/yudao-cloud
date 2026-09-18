---
name: travel-planner
description: 使用高德 MCP 和沙箱生成、校验可执行的多日旅行计划。当任务为 GENERATE_TRAVEL_PLAN 时必须使用。
---

# Travel Planner

输入是一个 JSON 对象，包含 `task=GENERATE_TRAVEL_PLAN`、`tripState`、默认值和输出要求。

## 执行流程

1. 读取 `tripState`，以 `destination`、`startDate`、`days`、`travelerCount` 为硬约束。
2. 规划城市顺序和每日主题，再查询具体 POI。多城市旅行先确定换城日。
3. 使用高德 MCP 搜索每个候选 POI并读取详情。只采用具有稳定 POI ID 与有效坐标的结果。
4. 使用高德路线能力核验每天相邻节点，按真实通行时间调整顺序。
5. 生成下列 JSON，写到 `/tmp/travel-plan.json`。
6. 在沙箱执行：

   `python skills/travel-planner/scripts/validate_plan.py /tmp/travel-plan.json <days>`

7. 如果退出码非 0，根据错误修正 JSON 后再次校验。通过后原样输出该 JSON。

## JSON 结构

- `summary`: 简短用户可见摘要。
- `overview`: 对象，包含 `slot=TRIP_OVERVIEW`、`skeleton`、`detail`、`status=RESOLVED`、`citationIds`。
- `daily_itinerary`: 数组，每项包含：
  - `day`: 从 1 连续递增。
  - `date`: ISO 日期。
  - `overview`: `slot=DAY_OVERVIEW` 的已完成对象。
  - `slots`: 节点数组。节点时间顺序递增，且必须包含住宿和至少一个游览节点。
  - `planning`: 可包含主题、区域与可行性说明。
- `transport`: 包含 `arrival` 和 `departure`，各含 `skeleton`、`detail`、`status`、`city`、`citationIds`。出发地未知时不得杜撰。
- `citation_ids`: 本次使用的引用标识去重数组。

每个 POI 节点必须包含 `slot`、`label`、`city`、`area`、`poiId`、`poiName`、`longitude`、`latitude`、`plannedStartTime`、`skeleton`、`detail`、`status=RESOLVED`、`citationIds`。

高德 MCP 的检索结果是事实来源，沙箱只负责批量结构与可行性校验，不能在脚本里生成虚假 POI。
