SELECT DATABASE() AS database_name, @@hostname AS mysql_hostname, VERSION() AS mysql_version;

SELECT menu_id, parent_id, menu_name, order_num, menu_type, path, component, perms, status, visible
FROM sys_menu
WHERE perms = 'warehouse:inbound:list'
   OR parent_id = 2609252000000000611
   OR menu_id IN (2609252000000000611, 2609252000000000006, 2609252000000000600)
ORDER BY parent_id, order_num, menu_id;

SELECT perms, COUNT(*) AS permission_count
FROM sys_menu
WHERE perms = 'warehouse:inbound:list'
GROUP BY perms;

SELECT COUNT(*) AS inbound_role_menu_count
FROM sys_role_menu rm
JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.perms = 'warehouse:inbound:list';
