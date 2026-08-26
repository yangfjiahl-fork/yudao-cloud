# 阿里云 ARMS：旅行 Agent 的 Spring AI 观测接入

## 目标

旅行规划使用 `Spring AI Alibaba DashScope` 调用模型。通过 ARMS Java Agent 自动采集 Spring AI / DashScope 的模型调用，并由 Gift 模块补充旅行编排阶段的自定义 Trace：

- `trip.agent.intake`：需求抽取
- `trip.agent.composer`：行程骨架生成
- `trip.agent.slot.resolve`：前端按节点补充景点、酒店等候选信息

自定义属性只包含阶段、旅行记录标识、Chat Role 标识、模型名、Token 用量、节点类型、候选数量和执行状态；不写入用户消息、Prompt、行程状态、地名或其他用户资料。

## 部署前提

1. 在 ARMS 应用监控控制台创建或选择目标 Java 应用，获取该应用的 `LicenseKey` 与区域 `ProfileId`。
2. 下载并挂载 ARMS Java Agent。应使用 5.1.0 或以上版本；该版本已支持 Spring AI Alibaba 1.1。
3. 目标进程只能挂载一个可进行字节码增强的 Java Agent。若现有部署仍启用 SkyWalking Agent，请替换为 ARMS Agent，或先确认两者的共存方案，避免重复增强与重复 Trace。

## 启动配置

将 Agent Jar 放在运行环境，例如 `/opt/AliyunJavaAgent/aliyun-java-agent.jar`。以下变量由部署平台、Kubernetes Secret 或密钥管理服务注入，禁止写进 `application*.yaml` 或提交到仓库：

```bash
export ARMS_AGENT_PATH=/opt/AliyunJavaAgent/aliyun-java-agent.jar
export ARMS_LICENSE_KEY='<ARMS LicenseKey>'
export ARMS_APP_NAME='yudao-server'
export ARMS_REGION_ID='cn-hangzhou'
```

在启动 `yudao-server` 的 JVM 参数中加入：

```bash
-javaagent:${ARMS_AGENT_PATH} \
-Darms.licenseKey=${ARMS_LICENSE_KEY} \
-Darms.appName=${ARMS_APP_NAME} \
-Daliyun.javaagent.profileId=${ARMS_REGION_ID} \
-Dotel.instrumentation.genai.capture-message-content=false
```

也可设置同名环境变量 `OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT=false`。此项必须保持关闭，避免将用户输入、System Prompt 或模型原文输出作为 `gen_ai.*` 属性发送到观测平台。

## 验证方式

1. 使用 C 端旅行对话完整走一次需求追问和“直接生成行程”。
2. 在 ARMS 的 Trace / LLM 观测中按 `trip.agent.intake`、`trip.agent.composer` 检索；应能看到其下游的 DashScope / Spring AI 模型调用与 Token 指标。
3. 调用一个行程节点补充接口，检索 `trip.agent.slot.resolve`；应看到 `trip.tool.status`、`trip.tool.candidate_count` 等属性。
4. 在 Trace 属性中抽查，不应出现用户消息、Prompt、完整行程 JSON、城市名称、手机号等内容。

## 说明

应用不配置独立的 OTLP Exporter。ARMS Java Agent 负责自动采集与上报，Gift 模块的 `Tracer` 仅创建父子 Span；未挂载 Agent 时该 `Tracer` 为无副作用的 No-op 实现，不影响现有模型调用。
