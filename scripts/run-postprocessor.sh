#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
KOTLIN_VERSION="${KOTLIN_VERSION:-2.0.21}"
TOOLS_DIR="$ROOT_DIR/.tools"
KOTLIN_HOME="$TOOLS_DIR/kotlinc-$KOTLIN_VERSION"
POSTPROCESSOR_SRC="$ROOT_DIR/postprocessor/src/main/kotlin/SamPostProcessor.kt"
POSTPROCESSOR_JAR="$TOOLS_DIR/sam-postprocessor.jar"

if [[ $# -lt 2 || $# -gt 3 ]]; then
  echo "Usage: $0 <input-kt-dir> <output-kt-dir> [results-dir]" >&2
  echo "  input-kt-dir   Directory of .kt files to post-process (converter output)" >&2
  echo "  output-kt-dir  Directory to write post-processed .kt files" >&2
  echo "  results-dir    Optional: directory to write sam-fixes.md report" >&2
  exit 64
fi

mkdir -p "$TOOLS_DIR"

if [[ ! -x "$KOTLIN_HOME/bin/kotlinc" ]]; then
  archive="$TOOLS_DIR/kotlin-compiler-$KOTLIN_VERSION.zip"
  url="https://github.com/JetBrains/kotlin/releases/download/v$KOTLIN_VERSION/kotlin-compiler-$KOTLIN_VERSION.zip"
  echo "Downloading Kotlin compiler $KOTLIN_VERSION..."
  curl -fsSL "$url" -o "$archive"
  rm -rf "$KOTLIN_HOME" "$TOOLS_DIR/kotlinc"
  unzip -q "$archive" -d "$TOOLS_DIR"
  mv "$TOOLS_DIR/kotlinc" "$KOTLIN_HOME"
fi

# Recompile only when the source is newer than the cached jar
if [[ ! -f "$POSTPROCESSOR_JAR" || "$POSTPROCESSOR_SRC" -nt "$POSTPROCESSOR_JAR" ]]; then
  echo "Compiling SAM post-processor..."
  "$KOTLIN_HOME/bin/kotlinc" "$POSTPROCESSOR_SRC" -include-runtime -d "$POSTPROCESSOR_JAR"
fi

java -jar "$POSTPROCESSOR_JAR" "$@"
