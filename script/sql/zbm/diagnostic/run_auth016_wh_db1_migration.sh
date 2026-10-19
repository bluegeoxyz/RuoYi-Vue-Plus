#!/usr/bin/env bash
set -euo pipefail

readonly repo_root="/mnt/e/space/project/zbm-platform"
readonly output_dir="${1:?usage: run_auth016_wh_db1_migration.sh <output-dir>}"

docker exec -i jdpm-mysql sh -c \
  'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 --table zbm-data' \
  < "${repo_root}/script/sql/zbm/migration/V2.0.36__fix_warehouse_inbound_menu_type.sql" \
  | tee -a "${output_dir}/V2.0.36-execution.log"
