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
