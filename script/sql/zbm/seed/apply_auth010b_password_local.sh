#!/usr/bin/env bash
set -euo pipefail

readonly credential_dir="${1:?usage: apply_auth010b_password_local.sh <credential-dir>}"
readonly hash_file="${credential_dir}/password.bcrypt"
test "$(stat -c '%a' "${hash_file}")" = "600"
password_hash="$(<"${hash_file}")"

printf "UPDATE sys_user SET password='%s' WHERE user_id BETWEEN 2199011000000002001 AND 2199011000000002010 AND remark='TEST-AUTH-010B';\n" \
  "${password_hash}" \
  | docker exec -i jdpm-mysql sh -c \
      'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 zbm-data'

unset password_hash
