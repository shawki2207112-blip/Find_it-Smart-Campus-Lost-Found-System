# Requirements traceability

**Status legend**

* **In zip** - the code/config/document is in this project. It does *not* mean it has been run: the authoring sandbox had
  no access to Maven Central, so `mvn -B verify` was **not executed** there. Run it first (`mvn -B verify`).
* **Manual step required** - cannot be done by the zip; the team must do it (repository, branch protection, Render, ...).

**Important note about the rubric.** The brief given to the tool that generated this project listed the requirements and
mentioned "all 100 marks + both bonus blocks" but did **not include the teacher's rubric text or the automatic-failure
list**. Sections B (rubric themes) and C (automatic-failure conditions) are therefore derived from the brief's own
sections and are marked *derived*. Paste the official rubric lines and marks next to them before handing in; no mark
values are invented here.

## A. Teacher requirements from the brief

| ID | Requirement | What satisfies it | Files / classes | How to verify | Status |
|---|---|---|---|---|---|
| A1 | Java 17, Spring Boot 3.2.x, Maven, no Lombok | Parent `spring-boot-starter-parent 3.2.5`, `java.version=17`, plain getters/records | `pom.xml` | `grep -ri lombok pom.xml src` returns nothing; `mvn -v` | In zip |
| A2 | Package root `com.findit`, layered modules | auth, user, item, category, claim, match, notification, admin, common, config | `src/main/java/com/findit/*` | `ls src/main/java/com/findit` | In zip |
| A3 | Roles STUDENT / SECURITY_STAFF / ADMIN in DB; registration creates STUDENT | `RoleName`, `Role`, `AuthService.register` | `user/Role*.java`, `auth/AuthService.java` | IT `AuthControllerIT.register_alwaysCreatesStudent_evenIfRoleIsSupplied` | In zip |
| A4 | Admin/security + roles + 8 categories seeded from env vars, no hard-coded credentials | `DataInitializer` reads `ADMIN_*`, `SECURITY_*` | `config/DataInitializer.java`, `.env.example` | Start app; log in with `.env` values; `scripts/verify-requirements.sh` secret check | In zip |
| A5 | F1 report items, CRUD with ownership checks, admin may delete any | `ItemService`, `ItemController` | `item/ItemService.java`, `item/ItemController.java` | `ItemControllerIT.studentCannotEditOrDeleteAnotherStudentsItem`, `ownerAndAdminCanDelete...` | In zip |
| A6 | F2 smart match scoring 30/25/30/15, threshold 60 configurable, MATCHED, notifications | `MatchScorer` + 4 `ScoreComponent`s, `MatchService`, `findit.match.threshold` | `match/*`, `application.yml` | `MatchScoringTest`, `MatchServiceTest`; Swagger: post lost + found | In zip |
| A7 | Students view suggestions for own items only | `MatchService.listForUser/listForItem` | `match/MatchController.java` | `MatchServiceTest.listForItem_byNonOwner_throwsForbidden` | In zip |
| A8 | F3 claim: not own item, one PENDING per user/item, approve/reject, return, 409 | `ClaimService`, `ClaimController` | `claim/*` | `ClaimServiceTest`, `ClaimLifecycleIT` | In zip |
| A9 | F4 lifecycle enforced in ONE place + `ItemStatusHistory` | `ItemStatus.allowedNext`, `ItemStatusService.transition` | `item/ItemStatus.java`, `item/ItemStatusService.java` | `ItemStatusServiceTest`; `GET /api/items/{id}/history` | In zip |
| A10 | F5 search/filter/pagination with JPA Specifications; role-aware visibility | `ItemSpecifications`, `ItemService.search` | `item/ItemSpecifications.java` | `ItemControllerIT.search_...`, `securityStaff_seesOnlyFoundItemsInSearch` | In zip |
| A11 | F6 notifications (5 types) persisted, list/mark read/delete | `Notification`, `NotificationService`, `NotificationController` | `notification/*` | `NotificationFactoryTest`, `AdminAndNotificationIT.notifications_...` | In zip |
| A12 | F7 admin statistics (returned, top-5 lost categories, unresolved, by status, users) | `StatisticsServiceImpl`, `AdminController` | `admin/*` | `AdminAndNotificationIT.adminStats_returnsCorrectCounts` | In zip |
| A13 | PostgreSQL, >= 6 tables (10 built), LAZY, unique constraints, indexes, `ddl-auto=update` documented | 9 entities + `user_roles` join table | entities in each module, README section 6 | README ER diagram; start with Docker, inspect with `psql \dt` | In zip |
| A14 | BCrypt registration, JWT login, logout with blacklist, scheduled purge, secrets from env | `AuthService`, `JwtService`, `TokenBlacklist*`, `JwtAuthenticationFilter` | `auth/*` | `AuthControllerIT.registerLoginLogoutFlow...`, `AuthServiceTest`, `JwtServiceTest` | In zip |
| A15 | URL rules + `@PreAuthorize` + `@EnableMethodSecurity`, ownership checks in services | `SecurityConfig`, controllers, services | `config/SecurityConfig.java` | `SecurityRulesIT`, verify script | In zip |
| A16 | Custom 401/403 JSON with the shared ErrorResponse | `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`, `ErrorResponseWriter` | `config/Rest*.java`, `common/ErrorResponse*.java` | `AuthControllerIT.requestWithoutToken...`, `SecurityRulesIT.studentOnAdminEndpoint...` | In zip |
| A17 | Public paths: register, login, swagger, api-docs, health, prometheus, static UI | `SecurityConfig` matchers | `config/SecurityConfig.java` | `SecurityRulesIT.publicEndpointsAreReachableWithoutToken` | In zip |
| A18 | >= 5 controllers (7 required list), CRUD on >= 3 entities, 201+Location, 204, 400/401/403/404/409 | 9 `@RestController` classes | `*Controller.java` | verify script; Swagger UI | In zip |
| A19 | `GlobalExceptionHandler` with all listed exception types, no stack trace in body | `GlobalExceptionHandler` | `common/GlobalExceptionHandler.java` | `ItemControllerIT.unknownItem_returns404...` | In zip |
| A20 | Patterns: Factory | `NotificationFactory` | `notification/NotificationFactory.java` | `NotificationFactoryTest` | In zip |
| A21 | Patterns: Adapter (`NotificationSender`, Email + InApp, `findit.notification.channel`) | adapters + `EmailClient` | `notification/*Adapter.java` | `NotificationAdapterTest` | In zip |
| A22 | Patterns: Proxy (`StatisticsService`, 60 s own cache, delegate-once test) | `CachedStatisticsServiceProxy` | `admin/CachedStatisticsServiceProxy.java` | `StatisticsServiceTest.proxy_callsDelegateOnlyOnceWithinTtl` | In zip |
| A23 | Patterns: Singleton (`MatchConfig`, thread-safe, loaded once) | enum singleton + `MatchConfigLoader` | `match/MatchConfig.java` | `MatchScoringTest.matchConfig_isSingleton_...` | In zip |
| A24 | Patterns: Bridge (optional) | `MatchScorer` -> `ScoreComponent` | `match/MatchScorer.java` | `MatchScoringTest` | In zip |
| A25 | Patterns documented (class, role, reason) with Javadoc "Pattern: X - why" | README section 8 + Javadoc | `README.md` | read README section 8 | In zip |
| A26 | Unit tests >= 28 methods (service layer, no Spring) | 84 `@Test` methods | `src/test/java/com/findit/**/*Test.java` | `scripts/verify-requirements.sh`; surefire report | In zip (not executed in the authoring sandbox) |
| A27 | Integration tests >= 14 (SpringBootTest + MockMvc + H2 PostgreSQL mode, real login helper) | 25 `@Test` methods | `src/test/java/com/findit/it/*IT.java`, `src/test/resources/application-test.yml` | failsafe report | In zip (not executed in the authoring sandbox) |
| A28 | `mvn -B verify` passes with no external services, tests not disabled | H2 in tests, no `@Disabled`/skip | `pom.xml` | `mvn -B verify` | **Manual step required: run it** (not run in authoring sandbox) |
| A29 | Dockerfile multi-stage, non-root, EXPOSE 8080, HEALTHCHECK, `PORT` honoured | `Dockerfile`, `server.port=${PORT:8080}` | `Dockerfile`, `application.yml` | `docker build .`; `docker inspect --format '{{.State.Health.Status}}'` | In zip |
| A30 | docker-compose: app + postgres:16 (volume, healthcheck, service_healthy), env from `.env`, no secrets/defaults | `docker-compose.yml` | `docker-compose.yml`, `.env.example` | `cp .env.example .env && docker compose up --build`; open `http://localhost:8080` | In zip |
| A31 | `.env.example` with all variables, `.env` in `.gitignore` | listed variables | `.env.example`, `.gitignore` | verify script | In zip |
| A32 | Render `DATABASE_URL` support documented | `DatabaseUrlConfig`, `DatabaseUrlParser`, README section 9.4 / 12 | `config/DatabaseUrl*.java` | `DatabaseUrlParserTest` | In zip |
| A33 | GitHub Actions: triggers, `build-test` with Maven cache + report artifact, deploy only on main via hook, fails if hook fails | `ci-cd.yml` (`curl -fsS`) | `.github/workflows/ci-cd.yml` | Open a PR; Actions tab | In zip; **running it needs the GitHub repo** |
| A34 | PR template, CODEOWNERS (placeholders), branch scripts, GIT_WORKFLOW.md | files present | `.github/*`, `scripts/setup-*.sh`, `GIT_WORKFLOW.md` | read files | In zip |
| A35 | `render.yaml` Blueprint (autoDeploy false, DB, `sync:false` secrets) | `render.yaml` | `render.yaml` | Render > New > Blueprint | In zip |
| A36 | UI: Bootstrap 5 + vanilla JS pages incl. shared `api.js`, `auth.js`, `navbar.js`, `style.css` | 15 pages | `src/main/resources/static/**` | open `http://localhost:8080` | In zip |
| A37 | Monitoring: Actuator, Prometheus, Grafana provisioned dashboard, custom counters | `FindItMetrics`, `monitoring/**` | `monitoring/*`, `common/FindItMetrics.java` | `docker compose up`, open Grafana :3000 | In zip; **live Grafana check is manual** |
| A38 | Bonus 2: second platform guide + config | `docs/DEPLOY_OTHER_PLATFORM.md`, `fly.toml` | those files | follow the guide | In zip; **deployment is manual** |
| A39 | README (diagrams, API table, patterns, run, env table, CI/CD, Render, monitoring, testing, REPLACE ME placeholders) | `README.md` | `README.md` | read; verify script | In zip |
| A40 | DEMO_SCRIPT, Postman collection, REQUIREMENTS_TRACEABILITY, `verify-requirements.sh` | files present | `docs/*`, this file, `scripts/verify-requirements.sh` | `./scripts/verify-requirements.sh` | In zip |
| A41 | Code quality: constructor injection, no logic in controllers, DTOs, SLF4J, no unused imports, Javadoc on services | applied everywhere | all of `src/main/java` | code review; unused-import scan was run once by the generator | In zip |

## B. Rubric themes (derived from the brief - add the official lines and marks)

| Theme | Covered by | Status |
|---|---|---|
| Application features and correctness | A5-A12 | In zip |
| Database design | A13 | In zip |
| Security | A14-A17 | In zip |
| REST API quality | A18-A19 | In zip |
| Design patterns | A20-A25 | In zip |
| Testing | A26-A28 | In zip; running is manual |
| Docker | A29-A31 | In zip |
| CI/CD, Git workflow, branch protection | A33-A35 | In zip; **applying them is manual** (rows M1-M8) |
| UI | A36 | In zip |
| Documentation | A39-A40 | In zip |
| Bonus 1: monitoring (Prometheus + Grafana + custom counters) | A37 | In zip; live check manual |
| Bonus 2: second deployment | A38 | In zip; deployment manual |

## C. Automatic-failure conditions (derived - confirm against the official list)

| Condition | Where handled | How to check | Status |
|---|---|---|---|
| Tests failing or skipped in CI | tests are not disabled; CI runs `mvn -B verify` | Actions tab green | In zip; **must be confirmed by running** |
| Secrets/passwords committed | all via env vars; `.env` git-ignored; `.env.example` has `CHANGE_ME` only | verify script, `git log -p | grep -i password` | In zip |
| App does not start with `docker compose up --build` | compose + Dockerfile | run it | In zip; **must be confirmed by running** |
| No CI / no deploy workflow | workflow file | file exists | In zip |
| Fewer than the required tests / controllers / entities | 84 + 25 tests, 9 controllers, 9 entities (+ join table) | verify script | In zip |
| Unprotected `main` / direct pushes | script provided | Settings > Branches | **Manual step required** (M3) |
| Live deployment missing | Render config provided | open the live URL | **Manual step required** (M5-M6) |
| Project not showing real PR-based collaboration | process documented | PR list with reviews from 2+ members | **Manual step required** (M7) |

## D. Manual steps the zip cannot do

| ID | Step | Status |
|---|---|---|
| M1 | Run `mvn -B verify`, fix anything that fails on your machine (the generator could not run it) | Manual step required |
| M2 | Create the GitHub repository, push `main`, run `scripts/setup-branches.sh` | Manual step required |
| M3 | Enable branch protection: `scripts/setup-branch-protection.sh <owner>/<repo>` (needs `gh` and admin rights); replace usernames in `.github/CODEOWNERS` | Manual step required |
| M4 | Add GitHub secret `RENDER_DEPLOY_HOOK_URL` | Manual step required |
| M5 | Create the Render Blueprint (service + DB), fill `sync:false` variables, set Auto-Deploy OFF | Manual step required |
| M6 | Merge a PR into `main` so the `deploy` job runs; put the live URL and repo URL into the README (`REPLACE ME`) | Manual step required |
| M7 | Develop through real pull requests with real review approvals from teammates | Manual step required |
| M8 | Run the stack with Docker Compose and check Prometheus targets and the Grafana dashboard live | Manual step required |
| M9 | Bonus: deploy to Fly.io (or Railway) following `docs/DEPLOY_OTHER_PLATFORM.md` | Manual step required |
| M10 | Rehearse and give the demo (`docs/DEMO_SCRIPT.md`) | Manual step required |
