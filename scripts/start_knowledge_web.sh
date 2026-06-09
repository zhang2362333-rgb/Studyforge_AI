#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FRONTEND_ROOT="$ROOT_DIR/studyforge-frontend"
APP_DIR="$FRONTEND_ROOT/apps/knowledge-web"
VITE_BIN="$FRONTEND_ROOT/node_modules/.bin/vite"
PORT="${PORT:-5174}"
LOG_FILE="${LOG_FILE:-/tmp/studyforge-knowledge-vite.log}"
PID_FILE="${PID_FILE:-/tmp/studyforge-knowledge-vite.pid}"

if ss -ltn | grep -q ":${PORT} "; then
  echo "StudyForge knowledge web already appears to be listening on port ${PORT}."
  exit 0
fi

if [[ ! -x "$VITE_BIN" ]]; then
  echo "Frontend dependencies are missing. Running npm install..."
  (cd "$FRONTEND_ROOT" && npm install)
fi

cd "$APP_DIR"
setsid "$VITE_BIN" --host 0.0.0.0 --port "$PORT" > "$LOG_FILE" 2>&1 < /dev/null &
echo "$!" > "$PID_FILE"

echo "StudyForge knowledge web started."
echo "PID: $(cat "$PID_FILE")"
echo "URL: http://localhost:${PORT}"
echo "Log: $LOG_FILE"
