# Demo script - 15 minutes, three presenters

Prepare before the session: stack running (`docker compose up --build` or the Render URL), three browser profiles
(student A, student B, security staff) plus one for the admin, the Swagger tab open, Grafana dashboard open.
Seeded admin and security accounts come from your `.env`; register two students beforehand (`alice`, `bob`).

| Time | Presenter | Content |
|---|---|---|
| 0:00 - 1:30 | Allfi | Problem statement, roles, the lifecycle `REPORTED -> MATCHED -> CLAIMED -> VERIFIED -> RETURNED` |
| 1:30 - 3:30 | Beg | Architecture and ER diagram (README), BCrypt, JWT login/logout and blacklist, role checks (URL rules + `@PreAuthorize`), error format (401/403/404/409) |
| 3:30 - 5:00 | Allfi | Item module: CRUD, ownership checks, search with JPA Specifications, categories, restricted fields for other students |
| 5:00 - 6:30 | Allfi | CI: a pull request with the green `build-test` check, branch protection, `mvn -B verify` report (84 unit + 25 integration tests) |
| 6:30 - 12:00 | Shawki (leads), Allfi = student, Beg = security staff and admin | **Live demo of the full lifecycle (below)** |
| 12:00 - 13:30 | each own part | Design patterns, one minute each: Allfi - Factory and Adapter (notifications); Beg - Proxy (statistics); Shawki - Singleton and Bridge (matching). Show the `Pattern:` Javadoc and the unit test |
| 13:30 - 14:30 | Shawki | Docker Compose, Render deploy hook flow, Prometheus target and Grafana dashboard with the custom counters |
| 14:30 - 15:00 | all | Summary, known limitations, questions |

## Live demo of the full lifecycle (three roles)

1. **Alice (student)** - *Post item*: type LOST, "Black iPhone 13", description "black iphone cracked screen case",
   category Electronics, location "Library", today. Show it in *My items* with status REPORTED.
2. **Bob (student)** - post a FOUND item: "Black iPhone 13", same description, location "library". The system scores the
   pair (category 30 + location 25 + keywords + date) and stores a suggestion.
3. **Alice** - *Matches* page shows the suggestion with its score; *Notifications* shows `MATCH_FOUND`; both items are now
   MATCHED (open the item detail to show the status timeline).
4. **Alice** - open Bob's found item and submit a claim with a proof detail ("lock screen photo is my dog"). Status becomes
   CLAIMED; Bob gets `CLAIM_SUBMITTED`. Try to claim a second time: 409 message in the UI.
5. **Security staff** - *Claims*: the pending claim with the proof detail. First **Reject** (note required) to show the
   item returning to MATCHED and Alice's `CLAIM_REJECTED`; have Alice claim again, then **Approve** (item VERIFIED).
6. **Security staff** - switch the filter to *Approved claims*, click **Mark returned** (item RETURNED, final). Show the
   complete timeline and the `ITEM_RETURNED` notifications.
7. **Admin** - *Statistics*: items returned = 1, top lost categories, unresolved items. Mention that the statistics call
   is served by the caching Proxy (60 s).
8. Show the same flow's error handling in Swagger: call `PUT /api/claims/review/{id}/approve` twice -> 409 JSON.

Fallback if the live system is down: the Postman collection `docs/postman/FindIt.postman_collection.json` runs the same
flow against `localhost`.
