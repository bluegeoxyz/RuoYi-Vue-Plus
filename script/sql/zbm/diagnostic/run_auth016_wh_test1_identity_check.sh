#!/usr/bin/env bash
set -euo pipefail

readonly repo_root="/mnt/e/space/project/zbm-platform"

docker exec -i jdpm-mysql sh -c \
  'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 --table zbm-data' \
  < "${repo_root}/script/sql/zbm/diagnostic/auth016_wh_test1_identity_check.sql"
