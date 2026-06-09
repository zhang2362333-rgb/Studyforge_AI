#!/usr/bin/env bash
set -euo pipefail

API_BASE="${API_BASE:-http://localhost:8080/api/v1}"
DB_NAME="${DB_NAME:-test_studyforge_ai_v2}"
DB_USER="${DB_USER:-lynn}"
DB_CLIENT="${DB_CLIENT:-}"
ACCOUNT="${ACCOUNT:-chen_jiayi}"
PASSWORD="${PASSWORD:-StudyForge@2026}"

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

TOKEN="$(curl -fsS -H 'Content-Type: application/json' \
    -d "{\"account\":\"${ACCOUNT}\",\"password\":\"${PASSWORD}\"}" \
    "${API_BASE}/auth/login" | jq -r '.data.accessToken')"

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
    echo "Login failed for ${ACCOUNT}" >&2
    exit 1
fi

TITLE="持久化验证临时帖 $(date +%s)"
CREATE_RESPONSE="$(curl -fsS -H "Authorization: Bearer ${TOKEN}" \
    -H 'Content-Type: application/json' \
    -d "{\"categoryId\":1,\"originalLanguage\":\"zh_CN\",\"coverImageUrl\":null,\"title\":\"${TITLE}\",\"summary\":\"验证用户发布内容会写入数据库。\",\"content\":\"## 持久化验证\\n\\n这是一篇临时帖子，用于确认 posts 和 post_i18n 会同时写入数据库。\"}" \
    "${API_BASE}/posts")"
POST_ID="$(echo "$CREATE_RESPONSE" | jq -r '.data')"

if ! [[ "$POST_ID" =~ ^[0-9]+$ ]]; then
    echo "Post creation failed: ${CREATE_RESPONSE}" >&2
    exit 1
fi

cleanup() {
    "$DB_CLIENT" -u"${DB_USER}" "${DB_NAME}" -e "DELETE FROM posts WHERE post_id=${POST_ID};" >/dev/null 2>&1 || true
}
trap cleanup EXIT

DB_COUNT="$("$DB_CLIENT" -u"${DB_USER}" "${DB_NAME}" -N -B -e "SELECT COUNT(*) FROM posts p JOIN post_i18n pi ON pi.post_id=p.post_id WHERE p.post_id=${POST_ID} AND pi.title='${TITLE}';")"
DETAIL_ID="$(curl -fsS "${API_BASE}/posts/${POST_ID}?languageCode=zh_CN" | jq -r '.data.postId')"
PROFILE_FOUND="$(curl -fsS -H "Authorization: Bearer ${TOKEN}" "${API_BASE}/users/1/posts?languageCode=zh_CN&limit=5" | jq --argjson postId "$POST_ID" 'any(.data[]; .postId == $postId)')"

if [ "$DB_COUNT" != "1" ] || [ "$DETAIL_ID" != "$POST_ID" ] || [ "$PROFILE_FOUND" != "true" ]; then
    echo "Persistence verification failed" >&2
    echo "db_count=${DB_COUNT} detail_id=${DETAIL_ID} profile_found=${PROFILE_FOUND}" >&2
    exit 1
fi

echo "User persistence verified with temporary post #${POST_ID}"
