#!/usr/bin/env bash
set -euo pipefail

DB_NAME="${DB_NAME:-test_studyforge_ai_v2}"
DB_USER="${DB_USER:-lynn}"
DB_PASSWORD="${DB_PASSWORD:-}"
DB_HOST="${DB_HOST:-}"
DB_PORT="${DB_PORT:-}"
RESET_SEED="${RESET_SEED:-0}"
DB_CLIENT="${DB_CLIENT:-}"
CREATE_DATABASE="${CREATE_DATABASE:-1}"

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

if [[ ! "$DB_NAME" =~ ^[A-Za-z0-9_]+$ ]]; then
    echo "DB_NAME must contain only letters, numbers, and underscores: ${DB_NAME}" >&2
    exit 1
fi

if [ -z "$DB_CLIENT" ]; then
    if command -v mariadb >/dev/null 2>&1; then
        DB_CLIENT="mariadb"
    elif command -v mysql >/dev/null 2>&1; then
        DB_CLIENT="mysql"
    else
        echo "Neither mariadb nor mysql client was found in PATH" >&2
        exit 1
    fi
fi

DB_ARGS=(-u"$DB_USER")
if [ -n "$DB_PASSWORD" ]; then
    DB_ARGS+=("-p${DB_PASSWORD}")
fi
if [ -n "$DB_HOST" ]; then
    DB_ARGS+=("-h${DB_HOST}")
fi
if [ -n "$DB_PORT" ]; then
    DB_ARGS+=("-P${DB_PORT}")
fi

if [ "$CREATE_DATABASE" = "1" ]; then
    "$DB_CLIENT" "${DB_ARGS[@]}" -e "CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
fi
"$DB_CLIENT" "${DB_ARGS[@]}" "$DB_NAME" < "$ROOT_DIR/sql/001_schema.sql"

if [ "$RESET_SEED" = "1" ]; then
    echo "RESET_SEED=1: importing seed data and resetting business tables in ${DB_NAME}" >&2
    "$DB_CLIENT" "${DB_ARGS[@]}" "$DB_NAME" < "$ROOT_DIR/sql/002_seed_data.sql"
else
    echo "Schema imported into ${DB_NAME}. Existing user content was preserved." >&2
    echo "To reset local data intentionally, run: RESET_SEED=1 $0" >&2
fi

for migration in "$ROOT_DIR"/sql/[0-9][0-9][0-9]_*.sql; do
    case "$(basename "$migration")" in
        001_schema.sql|002_seed_data.sql)
            continue
            ;;
    esac
    echo "Applying migration $(basename "$migration") to ${DB_NAME}" >&2
    "$DB_CLIENT" "${DB_ARGS[@]}" "$DB_NAME" < "$migration"
done

"$DB_CLIENT" "${DB_ARGS[@]}" -N -B -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${DB_NAME}';"
