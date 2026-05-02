#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <input.java> <output.kt>" >&2
  exit 64
fi

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IDEA_HOME="${J2K_IDEA_HOME:-}"
JAVA_BIN="${JDK21_HOME:-${JAVA_HOME:-}}/bin/java"

if [[ -z "$IDEA_HOME" || ! -d "$IDEA_HOME" ]]; then
  echo "Set J2K_IDEA_HOME to an IntelliJ IDEA installation directory." >&2
  exit 65
fi

if [[ ! -x "$JAVA_BIN" ]]; then
  JAVA_BIN="$(command -v java)"
fi

JAR_PATH="$ROOT_DIR/.tools/static-j2k-wrapper.jar"
if [[ ! -f "$JAR_PATH" || "$ROOT_DIR/j2k-wrapper/src/main/java/StaticJ2kCli.java" -nt "$JAR_PATH" ]]; then
  "$ROOT_DIR/scripts/build-intellij-j2k-wrapper.sh" >/dev/null
fi

CP="$JAR_PATH:$("$ROOT_DIR/scripts/intellij-j2k-classpath.sh" "$IDEA_HOME")"
J2K_IDEA_HOME="$IDEA_HOME" "$JAVA_BIN" -cp "$CP" StaticJ2kCli "$1" "$2"
