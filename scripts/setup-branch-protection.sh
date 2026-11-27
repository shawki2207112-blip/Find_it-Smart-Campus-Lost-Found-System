#!/usr/bin/env bash
# Protects the "main" branch (and optionally "develop") using the GitHub CLI.
# Usage:  ./scripts/setup-branch-protection.sh <owner>/<repo> [--with-develop]
# Requires: gh (authenticated as a repository admin) - run `gh auth login` first.
set -euo pipefail

REPO="${1:-}"
if [[ -z "$REPO" || "$REPO" != */* ]]; then
  echo "Usage: $0 <owner>/<repo> [--with-develop]" >&2
  exit 1
fi
WITH_DEVELOP="${2:-}"

protect() {
  local branch="$1"
  echo "Protecting ${REPO}:${branch} ..."
  gh api --method PUT "repos/${REPO}/branches/${branch}/protection" --input - <<'JSON'
{
  "required_status_checks": { "strict": true, "contexts": ["build-test"] },
  "enforce_admins": true,
  "required_pull_request_reviews": {
    "required_approving_review_count": 1,
    "dismiss_stale_reviews": true,
    "require_code_owner_reviews": false
  },
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false
}
JSON
}

protect main
if [[ "$WITH_DEVELOP" == "--with-develop" ]]; then
  protect develop
fi
echo "Done. Verify under Settings > Branches in the GitHub repository."
