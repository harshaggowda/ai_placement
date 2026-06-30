#!/usr/bin/env bash
# Start the full local stack in the background.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
COMPOSE="$ROOT/infrastructure/docker/docker-compose.yml"

docker compose -f "$COMPOSE" up -d "$@"
docker compose -f "$COMPOSE" ps
