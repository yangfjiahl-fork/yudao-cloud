# Repository Guidelines

## Project Structure & Module Organization

This is a Java 17, Spring Boot 3.5 multi-module Maven repository. Root `pom.xml` aggregates framework code, business modules, gateway, and server apps.

- `yudao-framework/`: shared starters and common utilities.
- `yudao-module-*/`: domain modules, usually split into `*-api` and `*-server`.
- `yudao-gateway/` and `yudao-server/`: deployable service entry points.
- `sql/`: database initialization and migration scripts by database type.
- `script/`: Docker, IDE, and operational helper scripts.
- Tests live in each module’s `src/test/java`; resources use `src/main/resources` and `src/test/resources`.

## Build, Test, and Development Commands

- `mvn -DskipTests compile`: compile the full reactor without running tests.
- `mvn test`: run all tests in the Maven reactor.
- `mvn -pl yudao-module-system/yudao-module-system-server -am -DskipTests compile`: compile one module and dependencies.
- `mvn -pl yudao-server -am spring-boot:run`: run the main server locally.
- `mvn -pl yudao-gateway -am spring-boot:run`: run the gateway locally.

Prefer targeted `-pl ... -am` commands during feature work to keep feedback fast.

## API Documentation & Apifox

- The project's Apifox workspace is [app-api](https://app.apifox.com/project/8882743) (project ID `8882743`).
- Only synchronize App-side APIs (`/app-api/**`) to Apifox. Management-side APIs (`/admin-api/**`) do not need to be created, updated, or tested in Apifox.
- Use the long-lived Apifox `dev` branch for interface changes; do not create an Apifox AI branch unless the user explicitly changes this policy.
- Treat the backend-generated OpenAPI document as the source of truth when creating or updating interfaces in Apifox.
- Organize every Apifox endpoint under exactly one of these five top-level product folders; keep the existing functional folders as second-level folders and do not create additional top-level product folders unless the user changes this taxonomy:
  - `发现`: homepage and recommended content, carousels, itinerary categories, and reusable itinerary content.
  - `助手`: AI assistant, speech recognition, location resolution, nearby-place search, and itinerary-planning capabilities.
  - `探索`: regions, destinations, maps, and discovery filters used for browsing or exploration.
  - `我的`: authentication, member profile, favorites, feedback, and other account-owned data or actions.
  - `通用`: cross-domain application infrastructure and shared system capabilities, such as file upload, system dictionaries, and other reusable platform APIs.
- Do not leave endpoints or functional folders at the Apifox project root unless the user explicitly requests it. When a capability could fit more than one category, choose the category matching the primary user journey and keep related endpoints together.
- Name Apifox second-level functional folders and endpoint tags by capability only, such as `认证` or `旅行定位`. Never add the `用户 APP -` / `用户 App -` prefix or casing variants in the documentation layer. Do not modify backend annotations solely to enforce this documentation naming rule; normalize names when writing to Apifox.
- Every new or changed Apifox endpoint must have at least one executable API test case on the same `dev` branch, covering the primary success path with meaningful response assertions. Creating or updating only the endpoint definition is incomplete.
- Validate the test-case payload before writing it, use runtime authentication variables instead of fixed tokens, and run the test case when the target environment is available. If it cannot be run or does not pass, report that explicitly before handoff.
- Use literal sample values for ordinary business request parameters in Apifox endpoint examples and test cases. Do not turn business inputs into environment variables; reserve environment variables for runtime credentials or credential-chain values such as `accessToken` and `refreshToken`.
- API documentation and test cases must describe only the endpoint's externally observable capability, authentication requirements, inputs, outputs, and constraints. Do not expose backend implementation details such as internal defaulting, persistence behavior, enum or whitelist mechanisms, service orchestration, or internal field derivation.
- Never store Apifox access tokens in the repository, generated API documents, logs, or command output.

## Workspace and Branch Policy

Always work in the user's current shared checkout and its current branch. Do not create, switch to, or rely on a separate task branch or Git worktree unless the user explicitly asks for one. Before making changes, confirm the repository root and current branch so the work is based on the latest shared code.

## Travel Agent Prompt Responsibilities

The travel flow uses two independent Bailian Managed Agents with different Agent IDs and Sessions. Configure them through `yudao.gift.trip-managed-agent.intake-agent-id` / `intake-environment-id` and `plan-agent-id` / `plan-environment-id`; never configure the same Agent ID for both stages.

- **INTAKE Managed Agent**: owns extracting `TripState` deltas from the user message, including updates to existing values and itinerary override intent. After backend validation, the same Agent also generates contextual follow-up questions and suggestion pills from the validated state. It never generates an itinerary and cannot use tools or Skills. Its deployment prompt lives under `yudao-module-gift/yudao-module-gift-server/src/main/managed-agent/trip-intake`.
- **PLAN Managed Agent**: receives only the backend-validated `TripState` and generates the macro route. It never reads the raw latest user message or performs information collection. Its deployment prompt and Skill live under `yudao-module-gift/yudao-module-gift-server/src/main/managed-agent/travel-planner`.
- **Backend orchestration**: `ItineraryAgentServiceImpl` supplies the authoritative `TripState` and supported field metadata, validates and normalizes model output, persists plans, and never delegates data integrity or authorization to either Agent. `ItineraryAssembler` deterministically queries travel providers and builds the itinerary skeleton after the PLAN Agent returns a valid macro route.
- **Information schema**: `ItineraryInformationSchema` is the single source of truth for collected fields, required fields, follow-up questions and suggestion pills. Optional fields remain writable after first generation and cause a more personalized new itinerary version.
- **Overview prompt delivery**: `AiChatControlledGenerateService` reads the overview role's `ai_chat_role.system_message`, renders `{{variableName}}` placeholders, and appends the current `Asia/Shanghai` time. The travel flow supplies `provinceName`, `cityName`, and `districtName` as name values.

Key travel modules are `ItineraryAgentServiceImpl` (orchestration), `ItineraryResearchExecutor` (deterministic provider lookups), `ItinerarySlotResult` / `gift_trip_itinerary_slot` (independent slot state), `AiChatControlledGenerateService` (controlled model invocation), and `gift-trip-sse-api.txt` (C-end SSE and slot-resolve contract).

## Coding Style & Naming Conventions

Use Java 17 conventions with 4-space indentation. Keep packages under `cn.iocoder.yudao.module.<domain>`. Follow existing layers: `controller`, `service`, `dal`, `api`, `mq`, `framework`. Name implementations `*ServiceImpl`, mappers `*Mapper`, data objects `*DO`, request/response objects `*ReqVO` and `*RespVO`, and DTOs `*DTO`.

Use Lombok and existing framework helpers where already used. Avoid unrelated formatting churn and keep changes scoped to the module being edited.

## Testing Guidelines

Tests use JUnit 5 with Spring Boot test support from `yudao-spring-boot-starter-test`. Place tests in the affected module under `src/test/java`, with names ending in `Test` or `Tests`. For service logic, prefer focused unit tests with mocked collaborators; for mapper or integration behavior, follow existing resources such as `application-unit-test.yaml` and SQL fixtures.

前端接口联调约定：默认使用 test 数据库中的 `userId=288` 会员账号进行鉴权验证，并从 test 数据库运行时获取有效 access token；不得使用固定的 `288` 字符串作为 Token。Token 仅用于本地验证，不得写入代码、接口文档、测试样例、提交记录或日志输出。

本地 MySQL 联调约定：连接信息从当前 profile 的配置文件读取，只能在本地进程变量中短暂使用，禁止在命令输出、脚本、文档或提交中暴露密码、access token、地图 Key 等敏感值。优先执行只读查询；旅行规划回归仅查询 `userId=288` 的未过期会员 token。若本机未安装 MySQL 客户端，可复用本地 Maven 缓存中的 MySQL JDBC 驱动以内存方式查询，禁止为联调把凭据复制到新文件。需要产生业务数据时，必须通过 C 端接口创建独立会话，不得直接更新或删除 test 数据库中的旅行、聊天或令牌记录。

Run at least the affected module’s tests or compile command before handing off.

### 旅行规划固定回归场景

本地后端联调旅行规划 C 端流程时，除非任务另有要求，使用以下固定中文用例：

- 固定测试条件：`国庆节，2大2小上海出发去云南6天5晚，亲子。` 不得改用英文或翻译后的输入。
- 使用同一会话拆分发送中文消息，至少覆盖：`国庆节从上海出发去云南。`、`请按2026年10月1日出发安排：2大2小共4人，6天5晚，亲子游。`、必填预算及`请立即生成行程。`；不得一次性提交完整条件。
- 每轮检查 SSE：信息收集或可选信息补充阶段应返回 `accepted → stage(INTAKE) → stage(FOLLOW_UP) → intake_completed → question → done`；点击立即生成后应返回行程骨架事件。必填项完成后仍应出现可选信息追问和“立即生成行程”胶囊。
- 检查状态已落库：至少包含出发地、目的地、出发日期、天数、人数、预算和亲子偏好，且与输入一致。
- 检查最终骨架：有 6 个按天行程、可解析的 POI 坐标，以及当天相邻 POI（含住宿）的批量 `transportSegments`；每段须有距离、耗时、出行方式和 `VERIFIED` 或 `ESTIMATED` 状态。
- 分别调用 `TRIP_OVERVIEW` 与第 1～6 天的 `DAY_OVERVIEW` 节点解析接口；均须返回 `status=RESOLVED` 且 `detail` 非空，验证行程总览和每日总览由模型生成。
- 检查每日 `planning.status`。出现 `PENDING`、缺坐标、缺 summary、节点解析失败或交通测距失败时，必须在验收结果中明确报告，不得仅因骨架已生成而判定通过。
- 仅使用 test 数据库中会员 `userId=288` 的运行时有效 access token；不得将 token 写入代码、测试样例、接口文档、提交记录或日志输出。

## Commit & Pull Request Guidelines

Recent history uses simple Conventional Commit-style prefixes, for example `feat: save`. Prefer clear messages such as `feat(gift): grant wool on registration` or `fix(system): validate sms mock config`.

Pull requests should include a concise summary, affected modules, verification commands run, linked issues when applicable, and screenshots only for UI changes. Call out configuration keys, database changes, and backward-compatibility risks explicitly.

## Security & Configuration Tips

Do not commit secrets, tokens, or environment-specific credentials. Store runtime values in the configuration system or local environment. When adding new config keys, document the key, expected type, default behavior, and failure mode in the PR.
