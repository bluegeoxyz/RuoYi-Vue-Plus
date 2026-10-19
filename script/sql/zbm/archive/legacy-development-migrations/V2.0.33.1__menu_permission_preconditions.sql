/*
 * TASK-AUTH-012 - menu permission preconditions for V2.0.34.
 *
 * This migration is deliberately ordered after V2.0.33 and before V2.0.34.
 * V2.0.34 builds the role-menu target set from the current menu ancestry, so
 * chargeBinding must already be a child of charge management before V2.0.34.
 * V2.0.35 remains a compatible idempotent follow-up for environments that
 * applied the original manual execution order.
 *
 * No role grant is inserted or deleted here.
 */

SET NAMES utf8mb4;

DROP TEMPORARY TABLE IF EXISTS `tmp_auth012_guard`;
CREATE TEMPORARY TABLE `tmp_auth012_guard` (
    `guard_id` int NOT NULL PRIMARY KEY
) ENGINE=InnoDB;
INSERT INTO `tmp_auth012_guard` (`guard_id`) VALUES (1);

-- Fail closed when any required parent is absent or duplicated.
INSERT INTO `tmp_auth012_guard` (`guard_id`)
SELECT 1
WHERE (
    SELECT COUNT(*)
    FROM `sys_menu`
    WHERE `menu_id` IN (
        2609252000000000005,
        2609252000000000301,
        2609252000000000401,
        2609252000000000402
    )
) <> 4;

-- Fail closed on duplicate permissions or target menu-id collisions.
INSERT INTO `tmp_auth012_guard` (`guard_id`)
SELECT 1
WHERE EXISTS (
    SELECT `perms`
    FROM `sys_menu`
    WHERE `perms` IN (
        'property:resource:treeImport',
        'property:customer:import',
        'property:customer-relation:import'
    )
    GROUP BY `perms`
    HAVING COUNT(*) > 1
)
OR EXISTS (
    SELECT 1
    FROM `sys_menu`
    WHERE (`menu_id` = 2609251200000000011 AND `perms` <> 'property:resource:treeImport')
       OR (`menu_id` = 2609251200000000012 AND `perms` <> 'property:customer:import')
       OR (`menu_id` = 2609251200000000014 AND `perms` <> 'property:customer-relation:import')
);

-- Fail closed unless all five chargeBinding nodes are the known legacy or
-- already-migrated records. This prevents overwriting unknown manual menus.
INSERT INTO `tmp_auth012_guard` (`guard_id`)
SELECT 1
WHERE (
    SELECT COUNT(*)
    FROM `sys_menu`
    WHERE (`menu_id` = 2609252000000001821 AND `perms` = 'property:chargeBinding:list')
       OR (`menu_id` = 2609252000000001822 AND `perms` = 'property:chargeBinding:query')
       OR (`menu_id` = 2609252000000001823 AND `perms` = 'property:chargeBinding:add')
       OR (`menu_id` = 2609252000000001824 AND `perms` = 'property:chargeBinding:edit')
       OR (`menu_id` = 2609252000000001825 AND `perms` = 'property:chargeBinding:remove')
) <> 5
OR EXISTS (
    SELECT 1
    FROM `sys_menu`
    WHERE `menu_id` IN (
        2609252000000001822, 2609252000000001823,
        2609252000000001824, 2609252000000001825
    )
      AND `parent_id` NOT IN (2609252000000000303, 2609252000000001821)
)
OR EXISTS (
    SELECT 1
    FROM `sys_menu`
    WHERE `menu_id` = 2609252000000001821
      AND NOT (
          (`parent_id` = 2609252000000000303 AND `menu_type` = 'F')
          OR
          (`parent_id` = 2609252000000000005 AND `menu_type` = 'C'
           AND `path` = 'binding' AND `component` = 'property/charge/binding/index')
      )
);

START TRANSACTION;

INSERT INTO `sys_menu` (
    `menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`, `query_param`,
    `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `active_menu`, `ext`,
    `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`
)
SELECT 2609251200000000011, 2609252000000000301, '房屋资源导入', 90, '', NULL, NULL,
       'N', 'N', 'F', '0', '0', 'property:resource:treeImport', '#', NULL, NULL,
       NULL, 1, NOW(), NULL, NULL, '项目房屋树形原子导入'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `perms` = 'property:resource:treeImport'
);

INSERT INTO `sys_menu` (
    `menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`, `query_param`,
    `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `active_menu`, `ext`,
    `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`
)
SELECT 2609251200000000012, 2609252000000000401, '客户批量导入', 6, '', NULL, NULL,
       'N', 'N', 'F', '0', '0', 'property:customer:import', '#', NULL, NULL,
       NULL, 1, NOW(), NULL, NULL, 'Customer Excel 原子批量导入'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `perms` = 'property:customer:import'
);

INSERT INTO `sys_menu` (
    `menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`, `query_param`,
    `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `active_menu`, `ext`,
    `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`
)
SELECT 2609251200000000014, 2609252000000000402, '房屋客户关系批量导入', 10, '', NULL, NULL,
       'N', 'N', 'F', '0', '0', 'property:customer-relation:import', '#', NULL, NULL,
       NULL, 1, NOW(), NULL, NULL, 'RoomCustomerRelation Excel 原子批量导入'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `perms` = 'property:customer-relation:import'
);

UPDATE `sys_menu`
SET `parent_id` = 2609252000000000005,
    `menu_name` = '房屋收费设置',
    `order_num` = 25,
    `path` = 'binding',
    `component` = 'property/charge/binding/index',
    `is_frame` = 'N',
    `is_cache` = 'Y',
    `menu_type` = 'C',
    `perms` = 'property:chargeBinding:list',
    `icon` = 'house',
    `remark` = '收费管理下独立房屋收费设置页面',
    `update_time` = NOW()
WHERE `menu_id` = 2609252000000001821
  AND `perms` = 'property:chargeBinding:list';

UPDATE `sys_menu`
SET `parent_id` = 2609252000000001821,
    `update_time` = NOW()
WHERE (`menu_id` = 2609252000000001822 AND `perms` = 'property:chargeBinding:query')
   OR (`menu_id` = 2609252000000001823 AND `perms` = 'property:chargeBinding:add')
   OR (`menu_id` = 2609252000000001824 AND `perms` = 'property:chargeBinding:edit')
   OR (`menu_id` = 2609252000000001825 AND `perms` = 'property:chargeBinding:remove');

COMMIT;

DROP TEMPORARY TABLE IF EXISTS `tmp_auth012_guard`;

SELECT `menu_id`, `parent_id`, `menu_name`, `path`, `component`, `menu_type`, `perms`
FROM `sys_menu`
WHERE `perms` IN (
    'property:resource:treeImport',
    'property:customer:import',
    'property:customer-relation:import',
    'property:chargeBinding:list',
    'property:chargeBinding:query',
    'property:chargeBinding:add',
    'property:chargeBinding:edit',
    'property:chargeBinding:remove'
)
ORDER BY `menu_id`;
