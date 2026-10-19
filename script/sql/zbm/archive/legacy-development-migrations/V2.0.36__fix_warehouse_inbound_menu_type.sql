/*
 * TASK-AUTH-016-WH-DB-1 - fix the inbound page menu type drift.
 *
 * This migration changes only the confirmed legacy M record. It accepts the
 * already-fixed C state for repeat-safe manual execution and fails closed for
 * every other shape. No role-menu relation is changed.
 */

SET NAMES utf8mb4;

DROP TEMPORARY TABLE IF EXISTS `tmp_auth016_wh_db1_guard`;
CREATE TEMPORARY TABLE `tmp_auth016_wh_db1_guard` (
    `guard_id` int NOT NULL PRIMARY KEY
) ENGINE=InnoDB;
INSERT INTO `tmp_auth016_wh_db1_guard` (`guard_id`) VALUES (1);

-- The permission must be unique and the record must be either the confirmed
-- legacy M state or the exact final C state.
INSERT INTO `tmp_auth016_wh_db1_guard` (`guard_id`)
SELECT 1
WHERE (
    SELECT COUNT(*)
    FROM `sys_menu`
    WHERE `menu_id` = 2609252000000000611
      AND `parent_id` = 2609252000000000006
      AND `menu_name` = '入库管理'
      AND `path` = 'inbound'
      AND `component` = 'warehouse/inbound/index'
      AND `perms` = 'warehouse:inbound:list'
      AND `status` = '0'
      AND `visible` = '0'
      AND `menu_type` IN ('M', 'C')
) <> 1
OR (
    SELECT COUNT(*)
    FROM `sys_menu`
    WHERE `perms` = 'warehouse:inbound:list'
) <> 1;

START TRANSACTION;

UPDATE `sys_menu`
SET `menu_type` = 'C',
    `update_time` = NOW()
WHERE `menu_id` = 2609252000000000611
  AND `parent_id` = 2609252000000000006
  AND `menu_name` = '入库管理'
  AND `menu_type` = 'M'
  AND `path` = 'inbound'
  AND `component` = 'warehouse/inbound/index'
  AND `perms` = 'warehouse:inbound:list'
  AND `status` = '0'
  AND `visible` = '0';

-- Fail before COMMIT unless the exact target is now a component menu.
INSERT INTO `tmp_auth016_wh_db1_guard` (`guard_id`)
SELECT 1
WHERE (
    SELECT COUNT(*)
    FROM `sys_menu`
    WHERE `menu_id` = 2609252000000000611
      AND `parent_id` = 2609252000000000006
      AND `menu_type` = 'C'
      AND `path` = 'inbound'
      AND `component` = 'warehouse/inbound/index'
      AND `perms` = 'warehouse:inbound:list'
      AND `status` = '0'
      AND `visible` = '0'
) <> 1;

COMMIT;

DROP TEMPORARY TABLE IF EXISTS `tmp_auth016_wh_db1_guard`;

SELECT `menu_id`, `parent_id`, `menu_name`, `order_num`, `menu_type`, `path`,
       `component`, `perms`, `status`, `visible`
FROM `sys_menu`
WHERE `menu_id` = 2609252000000000611;
