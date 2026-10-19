-- TASK-015A：项目住户只读视图菜单与独立功能权限；业务范围仍复用 CUSTOMER_RELATION。
-- 当前项目 sys_menu 结构不包含 route_name，列集合与 Phase 1 基线保持一致。
INSERT INTO sys_menu (
    menu_id, parent_id, menu_name, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext,
    create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT 2609251200000000015, relation_menu.parent_id, '住户管理', 30, 'resident',
       'property/resident/index', NULL, 'N', 'Y', 'C', '0', '0',
       'property:resident:list', 'user', NULL, NULL, NULL, 1, NOW(), NULL, NULL,
       '项目级 Owner / Resident 查询视图'
FROM sys_menu relation_menu
WHERE relation_menu.perms = 'property:customerRelation:list'
  AND NOT EXISTS (SELECT 1 FROM sys_menu x WHERE x.perms = 'property:resident:list')
LIMIT 1;

INSERT INTO sys_menu (
    menu_id, parent_id, menu_name, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext,
    create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT 2609251200000000016, resident_menu.menu_id, '住户详情', 1, '', NULL, NULL,
       'N', 'N', 'F', '0', '0', 'property:resident:query', '#', NULL, NULL,
       NULL, 1, NOW(), NULL, NULL, '住户聚合详情'
FROM sys_menu resident_menu
WHERE resident_menu.perms = 'property:resident:list'
  AND NOT EXISTS (SELECT 1 FROM sys_menu x WHERE x.perms = 'property:resident:query')
LIMIT 1;

INSERT INTO sys_menu (
    menu_id, parent_id, menu_name, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon, active_menu, ext,
    create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT 2609251200000000017, resident_menu.menu_id, '住户导出', 2, '', NULL, NULL,
       'N', 'N', 'F', '0', '0', 'property:resident:export', '#', NULL, NULL,
       NULL, 1, NOW(), NULL, NULL, '住户项目视图脱敏导出'
FROM sys_menu resident_menu
WHERE resident_menu.perms = 'property:resident:list'
  AND NOT EXISTS (SELECT 1 FROM sys_menu x WHERE x.perms = 'property:resident:export')
LIMIT 1;
