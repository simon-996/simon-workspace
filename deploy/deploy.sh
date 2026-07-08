#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="${APP_DIR:-$(cd "$SCRIPT_DIR/.." && pwd)}"
DEPLOY_DIR="$APP_DIR/deploy"
TARGET="${1:-all}"
COMPOSE_FILE="$DEPLOY_DIR/docker-compose.yml"
ENV_FILE="$DEPLOY_DIR/.env"

cd "$DEPLOY_DIR"

if [ ! -f "$ENV_FILE" ]; then
  echo "[ERROR] Missing $ENV_FILE"
  echo "[ERROR] Copy $DEPLOY_DIR/.env.example to $ENV_FILE and fill production values first."
  exit 1
fi

if [ ! -f "$COMPOSE_FILE" ]; then
  echo "[ERROR] Missing $COMPOSE_FILE"
  exit 1
fi

require_file() {
  if [ ! -f "$1" ]; then
    echo "[ERROR] Missing $1"
    exit 1
  fi
}

require_dir() {
  if [ ! -d "$1" ]; then
    echo "[ERROR] Missing $1"
    exit 1
  fi
}

deploy_service() {
  local service="$1"
  echo "[DEPLOY] Deploy $service"
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --build --no-deps "$service"
}

case "$TARGET" in
  api)
    require_file "$APP_DIR/api/Dockerfile"
    require_file "$APP_DIR/api/app.jar"
    deploy_service simon-workspace-api
    ;;
  web)
    require_file "$APP_DIR/web/Dockerfile"
    require_file "$APP_DIR/web/nginx.conf"
    require_dir "$APP_DIR/web/dist"
    deploy_service simon-workspace-web
    ;;
  all)
    require_file "$APP_DIR/api/Dockerfile"
    require_file "$APP_DIR/api/app.jar"
    require_file "$APP_DIR/web/Dockerfile"
    require_file "$APP_DIR/web/nginx.conf"
    require_dir "$APP_DIR/web/dist"
    echo "[DEPLOY] Deploy all services"
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --build
    ;;
  *)
    echo "[ERROR] Unknown target: $TARGET"
    echo "Usage: $0 [all|api|web]"
    exit 1
    ;;
esac

echo "[DEPLOY] Current containers"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps

echo "[DEPLOY] Prune unused images"
docker image prune -f

echo "[DEPLOY] Done"
