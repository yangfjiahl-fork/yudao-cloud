# Trip Agent MVP Refactor TODO

> Target branch: `dev1030`
>
> Scope: stabilize the planning loop before adding booking, product, order, inventory, or payment capabilities.

## Phase 1 - Consolidate planning paths

- [x] Add `TripItineraryVersionService` as the single owner of itinerary version allocation and persistence.
- [x] Move assistant-message association, slot initialization, current-itinerary updates, and conversation-title updates into the version service.
- [x] Make `TripAgentServiceImpl` persist generated itineraries through the version service.
- [x] Make `ManagedTripAgentServiceImpl` persist generated itineraries through the version service.
- [x] Remove exact user-text routing from `AppTripChatMessageController`.
- [x] Add an orchestration decision for `INTAKE`, `UPDATE_STATE`, `GENERATE_PLAN`, `EDIT_PLAN`, and `CHAT` without coupling it to a fixed phrase.
- [x] Define the Managed Agent session lifecycle: create a fresh Session per planning run and retain only the latest ID for audit.
- [x] Run focused tests and the gift-server compile check.

## Phase 2 - Managed Agent macro skeleton

- [x] Define the macro-skeleton contract: daily city, area, theme, anchor POIs, and transfer days.
- [x] Let Managed Agent generate and validate the macro skeleton before Java performs POI expansion.
- [x] Search candidates per day around the selected city, area, and anchors.
- [x] Keep external calls progressive: skeleton first, details/routes on demand.

## Phase 3 - Unified editing model

- [x] Give every itinerary item a stable `itemId` plus day, type, time period, sort, time, duration, POI, lock, and source fields.
- [x] Define `TripChangeCommand` operations for add, remove, replace, move, update, lock, unlock, and replan.
- [x] Add `TripPlanEditorService` with base-version validation and affected-day detection.
- [x] Replan only affected days and preserve locked items.

## Phase 4 - Shared App and Agent commands

- [ ] Convert App manual edits and Agent natural-language edits into the same `TripChangeCommand` contract.
- [ ] Persist every accepted edit as a new immutable itinerary version.

## Phase 5 - POI fact verification

- [ ] Add `getPlaceDetail(poiId)` to the travel provider boundary.
- [ ] Re-verify Managed Agent POIs and persist a consistent POI snapshot.
- [ ] Keep unavailable facts `PENDING` or `ESTIMATED`; never fabricate `RESOLVED`.

## Fixed regression scenario

Use: `国庆节，2大2小上海出发去云南6天5晚，亲子。`

- [ ] Collect required information across multiple turns while still accepting optional preferences.
- [ ] Generate exactly six itinerary days with reasonable city and area choices.
- [ ] Verify POI coordinates and adjacent transport distance, duration, mode, and verification status.
- [ ] Resolve `TRIP_OVERVIEW` and all six `DAY_OVERVIEW` slots.
- [ ] Report every daily `planning.status` explicitly.
- [ ] Confirm a one-day edit does not rebuild the whole trip.
- [ ] Confirm locked items survive later Agent replanning.

## Milestone log

- 2026-09-21: Phase 1 started from remote `dev1030` at `5c9a1c9e6`.
- 2026-09-21: Added the shared itinerary version service and migrated both planner implementations.
- 2026-09-21: Kept only `/managed/run` and moved routing to the backend orchestration action.
- 2026-09-21: Restored Maven access with a temporary Aliyun mirror and the sandbox's dynamic HTTPS proxy. The 31-module `test-compile` passed, followed by 10 focused tests for itinerary persistence, orchestration decisions, and `/managed/run` AG-UI behavior.
- 2026-09-21: Added the Phase 2 macro-skeleton contract and server validation. Managed Agent now selects daily city, area, theme, anchors, and transfer days; Java queries each day's POI candidates in parallel and retains OR-Tools scheduling. Compile and 19 focused regression tests passed. Real Managed Agent + Amap verification remains in the fixed regression scenario.
- 2026-09-21: Started Phase 3 with stable itinerary item identities and the shared `TripChangeCommand` contract. Item fields are normalized at the immutable-version boundary for both planner paths; compile and 10 focused tests passed.
- 2026-09-21: Added `TripPlanEditorService` for safe local remove, move, update, lock, and unlock commands. It rejects stale base versions, detects affected days, and saves each accepted edit through the immutable-version service; the 31-module `test-compile` and 5 focused tests passed. POI-dependent add, replace, and replan commands remain explicitly unsupported until the verification/replanning path is connected.
- 2026-09-21: Completed Phase 3 local replanning. `REPLAN_DAY` queries and schedules only the selected macro day, while `REPLAN_TRIP` selects all macro days. Locked items are merged back by stable item/POI identity; affected days with retained locks are marked `PENDING` for route re-verification instead of claiming validated facts. The 31-module `test-compile` and 13 focused tests passed.
- 2026-09-21: Started Phase 4 with the App `POST /ai/chat/message/itinerary/change` endpoint. It verifies conversation ownership, maps the request directly to `TripChangeCommand`, and returns the newly persisted immutable version. No additional Agent run endpoint was added; `/managed/run` remains the only run entry. Compile and 7 focused controller/editor tests passed. Agent natural-language command conversion remains open.
