SELECT DATABASE() AS database_name, @@hostname AS mysql_hostname, VERSION() AS mysql_version;

SELECT menu_id, parent_id, menu_name, order_num, menu_type, path, component, perms, status, visible, update_time
FROM sys_menu
WHERE perms = 'warehouse:inbound:list'
   OR parent_id = 2609252000000000611
   OR menu_id IN (2609252000000000611, 2609252000000000006)
ORDER BY parent_id, order_num, menu_id;

SELECT perms, COUNT(*) AS permission_count
FROM sys_menu
WHERE perms = 'warehouse:inbound:list'
GROUP BY perms;

SELECT r.role_key, r.status AS role_status,
       MAX(CASE WHEN m.perms = 'warehouse:inbound:list' THEN 1 ELSE 0 END) AS has_inbound_page,
       MAX(CASE WHEN m.menu_id = 2609252000000000006 THEN 1 ELSE 0 END) AS has_warehouse_parent
FROM sys_role r
LEFT JOIN sys_role_menu rm ON rm.role_id = r.role_id
LEFT JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE r.role_key IN ('property_warehouse_keeper', 'property_warehouse_manager')
GROUP BY r.role_id, r.role_key, r.status
ORDER BY r.role_key;

SELECT u.user_name, u.status AS user_status, r.role_key, r.status AS role_status
FROM sys_user u
JOIN sys_user_role ur ON ur.user_id = u.user_id
JOIN sys_role r ON r.role_id = ur.role_id
WHERE u.user_name = 'admin'
ORDER BY r.role_key;

SELECT COUNT(*) AS duplicate_role_menu_relations
FROM (
    SELECT role_id, menu_id
    FROM sys_role_menu
    GROUP BY role_id, menu_id
    HAVING COUNT(*) > 1
) duplicated;
