#!/usr/bin/env bash
set -euo pipefail

readonly backup_dir="${1:?usage: backup_auth016_wh_db1.sh <backup-dir>}"
mkdir -p "${backup_dir}"

docker exec jdpm-mysql sh -c \
  'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --skip-lock-tables zbm-data sys_menu sys_role_menu' \
  > "${backup_dir}/zbm-data-auth016-wh-db1-menu.sql"

docker exec -i jdpm-mysql sh -c \
  'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 -N -B zbm-data' \
  <<'SQL' > "${backup_dir}/pre-target-and-tree.tsv"
SELECT menu_id,parent_id,menu_name,order_num,menu_type,path,IFNULL(component,''),IFNULL(perms,''),status,visible
FROM sys_menu
WHERE perms = 'warehouse:inbound:list'
   OR parent_id = 2609252000000000611
   OR menu_id IN (2609252000000000611,2609252000000000006)
ORDER BY parent_id,order_num,menu_id;
SQL

docker exec -i jdpm-mysql sh -c \
  'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 -N -B zbm-data' \
  <<'SQL' > "${backup_dir}/pre-non-target-menu.tsv"
SELECT menu_id,parent_id,menu_name,order_num,menu_type,path,IFNULL(component,''),IFNULL(perms,''),status,visible
FROM sys_menu
WHERE menu_id <> 2609252000000000611
ORDER BY menu_id;
SQL

(cd "${backup_dir}" && sha256sum zbm-data-auth016-wh-db1-menu.sql pre-target-and-tree.tsv pre-non-target-menu.tsv > SHA256SUMS)
