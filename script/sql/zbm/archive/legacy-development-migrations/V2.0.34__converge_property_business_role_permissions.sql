/*
 * TASK-AUTH-010A - Converge the seven standard property roles to the reviewed
 * minimum permission matrix after AUTH-004A through AUTH-006.
 *
 * Scope:
 * 1. Existing role rows, data_scope and ownership are not changed.
 * 2. Only sys_role_menu rows of the seven exact role_key values are changed.
 * 3. Menu IDs are resolved from enabled sys_menu rows; no ID is assumed.
 * 4. The pre-change result sets show excess/missing grants for operator review.
 * 5. Project and warehouse resource scope remains enforced by the existing
 *    PropertyProjectScopeService and WarehouseAccessService chains.
 */

SET NAMES utf8mb4;

DROP TEMPORARY TABLE IF EXISTS `tmp_auth010_role`;
CREATE TEMPORARY TABLE `tmp_auth010_role` (
    `role_key` varchar(100) NOT NULL,
    PRIMARY KEY (`role_key`)
) ENGINE=InnoDB;

INSERT INTO `tmp_auth010_role` (`role_key`) VALUES
('property_operation_manager'),
('property_customer_service'),
('property_cashier'),
('property_finance_manager'),
('property_warehouse_keeper'),
('property_warehouse_manager'),
('property_project_manager');

DROP TEMPORARY TABLE IF EXISTS `tmp_auth010_permission`;
CREATE TEMPORARY TABLE `tmp_auth010_permission` (
    `role_key` varchar(100) NOT NULL,
    `perms` varchar(100) NOT NULL,
    PRIMARY KEY (`role_key`, `perms`)
) ENGINE=InnoDB;

-- Customer service: customer-relation operations only. Reference data comes
-- from the CUSTOMER_RELATION_* context APIs, not project/room/customer CRUD.
INSERT INTO `tmp_auth010_permission` (`role_key`, `perms`) VALUES
('property_customer_service', 'property:customerRelation:list'),
('property_customer_service', 'property:customerRelation:query'),
('property_customer_service', 'property:customerRelation:add'),
('property_customer_service', 'property:customerRelation:edit'),
('property_customer_service', 'property:customerRelation:moveIn'),
('property_customer_service', 'property:customerRelation:moveOut');

-- Operation manager: authorized-project resources and resident operations.
INSERT INTO `tmp_auth010_permission` (`role_key`, `perms`) VALUES
('property_operation_manager', 'property:project:list'),
('property_operation_manager', 'property:project:query'),
('property_operation_manager', 'property:building:list'),
('property_operation_manager', 'property:building:query'),
('property_operation_manager', 'property:building:add'),
('property_operation_manager', 'property:building:edit'),
('property_operation_manager', 'property:building:remove'),
('property_operation_manager', 'property:building:export'),
('property_operation_manager', 'property:unit:list'),
('property_operation_manager', 'property:unit:query'),
('property_operation_manager', 'property:unit:add'),
('property_operation_manager', 'property:unit:edit'),
('property_operation_manager', 'property:unit:remove'),
('property_operation_manager', 'property:unit:export'),
('property_operation_manager', 'property:room:list'),
('property_operation_manager', 'property:room:query'),
('property_operation_manager', 'property:room:add'),
('property_operation_manager', 'property:room:edit'),
('property_operation_manager', 'property:room:remove'),
('property_operation_manager', 'property:room:export'),
('property_operation_manager', 'property:resource:treeImport'),
('property_operation_manager', 'property:customer:list'),
('property_operation_manager', 'property:customer:query'),
('property_operation_manager', 'property:customer:add'),
('property_operation_manager', 'property:customer:edit'),
('property_operation_manager', 'property:customer:remove'),
('property_operation_manager', 'property:customer:export'),
('property_operation_manager', 'property:customerRelation:list'),
('property_operation_manager', 'property:customerRelation:query'),
('property_operation_manager', 'property:customerRelation:add'),
('property_operation_manager', 'property:customerRelation:edit'),
('property_operation_manager', 'property:customerRelation:remove'),
('property_operation_manager', 'property:customerRelation:export'),
('property_operation_manager', 'property:customerRelation:moveIn'),
('property_operation_manager', 'property:customerRelation:moveOut'),
('property_operation_manager', 'property:resident:list'),
('property_operation_manager', 'property:resident:query'),
('property_operation_manager', 'property:resident:export');

-- Cashier: receivable reads and payment collection/allocation only.
INSERT INTO `tmp_auth010_permission` (`role_key`, `perms`) VALUES
('property_cashier', 'property:receivable:list'),
('property_cashier', 'property:receivable:query'),
('property_cashier', 'property:payment:list'),
('property_cashier', 'property:payment:query'),
('property_cashier', 'property:payment:add'),
('property_cashier', 'property:payment:allocate');

-- Finance manager: finance configuration and full implemented finance flow.
INSERT INTO `tmp_auth010_permission` (`role_key`, `perms`) VALUES
('property_finance_manager', 'property:chargeItem:list'),
('property_finance_manager', 'property:chargeItem:query'),
('property_finance_manager', 'property:chargeItem:add'),
('property_finance_manager', 'property:chargeItem:edit'),
('property_finance_manager', 'property:chargeItem:remove'),
('property_finance_manager', 'property:chargeItem:export'),
('property_finance_manager', 'property:chargeRule:list'),
('property_finance_manager', 'property:chargeRule:query'),
('property_finance_manager', 'property:chargeRule:add'),
('property_finance_manager', 'property:chargeRule:edit'),
('property_finance_manager', 'property:chargeRule:remove'),
('property_finance_manager', 'property:chargeRule:export'),
('property_finance_manager', 'property:chargeBinding:list'),
('property_finance_manager', 'property:chargeBinding:query'),
('property_finance_manager', 'property:chargeBinding:add'),
('property_finance_manager', 'property:chargeBinding:edit'),
('property_finance_manager', 'property:chargeBinding:remove'),
('property_finance_manager', 'property:receivable:list'),
('property_finance_manager', 'property:receivable:query'),
('property_finance_manager', 'property:receivable:generate'),
('property_finance_manager', 'property:receivable:cancel'),
('property_finance_manager', 'property:receivable:export'),
('property_finance_manager', 'property:payment:list'),
('property_finance_manager', 'property:payment:query'),
('property_finance_manager', 'property:payment:add'),
('property_finance_manager', 'property:payment:allocate'),
('property_finance_manager', 'property:payment:reverse'),
('property_finance_manager', 'property:payment:export');

-- Warehouse keeper: inventory reads plus inbound/outbound document preparation.
-- Master-data CRUD and confirm/complete permissions are deliberately excluded.
INSERT INTO `tmp_auth010_permission` (`role_key`, `perms`) VALUES
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

-- Warehouse manager: every currently implemented warehouse permission, listed
-- explicitly so future permissions are not granted automatically.
INSERT INTO `tmp_auth010_permission` (`role_key`, `perms`) VALUES
('property_warehouse_manager', 'warehouse:warehouse:list'),
('property_warehouse_manager', 'warehouse:warehouse:query'),
('property_warehouse_manager', 'warehouse:warehouse:add'),
('property_warehouse_manager', 'warehouse:warehouse:edit'),
('property_warehouse_manager', 'warehouse:warehouse:remove'),
('property_warehouse_manager', 'warehouse:zone:list'),
('property_warehouse_manager', 'warehouse:zone:query'),
('property_warehouse_manager', 'warehouse:zone:add'),
('property_warehouse_manager', 'warehouse:zone:edit'),
('property_warehouse_manager', 'warehouse:zone:remove'),
('property_warehouse_manager', 'warehouse:location:list'),
('property_warehouse_manager', 'warehouse:location:query'),
('property_warehouse_manager', 'warehouse:location:add'),
('property_warehouse_manager', 'warehouse:location:edit'),
('property_warehouse_manager', 'warehouse:location:remove'),
('property_warehouse_manager', 'warehouse:materialCategory:list'),
('property_warehouse_manager', 'warehouse:materialCategory:query'),
('property_warehouse_manager', 'warehouse:materialCategory:add'),
('property_warehouse_manager', 'warehouse:materialCategory:edit'),
('property_warehouse_manager', 'warehouse:materialCategory:remove'),
('property_warehouse_manager', 'warehouse:unit:list'),
('property_warehouse_manager', 'warehouse:unit:query'),
('property_warehouse_manager', 'warehouse:unit:add'),
('property_warehouse_manager', 'warehouse:unit:edit'),
('property_warehouse_manager', 'warehouse:unit:remove'),
('property_warehouse_manager', 'warehouse:material:list'),
('property_warehouse_manager', 'warehouse:material:query'),
('property_warehouse_manager', 'warehouse:material:add'),
('property_warehouse_manager', 'warehouse:material:edit'),
('property_warehouse_manager', 'warehouse:material:remove'),
('property_warehouse_manager', 'warehouse:inventory:list'),
('property_warehouse_manager', 'warehouse:inventory:query'),
('property_warehouse_manager', 'warehouse:inventory:export'),
('property_warehouse_manager', 'warehouse:inventoryTransaction:list'),
('property_warehouse_manager', 'warehouse:inventoryTransaction:query'),
('property_warehouse_manager', 'warehouse:inventoryTransaction:export'),
('property_warehouse_manager', 'warehouse:inbound:list'),
('property_warehouse_manager', 'warehouse:inbound:query'),
('property_warehouse_manager', 'warehouse:inbound:add'),
('property_warehouse_manager', 'warehouse:inbound:edit'),
('property_warehouse_manager', 'warehouse:inbound:remove'),
('property_warehouse_manager', 'warehouse:inbound:confirm'),
('property_warehouse_manager', 'warehouse:inbound:complete'),
('property_warehouse_manager', 'warehouse:inbound:cancel'),
('property_warehouse_manager', 'warehouse:inbound:export'),
('property_warehouse_manager', 'warehouse:outbound:list'),
('property_warehouse_manager', 'warehouse:outbound:query'),
('property_warehouse_manager', 'warehouse:outbound:add'),
('property_warehouse_manager', 'warehouse:outbound:edit'),
('property_warehouse_manager', 'warehouse:outbound:remove'),
('property_warehouse_manager', 'warehouse:outbound:confirm'),
('property_warehouse_manager', 'warehouse:outbound:complete'),
('property_warehouse_manager', 'warehouse:outbound:cancel'),
('property_warehouse_manager', 'warehouse:outbound:export'),
('property_warehouse_manager', 'warehouse:transfer:list'),
('property_warehouse_manager', 'warehouse:transfer:query'),
('property_warehouse_manager', 'warehouse:transfer:add'),
('property_warehouse_manager', 'warehouse:transfer:edit'),
('property_warehouse_manager', 'warehouse:transfer:remove'),
('property_warehouse_manager', 'warehouse:transfer:confirm'),
('property_warehouse_manager', 'warehouse:transfer:complete'),
('property_warehouse_manager', 'warehouse:transfer:cancel'),
('property_warehouse_manager', 'warehouse:transfer:export'),
('property_warehouse_manager', 'warehouse:stocktake:list'),
('property_warehouse_manager', 'warehouse:stocktake:query'),
('property_warehouse_manager', 'warehouse:stocktake:add'),
('property_warehouse_manager', 'warehouse:stocktake:edit'),
('property_warehouse_manager', 'warehouse:stocktake:remove'),
('property_warehouse_manager', 'warehouse:stocktake:confirm'),
('property_warehouse_manager', 'warehouse:stocktake:complete'),
('property_warehouse_manager', 'warehouse:stocktake:cancel'),
('property_warehouse_manager', 'warehouse:stocktake:export');

-- Project manager: authorized-project membership and property operations, with
-- finance read-only access. Project CRUD and enterprise grant administration
-- are deliberately excluded.
INSERT INTO `tmp_auth010_permission` (`role_key`, `perms`) VALUES
('property_project_manager', 'property:project:list'),
('property_project_manager', 'property:project:query'),
('property_project_manager', 'property:projectMember:list'),
('property_project_manager', 'property:projectMember:query'),
('property_project_manager', 'property:projectMember:add'),
('property_project_manager', 'property:projectMember:edit'),
('property_project_manager', 'property:projectMember:remove'),
('property_project_manager', 'property:projectMember:export'),
('property_project_manager', 'property:building:list'),
('property_project_manager', 'property:building:query'),
('property_project_manager', 'property:building:add'),
('property_project_manager', 'property:building:edit'),
('property_project_manager', 'property:building:remove'),
('property_project_manager', 'property:building:export'),
('property_project_manager', 'property:unit:list'),
('property_project_manager', 'property:unit:query'),
('property_project_manager', 'property:unit:add'),
('property_project_manager', 'property:unit:edit'),
('property_project_manager', 'property:unit:remove'),
('property_project_manager', 'property:unit:export'),
('property_project_manager', 'property:room:list'),
('property_project_manager', 'property:room:query'),
('property_project_manager', 'property:room:add'),
('property_project_manager', 'property:room:edit'),
('property_project_manager', 'property:room:remove'),
('property_project_manager', 'property:room:export'),
('property_project_manager', 'property:resource:treeImport'),
('property_project_manager', 'property:customer:list'),
('property_project_manager', 'property:customer:query'),
('property_project_manager', 'property:customer:add'),
('property_project_manager', 'property:customer:edit'),
('property_project_manager', 'property:customer:remove'),
('property_project_manager', 'property:customer:export'),
('property_project_manager', 'property:customerRelation:list'),
('property_project_manager', 'property:customerRelation:query'),
('property_project_manager', 'property:customerRelation:add'),
('property_project_manager', 'property:customerRelation:edit'),
('property_project_manager', 'property:customerRelation:remove'),
('property_project_manager', 'property:customerRelation:export'),
('property_project_manager', 'property:customerRelation:moveIn'),
('property_project_manager', 'property:customerRelation:moveOut'),
('property_project_manager', 'property:resident:list'),
('property_project_manager', 'property:resident:query'),
('property_project_manager', 'property:resident:export'),
('property_project_manager', 'property:receivable:list'),
('property_project_manager', 'property:receivable:query'),
('property_project_manager', 'property:payment:list'),
('property_project_manager', 'property:payment:query');

DROP TEMPORARY TABLE IF EXISTS `tmp_auth010_menu`;
CREATE TEMPORARY TABLE `tmp_auth010_menu` (
    `role_key` varchar(100) NOT NULL,
    `menu_id` bigint NOT NULL,
    PRIMARY KEY (`role_key`, `menu_id`)
) ENGINE=InnoDB;

-- Resolve leaf permissions and their navigation ancestors. Separate statements
-- avoid MySQL ERROR 1137 (Can't reopen table) seen with a UNION over one temp table.
INSERT IGNORE INTO `tmp_auth010_menu` (`role_key`, `menu_id`)
SELECT desired.`role_key`, leaf.`menu_id`
FROM `tmp_auth010_permission` desired
JOIN `sys_menu` leaf ON leaf.`perms` = desired.`perms` AND leaf.`status` = '0';

INSERT IGNORE INTO `tmp_auth010_menu` (`role_key`, `menu_id`)
SELECT desired.`role_key`, parent1.`menu_id`
FROM `tmp_auth010_permission` desired
JOIN `sys_menu` leaf ON leaf.`perms` = desired.`perms` AND leaf.`status` = '0'
JOIN `sys_menu` parent1 ON parent1.`menu_id` = leaf.`parent_id` AND parent1.`menu_id` <> 0;

INSERT IGNORE INTO `tmp_auth010_menu` (`role_key`, `menu_id`)
SELECT desired.`role_key`, parent2.`menu_id`
FROM `tmp_auth010_permission` desired
JOIN `sys_menu` leaf ON leaf.`perms` = desired.`perms` AND leaf.`status` = '0'
JOIN `sys_menu` parent1 ON parent1.`menu_id` = leaf.`parent_id` AND parent1.`menu_id` <> 0
JOIN `sys_menu` parent2 ON parent2.`menu_id` = parent1.`parent_id` AND parent2.`menu_id` <> 0;

INSERT IGNORE INTO `tmp_auth010_menu` (`role_key`, `menu_id`)
SELECT desired.`role_key`, parent3.`menu_id`
FROM `tmp_auth010_permission` desired
JOIN `sys_menu` leaf ON leaf.`perms` = desired.`perms` AND leaf.`status` = '0'
JOIN `sys_menu` parent1 ON parent1.`menu_id` = leaf.`parent_id` AND parent1.`menu_id` <> 0
JOIN `sys_menu` parent2 ON parent2.`menu_id` = parent1.`parent_id` AND parent2.`menu_id` <> 0
JOIN `sys_menu` parent3 ON parent3.`menu_id` = parent2.`parent_id` AND parent3.`menu_id` <> 0;

-- Permissions intentionally removed from the V2.0.32 repository baseline.
-- They are retained only for the drift guard below, never for final grants.
DROP TEMPORARY TABLE IF EXISTS `tmp_auth010_removed_permission`;
CREATE TEMPORARY TABLE `tmp_auth010_removed_permission` (
    `role_key` varchar(100) NOT NULL,
    `perms` varchar(100) NOT NULL,
    PRIMARY KEY (`role_key`, `perms`)
) ENGINE=InnoDB;

INSERT INTO `tmp_auth010_removed_permission` (`role_key`, `perms`) VALUES
('property_finance_manager', 'property:room:list'),
('property_customer_service', 'property:resident:list'),
('property_customer_service', 'property:resident:query'),
('property_customer_service', 'property:room:list'),
('property_customer_service', 'property:room:query'),
('property_customer_service', 'property:customer:list'),
('property_customer_service', 'property:customer:query'),
('property_cashier', 'property:receivable:export'),
('property_cashier', 'property:payment:export'),
('property_warehouse_keeper', 'warehouse:materialCategory:list'),
('property_warehouse_keeper', 'warehouse:materialCategory:query'),
('property_warehouse_keeper', 'warehouse:unit:list'),
('property_warehouse_keeper', 'warehouse:unit:query'),
('property_warehouse_keeper', 'warehouse:material:list'),
('property_warehouse_keeper', 'warehouse:material:query'),
('property_operation_manager', 'property:projectMember:list'),
('property_operation_manager', 'property:projectMember:query'),
('property_operation_manager', 'property:projectMember:add'),
('property_operation_manager', 'property:projectMember:edit'),
('property_operation_manager', 'property:projectMember:remove'),
('property_operation_manager', 'property:projectMember:export'),
('property_operation_manager', 'property:chargeBinding:list'),
('property_operation_manager', 'property:chargeBinding:query'),
('property_operation_manager', 'property:chargeBinding:add'),
('property_operation_manager', 'property:chargeBinding:edit'),
('property_operation_manager', 'property:chargeBinding:remove'),
('property_project_manager', 'property:chargeBinding:list'),
('property_project_manager', 'property:chargeBinding:query'),
('property_project_manager', 'property:chargeBinding:add'),
('property_project_manager', 'property:chargeBinding:edit'),
('property_project_manager', 'property:receivable:export'),
('property_project_manager', 'property:payment:export');

DROP TEMPORARY TABLE IF EXISTS `tmp_auth010_known_menu`;
CREATE TEMPORARY TABLE `tmp_auth010_known_menu` LIKE `tmp_auth010_menu`;
INSERT INTO `tmp_auth010_known_menu` SELECT `role_key`, `menu_id` FROM `tmp_auth010_menu`;

INSERT IGNORE INTO `tmp_auth010_known_menu` (`role_key`, `menu_id`)
SELECT removed.`role_key`, leaf.`menu_id`
FROM `tmp_auth010_removed_permission` removed
JOIN `sys_menu` leaf ON leaf.`perms` = removed.`perms`;

INSERT IGNORE INTO `tmp_auth010_known_menu` (`role_key`, `menu_id`)
SELECT removed.`role_key`, parent1.`menu_id`
FROM `tmp_auth010_removed_permission` removed
JOIN `sys_menu` leaf ON leaf.`perms` = removed.`perms`
JOIN `sys_menu` parent1 ON parent1.`menu_id` = leaf.`parent_id` AND parent1.`menu_id` <> 0;

INSERT IGNORE INTO `tmp_auth010_known_menu` (`role_key`, `menu_id`)
SELECT removed.`role_key`, parent2.`menu_id`
FROM `tmp_auth010_removed_permission` removed
JOIN `sys_menu` leaf ON leaf.`perms` = removed.`perms`
JOIN `sys_menu` parent1 ON parent1.`menu_id` = leaf.`parent_id` AND parent1.`menu_id` <> 0
JOIN `sys_menu` parent2 ON parent2.`menu_id` = parent1.`parent_id` AND parent2.`menu_id` <> 0;

INSERT IGNORE INTO `tmp_auth010_known_menu` (`role_key`, `menu_id`)
SELECT removed.`role_key`, parent3.`menu_id`
FROM `tmp_auth010_removed_permission` removed
JOIN `sys_menu` leaf ON leaf.`perms` = removed.`perms`
JOIN `sys_menu` parent1 ON parent1.`menu_id` = leaf.`parent_id` AND parent1.`menu_id` <> 0
JOIN `sys_menu` parent2 ON parent2.`menu_id` = parent1.`parent_id` AND parent2.`menu_id` <> 0
JOIN `sys_menu` parent3 ON parent3.`menu_id` = parent2.`parent_id` AND parent3.`menu_id` <> 0;

-- Fail closed before any write when the database differs from the reviewed
-- repository baseline, a role is missing/duplicated, or a desired menu is absent.
-- The duplicate-key error is intentional and keeps the migration portable
-- without creating a stored procedure solely for SIGNAL/IF control flow.
DROP TEMPORARY TABLE IF EXISTS `tmp_auth010_guard`;
CREATE TEMPORARY TABLE `tmp_auth010_guard` (`guard_id` int NOT NULL PRIMARY KEY) ENGINE=InnoDB;
INSERT INTO `tmp_auth010_guard` (`guard_id`) VALUES (1), (2), (3);

INSERT INTO `tmp_auth010_guard` (`guard_id`)
SELECT 1
FROM `sys_role` role
JOIN `tmp_auth010_role` target ON target.`role_key` = role.`role_key` AND role.`del_flag` = '0'
JOIN `sys_role_menu` role_menu ON role_menu.`role_id` = role.`role_id`
LEFT JOIN `tmp_auth010_known_menu` known
       ON known.`role_key` = role.`role_key` AND known.`menu_id` = role_menu.`menu_id`
WHERE known.`menu_id` IS NULL
LIMIT 1;

INSERT INTO `tmp_auth010_guard` (`guard_id`)
SELECT 2
FROM `tmp_auth010_role` target
LEFT JOIN `sys_role` role ON role.`role_key` = target.`role_key` AND role.`del_flag` = '0'
GROUP BY target.`role_key`
HAVING COUNT(role.`role_id`) <> 1
LIMIT 1;

INSERT INTO `tmp_auth010_guard` (`guard_id`)
SELECT 3
FROM `tmp_auth010_permission` desired
LEFT JOIN `sys_menu` menu ON menu.`perms` = desired.`perms` AND menu.`status` = '0'
WHERE menu.`menu_id` IS NULL
LIMIT 1;

-- Pre-change difference report. Review these result sets before production use.
SELECT role.`role_key`, menu.`menu_id`, menu.`menu_name`, menu.`perms` AS `permission_to_remove`
FROM `sys_role` role
JOIN `tmp_auth010_role` target ON target.`role_key` = role.`role_key` AND role.`del_flag` = '0'
JOIN `sys_role_menu` role_menu ON role_menu.`role_id` = role.`role_id`
JOIN `sys_menu` menu ON menu.`menu_id` = role_menu.`menu_id`
LEFT JOIN `tmp_auth010_menu` desired
       ON desired.`role_key` = role.`role_key` AND desired.`menu_id` = menu.`menu_id`
WHERE desired.`menu_id` IS NULL
ORDER BY role.`role_key`, menu.`menu_id`;

SELECT target.`role_key`, menu.`menu_id`, menu.`menu_name`, menu.`perms` AS `permission_to_add`
FROM `tmp_auth010_menu` desired
JOIN `tmp_auth010_role` target ON target.`role_key` = desired.`role_key`
JOIN `sys_menu` menu ON menu.`menu_id` = desired.`menu_id`
JOIN `sys_role` role ON role.`role_key` = target.`role_key`
LEFT JOIN `sys_role_menu` role_menu
       ON role_menu.`role_id` = role.`role_id` AND role_menu.`menu_id` = desired.`menu_id`
WHERE role_menu.`menu_id` IS NULL
ORDER BY target.`role_key`, menu.`menu_id`;

-- Missing/disabled permission menus must be investigated; expected: zero rows.
SELECT desired.`role_key`, desired.`perms` AS `missing_or_disabled_permission`
FROM `tmp_auth010_permission` desired
LEFT JOIN `sys_menu` menu ON menu.`perms` = desired.`perms` AND menu.`status` = '0'
WHERE menu.`menu_id` IS NULL
ORDER BY desired.`role_key`, desired.`perms`;

START TRANSACTION;

-- Remove grants outside the reviewed matrix only for the seven exact roles.
DELETE role_menu
FROM `sys_role_menu` role_menu
JOIN `sys_role` role ON role.`role_id` = role_menu.`role_id`
JOIN `tmp_auth010_role` target ON target.`role_key` = role.`role_key` AND role.`del_flag` = '0'
LEFT JOIN `tmp_auth010_menu` desired
       ON desired.`role_key` = role.`role_key` AND desired.`menu_id` = role_menu.`menu_id`
WHERE desired.`menu_id` IS NULL;

-- Add reviewed grants without duplicates.
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT role.`role_id`, desired.`menu_id`
FROM `tmp_auth010_menu` desired
JOIN `sys_role` role ON role.`role_key` = desired.`role_key`
WHERE role.`del_flag` = '0'
  AND NOT EXISTS (
      SELECT 1
      FROM `sys_role_menu` existing
      WHERE existing.`role_id` = role.`role_id`
        AND existing.`menu_id` = desired.`menu_id`
  );

COMMIT;

-- Post-change verification. Every anomaly query should return zero rows.
SELECT target.`role_key`, COUNT(role.`role_id`) AS `role_count`
FROM `tmp_auth010_role` target
LEFT JOIN `sys_role` role ON role.`role_key` = target.`role_key` AND role.`del_flag` = '0'
GROUP BY target.`role_key`
HAVING COUNT(role.`role_id`) <> 1;

SELECT role.`role_key`, menu.`menu_id`, menu.`menu_name`, menu.`perms` AS `unexpected_grant`
FROM `sys_role` role
JOIN `tmp_auth010_role` target ON target.`role_key` = role.`role_key` AND role.`del_flag` = '0'
JOIN `sys_role_menu` role_menu ON role_menu.`role_id` = role.`role_id`
JOIN `sys_menu` menu ON menu.`menu_id` = role_menu.`menu_id`
LEFT JOIN `tmp_auth010_menu` desired
       ON desired.`role_key` = role.`role_key` AND desired.`menu_id` = menu.`menu_id`
WHERE desired.`menu_id` IS NULL;

SELECT desired.`role_key`, desired.`menu_id` AS `missing_grant`
FROM `tmp_auth010_menu` desired
JOIN `sys_role` role ON role.`role_key` = desired.`role_key`
LEFT JOIN `sys_role_menu` role_menu
       ON role_menu.`role_id` = role.`role_id` AND role_menu.`menu_id` = desired.`menu_id`
WHERE role_menu.`menu_id` IS NULL;

SELECT role.`role_key`, menu.`perms` AS `unexpected_system_permission`
FROM `sys_role` role
JOIN `tmp_auth010_role` target ON target.`role_key` = role.`role_key` AND role.`del_flag` = '0'
JOIN `sys_role_menu` role_menu ON role_menu.`role_id` = role.`role_id`
JOIN `sys_menu` menu ON menu.`menu_id` = role_menu.`menu_id`
WHERE menu.`perms` LIKE 'system:%' OR menu.`perms` LIKE 'monitor:%';

SELECT `role_id`, `menu_id`, COUNT(*) AS `grant_count`
FROM `sys_role_menu`
GROUP BY `role_id`, `menu_id`
HAVING COUNT(*) > 1;

DROP TEMPORARY TABLE IF EXISTS `tmp_auth010_menu`;
DROP TEMPORARY TABLE IF EXISTS `tmp_auth010_guard`;
DROP TEMPORARY TABLE IF EXISTS `tmp_auth010_known_menu`;
DROP TEMPORARY TABLE IF EXISTS `tmp_auth010_removed_permission`;
DROP TEMPORARY TABLE IF EXISTS `tmp_auth010_permission`;
DROP TEMPORARY TABLE IF EXISTS `tmp_auth010_role`;
