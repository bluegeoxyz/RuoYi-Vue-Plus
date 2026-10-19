-- 房屋客户关系 Excel 原子导入独立权限，不继承 customer-relation:add。
INSERT INTO sys_menu (
    menu_id, parent_id, menu_name, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext,
    create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT 2609251200000000014, m.parent_id, '房屋客户关系批量导入', 10, '', NULL, NULL,
       'N', 'N', 'F', '0', '0', 'property:customer-relation:import', '#', NULL, NULL,
       NULL, 1, NOW(), NULL, NULL, 'RoomCustomerRelation Excel 原子批量导入'
FROM sys_menu m
WHERE m.perms = 'property:customerRelation:add'
  AND NOT EXISTS (SELECT 1 FROM sys_menu x WHERE x.perms = 'property:customer-relation:import')
LIMIT 1;
