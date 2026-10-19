#!/usr/bin/env bash
set -euo pipefail

readonly backup_dir="${1:?usage: backup_auth010b_post_state.sh <backup-dir>}"
readonly tables=(
  sys_dept sys_post sys_user sys_user_post sys_user_role hr_employee
  pm_project pm_building pm_unit pm_room pm_customer pm_room_customer_rel
  pm_charge_item pm_charge_rule pm_charge_binding pm_receivable_batch pm_receivable
  pm_project_member pm_project_management_grant
  wm_warehouse wm_warehouse_zone wm_warehouse_location wm_material_category wm_unit wm_material wm_inventory
)

docker exec jdpm-mysql sh -c \
  'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --skip-lock-tables zbm-data "$@"' \
  sh "${tables[@]}" > "${backup_dir}/zbm-data-auth010b-post-state.sql"

cd "${backup_dir}"
sha256sum zbm-data-auth010b-affected-tables.sql zbm-data-auth010b-post-state.sql \
  diagnostic-after-first-run.log diagnostic-after-second-run.log http-verification.json > SHA256SUMS.final
cat SHA256SUMS.final
