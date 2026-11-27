#!/usr/bin/env bash
# Static self-check of the FindIt project against the lab requirements. Prints PASS/FAIL per check.
# It reads files only: it does NOT run the tests (use `mvn -B verify` for that).
# Usage: ./scripts/verify-requirements.sh      (run from anywhere; it moves to the project root)
set -uo pipefail
cd "$(dirname "$0")/.."

FAILS=0
check() {
  local description="$1"; shift
  if "$@" >/dev/null 2>&1; then
    echo "PASS  $description"
  else
    echo "FAIL  $description"
    FAILS=$((FAILS + 1))
  fi
}

count_tests() { grep -rhE '^\s*@(Test|ParameterizedTest)\b' "$@" 2>/dev/null | wc -l; }
unit_tests=$(count_tests $(find src/test/java -name '*Test.java'))
it_tests=$(count_tests $(find src/test/java -name '*IT.java'))
controllers=$(grep -rl '@RestController' src/main/java | wc -l)
entities=$(grep -rlE '^@Entity' src/main/java | wc -l)

check "Unit test methods >= 20 (found $unit_tests)" test "$unit_tests" -ge 20
check "Integration test methods >= 10 (found $it_tests)" test "$it_tests" -ge 10
check "@RestController classes >= 5 (found $controllers)" test "$controllers" -ge 5
check "@Entity classes >= 6 (found $entities)" test "$entities" -ge 6

check "Dockerfile exists" test -f Dockerfile
check "docker-compose.yml exists" test -f docker-compose.yml
check ".env.example exists" test -f .env.example
check ".gitignore contains .env" grep -qxE '\.env' .gitignore
check "No .env file is shipped" test ! -f .env

# No literal secrets: a password/secret key followed by a value that is not a ${...} placeholder.
no_literal_yaml_secrets() {
  ! grep -rEi '^\s*[A-Za-z_.-]*(password|secret)[A-Za-z_.-]*:\s*["'"'"']?[A-Za-z0-9]' \
      src/main/resources/application*.yml docker-compose.yml monitoring 2>/dev/null
}
no_literal_java_secrets() {
  ! grep -rEi '(password|passwd|secret|apikey)[A-Za-z_]*\s*=\s*"[^"]+"' src/main/java 2>/dev/null
}
check "No literal passwords/secrets in application*.yml, compose or monitoring config" no_literal_yaml_secrets
check "No literal passwords/secrets in Java main sources" no_literal_java_secrets
check "Compose has no default for DB password and JWT secret" \
  bash -c "grep -q 'POSTGRES_PASSWORD:?' docker-compose.yml && grep -q 'JWT_SECRET:?' docker-compose.yml"

check "CI workflow exists and runs mvn" bash -c "grep -q 'mvn' .github/workflows/ci-cd.yml"
check "CI triggers on pull_request and push to main/develop" \
  bash -c "grep -q 'pull_request' .github/workflows/ci-cd.yml && grep -q 'push' .github/workflows/ci-cd.yml && grep -q 'develop' .github/workflows/ci-cd.yml"
check "Deploy job is conditioned on main and needs build-test" \
  bash -c "grep -q 'refs/heads/main' .github/workflows/ci-cd.yml && grep -q 'needs: build-test' .github/workflows/ci-cd.yml"
check "Deploy job uses RENDER_DEPLOY_HOOK_URL secret" grep -q 'RENDER_DEPLOY_HOOK_URL' .github/workflows/ci-cd.yml
check "render.yaml has autoDeploy: false" grep -q 'autoDeploy: false' render.yaml
check "PR template, CODEOWNERS, branch scripts exist" \
  bash -c "test -f .github/pull_request_template.md && test -f .github/CODEOWNERS && test -f scripts/setup-branches.sh && test -f scripts/setup-branch-protection.sh"

SEC=src/main/java/com/findit/config/SecurityConfig.java
check "SecurityConfig has @EnableMethodSecurity" grep -q 'EnableMethodSecurity' "$SEC"
check "SecurityConfig uses BCryptPasswordEncoder" grep -q 'BCryptPasswordEncoder' "$SEC"
check "@PreAuthorize used on controllers" bash -c "grep -rq '@PreAuthorize' src/main/java --include=*Controller.java"

check "Pattern: Factory (NotificationFactory)" test -f src/main/java/com/findit/notification/NotificationFactory.java
check "Pattern: Adapter (Email + InApp adapters, NotificationSender)" \
  bash -c "test -f src/main/java/com/findit/notification/EmailNotificationAdapter.java && test -f src/main/java/com/findit/notification/InAppNotificationAdapter.java && test -f src/main/java/com/findit/notification/NotificationSender.java"
check "Pattern: Proxy (CachedStatisticsServiceProxy + StatisticsService)" \
  bash -c "test -f src/main/java/com/findit/admin/CachedStatisticsServiceProxy.java && test -f src/main/java/com/findit/admin/StatisticsService.java"
check "Pattern: Singleton (MatchConfig)" test -f src/main/java/com/findit/match/MatchConfig.java
check "Pattern: Bridge (MatchScorer + ScoreComponent)" \
  bash -c "test -f src/main/java/com/findit/match/MatchScorer.java && test -f src/main/java/com/findit/match/ScoreComponent.java"
check "Every pattern class has a 'Pattern:' Javadoc" \
  bash -c "grep -l 'Pattern:' src/main/java/com/findit/*/*.java | wc -l | xargs test 8 -le"
check "No Lombok" bash -c "! grep -rq 'lombok' pom.xml src"

check "README contains mermaid" grep -q 'mermaid' README.md
check "README contains erDiagram" grep -q 'erDiagram' README.md
check "README contains an API endpoint table" grep -qE '^\| *Method *\|' README.md
check "README has live URL / repo URL placeholders" grep -q 'REPLACE ME' README.md
check "REQUIREMENTS_TRACEABILITY.md exists" test -f REQUIREMENTS_TRACEABILITY.md
check "GIT_WORKFLOW.md, DEMO_SCRIPT.md, DEPLOY_OTHER_PLATFORM.md exist" \
  bash -c "test -f GIT_WORKFLOW.md && test -f docs/DEMO_SCRIPT.md && test -f docs/DEPLOY_OTHER_PLATFORM.md"
check "Postman collection exists and is valid JSON" \
  bash -c "python3 -c \"import json;json.load(open('docs/postman/FindIt.postman_collection.json'))\""

check "pom has spring-boot-starter-actuator" grep -q 'spring-boot-starter-actuator' pom.xml
check "pom has micrometer-registry-prometheus" grep -q 'micrometer-registry-prometheus' pom.xml
check "Prometheus scrape config and Grafana dashboard exist" \
  bash -c "test -f monitoring/prometheus.yml && python3 -c \"import json;json.load(open('monitoring/grafana/dashboards/findit-overview.json'))\""
check "Custom Micrometer counters defined" \
  bash -c "grep -q 'findit.items.created' src/main/java/com/findit/common/FindItMetrics.java && grep -q 'findit.claims' src/main/java/com/findit/common/FindItMetrics.java && grep -q 'findit.matches.found' src/main/java/com/findit/common/FindItMetrics.java"
check "UI pages exist (15 html files)" bash -c "test \$(find src/main/resources/static -name '*.html' | wc -l) -ge 15"
check "fly.toml exists (bonus deployment)" test -f fly.toml

echo
if [[ $FAILS -eq 0 ]]; then
  echo "RESULT: all checks passed"
else
  echo "RESULT: $FAILS check(s) failed"
fi
exit $FAILS
