---
name: travel-planner
description: 生成粗粒度多日旅行路线。当任务为 GENERATE_TRIP_MACRO_SKELETON 时使用。
---

# Travel Planner

输入是一个 JSON 对象，包含 `task=GENERATE_TRIP_MACRO_SKELETON`、`tripState`、默认值和输出要求。

## 执行流程

1. 读取 `tripState`，以 `destination`、`days`、`travelerCount` 为硬约束；`startDate` 如有则遵守。
2. 规划城市顺序、每日区域和主题。
3. 每天给出 1～2 个 `anchorPoiNames`，只作为后端高德检索锚点，不输出未经服务端核验的事实字段。
4. 不查询当天相邻节点的路线、距离、驾车、公交或步行导航。
5. 优先不调用高德工具；确需核对区域可行性时，每个城市或核心区域最多搜索一次，失败至多重试一次，禁止并发调用。
6. 直接调用已启用的高德 MCP，禁止调用 `activate_skill` 或加载其他地图 Skill 的完整内容。
7. 直接生成下列 JSON。服务端负责结构校验、POI 查询和日内排程。

## JSON 结构

- `macro_skeleton.days`: 数量必须与 `tripState.days` 相同，每项包含：
  - `day`: 从 1 连续递增。
  - `city`: 当天所在城市。
  - `area`: 当天主要活动区域。
  - `theme`: 当天主题。
  - `anchorPoiNames`: 1～2 个后端检索锚点名称。

不要输出坐标、酒店、餐厅、日内时刻、POI 详情或完整 `daily_itinerary`；这些由 Java 服务端查询、校验和编排。
