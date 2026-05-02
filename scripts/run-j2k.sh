#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <java-project-dir> <converted-output-dir>" >&2
  exit 64
fi

SOURCE_DIR="$1"
OUTPUT_DIR="$2"
MODE_FILE="$OUTPUT_DIR/conversion-mode.txt"
ERROR_LOG="$OUTPUT_DIR/conversion-errors.log"

rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

if [[ ! -d "$SOURCE_DIR" ]]; then
  echo "Source directory does not exist: $SOURCE_DIR" >&2
  exit 66
fi

run_real_j2k() {
  local java_file="$1"
  local output_file="$2"
  mkdir -p "$(dirname "$output_file")"

  # The JetBrains J2K engine is distributed as IntelliJ/Kotlin-plugin internals,
  # so this adapter accepts a caller-provided wrapper jar instead of pretending
  # there is a stable public CLI. Supported wrappers should accept:
  #   <input.java> <output.kt>
  if [[ -n "${J2K_CLI_CMD:-}" ]]; then
    "$J2K_CLI_CMD" "$java_file" "$output_file"
  else
    java -jar "$J2K_CLI_JAR" "$java_file" "$output_file"
  fi
}

fallback_convert() {
  local java_file="$1"
  local output_file="$2"
  mkdir -p "$(dirname "$output_file")"

  perl -pe '
    s/\r$//;
    s/;\s*$//;
    s/\bpublic\s+class\b/class/g;
    s/\bpublic\s+interface\b/interface/g;
    s/\bpublic\s+enum\b/enum class/g;
    s/\bprivate\s+final\s+/private val /g;
    s/\bpublic\s+static\s+final\s+/const val /g;
    s/\bpublic\s+static\s+void\s+main\s*\(String\[\]\s+args\)/fun main(args: Array<String>)/g;
    s/\bSystem\.out\.println\(/println(/g;
    s/\bSystem\.err\.println\(/System.err.println(/g;
    s/\bnew\s+//g;
  ' "$java_file" > "$output_file"
}

if [[ -n "${J2K_CLI_CMD:-}" ]]; then
  if [[ ! -x "$J2K_CLI_CMD" ]]; then
    echo "J2K_CLI_CMD was set but does not point to an executable file: $J2K_CLI_CMD" >&2
    exit 65
  fi
  MODE="jetbrains-j2k-wrapper"
elif [[ -n "${J2K_CLI_JAR:-}" ]]; then
  if [[ ! -f "$J2K_CLI_JAR" ]]; then
    echo "J2K_CLI_JAR was set but does not point to a file: $J2K_CLI_JAR" >&2
    exit 65
  fi
  MODE="jetbrains-j2k-wrapper"
else
  MODE="static-fallback-translator"
fi

CONVERSION_FAILURES=0
: > "$ERROR_LOG"

while IFS= read -r -d '' java_file; do
  rel="${java_file#$SOURCE_DIR/}"
  output_file="$OUTPUT_DIR/${rel%.java}.kt"
  if [[ "$MODE" == "jetbrains-j2k-wrapper" ]]; then
    if ! run_real_j2k "$java_file" "$output_file" 2>>"$ERROR_LOG"; then
      CONVERSION_FAILURES=$((CONVERSION_FAILURES + 1))
      echo "FAILED: $rel" >> "$ERROR_LOG"
      if [[ "${J2K_CONTINUE_ON_ERROR:-0}" != "1" ]]; then
        echo "Conversion failed for $rel. See $ERROR_LOG for details." >&2
        exit 1
      fi
    fi
  else
    fallback_convert "$java_file" "$output_file"
  fi
done < <(find "$SOURCE_DIR" -type f -name '*.java' -print0)

{
  echo "mode=$MODE"
  echo "source=$SOURCE_DIR"
  echo "output=$OUTPUT_DIR"
  echo "converted_files=$(find "$OUTPUT_DIR" -type f -name '*.kt' | wc -l | tr -d ' ')"
  echo "conversion_failures=$CONVERSION_FAILURES"
  if [[ "$CONVERSION_FAILURES" -gt 0 ]]; then
    echo "error_log=conversion-errors.log"
  fi
  if [[ "$MODE" == "static-fallback-translator" ]]; then
    echo "note=Set J2K_CLI_JAR to a wrapper around JetBrains static J2K to measure the actual converter."
  fi
} > "$MODE_FILE"

echo "Conversion complete: $(cat "$MODE_FILE" | tr '\n' ' ')"
