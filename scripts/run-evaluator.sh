#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
KOTLIN_VERSION="${KOTLIN_VERSION:-2.0.21}"
TOOLS_DIR="$ROOT_DIR/.tools"
KOTLIN_HOME="$TOOLS_DIR/kotlinc-$KOTLIN_VERSION"
EVALUATOR_SRC="$ROOT_DIR/evaluator/src/main/kotlin/J2kEvaluator.kt"
EVALUATOR_JAR="$TOOLS_DIR/j2k-evaluator.jar"

if [[ $# -ne 3 ]]; then
  echo "Usage: $0 <java-source-dir> <converted-kotlin-dir> <results-dir>" >&2
  exit 64
fi

mkdir -p "$TOOLS_DIR"

if [[ ! -x "$KOTLIN_HOME/bin/kotlinc" ]]; then
  archive="$TOOLS_DIR/kotlin-compiler-$KOTLIN_VERSION.zip"
  url="https://github.com/JetBrains/kotlin/releases/download/v$KOTLIN_VERSION/kotlin-compiler-$KOTLIN_VERSION.zip"
  echo "Downloading Kotlin compiler $KOTLIN_VERSION"
  curl -fsSL "$url" -o "$archive"
  rm -rf "$KOTLIN_HOME" "$TOOLS_DIR/kotlinc"
  unzip -q "$archive" -d "$TOOLS_DIR"
  mv "$TOOLS_DIR/kotlinc" "$KOTLIN_HOME"
fi

"$KOTLIN_HOME/bin/kotlinc" "$EVALUATOR_SRC" -include-runtime -d "$EVALUATOR_JAR"
java -jar "$EVALUATOR_JAR" "$1" "$2" "$3"

if [[ -f "$2/conversion-errors.log" ]]; then
  cp "$2/conversion-errors.log" "$3/conversion-errors.log"
fi

if [[ "${J2K_COMPILE_SMOKE:-0}" == "1" ]]; then
  RESULTS_DIR="$3"
  CONVERTED_DIR="$2"
  SMOKE_MD="$RESULTS_DIR/compile-smoke.md"
  SMOKE_JSON="$RESULTS_DIR/compile-smoke.json"
  SMOKE_LOG="$RESULTS_DIR/compile-smoke.log"
  SMOKE_JAR="$TOOLS_DIR/compile-smoke.jar"
  KOTLIN_FILE_COUNT="$(find "$CONVERTED_DIR" -type f -name '*.kt' | wc -l | tr -d ' ')"

  if [[ "$KOTLIN_FILE_COUNT" == "0" ]]; then
    STATUS="unavailable"
    MESSAGE="No Kotlin files were produced, so the compile smoke test was not run."
    : > "$SMOKE_LOG"
  else
    set +e
    "$KOTLIN_HOME/bin/kotlinc" "$CONVERTED_DIR" -d "$SMOKE_JAR" >"$SMOKE_LOG" 2>&1
    EXIT_CODE="$?"
    set -e
    if [[ "$EXIT_CODE" == "0" ]]; then
      STATUS="passed"
      MESSAGE="Generated Kotlin compiled successfully in a standalone smoke test."
    else
      STATUS="failed"
      MESSAGE="Generated Kotlin did not compile in a standalone smoke test. This is recorded as a diagnostic signal only; it does not fail the pipeline."
    fi
  fi

  ERROR_COUNT="$(grep -Ec '(^|: )error:|^exception:' "$SMOKE_LOG" || true)"
  WARNING_COUNT="$(grep -Ec '(^|: )warning:' "$SMOKE_LOG" || true)"

  {
    echo "# Kotlin Compile Smoke Test"
    echo
    echo "- Status: \`$STATUS\`"
    echo "- Kotlin files considered: $KOTLIN_FILE_COUNT"
    echo "- Errors: $ERROR_COUNT"
    echo "- Warnings: $WARNING_COUNT"
    echo "- Message: $MESSAGE"
    echo
    echo "## First Diagnostics"
    echo
    echo '```text'
    sed -n '1,80p' "$SMOKE_LOG"
    echo '```'
  } > "$SMOKE_MD"

  {
    echo "{"
    echo "  \"status\": \"$STATUS\","
    echo "  \"kotlinFiles\": $KOTLIN_FILE_COUNT,"
    echo "  \"errors\": $ERROR_COUNT,"
    echo "  \"warnings\": $WARNING_COUNT,"
    echo "  \"message\": \"${MESSAGE//\"/\\\"}\""
    echo "}"
  } > "$SMOKE_JSON"

  {
    echo
    echo "## Kotlin Compile Smoke Test"
    echo
    echo "- Status: \`$STATUS\`"
    echo "- Kotlin files considered: $KOTLIN_FILE_COUNT"
    echo "- Errors: $ERROR_COUNT"
    echo "- Warnings: $WARNING_COUNT"
    echo "- Diagnostics: \`compile-smoke.md\`"
  } >> "$RESULTS_DIR/summary.md"
fi
