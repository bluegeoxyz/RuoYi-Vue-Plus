/* Fix inbound menu orphaned by the incorrect parent id in V2.0.27. */
UPDATE `sys_menu`
SET `parent_id` = 2609252000000000006,
    `update_by` = 1,
    `update_time` = NOW()
WHERE `menu_id` = 2609252000000000611
  AND `parent_id` <> 2609252000000000006;
