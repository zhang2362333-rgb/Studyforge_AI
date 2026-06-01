#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SHA="${1:-$(git -C "$ROOT_DIR" rev-parse --short HEAD 2>/dev/null || date +%Y%m%d%H%M%S)}"
OUT_DIR="${OUT_DIR:-$ROOT_DIR/releases}"
WORK_DIR="$(mktemp -d)"
ARTIFACT="${ARTIFACT:-$OUT_DIR/studyforge-release-${SHA}.tar.gz}"

cleanup() {
    rm -rf "$WORK_DIR"
}
trap cleanup EXIT

mkdir -p "$OUT_DIR"

cd "$ROOT_DIR/studyforge-server"
mvn -DskipTests package

cd "$ROOT_DIR/studyforge-frontend"
if [ ! -d node_modules ]; then
    npm ci
fi
npm run build

mkdir -p "$WORK_DIR/backend" "$WORK_DIR/frontend" "$WORK_DIR/config"
cp "$ROOT_DIR/studyforge-server/studyforge-webapi/target/studyforge-webapi-1.0.0-SNAPSHOT.war" \
    "$WORK_DIR/backend/studyforge-webapi.war"
cp -R "$ROOT_DIR/studyforge-frontend/apps/knowledge-web/dist" "$WORK_DIR/frontend/knowledge"
cp -R "$ROOT_DIR/studyforge-frontend/apps/portal-web/dist" "$WORK_DIR/frontend/portal"
cp "$ROOT_DIR/studyforge-server/studyforge-webapi/src/main/resources/jdbc.properties.example" \
    "$WORK_DIR/config/jdbc.properties.example"

tar -czf "$ARTIFACT" -C "$WORK_DIR" .
echo "$ARTIFACT"
