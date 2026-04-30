#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <java-project-dir> <converted-output-dir>" >&2
  exit 64
fi

SOURCE_DIR="$1"
OUTPUT_DIR="$2"
MODE_FILE="$OUTPUT_DIR/conversion-mode.txt"

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
  java -jar "$J2K_CLI_JAR" "$java_file" "$output_file"
}

fallback_convert() {
  local java_file="$1"
  local output_file="$2"
  mkdir -p "$(dirname "$output_file")"

  awk '
    BEGIN { in_block_comment = 0 }
    {
      line = $0
      gsub(/\r$/, "", line)
      gsub(/;[[:space:]]*$/, "", line)
      gsub(/\bpublic[[:space:]]+class\b/, "class", line)
      gsub(/\bpublic[[:space:]]+interface\b/, "interface", line)
      gsub(/\bpublic[[:space:]]+enum\b/, "enum class", line)
      gsub(/\bprivate[[:space:]]+final[[:space:]]+/, "private val ", line)
      gsub(/\bpublic[[:space:]]+static[[:space:]]+final[[:space:]]+/, "const val ", line)
      gsub(/\bpublic[[:space:]]+static[[:space:]]+void[[:space:]]+main[[:space:]]*\(String\[\][[:space:]]+args\)/, "fun main(args: Array<String>)", line)
      gsub(/\bSystem\.out\.println\(/, "println(", line)
      gsub(/\bSystem\.err\.println\(/, "System.err.println(", line)
      gsub(/\bnew[[:space:]]+/, "", line)
      print line
    }
  ' "$java_file" > "$output_file"
}

if [[ -n "${J2K_CLI_JAR:-}" ]]; then
  if [[ ! -f "$J2K_CLI_JAR" ]]; then
    echo "J2K_CLI_JAR was set but does not point to a file: $J2K_CLI_JAR" >&2
    exit 65
  fi
  MODE="jetbrains-j2k-wrapper"
else
  MODE="static-fallback-translator"
fi

while IFS= read -r -d '' java_file; do
  rel="${java_file#$SOURCE_DIR/}"
  output_file="$OUTPUT_DIR/${rel%.java}.kt"
  if [[ "$MODE" == "jetbrains-j2k-wrapper" ]]; then
    run_real_j2k "$java_file" "$output_file"
  else
    fallback_convert "$java_file" "$output_file"
  fi
done < <(find "$SOURCE_DIR" -type f -name '*.java' -print0)

{
  echo "mode=$MODE"
  echo "source=$SOURCE_DIR"
  echo "output=$OUTPUT_DIR"
  echo "converted_files=$(find "$OUTPUT_DIR" -type f -name '*.kt' | wc -l | tr -d ' ')"
  if [[ "$MODE" == "static-fallback-translator" ]]; then
    echo "note=Set J2K_CLI_JAR to a wrapper around JetBrains static J2K to measure the actual converter."
  fi
} > "$MODE_FILE"

echo "Conversion complete: $(cat "$MODE_FILE" | tr '\n' ' ')"
