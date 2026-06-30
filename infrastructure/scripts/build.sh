#!/usr/bin/env bash
# Build all images: JVM reactor (skip tests) then every docker-compose service.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
COMPOSE="$ROOT/infrastructure/docker/docker-compose.yml"

echo "==> Building JVM reactor"
( cd "$ROOT" && mvn -B -DskipTests clean package )

echo "==> Building container images"
docker compose -f "$COMPOSE" build
