#!/usr/bin/env bash

set -e

echo "🔍 Collecting GitHub Actions storage usage for user: $GH_USER"
echo

if [ -z "$GH_USER" ]; then
  echo "❌ Please export your GitHub username first:"
  echo "   export GH_USER=vcharrie"
  exit 1
fi

TOTAL_ARTIFACTS=0
TOTAL_CACHES=0

echo "📁 Fetching repositories for $GH_USER..."
REPOS=$(gh repo list "$GH_USER" --limit 200 --json name --jq '.[].name')

for REPO in $REPOS; do
  echo "──────────────────────────────────────────────"
  echo "📦 Repository: $REPO"

  # ARTIFACTS
  ARTIFACT_BYTES=$(gh api \
    repos/$GH_USER/$REPO/actions/artifacts \
    --jq '.artifacts | map(.size_in_bytes) | add // 0')

  ARTIFACT_MB=$((ARTIFACT_BYTES / 1024 / 1024))
  TOTAL_ARTIFACTS=$((TOTAL_ARTIFACTS + ARTIFACT_BYTES))

  echo "  • Artifacts: ${ARTIFACT_MB} MB"

  # CACHES
  CACHE_BYTES=$(gh api \
    repos/$GH_USER/$REPO/actions/caches \
    --jq '.actions_caches | map(.size_in_bytes) | add // 0')

  CACHE_MB=$((CACHE_BYTES / 1024 / 1024))
  TOTAL_CACHES=$((TOTAL_CACHES + CACHE_BYTES))

  echo "  • Caches: ${CACHE_MB} MB"
done

echo "──────────────────────────────────────────────"
echo "📊 GLOBAL SUMMARY"
echo

TOTAL_MB=$(((TOTAL_ARTIFACTS + TOTAL_CACHES) / 1024 / 1024))
ART_MB=$((TOTAL_ARTIFACTS / 1024 / 1024))
CACHE_MB=$((TOTAL_CACHES / 1024 / 1024))

echo "  • Total artifacts: ${ART_MB} MB"
echo "  • Total caches:    ${CACHE_MB} MB"
echo "  • TOTAL USAGE:     ${TOTAL_MB} MB"
echo
echo "Done."
