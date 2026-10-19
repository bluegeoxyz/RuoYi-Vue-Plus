#!/bin/sh
set -eu

version=${1:?usage: rollback-backend.sh VERSION}
base_dir=${ZBM_BACKEND_DIR:-/opt/zbm/backend}
release_dir=$base_dir/releases/$version
compose_dir=${ZBM_COMPOSE_DIR:-/opt/zbm}

test -s "$release_dir/ruoyi-admin.jar"
test -r "$release_dir/ruoyi-admin.jar"
ln -s "$release_dir" "$base_dir/current.next"
mv -Tf "$base_dir/current.next" "$base_dir/current"
cd "$compose_dir"
docker compose up -d --no-deps --force-recreate backend
docker compose ps backend
