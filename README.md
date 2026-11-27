# FindIt - Smart Campus Lost & Found System

CSE 3220 lab project (team of 3). A Spring Boot REST application where students post lost or found items, the system
scores lost-vs-found posts and suggests matches, and security staff verify every claim before an item is handed over.

| | |
|---|---|
| **Live URL** | REPLACE ME (e.g. `https://findit.onrender.com`) |
| **Repository URL** | REPLACE ME (e.g. `https://github.com/<owner>/findit`) |
| **Swagger UI** | `<live URL>/swagger-ui.html` |

## 1. Problem statement

Lost items on a campus are usually reported on noticeboards or group chats. Owners and finders rarely meet, and when they
do there is no check that the person collecting an item really owns it. FindIt gives the campus one place to report lost
and found items, automatically suggests likely pairs, and puts a human check (security staff) between a claim and the
hand-over, with a full history of every status change.

## 2. Features

| # | Feature | Summary |
|---|---|---|
| F1 | Report items | LOST / FOUND with title, description, category, location, event date, optional `photoRef` (a URL string, no upload). CRUD with ownership checks. |
| F2 | Smart match suggestions | On creation, compared with open items of the opposite type in the same category. Score 0-100 (category 30, location 25, keyword Jaccard 30, date proximity 15). Stored when score >= `findit.match.threshold` (default 60). |
| F3 | Claim and verification | A student claims a FOUND item with a `proofDetail`; security staff approve or reject (reject needs a note); staff mark a VERIFIED item as returned. Invalid steps give HTTP 409. |
| F4 | Lifecycle | `REPORTED -> MATCHED -> CLAIMED -> VERIFIED -> RETURNED`, enforced in one method (`ItemStatusService.transition`), every change stored in `item_status_history`. |
| F5 | Search / filter | `GET /api/items` with `category`, `location`, `type`, `status`, `fromDate`, `toDate`, `page`, `size`, `sort` (JPA Specifications). Students see non-sensitive fields of other people's items. |
| F6 | Notifications | `MATCH_FOUND`, `CLAIM_SUBMITTED`, `CLAIM_APPROVED`, `CLAIM_REJECTED`, `ITEM_RETURNED` stored per user; list, mark read, delete. |
| F7 | Admin statistics | Items returned, top 5 lost categories, unresolved items, totals by status, total users. |

Extra: JWT logout with a token blacklist, Prometheus/Grafana monitoring, Swagger UI, a small Bootstrap web UI.

## 3. Roles

| Role (authority) | Can do |
|---|---|
| `ROLE_STUDENT` | Register, post lost/found items, view/edit/delete **own** items, search, submit/withdraw claims, see match suggestions for **own** items, own notifications. |
| `ROLE_SECURITY_STAFF` | See all FOUND items, list and review claims (approve / reject), mark verified items as returned. |
| `ROLE_ADMIN` | Manage users (list, view, change roles, enable/disable, delete), statistics, delete any post, manage categories. |

Public registration **always** creates a STUDENT. Admin and security accounts are seeded on startup (section 9).

## 4. Tech stack

Java 17, Spring Boot 3.2.x, Maven, Spring Web, Spring Data JPA, Spring Security 6, PostgreSQL 16, Bean Validation,
springdoc-openapi (Swagger UI), Actuator + Micrometer Prometheus, JJWT 0.12, JUnit 5, Mockito, SpringBootTest + MockMvc,
H2 (test scope only), Docker / Docker Compose, GitHub Actions, Render, Prometheus + Grafana. No Lombok.

## 5. Architecture

```mermaid
flowchart LR
    subgraph Client
        B[Browser - Bootstrap UI + vanilla JS]
    end
    subgraph App["Spring Boot application (Docker container)"]
        SF[Security filter chain<br/>JwtAuthenticationFilter]
        C[Controllers - REST + DTOs]
        S[Services - business rules]
        R[Repositories - Spring Data JPA]
        SF --> C --> S --> R
        ACT[Actuator /actuator/prometheus]
    end
    DB[(PostgreSQL)]
    B -->|HTTPS + Bearer JWT| SF
    R --> DB
    subgraph Monitoring
        P[Prometheus] --> G[Grafana]
    end
    P -->|scrape every 15s| ACT
    subgraph CICD["CI/CD"]
        GH[GitHub Actions<br/>mvn verify] -->|deploy hook on main| RN[Render]
    end
    RN -->|runs Docker image| App
    RN --> RDB[(Render PostgreSQL)]
```

Package layout (`com.findit`): `auth`, `user`, `item`, `category`, `claim`, `match`, `notification`, `admin`,
`common` (errors, metrics), `config` (security, seeding, DB URL, OpenAPI). Controllers only translate HTTP; all rules
live in services; entities are never returned from the API (DTO records).

## 6. Database (ER diagram)

Ten tables. `ddl-auto=update` is used on purpose: the schema is created automatically on first start (also in Docker
Compose and on Render), which keeps the lab simple. A production system would use Flyway/Liquibase migrations instead.

```mermaid
erDiagram
    users ||--o{ items : "posts"
    users ||--o{ claims : "submits"
    users ||--o{ notifications : "receives"
    users ||--o{ user_roles : "has"
    roles ||--o{ user_roles : "granted via"
    categories ||--o{ items : "classifies"
    items ||--o{ claims : "receives"
    items ||--o{ item_status_history : "records"
    users ||--o{ item_status_history : "changed_by"
    items ||--o{ match_suggestions : "lost_item"
    items ||--o{ match_suggestions : "found_item"

    users {
        bigint id PK
        varchar full_name
        varchar email UK
        varchar password
        boolean enabled
        timestamp created_at
    }
    roles {
        bigint id PK
        varchar name UK
    }
    user_roles {
        bigint user_id FK
        bigint role_id FK
    }
    categories {
        bigint id PK
        varchar name UK
    }
    items {
        bigint id PK
        varchar type "LOST or FOUND"
        varchar title
        varchar description
        bigint category_id FK
        varchar location
        date event_date
        varchar photo_ref
        varchar status
        bigint owner_id FK
        timestamp created_at
    }
    claims {
        bigint id PK
        bigint item_id FK
        bigint claimant_id FK
        varchar proof_detail
        varchar status
        varchar review_note
        bigint reviewed_by FK
        timestamp created_at
        timestamp reviewed_at
    }
    match_suggestions {
        bigint id PK
        bigint lost_item_id FK
        bigint found_item_id FK
        int score
        timestamp created_at
    }
    notifications {
        bigint id PK
        bigint recipient_id FK
        varchar type
        varchar message
        boolean is_read
        timestamp created_at
    }
    item_status_history {
        bigint id PK
        bigint item_id FK
        varchar from_status
        varchar to_status
        bigint changed_by FK
        timestamp changed_at
    }
    token_blacklist {
        bigint id PK
        varchar jti UK
        timestamp expires_at
    }
```

Unique constraints: `users.email`, `roles.name`, `categories.name`, `token_blacklist.jti`, `(lost_item_id, found_item_id)`.
Indexes on `items(status)`, `items(category_id)`, `items(type)`. All `@ManyToOne` / `@OneToMany` use `FetchType.LAZY`;
only `User.roles` is eager (every request needs the roles).

## 7. REST API

All endpoints are under `/api`. Errors always use `{timestamp, status, error, message, path, fieldErrors?}`.
Interactive documentation: `/swagger-ui.html` (use **Authorize** with the token from login).

| Method | Path | Role | Description | Success |
|---|---|---|---|---|
| POST | `/api/auth/register` | public | Register a student | 201 + Location |
| POST | `/api/auth/login` | public | Log in, returns JWT | 200 |
| POST | `/api/auth/logout` | any logged-in | Blacklist the current token | 204 |
| GET | `/api/auth/me` | any logged-in | Current user | 200 |
| GET | `/api/public/items/recent` | public | Latest open items, non-sensitive fields (landing page) | 200 |
| POST | `/api/items` | STUDENT | Report a lost/found item (runs matching) | 201 + Location |
| GET | `/api/items` | any logged-in | Search/filter/paginate (role-aware visibility) | 200 |
| GET | `/api/items/my` | STUDENT | Own items | 200 |
| GET | `/api/items/{id}` | any logged-in | One item (restricted fields unless owner/staff/admin) | 200 |
| GET | `/api/items/{id}/history` | any logged-in | Status timeline | 200 |
| PUT | `/api/items/{id}` | STUDENT (owner) | Edit own item while REPORTED/MATCHED | 200 |
| DELETE | `/api/items/{id}` | STUDENT (owner) / ADMIN | Delete item | 204 |
| GET | `/api/categories` | any logged-in | List categories | 200 |
| GET | `/api/categories/{id}` | any logged-in | One category | 200 |
| POST | `/api/categories` | ADMIN | Create category | 201 + Location |
| PUT | `/api/categories/{id}` | ADMIN | Rename category | 200 |
| DELETE | `/api/categories/{id}` | ADMIN | Delete (409 if in use) | 204 |
| GET | `/api/matches/my` | STUDENT | Suggestions for own items | 200 |
| GET | `/api/matches/item/{itemId}` | STUDENT (owner) | Suggestions for one own item | 200 |
| POST | `/api/claims` | STUDENT | Claim a FOUND item with `proofDetail` | 201 + Location |
| GET | `/api/claims/my` | STUDENT | Own claims | 200 |
| GET | `/api/claims/{id}` | claimant / STAFF / ADMIN | One claim | 200 |
| DELETE | `/api/claims/{id}` | STUDENT (claimant) | Withdraw a PENDING claim | 204 |
| GET | `/api/claims/review?status=PENDING` | SECURITY_STAFF | List claims by status | 200 |
| PUT | `/api/claims/review/{id}/approve` | SECURITY_STAFF | Approve (item -> VERIFIED) | 200 |
| PUT | `/api/claims/review/{id}/reject` | SECURITY_STAFF | Reject with `reviewNote` | 200 |
| POST | `/api/claims/review/{id}/return` | SECURITY_STAFF | Mark item RETURNED | 200 |
| GET | `/api/notifications` | any logged-in | Own notifications | 200 |
| PUT | `/api/notifications/{id}/read` | owner | Mark read | 200 |
| DELETE | `/api/notifications/{id}` | owner | Delete | 204 |
| GET | `/api/admin/users` | ADMIN | List users | 200 |
| GET | `/api/admin/users/{id}` | ADMIN | One user | 200 |
| PUT | `/api/admin/users/{id}/roles` | ADMIN | Replace roles | 200 |
| PUT | `/api/admin/users/{id}/enabled` | ADMIN | Enable / disable | 200 |
| DELETE | `/api/admin/users/{id}` | ADMIN | Delete (409 if user owns items/claims) | 204 |
| GET | `/api/admin/stats` | ADMIN | Statistics (cached 60 s by the Proxy) | 200 |
| GET | `/actuator/health`, `/actuator/prometheus` | public | Health and metrics | 200 |

Status codes: 400 validation, 401 missing/invalid/blacklisted token, 403 wrong role or not the owner, 404 unknown id,
409 invalid state transition or duplicate.

`GET /api/public/items/recent` is a small addition to the brief's public list so the landing page can show recent items
without logging in; it never returns descriptions or owners.

## 8. Design patterns

Each class carries a `Pattern: X - why` Javadoc line. All are used by real code paths and have unit tests.

| Pattern | Classes and their role | Why |
|---|---|---|
| **Factory** | `NotificationFactory.create(type, recipient, context)` builds the `Notification` (type + message) for each event. `NotificationService` is the only caller; services never assemble messages. | Message wording and object creation live in one place; adding an event type touches one class. |
| **Adapter** | `NotificationSender` is the target interface. `InAppNotificationAdapter` adapts the repository (saves a row); `EmailNotificationAdapter` adapts the incompatible `EmailClient.sendMail(to, subject, body)` API. Chosen by `findit.notification.channel` (`in-app` default, `email`). | Business code depends only on `NotificationSender`; delivery channels can be swapped by configuration. |
| **Proxy** | `StatisticsService` (subject), `StatisticsServiceImpl` (real subject, DB queries), `CachedStatisticsServiceProxy` (`@Primary`, own 60-second cache, delegates on a miss). | Statistics need several count queries; the proxy avoids repeating them for every dashboard refresh without changing callers. Test: delegate called once within the TTL. |
| **Singleton** | `MatchConfig` is an `enum` singleton holding threshold and weights, loaded once at startup by `MatchConfigLoader` from `findit.match.*`; the score components read it. | One shared, thread-safe configuration for the scoring code (enum singletons are thread-safe by JVM guarantee, `settings` is `volatile`). Spring beans are singleton-scoped by default too, but this one is explicit and framework-independent. |
| **Bridge** (optional) | `MatchScorer` (abstraction) delegates to `ScoreComponent` implementors `CategoryScore`, `LocationScore`, `KeywordScore`, `DateScore`. | Scoring rules can change or be added independently of how scores are combined. |

## 9. Running the project

### 9.1 Docker Compose (recommended)

```bash
cp .env.example .env        # then edit every CHANGE_ME value
docker compose up --build
```

App: <http://localhost:8080> - Swagger: <http://localhost:8080/swagger-ui.html> - Prometheus: <http://localhost:9090> -
Grafana: <http://localhost:3000>. The schema is created automatically. Compose refuses to start if `POSTGRES_PASSWORD`,
`JWT_SECRET` or `GRAFANA_ADMIN_PASSWORD` is missing (they have no default).

### 9.2 Local with Maven and a local PostgreSQL

```bash
createdb findit            # or create the database any other way
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/findit
export SPRING_DATASOURCE_USERNAME=<db user>
export SPRING_DATASOURCE_PASSWORD=<db password>
export JWT_SECRET=<at least 32 random characters>
export ADMIN_EMAIL=admin@example.com   ADMIN_PASSWORD=<choose>
export SECURITY_EMAIL=security@example.com   SECURITY_PASSWORD=<choose>
mvn spring-boot:run
```

### 9.3 Tests

```bash
mvn -B verify      # unit tests (surefire) + integration tests (failsafe); needs no external service
```

### 9.4 Environment variables

| Variable | Required | Meaning |
|---|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | compose | Database container settings |
| `SPRING_DATASOURCE_URL` | yes* | JDBC URL, e.g. `jdbc:postgresql://db:5432/findit` |
| `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | yes* | DB credentials (compose passes `POSTGRES_USER` / `POSTGRES_PASSWORD`) |
| `DATABASE_URL` | Render | `postgres://user:pass@host:5432/db`; when set it replaces the three variables above (`DatabaseUrlConfig`) |
| `JWT_SECRET` | yes | HMAC secret, at least 32 characters, **no default** |
| `JWT_EXPIRATION_MS` | no | Token lifetime, default `86400000` (24 h) |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | no | Seeded admin account (skipped if blank) |
| `SECURITY_EMAIL`, `SECURITY_PASSWORD` | no | Seeded security-staff account (skipped if blank) |
| `GRAFANA_ADMIN_PASSWORD` | compose | Grafana admin password |
| `PORT` | no | HTTP port (Render sets it), default `8080` |

\* not needed when `DATABASE_URL` is set.

### 9.5 Seeded accounts

On startup `DataInitializer` creates the three roles, the eight default categories (Electronics, Books, ID/Cards,
Clothing, Keys, Bags, Accessories, Other) and - only if the e-mail does not exist yet - one ADMIN and one SECURITY_STAFF
account whose e-mail and password come from `ADMIN_EMAIL` / `ADMIN_PASSWORD` and `SECURITY_EMAIL` /
`SECURITY_PASSWORD`. Nothing is hard-coded. Students register themselves through `/register.html`.

### 9.6 Web UI

Served by the same application from `src/main/resources/static`: `index.html`, `login.html`, `register.html`,
`student/*` (post-item, my-items, search, item-detail with status timeline and claim form, matches, claims),
`notifications.html`, `security/*` (claims, found-items), `admin/*` (users, stats, categories). Shared files:
`css/style.css`, `js/api.js` (fetch wrapper), `js/auth.js` (token and role helpers), `js/navbar.js` (role-aware navbar).

## 10. CI/CD

`.github/workflows/ci-cd.yml`

| Job | Runs on | Steps |
|---|---|---|
| `build-test` | every pull request to `main`/`develop` and every push to them | checkout, Java 17 (Temurin, Maven cache), `mvn -B verify`, upload surefire/failsafe reports, `docker build` check |
| `deploy` | **push to `main` only**, `needs: build-test` | `curl -fsS -X POST "$RENDER_DEPLOY_HOOK_URL"` - the job fails if the hook call fails |

GitHub secret required: `RENDER_DEPLOY_HOOK_URL`. Set the Render service's **Auto-Deploy to OFF** so GitHub Actions is
the only deployer. Branch protection is applied by `scripts/setup-branch-protection.sh` (main: PR required, 1 approving
review, status check `build-test`, stale reviews dismissed, enforced for admins, no force push, no deletion).

## 11. Git workflow (summary)

`main` = protected, always deployable. `develop` = integration branch. Work happens on `feature/<topic>` branches that
start from `develop`, are merged by pull request after review and a green `build-test`. Commit messages follow
`type(scope): summary`. Full rules, member split and merge-conflict handling: [GIT_WORKFLOW.md](GIT_WORKFLOW.md).

## 12. Deploying to Render (step by step)

1. Push the repository to GitHub (section 11 of GIT_WORKFLOW.md).
2. In Render choose **New + > Blueprint**, select the repository. Render reads `render.yaml` and creates the web service
   `findit` (Docker, `autoDeploy: false`) and the PostgreSQL database `findit-db`.
3. When prompted, fill the `sync: false` variables: `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `SECURITY_EMAIL`,
   `SECURITY_PASSWORD`. `JWT_SECRET` is generated by Render. `DATABASE_URL` is wired from the database automatically.
4. Confirm the service shows **Auto-Deploy: No** (Settings > Build & Deploy).
5. Copy the **Deploy Hook** URL (Settings > Deploy Hook) and store it in GitHub: Settings > Secrets and variables >
   Actions > New repository secret `RENDER_DEPLOY_HOOK_URL`.
6. Merge a pull request into `main`: the `deploy` job calls the hook and Render builds the Docker image.
7. Open the service URL, check `/actuator/health` and log in with the seeded admin. Put the URL into this README.

Exact Render environment variables: `DATABASE_URL`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `ADMIN_EMAIL`,
`ADMIN_PASSWORD`, `SECURITY_EMAIL`, `SECURITY_PASSWORD` (`PORT` is injected by Render). A second platform (Fly.io) is
described in [docs/DEPLOY_OTHER_PLATFORM.md](docs/DEPLOY_OTHER_PLATFORM.md).

## 13. Monitoring

Actuator exposes `health`, `info`, `metrics`, `prometheus`. With Docker Compose:

* Prometheus <http://localhost:9090> (config `monitoring/prometheus.yml`, scrapes `app:8080/actuator/prometheus`)
* Grafana <http://localhost:3000> (user `admin`, password = `GRAFANA_ADMIN_PASSWORD`), dashboard **FindIt Overview**
  is provisioned: HTTP request rate, p95 latency, 5xx count, JVM heap, CPU, DB pool usage, plus the custom counters.

Custom Micrometer counters: `findit_items_created_total{type}`, `findit_claims_total{status}`,
`findit_matches_found_total`.

## 14. Testing

| Kind | Where | Count |
|---|---|---|
| Unit tests (JUnit 5 + Mockito, no Spring context) | `src/test/java/com/findit/**/*Test.java` | 84 `@Test` methods |
| Integration tests (`@SpringBootTest`, MockMvc, H2 in PostgreSQL mode, real `/api/auth/login` tokens) | `src/test/java/com/findit/it/*IT.java` | 25 `@Test` methods |

Unit tests cover AuthService, JwtService, ItemService, ItemStatusService (valid/invalid transitions, history), every
score component and the scorer, MatchService (threshold, persisted / not persisted, items moved to MATCHED),
ClaimService, NotificationFactory, both notification adapters, StatisticsService and the caching proxy,
AdminUserService, the DATABASE_URL parser. Integration tests cover the register/login/logout flow, 401/403/404/409
formats, validation, search filters, ownership, the full lifecycle (lost -> found -> match -> claim -> approve -> return),
admin statistics, notifications and categories.

Run them with `mvn -B verify`. `scripts/verify-requirements.sh` is a static checklist (it counts tests, controllers and
entities and greps for secrets); it does not run the tests.

## 15. Documentation index

* [REQUIREMENTS_TRACEABILITY.md](REQUIREMENTS_TRACEABILITY.md) - every requirement mapped to files, verification and status
* [GIT_WORKFLOW.md](GIT_WORKFLOW.md) - branching, PRs, member split
* [docs/DEMO_SCRIPT.md](docs/DEMO_SCRIPT.md) - 15-minute presentation outline
* [docs/DEPLOY_OTHER_PLATFORM.md](docs/DEPLOY_OTHER_PLATFORM.md) - Fly.io guide (bonus)
* [docs/postman/FindIt.postman_collection.json](docs/postman/FindIt.postman_collection.json) - Postman collection
