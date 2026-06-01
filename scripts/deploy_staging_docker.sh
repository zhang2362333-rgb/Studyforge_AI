#!/usr/bin/env bash
set -euo pipefail

APP_ROOT="${APP_ROOT:-/opt/studyforge-staging}"
COMPOSE_FILE="${COMPOSE_FILE:-${APP_ROOT}/docker-compose.yml}"
ENV_FILE="${ENV_FILE:-${APP_ROOT}/.env}"
EXAMPLE_ENV_FILE="${EXAMPLE_ENV_FILE:-${APP_ROOT}/.env.example}"

COMPOSE_SOURCE="${COMPOSE_SOURCE:-/tmp/docker-compose.staging.yml}"
ENV_EXAMPLE_SOURCE="${ENV_EXAMPLE_SOURCE:-/tmp/.env.staging.example}"

mkdir -p "$APP_ROOT"

if [ ! -f "$COMPOSE_SOURCE" ]; then
    echo "Compose file not found: ${COMPOSE_SOURCE}" >&2
    exit 1
fi

cp "$COMPOSE_SOURCE" "$COMPOSE_FILE"

if [ -f "$ENV_EXAMPLE_SOURCE" ]; then
    cp "$ENV_EXAMPLE_SOURCE" "$EXAMPLE_ENV_FILE"
fi

if [ ! -f "$ENV_FILE" ]; then
    if [ ! -f "$EXAMPLE_ENV_FILE" ]; then
        echo "Missing ${ENV_FILE} and ${EXAMPLE_ENV_FILE}." >&2
        exit 1
    fi
    cp "$EXAMPLE_ENV_FILE" "$ENV_FILE"
    chmod 600 "$ENV_FILE" || true
    echo "Created ${ENV_FILE} from ${EXAMPLE_ENV_FILE}."
fi

if ! command -v docker >/dev/null 2>&1; then
    echo "docker was not found on the server" >&2
    exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
    echo "docker compose plugin was not found on the server" >&2
    exit 1
fi

if [ -n "${GHCR_READ_TOKEN_B64:-}" ]; then
    GHCR_READ_TOKEN="$(printf '%s' "$GHCR_READ_TOKEN_B64" | base64 -d)"
fi

if [ -n "${GHCR_USERNAME:-}" ] && [ -n "${GHCR_READ_TOKEN:-}" ]; then
    printf '%s' "$GHCR_READ_TOKEN" | docker login ghcr.io -u "$GHCR_USERNAME" --password-stdin
fi

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" pull
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d mysql
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" run --rm migrate
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d api web
docker image prune -f --filter "until=168h" >/dev/null

echo "StudyForge staging Docker deployment completed."
