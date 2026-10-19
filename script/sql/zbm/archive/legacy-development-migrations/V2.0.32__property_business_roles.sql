/*
 * TASK-ROLE-001 - Property business role initialization.
 *
 * Rules:
 * 1. Existing roles with the same role_key are never updated.
 * 2. Existing role-menu grants are never deleted or overwritten.
 * 3. Menu IDs are resolved from the current sys_menu permission strings.
 * 4. Parent menus are resolved from the current menu tree; no menu ID is assumed.
 * 5. data_scope = 5 (self) avoids granting global department data access. Property
 *    project access remains controlled by pm_user_project/project grants.
 */

SET NAMES utf8mb4;

DROP TEMPORARY TABLE IF EXISTS `tmp_property_preexisting_role`;
CREATE TEMPORARY TABLE `tmp_property_preexisting_role` (
    `role_key` varchar(100) NOT NULL,
    PRIMARY KEY (`role_key`)
) ENGINE=InnoDB;

INSERT INTO `tmp_property_preexisting_role` (`role_key`)
SELECT DISTINCT `role_key`
FROM `sys_role`
WHERE `role_key` IN (
    'property_operation_manager', 'property_customer_service', 'property_cashier',
    'property_finance_manager', 'property_warehouse_keeper',
    'property_warehouse_manager', 'property_project_manager'
);

START TRANSACTION;

INSERT INTO `sys_role`
(`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`,
 `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT UUID_SHORT(), '物业运营主管', 'property_operation_manager', 100, '5',
       1, 1, '0', '0', NULL, 1, NOW(), NULL, NULL,
       '物业日常运营管理；项目范围由物业项目授权控制'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role` WHERE `role_key` = 'property_operation_manager'
);

INSERT INTO `sys_role`
(`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`,
 `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT UUID_SHORT(), '物业客服', 'property_customer_service', 110, '5',
       1, 1, '0', '0', NULL, 1, NOW(), NULL, NULL,
       '住户、房屋查询及入住迁出业务；项目范围由物业项目授权控制'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role` WHERE `role_key` = 'property_customer_service'
);

INSERT INTO `sys_role`
(`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`,
 `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT UUID_SHORT(), '物业收费员', 'property_cashier', 120, '5',
       1, 1, '0', '0', NULL, 1, NOW(), NULL, NULL,
       '应收查询、收款及核销，不含收款冲销；项目范围由物业项目授权控制'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role` WHERE `role_key` = 'property_cashier'
);

INSERT INTO `sys_role`
(`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`,
 `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT UUID_SHORT(), '物业财务主管', 'property_finance_manager', 130, '5',
       1, 1, '0', '0', NULL, 1, NOW(), NULL, NULL,
       '收费配置、应收及收款管理，包含授权冲销；项目范围由物业项目授权控制'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role` WHERE `role_key` = 'property_finance_manager'
);

INSERT INTO `sys_role`
(`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`,
 `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT UUID_SHORT(), '仓库管理员', 'property_warehouse_keeper', 140, '5',
       1, 1, '0', '0', NULL, 1, NOW(), NULL, NULL,
       '库存、物料查询及出入库制单，不含确认和完成操作'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role` WHERE `role_key` = 'property_warehouse_keeper'
);

INSERT INTO `sys_role`
(`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`,
 `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT UUID_SHORT(), '仓库主管', 'property_warehouse_manager', 150, '5',
       1, 1, '0', '0', NULL, 1, NOW(), NULL, NULL,
       '仓库全业务管理，包含当前已实现的确认和完成操作'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role` WHERE `role_key` = 'property_warehouse_manager'
);

INSERT INTO `sys_role`
(`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`,
 `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT UUID_SHORT(), '物业项目经理', 'property_project_manager', 160, '5',
       1, 1, '0', '0', NULL, 1, NOW(), NULL, NULL,
       '项目运营业务管理，不含系统管理和企业级项目授权；项目范围由物业项目授权控制'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role` WHERE `role_key` = 'property_project_manager'
);

DROP TEMPORARY TABLE IF EXISTS `tmp_property_role_permission`;
CREATE TEMPORARY TABLE `tmp_property_role_permission` (
    `role_key` varchar(100) NOT NULL,
    `perms` varchar(100) NOT NULL,
    PRIMARY KEY (`role_key`, `perms`)
) ENGINE=InnoDB;

-- Property customer service: masked resident/customer reads, room reads and occupancy operations.
INSERT INTO `tmp_property_role_permission` (`role_key`, `perms`) VALUES
('property_customer_service', 'property:resident:list'),
('property_customer_service', 'property:resident:query'),
('property_customer_service', 'property:room:list'),
('property_customer_service', 'property:room:query'),
('property_customer_service', 'property:customer:list'),
('property_customer_service', 'property:customer:query'),
('property_customer_service', 'property:customerRelation:list'),
('property_customer_service', 'property:customerRelation:query'),
('property_customer_service', 'property:customerRelation:add'),
('property_customer_service', 'property:customerRelation:edit'),
('property_customer_service', 'property:customerRelation:moveIn'),
('property_customer_service', 'property:customerRelation:moveOut');

-- Property operation manager: daily project/resource/customer operations; no sensitive grants or finance writes.
INSERT INTO `tmp_property_role_permission` (`role_key`, `perms`)
SELECT 'property_operation_manager', `perms`
FROM `sys_menu`
WHERE `perms` IN (
    'property:project:list', 'property:project:query',
    'property:projectMember:list', 'property:projectMember:query',
    'property:projectMember:add', 'property:projectMember:edit',
    'property:projectMember:remove', 'property:projectMember:export',
    'property:building:list', 'property:building:query', 'property:building:add',
    'property:building:edit', 'property:building:remove', 'property:building:export',
    'property:unit:list', 'property:unit:query', 'property:unit:add',
    'property:unit:edit', 'property:unit:remove', 'property:unit:export',
    'property:room:list', 'property:room:query', 'property:room:add',
    'property:room:edit', 'property:room:remove', 'property:room:export',
    'property:customer:list', 'property:customer:query', 'property:customer:add',
    'property:customer:edit', 'property:customer:remove', 'property:customer:export',
    'property:customerRelation:list', 'property:customerRelation:query',
    'property:customerRelation:add', 'property:customerRelation:edit',
    'property:customerRelation:remove', 'property:customerRelation:export',
    'property:customerRelation:moveIn', 'property:customerRelation:moveOut',
    'property:resident:list', 'property:resident:query', 'property:resident:export',
    'property:chargeBinding:list', 'property:chargeBinding:query',
    'property:chargeBinding:add', 'property:chargeBinding:edit', 'property:chargeBinding:remove'
);

-- Cashier: receivable reads and payment collection/allocation; deliberately excludes reverse.
INSERT INTO `tmp_property_role_permission` (`role_key`, `perms`) VALUES
('property_cashier', 'property:receivable:list'),
('property_cashier', 'property:receivable:query'),
('property_cashier', 'property:receivable:export'),
('property_cashier', 'property:payment:list'),
('property_cashier', 'property:payment:query'),
('property_cashier', 'property:payment:add'),
('property_cashier', 'property:payment:allocate'),
('property_cashier', 'property:payment:export');

-- Finance manager: charge setup, receivables and payments, including reversal.
INSERT INTO `tmp_property_role_permission` (`role_key`, `perms`)
SELECT 'property_finance_manager', `perms`
FROM `sys_menu`
WHERE `perms` LIKE 'property:chargeItem:%'
   OR `perms` LIKE 'property:chargeRule:%'
   OR `perms` LIKE 'property:chargeBinding:%'
   OR `perms` LIKE 'property:receivable:%'
   OR `perms` LIKE 'property:payment:%';

-- Warehouse keeper: read master/inventory data and prepare inbound/outbound documents.
INSERT INTO `tmp_property_role_permission` (`role_key`, `perms`) VALUES
('property_warehouse_keeper', 'warehouse:materialCategory:list'),
('property_warehouse_keeper', 'warehouse:materialCategory:query'),
('property_warehouse_keeper', 'warehouse:unit:list'),
('property_warehouse_keeper', 'warehouse:unit:query'),
('property_warehouse_keeper', 'warehouse:material:list'),
('property_warehouse_keeper', 'warehouse:material:query'),
('property_warehouse_keeper', 'warehouse:inventory:list'),
('property_warehouse_keeper', 'warehouse:inventory:query'),
('property_warehouse_keeper', 'warehouse:inventory:export'),
('property_warehouse_keeper', 'warehouse:inventoryTransaction:list'),
('property_warehouse_keeper', 'warehouse:inventoryTransaction:query'),
('property_warehouse_keeper', 'warehouse:inventoryTransaction:export'),
('property_warehouse_keeper', 'warehouse:inbound:list'),
('property_warehouse_keeper', 'warehouse:inbound:query'),
('property_warehouse_keeper', 'warehouse:inbound:add'),
('property_warehouse_keeper', 'warehouse:inbound:edit'),
('property_warehouse_keeper', 'warehouse:inbound:remove'),
('property_warehouse_keeper', 'warehouse:inbound:cancel'),
('property_warehouse_keeper', 'warehouse:inbound:export'),
('property_warehouse_keeper', 'warehouse:outbound:list'),
('property_warehouse_keeper', 'warehouse:outbound:query'),
('property_warehouse_keeper', 'warehouse:outbound:add'),
('property_warehouse_keeper', 'warehouse:outbound:edit'),
('property_warehouse_keeper', 'warehouse:outbound:remove'),
('property_warehouse_keeper', 'warehouse:outbound:cancel'),
('property_warehouse_keeper', 'warehouse:outbound:export');

-- Warehouse manager: all currently implemented Warehouse Phase 1 permissions.
INSERT INTO `tmp_property_role_permission` (`role_key`, `perms`)
SELECT 'property_warehouse_manager', `perms`
FROM `sys_menu`
WHERE `perms` LIKE 'warehouse:%';

-- Project manager: project operations, property resources/customer operations and financial reads.
INSERT INTO `tmp_property_role_permission` (`role_key`, `perms`)
SELECT 'property_project_manager', `perms`
FROM `sys_menu`
WHERE `perms` IN (
    'property:project:list', 'property:project:query',
    'property:projectMember:list', 'property:projectMember:query',
    'property:projectMember:add', 'property:projectMember:edit',
    'property:projectMember:remove', 'property:projectMember:export',
    'property:building:list', 'property:building:query', 'property:building:add',
    'property:building:edit', 'property:building:remove', 'property:building:export',
    'property:unit:list', 'property:unit:query', 'property:unit:add',
    'property:unit:edit', 'property:unit:remove', 'property:unit:export',
    'property:room:list', 'property:room:query', 'property:room:add',
    'property:room:edit', 'property:room:remove', 'property:room:export',
    'property:customer:list', 'property:customer:query',
    'property:customerRelation:list', 'property:customerRelation:query',
    'property:customerRelation:add', 'property:customerRelation:edit',
    'property:customerRelation:remove', 'property:customerRelation:moveIn',
    'property:customerRelation:moveOut',
    'property:resident:list', 'property:resident:query', 'property:resident:export',
    'property:chargeBinding:list', 'property:chargeBinding:query',
    'property:chargeBinding:add', 'property:chargeBinding:edit',
    'property:receivable:list', 'property:receivable:query', 'property:receivable:export',
    'property:payment:list', 'property:payment:query', 'property:payment:export'
);

DROP TEMPORARY TABLE IF EXISTS `tmp_property_role_menu`;
CREATE TEMPORARY TABLE `tmp_property_role_menu` (
    `role_key` varchar(100) NOT NULL,
    `menu_id` bigint NOT NULL,
    PRIMARY KEY (`role_key`, `menu_id`)
) ENGINE=InnoDB;

-- Resolve permission menus plus up to three ancestor levels from the current menu tree.
INSERT IGNORE INTO `tmp_property_role_menu` (`role_key`, `menu_id`)
SELECT desired.`role_key`, leaf.`menu_id`
FROM `tmp_property_role_permission` desired
JOIN `sys_menu` leaf ON leaf.`perms` = desired.`perms` AND leaf.`status` = '0';

INSERT IGNORE INTO `tmp_property_role_menu` (`role_key`, `menu_id`)
SELECT desired.`role_key`, parent1.`menu_id`
FROM `tmp_property_role_permission` desired
JOIN `sys_menu` leaf ON leaf.`perms` = desired.`perms` AND leaf.`status` = '0'
JOIN `sys_menu` parent1 ON parent1.`menu_id` = leaf.`parent_id` AND parent1.`menu_id` <> 0;

INSERT IGNORE INTO `tmp_property_role_menu` (`role_key`, `menu_id`)
SELECT desired.`role_key`, parent2.`menu_id`
FROM `tmp_property_role_permission` desired
JOIN `sys_menu` leaf ON leaf.`perms` = desired.`perms` AND leaf.`status` = '0'
JOIN `sys_menu` parent1 ON parent1.`menu_id` = leaf.`parent_id` AND parent1.`menu_id` <> 0
JOIN `sys_menu` parent2 ON parent2.`menu_id` = parent1.`parent_id` AND parent2.`menu_id` <> 0;

INSERT IGNORE INTO `tmp_property_role_menu` (`role_key`, `menu_id`)
SELECT desired.`role_key`, parent3.`menu_id`
FROM `tmp_property_role_permission` desired
JOIN `sys_menu` leaf ON leaf.`perms` = desired.`perms` AND leaf.`status` = '0'
JOIN `sys_menu` parent1 ON parent1.`menu_id` = leaf.`parent_id` AND parent1.`menu_id` <> 0
JOIN `sys_menu` parent2 ON parent2.`menu_id` = parent1.`parent_id` AND parent2.`menu_id` <> 0
JOIN `sys_menu` parent3 ON parent3.`menu_id` = parent2.`parent_id` AND parent3.`menu_id` <> 0;

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT role.`role_id`, desired.`menu_id`
FROM `tmp_property_role_menu` desired
JOIN `sys_role` role ON role.`role_key` = desired.`role_key`
LEFT JOIN `tmp_property_preexisting_role` preexisting ON preexisting.`role_key` = role.`role_key`
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` existing
    WHERE existing.`role_id` = role.`role_id`
      AND existing.`menu_id` = desired.`menu_id`
)
  AND preexisting.`role_key` IS NULL;

-- Expected result: zero rows. A row means the target permission menu is absent or disabled.
SELECT desired.`role_key`, desired.`perms` AS `missing_or_disabled_permission`
FROM `tmp_property_role_permission` desired
LEFT JOIN `sys_menu` menu ON menu.`perms` = desired.`perms` AND menu.`status` = '0'
WHERE menu.`menu_id` IS NULL
ORDER BY desired.`role_key`, desired.`perms`;

COMMIT;

DROP TEMPORARY TABLE IF EXISTS `tmp_property_role_menu`;
DROP TEMPORARY TABLE IF EXISTS `tmp_property_role_permission`;
DROP TEMPORARY TABLE IF EXISTS `tmp_property_preexisting_role`;

-- Verification queries (all anomaly queries should return zero rows).
SELECT expected.`role_key`, COUNT(role.`role_id`) AS `role_count`
FROM (
    SELECT 'property_operation_manager' AS `role_key`
    UNION ALL SELECT 'property_customer_service'
    UNION ALL SELECT 'property_cashier'
    UNION ALL SELECT 'property_finance_manager'
    UNION ALL SELECT 'property_warehouse_keeper'
    UNION ALL SELECT 'property_warehouse_manager'
    UNION ALL SELECT 'property_project_manager'
) expected
LEFT JOIN `sys_role` role ON role.`role_key` = expected.`role_key`
GROUP BY expected.`role_key`
HAVING COUNT(role.`role_id`) <> 1;

SELECT `role_id`, `menu_id`, COUNT(*) AS `grant_count`
FROM `sys_role_menu`
GROUP BY `role_id`, `menu_id`
HAVING COUNT(*) > 1;

SELECT role.`role_key`, menu.`perms`
FROM `sys_role` role
JOIN `sys_role_menu` role_menu ON role_menu.`role_id` = role.`role_id`
JOIN `sys_menu` menu ON menu.`menu_id` = role_menu.`menu_id`
WHERE role.`role_key` IN (
    'property_operation_manager', 'property_customer_service', 'property_cashier',
    'property_finance_manager', 'property_warehouse_keeper',
    'property_warehouse_manager', 'property_project_manager'
)
  AND (menu.`perms` LIKE 'system:%' OR menu.`perms` LIKE 'monitor:%')
ORDER BY role.`role_key`, menu.`perms`;
