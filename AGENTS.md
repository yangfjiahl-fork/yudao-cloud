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

## Workspace and Branch Policy

Always work in the user's current shared checkout and its current branch. Do not create, switch to, or rely on a separate task branch or Git worktree unless the user explicitly asks for one. Before making changes, confirm the repository root and current branch so the work is based on the latest shared code.

## Travel Agent Prompt Responsibilities

The travel planner has one database-managed information-extraction role. Its identifier is configured through `infra_config` key `trip.agent.intakeRoleId`; never hardcode a role's static prompt rules in the business service.

- **INTAKE role**: owns extracting the complete `TripState` from the user message, including updates to existing values and itinerary override intent; it also generates the contextual follow-up question and suggestion pills after the backend validates missing fields. Its JSON contract lives in `ai_chat_role.system_message`.
- **Backend orchestration**: `TripAgentServiceImpl` supplies the current `TripState`, the supported field metadata and prompt variables; it validates and normalizes model output, persists plans, and never delegates data integrity or authorization to the model. `TripItineraryAssembler` deterministically queries travel providers and builds the itinerary skeleton after required information is complete.
- **Information schema**: `TripInformationSchema` is the single source of truth for collected fields, required fields, follow-up questions and suggestion pills. Optional fields remain writable after first generation and cause a more personalized new itinerary version.
- **Prompt delivery**: `AiChatControlledGenerateService` reads `ai_chat_role.system_message`, renders `{{variableName}}` placeholders, and appends the current `Asia/Shanghai` time. The travel flow supplies `provinceName`, `cityName`, and `districtName` as name values.

Key travel modules are `TripAgentServiceImpl` (orchestration), `TripResearchExecutor` (deterministic provider lookups), `TripItinerarySlotDO` / `gift_trip_itinerary_slot` (independent slot state), `AiChatControlledGenerateService` (controlled model invocation), and `gift-trip-sse-api.txt` (C-end SSE and slot-resolve contract).

## Coding Style & Naming Conventions

Use Java 17 conventions with 4-space indentation. Keep packages under `cn.iocoder.yudao.module.<domain>`. Follow existing layers: `controller`, `service`, `dal`, `api`, `mq`, `framework`. Name implementations `*ServiceImpl`, mappers `*Mapper`, data objects `*DO`, request/response objects `*ReqVO` and `*RespVO`, and DTOs `*DTO`.

Use Lombok and existing framework helpers where already used. Avoid unrelated formatting churn and keep changes scoped to the module being edited.

## Testing Guidelines

Tests use JUnit 5 with Spring Boot test support from `yudao-spring-boot-starter-test`. Place tests in the affected module under `src/test/java`, with names ending in `Test` or `Tests`. For service logic, prefer focused unit tests with mocked collaborators; for mapper or integration behavior, follow existing resources such as `application-unit-test.yaml` and SQL fixtures.

前端接口联调约定：默认使用 test 数据库中的 `userId=288` 会员账号进行鉴权验证，并从 test 数据库运行时获取有效 access token；不得使用固定的 `288` 字符串作为 Token。Token 仅用于本地验证，不得写入代码、接口文档、测试样例、提交记录或日志输出。

Run at least the affected module’s tests or compile command before handing off.

## Commit & Pull Request Guidelines

Recent history uses simple Conventional Commit-style prefixes, for example `feat: save`. Prefer clear messages such as `feat(gift): grant wool on registration` or `fix(system): validate sms mock config`.

Pull requests should include a concise summary, affected modules, verification commands run, linked issues when applicable, and screenshots only for UI changes. Call out configuration keys, database changes, and backward-compatibility risks explicitly.

## Security & Configuration Tips

Do not commit secrets, tokens, or environment-specific credentials. Store runtime values in the configuration system or local environment. When adding new config keys, document the key, expected type, default behavior, and failure mode in the PR.
