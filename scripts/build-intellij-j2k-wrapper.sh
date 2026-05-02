#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IDEA_HOME="${J2K_IDEA_HOME:-}"
JAVA_BIN="${JDK21_HOME:-${JAVA_HOME:-}}/bin/java"
JAVAC_BIN="${JDK21_HOME:-${JAVA_HOME:-}}/bin/javac"

if [[ -z "$IDEA_HOME" || ! -d "$IDEA_HOME" ]]; then
  echo "Set J2K_IDEA_HOME to an IntelliJ IDEA installation directory." >&2
  exit 65
fi

if [[ ! -x "$JAVA_BIN" || ! -x "$JAVAC_BIN" ]]; then
  JAVA_BIN="$(command -v java)"
  JAVAC_BIN="$(command -v javac)"
fi

if [[ -z "$JAVA_BIN" || -z "$JAVAC_BIN" ]]; then
  echo "java and javac are required. Use JDK 21 when running against IDEA 2025.x." >&2
  exit 65
fi

CP="$("$ROOT_DIR/scripts/intellij-j2k-classpath.sh" "$IDEA_HOME")"
CLASSES_DIR="$ROOT_DIR/.tools/j2k-wrapper-classes"
JAR_PATH="$ROOT_DIR/.tools/static-j2k-wrapper.jar"

rm -rf "$CLASSES_DIR"
mkdir -p "$CLASSES_DIR" "$(dirname "$JAR_PATH")"

"$JAVAC_BIN" -proc:none -cp "$CP" -d "$CLASSES_DIR" "$ROOT_DIR/j2k-wrapper/src/main/java/StaticJ2kCli.java"
"${JAVA_BIN%/java}/jar" --create --file "$JAR_PATH" --main-class StaticJ2kCli -C "$CLASSES_DIR" .

echo "$JAR_PATH"
