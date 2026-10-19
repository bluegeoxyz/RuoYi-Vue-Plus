-- 项目房屋树形原子导入独立权限。请按目标环境实际资源菜单 ID 调整 parent_id。
INSERT INTO sys_menu (
    menu_id, parent_id, menu_name, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext,
    create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT 2609251200000000011, m.menu_id, '房屋资源导入', 90, '', NULL, NULL,
       'N', 'N', 'F', '0', '0', 'property:resource:treeImport', '#', NULL, NULL,
       NULL, 1, NOW(), NULL, NULL, '项目房屋树形原子导入'
FROM sys_menu m
WHERE m.perms = 'property:building:list'
  AND NOT EXISTS (SELECT 1 FROM sys_menu x WHERE x.perms = 'property:resource:treeImport')
LIMIT 1;
