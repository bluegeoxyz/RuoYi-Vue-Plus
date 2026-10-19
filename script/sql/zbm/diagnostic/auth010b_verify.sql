/* TASK-AUTH-010B read-only database verification. */
SET NAMES utf8mb4;

SELECT user.`user_name`, employee.`employee_no`, employee.`employment_status`,
       GROUP_CONCAT(DISTINCT role.`role_key` ORDER BY role.`role_key`) AS `roles`,
       GROUP_CONCAT(DISTINCT project.`project_code` ORDER BY project.`project_code`) AS `projects`
FROM `sys_user` user
JOIN `hr_employee` employee ON employee.`user_id` = user.`user_id` AND employee.`del_flag` = '0'
JOIN `sys_user_role` user_role ON user_role.`user_id` = user.`user_id`
JOIN `sys_role` role ON role.`role_id` = user_role.`role_id`
LEFT JOIN `pm_project_member` member ON member.`employee_id` = employee.`id`
    AND member.`member_status` = 'ACTIVE' AND member.`status` = '0' AND member.`del_flag` = '0'
LEFT JOIN `pm_project` project ON project.`id` = member.`project_id`
WHERE user.`user_id` BETWEEN 2199011000000002001 AND 2199011000000002010
GROUP BY user.`user_id`, user.`user_name`, employee.`employee_no`, employee.`employment_status`
ORDER BY user.`user_id`;

SELECT role.`role_key`, COUNT(DISTINCT menu.`perms`) AS `permission_count`
FROM `sys_role` role
JOIN `sys_role_menu` role_menu ON role_menu.`role_id` = role.`role_id`
JOIN `sys_menu` menu ON menu.`menu_id` = role_menu.`menu_id` AND menu.`perms` IS NOT NULL AND menu.`perms` <> ''
WHERE role.`role_key` IN (
    'property_customer_service', 'property_operation_manager', 'property_cashier',
    'property_finance_manager', 'property_warehouse_keeper',
    'property_warehouse_manager', 'property_project_manager'
)
GROUP BY role.`role_key`
ORDER BY role.`role_key`;

SELECT 'superadmin_test_users' AS `check_name`, COUNT(*) AS `actual`
FROM `sys_user_role` user_role
JOIN `sys_role` role ON role.`role_id` = user_role.`role_id`
WHERE user_role.`user_id` BETWEEN 2199011000000002001 AND 2199011000000002010
  AND role.`role_key` = 'superadmin'
UNION ALL
SELECT 'no_project_memberships', COUNT(*)
FROM `pm_project_member`
WHERE `employee_id` = 2199011000000003010 AND `del_flag` = '0'
UNION ALL
SELECT 'duplicate_user_role', COUNT(*)
FROM (SELECT `user_id`, `role_id` FROM `sys_user_role` GROUP BY `user_id`, `role_id` HAVING COUNT(*) > 1) duplicate_role
UNION ALL
SELECT 'duplicate_project_member', COUNT(*)
FROM (SELECT `project_id`, `employee_id` FROM `pm_project_member` WHERE `del_flag` = '0' GROUP BY `project_id`, `employee_id` HAVING COUNT(*) > 1) duplicate_member;

SELECT warehouse.`warehouse_code`, warehouse.`warehouse_scope`, project.`project_code`, warehouse.`company_id`,
       location.`location_code`, material.`material_code`, inventory.`quantity`
FROM `wm_warehouse` warehouse
LEFT JOIN `pm_project` project ON project.`id` = warehouse.`project_id`
LEFT JOIN `wm_warehouse_location` location ON location.`warehouse_id` = warehouse.`id` AND location.`del_flag` = '0'
LEFT JOIN `wm_inventory` inventory ON inventory.`warehouse_id` = warehouse.`id` AND inventory.`location_id` = location.`id`
LEFT JOIN `wm_material` material ON material.`id` = inventory.`material_id`
WHERE warehouse.`id` BETWEEN 2199011000000001201 AND 2199011000000001203
ORDER BY warehouse.`id`;
