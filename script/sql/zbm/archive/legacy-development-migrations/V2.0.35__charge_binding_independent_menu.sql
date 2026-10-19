/*
 * TASK-AUTH-011 - Move house charge binding from the room-management menu to
 * an independent page under charge management. No role grant is changed here.
 *
 * Compatibility follow-up. New installations must apply
 * V2.0.33.1__menu_permission_preconditions.sql before V2.0.34. This script
 * remains idempotent for environments following the earlier manual order.
 */

SET NAMES utf8mb4;

START TRANSACTION;

INSERT INTO `sys_menu`
(`menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`,
 `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`,
 `perms`, `icon`, `active_menu`, `ext`, `create_dept`, `create_by`,
 `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609252000000001821, 2609252000000000005, '房屋收费设置', 25, 'binding',
 'property/charge/binding/index', NULL, 'N', 'Y', 'C', '0', '0',
 'property:chargeBinding:list', 'house', NULL, NULL, NULL, 1, NOW(), NULL, NULL,
 '收费管理下独立房屋收费设置页面'),
(2609252000000001822, 2609252000000001821, '房屋收费设置查询', 1, '', NULL,
 NULL, 'N', 'N', 'F', '0', '0', 'property:chargeBinding:query', '#', NULL, NULL,
 NULL, 1, NOW(), NULL, NULL, '房屋收费设置详情'),
(2609252000000001823, 2609252000000001821, '房屋收费设置新增', 2, '', NULL,
 NULL, 'N', 'N', 'F', '0', '0', 'property:chargeBinding:add', '#', NULL, NULL,
 NULL, 1, NOW(), NULL, NULL, '新增房屋收费设置'),
(2609252000000001824, 2609252000000001821, '房屋收费设置修改', 3, '', NULL,
 NULL, 'N', 'N', 'F', '0', '0', 'property:chargeBinding:edit', '#', NULL, NULL,
 NULL, 1, NOW(), NULL, NULL, '终止、启停及维护房屋收费设置'),
(2609252000000001825, 2609252000000001821, '房屋收费设置删除', 4, '', NULL,
 NULL, 'N', 'N', 'F', '0', '0', 'property:chargeBinding:remove', '#', NULL, NULL,
 NULL, 1, NOW(), NULL, NULL, '删除未产生应收的房屋收费设置')
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `menu_name` = VALUES(`menu_name`),
    `order_num` = VALUES(`order_num`),
    `path` = VALUES(`path`),
    `component` = VALUES(`component`),
    `is_cache` = VALUES(`is_cache`),
    `menu_type` = VALUES(`menu_type`),
    `visible` = VALUES(`visible`),
    `status` = VALUES(`status`),
    `perms` = VALUES(`perms`),
    `icon` = VALUES(`icon`),
    `remark` = VALUES(`remark`),
    `update_time` = NOW();

COMMIT;

-- Expected: one C menu under charge management and four child buttons.
SELECT `menu_id`, `parent_id`, `menu_name`, `path`, `component`, `menu_type`, `perms`
FROM `sys_menu`
WHERE `menu_id` IN (
    2609252000000001821, 2609252000000001822, 2609252000000001823,
    2609252000000001824, 2609252000000001825
)
ORDER BY `menu_id`;
