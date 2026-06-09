#!/usr/bin/env bash
set -euo pipefail

PID_FILE="${PID_FILE:-/tmp/studyforge-api-maven.pid}"

if [ ! -f "$PID_FILE" ]; then
    echo "StudyForge API PID file not found"
    exit 0
fi

PID="$(cat "$PID_FILE")"

if kill -0 "$PID" 2>/dev/null; then
    kill "$PID"
    rm -f "$PID_FILE"
    echo "StudyForge API stopped"
else
    rm -f "$PID_FILE"
    echo "StudyForge API process not running"
fi
