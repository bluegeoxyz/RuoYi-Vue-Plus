#!/bin/sh
set -eu

version=${1:?usage: release-frontend.sh VERSION [INCOMING_DIR]}
base_dir=${ZBM_FRONTEND_DIR:-/opt/zbm/frontend}
incoming_dir=${2:-$base_dir/incoming/$version}
archive=$incoming_dir/frontend-dist.tar.gz
checksum=$incoming_dir/frontend-dist.tar.gz.sha256
release_dir=$base_dir/releases/$version
compose_dir=${ZBM_COMPOSE_DIR:-/opt/zbm}

test -s "$archive"
test -s "$checksum"
(cd "$incoming_dir" && sha256sum -c frontend-dist.tar.gz.sha256)
test ! -e "$release_dir"

mkdir -p "$base_dir/releases"
staging="$release_dir.tmp.$$"
mkdir "$staging"
tar -xzf "$archive" -C "$staging"
test -s "$staging/index.html"
cp "$checksum" "$staging/frontend-dist.tar.gz.sha256"
chmod -R a+rX "$staging"
mv "$staging" "$release_dir"

ln -s "$release_dir" "$base_dir/current.next"
mv -Tf "$base_dir/current.next" "$base_dir/current"

# The container mounts the stable parent directory, not the current target.
# Nginx resolves /usr/share/nginx/zbm/current on every request, so no recreate is needed.
cd "$compose_dir"
if ! docker compose ps --status running --quiet frontend | grep -q .; then
    docker compose up -d --no-deps frontend
fi
docker compose exec -T frontend nginx -t
docker compose ps frontend
