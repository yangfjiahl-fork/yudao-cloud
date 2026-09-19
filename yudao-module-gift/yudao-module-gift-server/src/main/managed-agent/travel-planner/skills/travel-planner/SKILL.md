---
name: travel-planner
description: 使用高德 MCP 生成可执行的粗粒度多日旅行骨架。当任务为 GENERATE_TRAVEL_PLAN 时必须使用。
---

# Travel Planner

输入是一个 JSON 对象，包含 `task=GENERATE_TRAVEL_PLAN`、`tripState`、默认值和输出要求。

## 执行流程

1. 读取 `tripState`，以 `destination`、`startDate`、`days`、`travelerCount` 为硬约束。
2. 规划城市顺序和每日主题，再查询具体 POI。多城市旅行先确定换城日。
3. 每个城市或核心区域只做一次高德文本搜索；每天选择 1～2 个核心游览 POI 和一个住宿锚点，再读取这些已选 POI 的详情。只采用具有稳定 POI ID 与有效坐标的结果。
4. 不查询当天相邻节点的路线、距离、驾车、公交或步行导航。后端会在用户查看当天路线时批量测距；只有跨城或跨区域衔接明显不可行时才查询一条主连接路线。
5. 餐饮、夜间活动和休息可写成区域建议，复用核心 POI 的 `area`，不要为这些非核心项重复查询 POI。
6. 单次规划的高德调用总数不得超过 `days + 6`；每个城市或核心区域只搜索一次，每个已选 POI 只查一次详情。失败时至多重试一次，禁止并发调用。
7. 直接调用已启用的高德 MCP，禁止调用 `activate_skill` 或加载其他地图 Skill 的完整内容。
8. 直接生成下列 JSON。服务端负责结构、坐标和时间顺序校验。

## JSON 结构

- `summary`: 简短用户可见摘要。
- `overview`: 对象，包含 `slot=TRIP_OVERVIEW`、`skeleton`、`detail`、`status=RESOLVED`、`citationIds`。
- `daily_itinerary`: 数组，每项包含：
  - `day`: 从 1 连续递增。
  - `date`: ISO 日期。
  - `overview`: `slot=DAY_OVERVIEW` 的已完成对象。
- `slots`: 节点数组。节点时间顺序递增，且必须包含住宿和至少一个核心游览节点。通常使用 `MORNING`、可选 `AFTERNOON` 和 `ACCOMMODATION`；不要求填满所有时段。
  - `planning`: 可包含主题、区域与可行性说明。
- `transport`: 包含 `arrival` 和 `departure`，各含 `skeleton`、`detail`、`status`、`city`、`citationIds`。出发地未知时不得杜撰。
- `citation_ids`: 本次使用的引用标识去重数组。

每个输出的核心游览节点和住宿锚点必须包含 `slot`、`label`、`city`、`area`、`poiId`、`poiName`、`longitude`、`latitude`、`plannedStartTime`、`skeleton`、`detail`、`status=RESOLVED`、`citationIds`。非核心建议应省略，不要伪造 POI。

高德 MCP 的检索结果是事实来源；不要用模型或脚本生成虚假 POI。
