#!/usr/bin/env bash
set -euo pipefail

export http_proxy="${http_proxy:-http://127.0.0.1:7897}"
export https_proxy="${https_proxy:-http://127.0.0.1:7897}"

cd "$(dirname "$0")/../studyforge-server"
mvn -DskipTests package
