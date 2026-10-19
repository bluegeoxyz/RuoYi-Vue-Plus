#!/usr/bin/env bash
set -euo pipefail

readonly repo_root="/mnt/e/space/project/zbm-platform"
readonly output_dir="${1:?usage: run_auth016_wh_db1_postcheck.sh <output-dir>}"

docker exec -i jdpm-mysql sh -c \
  'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 --table zbm-data' \
  < "${repo_root}/script/sql/zbm/diagnostic/auth016_wh_db1_postcheck.sql" \
  | tee "${output_dir}/postcheck.log"

docker exec -i jdpm-mysql sh -c \
  'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 -N -B zbm-data' \
  <<'SQL' > "${output_dir}/post-non-target-menu.tsv"
SELECT menu_id,parent_id,menu_name,order_num,menu_type,path,IFNULL(component,''),IFNULL(perms,''),status,visible
FROM sys_menu
WHERE menu_id <> 2609252000000000611
ORDER BY menu_id;
SQL

test "$(sha256sum "${output_dir}/pre-non-target-menu.tsv" | cut -d' ' -f1)" = \
     "$(sha256sum "${output_dir}/post-non-target-menu.tsv" | cut -d' ' -f1)"
sha256sum "${output_dir}/post-non-target-menu.tsv" > "${output_dir}/POST_SHA256SUMS"
