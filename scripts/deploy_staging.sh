#!/usr/bin/env bash
set -euo pipefail

ARTIFACT="${1:?release artifact path is required}"
SHA="${2:-manual}"

APP_ROOT="${APP_ROOT:-/opt/studyforge-staging}"
FRONTEND_KNOWLEDGE_DIR="${FRONTEND_KNOWLEDGE_DIR:-/var/www/studyforge-staging/knowledge}"
FRONTEND_PORTAL_DIR="${FRONTEND_PORTAL_DIR:-/var/www/studyforge-staging/portal}"
BACKEND_WAR_DIR="${BACKEND_WAR_DIR:-/opt/tomcat-staging/webapps}"
BACKEND_CONTEXT="${BACKEND_CONTEXT:-ROOT}"
BACKEND_SERVICE="${BACKEND_SERVICE:-tomcat-staging}"
KEEP_RELEASES="${KEEP_RELEASES:-5}"
HEALTH_URL="${HEALTH_URL:-}"
SUDO="${SUDO:-sudo -n}"
DEPLOY_ENV_FILE="${DEPLOY_ENV_FILE:-/etc/studyforge/staging.env}"

if [ -r "$DEPLOY_ENV_FILE" ]; then
  set -a
  # shellcheck disable=SC1090
  . "$DEPLOY_ENV_FILE"
  set +a
fi

DB_MIGRATE="${DB_MIGRATE:-0}"
DB_CLIENT="${DB_CLIENT:-mysql}"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_PASSWORD="${DB_PASSWORD:-}"
CREATE_DATABASE="${CREATE_DATABASE:-0}"

TIMESTAMP="$(date +%Y%m%d%H%M%S)"
RELEASE_DIR="${APP_ROOT}/releases/${TIMESTAMP}-${SHA:0:8}"
BACKEND_WAR="${BACKEND_WAR_DIR}/${BACKEND_CONTEXT}.war"
BACKEND_EXPLODED="${BACKEND_WAR_DIR}/${BACKEND_CONTEXT}"

if [ ! -f "$ARTIFACT" ]; then
  echo "Artifact not found: $ARTIFACT" >&2
  exit 1
fi

$SUDO mkdir -p "$RELEASE_DIR" "$FRONTEND_KNOWLEDGE_DIR" "$FRONTEND_PORTAL_DIR" "$BACKEND_WAR_DIR"
$SUDO tar -xzf "$ARTIFACT" -C "$RELEASE_DIR"

if [ ! -f "$RELEASE_DIR/backend/studyforge-webapi.war" ]; then
  echo "Backend WAR missing from artifact" >&2
  exit 1
fi

if [ ! -d "$RELEASE_DIR/frontend/knowledge" ] || [ ! -d "$RELEASE_DIR/frontend/portal" ]; then
  echo "Frontend dist directories missing from artifact" >&2
  exit 1
fi

if [ "$DB_MIGRATE" = "1" ]; then
  if [ ! -x "$RELEASE_DIR/scripts/import_local_db.sh" ] || [ ! -d "$RELEASE_DIR/sql" ]; then
    echo "Database migration assets missing from artifact" >&2
    exit 1
  fi
  if [ -z "${DB_NAME:-}" ] || [ -z "${DB_USER:-}" ]; then
    echo "DB_NAME and DB_USER are required when DB_MIGRATE=1" >&2
    exit 1
  fi
  DB_CLIENT="$DB_CLIENT" \
    DB_NAME="$DB_NAME" \
    DB_USER="$DB_USER" \
    DB_PASSWORD="$DB_PASSWORD" \
    DB_HOST="$DB_HOST" \
    DB_PORT="$DB_PORT" \
    CREATE_DATABASE="$CREATE_DATABASE" \
    RESET_SEED=0 \
    "$RELEASE_DIR/scripts/import_local_db.sh"
fi

$SUDO rsync -a --delete "$RELEASE_DIR/frontend/knowledge/" "$FRONTEND_KNOWLEDGE_DIR/"
$SUDO rsync -a --delete "$RELEASE_DIR/frontend/portal/" "$FRONTEND_PORTAL_DIR/"

$SUDO systemctl stop "$BACKEND_SERVICE"
$SUDO rm -rf "$BACKEND_EXPLODED" "$BACKEND_WAR"
$SUDO cp "$RELEASE_DIR/backend/studyforge-webapi.war" "$BACKEND_WAR"
$SUDO systemctl start "$BACKEND_SERVICE"

$SUDO ln -sfn "$RELEASE_DIR" "$APP_ROOT/current"

if [ "$KEEP_RELEASES" -gt 0 ]; then
  find "$APP_ROOT/releases" -mindepth 1 -maxdepth 1 -type d | sort | head -n "-${KEEP_RELEASES}" | xargs -r $SUDO rm -rf
fi

if [ -n "$HEALTH_URL" ] && command -v curl >/dev/null 2>&1; then
  for _ in $(seq 1 30); do
    if curl -fsS "$HEALTH_URL" >/dev/null; then
      echo "Health check passed: $HEALTH_URL"
      break
    fi
    sleep 2
  done
fi

echo "Staging deployed: $SHA"
