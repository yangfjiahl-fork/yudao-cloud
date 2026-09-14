# 阿里云 CMS AI 可观测：旅行 Agent 的 Spring AI 观测接入

## 目标

旅行规划使用 `Spring AI Alibaba DashScope` 调用模型。ARMS Java Agent 保留已有 HTTP、Dubbo、MySQL 等自动链路；应用内 OpenTelemetry SDK 将 GenAI Span 直接导出至 CMS AI 可观测，Gift 模块补充旅行编排阶段的自定义 Trace：

- `trip.agent.intake`：需求抽取
- `trip.agent.composer`：行程骨架生成
- `trip.agent.slot.resolve`：前端按节点补充景点、酒店等候选信息

自定义属性只包含阶段、旅行记录标识、Chat Role 标识、模型名、Token 用量、节点类型、候选数量和执行状态；不写入用户消息、Prompt、行程状态、地名或其他用户资料。

## 部署前提

1. 在 CMS 可观测链路 OpenTelemetry 版控制台获取 Workspace、Project、LicenseKey 和区域 Endpoint。
2. 下载并挂载 ARMS Java Agent，以保留已有非 AI 调用链。
3. 目标进程只能挂载一个可进行字节码增强的 Java Agent。若现有部署仍启用 SkyWalking Agent，请替换为 ARMS Agent，或先确认两者的共存方案，避免重复增强与重复 Trace。

## 启动配置

将 Agent Jar 放在运行环境，例如 `/opt/AliyunJavaAgent/aliyun-java-agent.jar`。以下变量由部署平台、Kubernetes Secret 或密钥管理服务注入，禁止写进 `application*.yaml` 或提交到仓库：

```bash
export ARMS_AGENT_PATH=/opt/AliyunJavaAgent/aliyun-java-agent.jar
export ARMS_LICENSE_KEY='<ARMS LicenseKey>'
export ARMS_APP_NAME='yudao-server'
export ARMS_REGION_ID='cn-hangzhou'
```

在启动 `yudao-server` 的 JVM 参数中保留 ARMS Agent：

```bash
-javaagent:${ARMS_AGENT_PATH} \
-Darms.licenseKey=${ARMS_LICENSE_KEY} \
-Darms.appName=${ARMS_APP_NAME} \
-Daliyun.javaagent.profileId=${ARMS_REGION_ID}
```

同时设置 CMS OTLP 环境变量。`OTEL_RESOURCE_ATTRIBUTES` 必须在已有属性后追加，不能覆盖已有的 `service.name`；认证信息由密钥管理服务注入：

```bash
export YUDAO_OTEL_GEN_AI_CMS_EXPORT_ENABLED=true
export OTEL_SERVICE_NAME=yudao-server
export OTEL_RESOURCE_ATTRIBUTES="${OTEL_RESOURCE_ATTRIBUTES:+${OTEL_RESOURCE_ATTRIBUTES},}acs.arms.service.feature=genai_app,gen_ai.instrumentation.sdk.name=loongsuite-genai-utils,acs.cms.workspace=${CMS_WORKSPACE_ID}"
export OTEL_EXPORTER_OTLP_TRACES_ENDPOINT="https://${CMS_PROJECT}.${CMS_REGION}.log.aliyuncs.com/apm/trace/opentelemetry/v1/traces"
export OTEL_EXPORTER_OTLP_METRICS_ENDPOINT="https://${CMS_PROJECT}.${CMS_REGION}.log.aliyuncs.com/apm/trace/opentelemetry/v1/metrics"
export OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
export OTEL_EXPORTER_OTLP_HEADERS="x-arms-license-key=${CMS_LICENSE_KEY},x-arms-project=${CMS_PROJECT},x-cms-workspace=${CMS_WORKSPACE_ID}"
export OTEL_SEMCONV_STABILITY_OPT_IN=gen_ai_latest_experimental
```

默认不采集消息正文，避免将用户输入、System Prompt 或模型原文输出发送到观测平台。如业务明确通过安全评审需要采集，才额外设置：

```bash
export OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT=span_and_event
export OTEL_INSTRUMENTATION_GENAI_EMIT_EVENT=true
```

## 验证方式

1. 使用 C 端旅行对话完整走一次需求追问和“直接生成行程”。
2. 在 CMS AI 可观测的调用链中检索 `invoke_agent travel-planner` 与 `chat <模型名>`；应能看到 DashScope 模型调用与 Token 指标。
3. 调用一个行程节点补充接口，检索 `trip.agent.slot.resolve`；应看到 `trip.tool.status`、`trip.tool.candidate_count` 等属性。
4. 在 Trace 属性中抽查，不应出现用户消息、Prompt、完整行程 JSON、城市名称、手机号等内容。

## 说明

`yudao.otel.gen-ai.cms-export-enabled=true` 会初始化独立的 OpenTelemetry SDK，但不会覆盖 ARMS Java Agent 的全局 Provider。因此 CMS 负责 GenAI Span 导出，ARMS Agent 继续负责原有自动埋点，两者不会重复接管同一个 Provider。
