#!/bin/sh
set -eu

version=${1:?usage: release-backend.sh VERSION [INCOMING_DIR]}
base_dir=${ZBM_BACKEND_DIR:-/opt/zbm/backend}
incoming_dir=${2:-$base_dir/incoming/$version}
artifact=$incoming_dir/ruoyi-admin.jar
checksum=$incoming_dir/ruoyi-admin.jar.sha256
release_dir=$base_dir/releases/$version
compose_dir=${ZBM_COMPOSE_DIR:-/opt/zbm}

test -s "$artifact"
test -s "$checksum"
(cd "$incoming_dir" && sha256sum -c ruoyi-admin.jar.sha256)
if command -v unzip >/dev/null 2>&1; then
    unzip -tqq "$artifact"
else
    test "$(od -An -tx1 -N4 "$artifact" | tr -d ' \n')" = "504b0304"
fi
test ! -e "$release_dir"

mkdir -p "$base_dir/releases"
staging="$release_dir.tmp.$$"
mkdir "$staging"
cp "$artifact" "$staging/ruoyi-admin.jar"
cp "$checksum" "$staging/ruoyi-admin.jar.sha256"
chmod 0555 "$staging"
chmod 0444 "$staging/ruoyi-admin.jar" "$staging/ruoyi-admin.jar.sha256"
mv "$staging" "$release_dir"

ln -s "$release_dir" "$base_dir/current.next"
mv -Tf "$base_dir/current.next" "$base_dir/current"

cd "$compose_dir"
docker compose up -d --no-deps --force-recreate backend
docker compose ps backend
