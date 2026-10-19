/*
 * TASK-ROLE-001 follow-up.
 *
 * SysRoleMapper applies data permission to create_dept/create_by. V2.0.32
 * intentionally did not bind roles to a business department, but NULL ownership
 * makes the records invisible to non-superadmin role administrators. Repair only
 * the seven rows carrying the exact seed remarks written by V2.0.32.
 */

SET NAMES utf8mb4;

START TRANSACTION;

SET @property_role_owner_user_id = (
    SELECT su.`user_id`
    FROM `sys_role` admin_role
    JOIN `sys_user_role` user_role ON user_role.`role_id` = admin_role.`role_id`
    JOIN `sys_user` su ON su.`user_id` = user_role.`user_id`
    WHERE admin_role.`role_key` = 'superadmin'
      AND admin_role.`del_flag` = '0'
      AND su.`del_flag` = '0'
    ORDER BY su.`user_id`
    LIMIT 1
);

SET @property_role_owner_dept_id = (
    SELECT su.`dept_id`
    FROM `sys_user` su
    WHERE su.`user_id` = @property_role_owner_user_id
    LIMIT 1
);

UPDATE `sys_role` target
JOIN (
    SELECT 'property_operation_manager' AS `role_key`,
           '物业日常运营管理；项目范围由物业项目授权控制' AS `remark`
    UNION ALL
    SELECT 'property_customer_service',
           '住户、房屋查询及入住迁出业务；项目范围由物业项目授权控制'
    UNION ALL
    SELECT 'property_cashier',
           '应收查询、收款及核销，不含收款冲销；项目范围由物业项目授权控制'
    UNION ALL
    SELECT 'property_finance_manager',
           '收费配置、应收及收款管理，包含授权冲销；项目范围由物业项目授权控制'
    UNION ALL
    SELECT 'property_warehouse_keeper',
           '库存、物料查询及出入库制单，不含确认和完成操作'
    UNION ALL
    SELECT 'property_warehouse_manager',
           '仓库全业务管理，包含当前已实现的确认和完成操作'
    UNION ALL
    SELECT 'property_project_manager',
           '项目运营业务管理，不含系统管理和企业级项目授权；项目范围由物业项目授权控制'
) seeded ON seeded.`role_key` = target.`role_key` AND seeded.`remark` = target.`remark`
SET target.`create_dept` = @property_role_owner_dept_id,
    target.`create_by` = @property_role_owner_user_id,
    target.`update_by` = @property_role_owner_user_id,
    target.`update_time` = NOW()
WHERE target.`create_dept` IS NULL
  AND target.`create_by` = 1
  AND target.`del_flag` = '0'
  AND @property_role_owner_user_id IS NOT NULL
  AND @property_role_owner_dept_id IS NOT NULL;

COMMIT;

-- Expected result: seven rows, all with non-NULL create_dept and create_by.
SELECT `role_name`, `role_key`, `create_dept`, `create_by`, `status`, `del_flag`
FROM `sys_role`
WHERE `role_key` IN (
    'property_operation_manager', 'property_customer_service', 'property_cashier',
    'property_finance_manager', 'property_warehouse_keeper',
    'property_warehouse_manager', 'property_project_manager'
)
ORDER BY `role_sort`;

-- Expected result: zero rows.
SELECT `role_key`, `create_dept`, `create_by`
FROM `sys_role`
WHERE `role_key` IN (
    'property_operation_manager', 'property_customer_service', 'property_cashier',
    'property_finance_manager', 'property_warehouse_keeper',
    'property_warehouse_manager', 'property_project_manager'
)
  AND (`create_dept` IS NULL OR `create_by` IS NULL);
