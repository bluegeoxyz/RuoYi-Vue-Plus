#!/usr/bin/env bash
set -euo pipefail

readonly repo_root="/mnt/e/space/project/zbm-platform"
readonly backup_dir="${1:?usage: run_auth010b_verify.sh <backup-dir>}"

docker exec -i jdpm-mysql sh -c \
  'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 zbm-data' \
  < "${repo_root}/script/sql/zbm/diagnostic/auth010b_verify.sql" \
  | tee "${backup_dir}/diagnostic-after-first-run.log"
