# Git workflow

## Branches

| Branch | Purpose | Rules |
|---|---|---|
| `main` | Always deployable; a push here triggers the Render deploy | Protected: pull request only, 1 approving review, status check `build-test` must pass, stale reviews dismissed, applies to admins too, no force push, no deletion |
| `develop` | Integration branch where finished features meet | Not protected (see the note under "Pull request flow"); PRs into it still need a review |
| `feature/<topic>` | One small piece of work, e.g. `feature/item-search`, `fix/claim-409` | Created from `develop`, deleted after merge |

Release: when `develop` is stable, open a pull request `develop -> main`. Never commit directly to `main`.

## Setup (run once by the team lead)

```bash
git init -b main && git add . && git commit -m "chore: initial project"
git remote add origin https://github.com/<owner>/findit.git
git push -u origin main
./scripts/setup-branches.sh                              # creates and pushes develop
./scripts/setup-branch-protection.sh <owner>/findit   # protects main only; needs gh CLI + admin rights
```

Also add the GitHub secret `RENDER_DEPLOY_HOOK_URL` and replace the placeholder usernames in `.github/CODEOWNERS`.

## Pull request flow

1. `git checkout develop && git pull && git checkout -b feature/<topic>`
2. Commit small, working steps. Run `mvn -B verify` before pushing.
3. `git push -u origin feature/<topic>` and open a pull request **into `develop`** using the template
   (tests added, linked issue, reviewer).
4. CI job `build-test` must be green. Another member reviews and approves (never approve your own PR).
5. Squash or merge into `develop`, delete the branch.
6. When ready to release: pull request `develop -> main`, same checks, then the `deploy` job calls Render.

Note: the packages depend on each other (`item`, `claim`, `match`, `notification` and `admin` use each other), so a
branch with only one member's files cannot compile alone. Only the project-setup pull request is green by itself.
Therefore only `main` is protected; the first feature PRs into `develop` may show a red `build-test` check
("missing classes from another module"), say so in the PR comment. After all feature PRs are merged, `develop`
contains everything and the `develop -> main` pull request must be green.

## Commit message convention

`type(scope): short summary in the imperative` - for example `feat(claim): reject claim needs review note`.

Types: `feat`, `fix`, `test`, `docs`, `refactor`, `chore`, `ci`. Reference the issue in the body (`Closes #12`).

## How the three members split the work

| Member | Owns | Typical branches |
|---|---|---|
| **Allfi** | `item` (CRUD, search), `category`, `notification`, `user`, `DataInitializer`, CI workflow, project setup, GitHub setup | `feature/item-category-notification`, `feature/ci` |
| **Beg** | `auth` (JWT, login/logout), security config (`SecurityConfig`, 401/403 handlers), `admin` (users, statistics Proxy), `common` (error handling, metrics) | `feature/auth-security`, `feature/admin-errors` |
| **Shawki** | `ItemStatusService` (lifecycle), `match`, `claim`, Docker, `DatabaseUrl*`, Render, monitoring, README and docs | `feature/lifecycle-match-claim`, `feature/docker-render` |

Each member also owns the unit/integration tests and UI pages of their own modules. Shared files (`pom.xml`, `application.yml`, `README.md`) are edited in small, separate pull
requests so conflicts stay small. `CODEOWNERS` requests a review from the owner of the touched folder.

Review rotation: Beg reviews Allfi, Allfi reviews Shawki, Shawki reviews Beg. Nobody approves their own pull request.

## Resolving merge conflicts

1. Update your branch with the latest target: `git fetch origin && git merge origin/develop` (or `git rebase origin/develop`
   for your own, unshared branch).
2. `git status` lists the conflicted files. Open each one and look for `<<<<<<<`, `=======`, `>>>>>>>`.
3. Keep the correct combination of both sides (talk to the other author if unsure), delete the markers.
4. `git add <file>` for every resolved file, then `git merge --continue` (or `git rebase --continue`).
5. Run `mvn -B verify`; push; the pull request re-runs CI.

Never resolve a conflict by blindly taking one side in `pom.xml` or `application.yml`: both members usually added
something that must stay.
