#!/usr/bin/env bash
# Creates the long-lived "develop" branch from main and pushes it. Run once, after the first push of main.
set -euo pipefail

if ! git rev-parse --verify main >/dev/null 2>&1; then
  echo "Branch 'main' does not exist locally. Commit your code on main first." >&2
  exit 1
fi

git checkout main
git pull --ff-only origin main || true

if git rev-parse --verify develop >/dev/null 2>&1; then
  echo "Branch 'develop' already exists locally."
else
  git checkout -b develop
fi

git push -u origin develop
echo "Done. 'develop' exists on origin. Feature branches start from develop: git checkout -b feature/<name> develop"
