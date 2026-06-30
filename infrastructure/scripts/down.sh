#!/usr/bin/env bash
# Stop the stack. Pass -v to also drop named volumes (databases, redis, pgadmin).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
COMPOSE="$ROOT/infrastructure/docker/docker-compose.yml"

docker compose -f "$COMPOSE" down "$@"
