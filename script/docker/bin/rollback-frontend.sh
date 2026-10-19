#!/bin/sh
set -eu

version=${1:?usage: rollback-frontend.sh VERSION}
base_dir=${ZBM_FRONTEND_DIR:-/opt/zbm/frontend}
release_dir=$base_dir/releases/$version
compose_dir=${ZBM_COMPOSE_DIR:-/opt/zbm}

test -s "$release_dir/index.html"
test -r "$release_dir/index.html"
ln -s "$release_dir" "$base_dir/current.next"
mv -Tf "$base_dir/current.next" "$base_dir/current"
cd "$compose_dir"
if ! docker compose ps --status running --quiet frontend | grep -q .; then
    docker compose up -d --no-deps frontend
fi
docker compose exec -T frontend nginx -t
docker compose ps frontend
