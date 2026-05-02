#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <idea-home>" >&2
  exit 64
fi

IDEA_HOME="$1"
find \
  "$IDEA_HOME/lib" \
  "$IDEA_HOME/plugins/java/lib" \
  "$IDEA_HOME/plugins/Kotlin/lib" \
  "$IDEA_HOME/plugins/Kotlin/kotlinc/lib" \
  -maxdepth 1 -name '*.jar' | tr '\n' ':'
