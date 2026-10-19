/*
 * ============================================================
 * ZBM Property Management - Legacy Development Baseline
 * Version : V2.0.33
 * Backend : RuoYi-Vue-Plus 6.X / 6.0
 * Frontend: CrazyLionCat/plus-ui 6.X-Vue
 * Database: MySQL 8.0 / 8.4
 * ============================================================
 *
 * Prerequisite:
 *   1. Initialize the official RuoYi-Vue-Plus 6.X main database first.
 *   2. Confirm sys_user / sys_dept / sys_post / sys_dict_type /
 *      sys_dict_data / sys_menu exist.
 *
 * Recommended execution order:
 *   01_schema.sql
 *   02_dict.sql
 *   03_menu_permission.sql
 *   ../migration/V2.0.32__property_business_roles.sql
 *   ../migration/V2.0.33__fix_property_business_role_ownership.sql
 *   ../migration/V2.0.33.1__menu_permission_preconditions.sql
 *
 * Or, when using mysql CLI from this directory:
 *   SOURCE 99_init.sql;
 *
 * Notes:
 *   - This directory is retained only for historical tests and traceability.
 *   - Do not use it for new installations and do not maintain it as a second
 *     authoritative baseline.
 *   - The sole V1 authority is
 *     ruoyi-admin/src/main/resources/db/migration/V1__initial_schema.sql.
 *   - Historical V2 migrations are archived under
 *     script/sql/zbm/archive/legacy-development-migrations/.
 *   - No physical FOREIGN KEY constraints are created.
 *     Cross-table consistency is enforced in the service layer.
 *   - Java entity for table pm_unit is BuildingUnit.
 *   - pm_customer is enterprise-level master data and has NO project_id.
 *   - Phase names belong to the development plan and are not SQL directories.
 * ============================================================
 */
