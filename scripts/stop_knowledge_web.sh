#!/usr/bin/env bash
set -euo pipefail

PORT="${PORT:-5174}"
PID_FILE="${PID_FILE:-/tmp/studyforge-knowledge-vite.pid}"

if [[ -f "$PID_FILE" ]]; then
  PID="$(cat "$PID_FILE")"

  if [[ -n "$PID" ]] && kill -0 "$PID" 2>/dev/null; then
    kill "$PID"
    rm -f "$PID_FILE"
    echo "StudyForge knowledge web stopped."
    exit 0
  fi
fi

PIDS="$(pgrep -f "vite .*--port ${PORT}" || true)"

if [[ -n "$PIDS" ]]; then
  kill $PIDS
  rm -f "$PID_FILE"
  echo "StudyForge knowledge web stopped."
else
  echo "StudyForge knowledge web is not running."
fi
