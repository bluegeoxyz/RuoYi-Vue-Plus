#!/usr/bin/env bash
set -euo pipefail

# Local developer helper. The credential file is outside Git and must be mode 0600.
readonly repo_root="/mnt/e/space/project/zbm-platform"
readonly credential_dir="${1:?usage: run_auth010b_seed_local.sh <credential-backup-dir>}"
readonly hash_file="${credential_dir}/password.bcrypt"

test "$(stat -c '%a' "${hash_file}")" = "600"
password_hash="$(<"${hash_file}")"

{
  printf "SET @auth010b_allow_development_seed = 'AUTH010B_LOCAL_DEV';\n"
  printf "SET @auth010b_password_hash = '%s';\n" "${password_hash//\'/\'\'}"
  cat "${repo_root}/script/sql/zbm/seed/auth010b_property_test_identities.sql"
} | docker exec -i jdpm-mysql sh -c \
  'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 zbm-data'

unset password_hash
