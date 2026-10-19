-- Customer Excel 原子导入独立权限，不继承 customer:add。
INSERT INTO sys_menu (
    menu_id, parent_id, menu_name, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext,
    create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT 2609251200000000012, m.parent_id, '客户批量导入', 6, '', NULL, NULL,
       'N', 'N', 'F', '0', '0', 'property:customer:import', '#', NULL, NULL,
       NULL, 1, NOW(), NULL, NULL, 'Customer Excel 原子批量导入'
FROM sys_menu m
WHERE m.perms = 'property:customer:add'
  AND NOT EXISTS (SELECT 1 FROM sys_menu x WHERE x.perms = 'property:customer:import')
LIMIT 1;
