/*
 * ============================================================
 * ZBM Property Management - Current Baseline
 * File    : 99_init.sql
 * Version : V2.0.33
 *
 * MySQL CLI entry script.
 * Run this file FROM this directory:
 *
 *   mysql -u<user> -p <database> < 99_init.sql
 *
 * NOTE:
 *   SOURCE resolves paths from the mysql client working context.
 *   If your IDE/database client does not support SOURCE,
 *   execute 01_schema.sql, 02_dict.sql, 03_menu_permission.sql,
 *   ../migration/V2.0.32__property_business_roles.sql and
 *   ../migration/V2.0.33__fix_property_business_role_ownership.sql in that order.
 * ============================================================
 */

SOURCE 01_schema.sql;
SOURCE 02_dict.sql;
SOURCE 03_menu_permission.sql;
SOURCE ../migration/V2.0.32__property_business_roles.sql;
SOURCE ../migration/V2.0.33__fix_property_business_role_ownership.sql;
