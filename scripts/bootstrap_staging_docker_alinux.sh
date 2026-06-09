#!/usr/bin/env bash
set -euo pipefail

DEPLOY_USER="${DEPLOY_USER:-deploy}"
APP_ROOT="${APP_ROOT:-/opt/studyforge-staging}"
SSH_PUBLIC_KEY="${SSH_PUBLIC_KEY:-}"

if [ ! -f /etc/os-release ]; then
    echo "Cannot detect server OS: /etc/os-release is missing." >&2
    exit 1
fi

# shellcheck disable=SC1091
. /etc/os-release

case "${ID:-}" in
    alinux|alinux3|centos|rhel|rocky|almalinux)
        ;;
    *)
        echo "This bootstrap script is intended for Alibaba Cloud Linux / RHEL-like servers. Detected ID=${ID:-unknown}." >&2
        exit 1
        ;;
esac

SUDO=""
if [ "$(id -u)" -ne 0 ]; then
    SUDO="sudo"
fi

PM="$(command -v dnf || command -v yum || true)"
if [ -z "$PM" ]; then
    echo "Neither dnf nor yum was found." >&2
    exit 1
fi

$SUDO "$PM" makecache -y
$SUDO "$PM" install -y yum-utils device-mapper-persistent-data lvm2 curl vim openssh-server ca-certificates

if [ ! -f /etc/yum.repos.d/docker-ce.repo ]; then
    $SUDO curl -fsSL https://download.docker.com/linux/centos/docker-ce.repo -o /etc/yum.repos.d/docker-ce.repo
fi

if ! $SUDO "$PM" install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin; then
    $SUDO "$PM" install -y --nobest --allowerasing docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
fi

$SUDO systemctl enable --now docker
$SUDO systemctl enable --now sshd

if ! id "$DEPLOY_USER" >/dev/null 2>&1; then
    $SUDO useradd -m -s /bin/bash "$DEPLOY_USER"
fi

$SUDO usermod -aG docker "$DEPLOY_USER"

$SUDO mkdir -p "$APP_ROOT"
$SUDO chown -R "${DEPLOY_USER}:${DEPLOY_USER}" "$APP_ROOT"

if [ -n "$SSH_PUBLIC_KEY" ]; then
    $SUDO install -d -m 700 -o "$DEPLOY_USER" -g "$DEPLOY_USER" "/home/${DEPLOY_USER}/.ssh"
    printf '%s\n' "$SSH_PUBLIC_KEY" | $SUDO tee "/home/${DEPLOY_USER}/.ssh/authorized_keys" >/dev/null
    $SUDO chown "${DEPLOY_USER}:${DEPLOY_USER}" "/home/${DEPLOY_USER}/.ssh/authorized_keys"
    $SUDO chmod 600 "/home/${DEPLOY_USER}/.ssh/authorized_keys"
fi

docker --version
docker compose version

cat <<EOF
Alibaba Cloud Linux Docker bootstrap finished.

Next:
1. Reconnect SSH as ${DEPLOY_USER}, so docker group membership takes effect.
2. Confirm: ssh ${DEPLOY_USER}@SERVER_IP "docker ps"
3. Open TCP 7897 in the cloud security group.
EOF
