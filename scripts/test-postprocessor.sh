#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FIXTURE_DIR="$ROOT_DIR/postprocessor/testdata/sam-basic"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

mkdir -p "$TMP_DIR/input" "$TMP_DIR/output" "$TMP_DIR/results"
cp "$FIXTURE_DIR/input/SamBasic.kt" "$TMP_DIR/input/SamBasic.kt"

bash "$ROOT_DIR/scripts/run-postprocessor.sh" \
  "$TMP_DIR/input" \
  "$TMP_DIR/output" \
  "$TMP_DIR/results" > "$TMP_DIR/run.log"

diff -u "$FIXTURE_DIR/expected/SamBasic.kt" "$TMP_DIR/output/SamBasic.kt"
grep -q "SAM conversions | 1" "$TMP_DIR/results/sam-fixes.md"

echo "SAM post-processor golden test passed."
