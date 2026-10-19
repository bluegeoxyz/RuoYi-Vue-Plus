/*
 * ============================================================
 * ZBM Property Management - Current Baseline
 * File    : 03_menu_permission.sql
 * Version : V2.0.33
 * Purpose : Current property menus and permission buttons
 *
 * RuoYi-Vue-Plus 6.X SysMenu fields:
 * menu_id, parent_id, menu_name, order_num, path, component,
 * query_param, is_frame, is_cache, menu_type, visible, status,
 * perms, icon, active_menu, ext, audit fields, remark.
 *
 * Component path verification:
 *   Verified against the local plus-ui 6.X-Vue workspace. The frontend
 *   resolves database component values against the Vue views directory (recursive) without
 *   the src/views prefix or .vue suffix. Phase 1 pages must be created at
 *   the paths declared below.
 * ============================================================
 */

SET NAMES utf8mb4;

-- ============================================================
-- ID ranges
-- 2609252000000000001+ : directories
-- 2609252000000000100+ : pages
-- 2609252000000001000+ : buttons
-- ============================================================

-- ============================================================
-- 1. Top-level business domains
-- ============================================================

INSERT INTO `sys_menu`
(`menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`,
 `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`,
 `perms`, `icon`, `active_menu`, `ext`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609252000000000001, 0, '企业中心',  10, 'enterprise', NULL, NULL, 'N', 'N', 'M', '0', '0', NULL, 'user-filled',     NULL, NULL, NULL, 1, NOW(), NULL, NULL, 'V2 企业 HR 域'),
(2609252000000000002, 0, '项目中心',  20, 'project',    NULL, NULL, 'N', 'N', 'M', '0', '0', NULL, 'office-building', NULL, NULL, NULL, 1, NOW(), NULL, NULL, 'V2 物业项目域'),
(2609252000000000003, 0, '资源中心',  30, 'resource',   NULL, NULL, 'N', 'N', 'M', '0', '0', NULL, 'house',           NULL, NULL, NULL, 1, NOW(), NULL, NULL, 'V2 物业资源域'),
(2609252000000000004, 0, '客户中心',  40, 'customer',   NULL, NULL, 'N', 'N', 'M', '0', '0', NULL, 'user',            NULL, NULL, NULL, 1, NOW(), NULL, NULL, 'V2 物业客户域')
ON DUPLICATE KEY UPDATE
    `menu_name` = VALUES(`menu_name`),
    `order_num` = VALUES(`order_num`),
    `path`      = VALUES(`path`),
    `icon`      = VALUES(`icon`),
    `remark`    = VALUES(`remark`);

-- ============================================================
-- 2. Page menus
-- ============================================================

INSERT INTO `sys_menu`
(`menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`,
 `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`,
 `perms`, `icon`, `active_menu`, `ext`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
-- Enterprise / HR
(2609252000000000101, 2609252000000000001, '员工管理', 10, 'employee', 'hr/employee/index',
 NULL, 'N', 'Y', 'C', '0', '0', 'hr:employee:list', 'peoples', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '员工档案'),

-- Project
(2609252000000000201, 2609252000000000002, '项目管理', 10, 'project', 'property/project/index',
 NULL, 'N', 'Y', 'C', '0', '0', 'property:project:list', 'office-building', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '物业项目管理'),
(2609252000000000202, 2609252000000000002, '项目成员', 20, 'member', 'property/project/member/index',
 NULL, 'N', 'Y', 'C', '0', '0', 'property:projectMember:list', 'user-filled', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '物业项目成员管理'),

-- Resource
(2609252000000000301, 2609252000000000003, '楼栋管理', 10, 'building', 'property/resource/building/index',
 NULL, 'N', 'Y', 'C', '0', '0', 'property:building:list', 'office-building', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '楼栋管理'),
(2609252000000000302, 2609252000000000003, '单元管理', 20, 'unit', 'property/resource/unit/index',
 NULL, 'N', 'Y', 'C', '0', '0', 'property:unit:list', 'connection', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '楼栋单元管理；Java Entity = BuildingUnit'),
(2609252000000000303, 2609252000000000003, '房屋管理', 30, 'room', 'property/resource/room/index',
 NULL, 'N', 'Y', 'C', '0', '0', 'property:room:list', 'house', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '房屋管理'),

-- Customer
(2609252000000000401, 2609252000000000004, '客户档案', 10, 'customer', 'property/customer/index',
 NULL, 'N', 'Y', 'C', '0', '0', 'property:customer:list', 'user', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '企业级客户主数据'),
(2609252000000000402, 2609252000000000004, '房客关系', 20, 'relation', 'property/customer/relation/index',
 NULL, 'N', 'Y', 'C', '0', '0', 'property:customerRelation:list', 'link', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '房屋客户关系 / 迁入迁出')
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `menu_name` = VALUES(`menu_name`),
    `order_num` = VALUES(`order_num`),
    `path`      = VALUES(`path`),
    `component` = VALUES(`component`),
    `perms`     = VALUES(`perms`),
    `icon`      = VALUES(`icon`),
    `remark`    = VALUES(`remark`);


-- ============================================================
-- 3. Button permissions
-- Standard: query / add / edit / remove / export
-- ============================================================

INSERT INTO `sys_menu`
(`menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`,
 `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`,
 `perms`, `icon`, `active_menu`, `ext`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES

-- Employee
(2609252000000001001, 2609252000000000101, '员工查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'hr:employee:query',  '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001002, 2609252000000000101, '员工新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'hr:employee:add',    '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001003, 2609252000000000101, '员工修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'hr:employee:edit',   '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001004, 2609252000000000101, '员工删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'hr:employee:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001005, 2609252000000000101, '员工导出', 5, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'hr:employee:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001006, 2609252000000000101, '账号绑定', 6, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'hr:employee:bindUser','#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),

-- Project
(2609252000000001101, 2609252000000000201, '项目查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:project:query',  '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001102, 2609252000000000201, '项目新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:project:add',    '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001103, 2609252000000000201, '项目修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:project:edit',   '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001104, 2609252000000000201, '项目删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:project:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001105, 2609252000000000201, '项目导出', 5, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:project:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),

-- Project Member
(2609252000000001201, 2609252000000000202, '成员查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:projectMember:query',  '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001202, 2609252000000000202, '成员新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:projectMember:add',    '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001203, 2609252000000000202, '成员修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:projectMember:edit',   '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001204, 2609252000000000202, '成员移除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:projectMember:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001205, 2609252000000000202, '成员导出', 5, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:projectMember:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),

-- Building
(2609252000000001301, 2609252000000000301, '楼栋查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:building:query',  '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001302, 2609252000000000301, '楼栋新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:building:add',    '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001303, 2609252000000000301, '楼栋修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:building:edit',   '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001304, 2609252000000000301, '楼栋删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:building:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001305, 2609252000000000301, '楼栋导出', 5, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:building:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),

-- BuildingUnit
(2609252000000001401, 2609252000000000302, '单元查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:unit:query',  '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001402, 2609252000000000302, '单元新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:unit:add',    '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001403, 2609252000000000302, '单元修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:unit:edit',   '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001404, 2609252000000000302, '单元删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:unit:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001405, 2609252000000000302, '单元导出', 5, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:unit:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),

-- Room
(2609252000000001501, 2609252000000000303, '房屋查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:room:query',  '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001502, 2609252000000000303, '房屋新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:room:add',    '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001503, 2609252000000000303, '房屋修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:room:edit',   '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001504, 2609252000000000303, '房屋删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:room:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001505, 2609252000000000303, '房屋导出', 5, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:room:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),

-- Customer
(2609252000000001601, 2609252000000000401, '客户查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customer:query',  '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001602, 2609252000000000401, '客户新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customer:add',    '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001603, 2609252000000000401, '客户修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customer:edit',   '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001604, 2609252000000000401, '客户删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customer:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001605, 2609252000000000401, '客户导出', 5, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customer:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),

-- RoomCustomerRelation
(2609252000000001701, 2609252000000000402, '关系查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customerRelation:query',   '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001702, 2609252000000000402, '关系新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customerRelation:add',     '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001703, 2609252000000000402, '关系修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customerRelation:edit',    '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001704, 2609252000000000402, '关系删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customerRelation:remove',  '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001705, 2609252000000000402, '关系导出', 5, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customerRelation:export',  '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001706, 2609252000000000402, '客户迁入', 6, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customerRelation:moveIn',  '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001707, 2609252000000000402, '客户迁出', 7, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customerRelation:moveOut', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL)

ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `menu_name` = VALUES(`menu_name`),
    `order_num` = VALUES(`order_num`),
    `perms`     = VALUES(`perms`),
    `remark`    = VALUES(`remark`);

-- ============================================================
-- 4. Post-V2.0.0 Final Menus And Permissions
-- ============================================================

INSERT INTO `sys_menu`
(`menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`,
 `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`,
 `perms`, `icon`, `active_menu`, `ext`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
-- Project management grants
(2609252000000000203, 2609252000000000002, '项目管理授权', 30, 'admin-scope', 'property/project/admin-scope/index', NULL, 'N', 'Y', 'C', '0', '0', 'property:projectAdminScope:list', 'lock', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '企业项目管理范围授权'),
(2609252000000001206, 2609252000000000203, '授权查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:projectAdminScope:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001207, 2609252000000000203, '授权新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:projectAdminScope:add', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001208, 2609252000000000203, '授权修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:projectAdminScope:edit', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001209, 2609252000000000203, '授权撤销', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:projectAdminScope:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),

-- Customer security and grants
(2609252000000001606, 2609252000000000401, '敏感资料查看', 6, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customer:pii:view', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '完整客户敏感字段查看'),
(2609252000000001607, 2609252000000000401, '敏感资料导出', 7, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customer:pii:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '客户敏感字段导出'),
(2609252000000001611, 2609252000000000401, '客户授权列表', 11, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customerGrant:list', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '企业客户主档授权列表'),
(2609252000000001612, 2609252000000000401, '客户授权查询', 12, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customerGrant:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '企业客户主档授权历史'),
(2609252000000001613, 2609252000000000401, '客户授权新增', 13, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customerGrant:add', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '企业客户主档授权新增'),
(2609252000000001614, 2609252000000000401, '客户授权修改', 14, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customerGrant:edit', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '企业客户主档授权启停'),
(2609252000000001615, 2609252000000000401, '客户授权撤销', 15, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customerGrant:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '企业客户主档授权撤销'),

-- Atomic imports
(2609251200000000011, 2609252000000000301, '房屋资源导入', 90, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:resource:treeImport', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '项目房屋树形原子导入'),
(2609251200000000012, 2609252000000000401, '客户批量导入', 6, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customer:import', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, 'Customer Excel 原子批量导入'),
(2609251200000000014, 2609252000000000402, '房屋客户关系批量导入', 10, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:customer-relation:import', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, 'RoomCustomerRelation Excel 原子批量导入'),

-- Resident view
(2609251200000000015, 2609252000000000004, '住户管理', 30, 'resident', 'property/resident/index', NULL, 'N', 'Y', 'C', '0', '0', 'property:resident:list', 'user', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '项目级 Owner / Resident 查询视图'),
(2609251200000000016, 2609251200000000015, '住户详情', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:resident:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '住户聚合详情'),
(2609251200000000017, 2609251200000000015, '住户导出', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:resident:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '住户项目视图脱敏导出'),

-- Charge management
(2609252000000000005, 0, '收费管理', 50, 'charge', NULL, NULL, 'N', 'N', 'M', '0', '0', NULL, 'money', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '物业收费业务域'),
(2609252000000000501, 2609252000000000005, '收费项目', 10, 'item', 'property/charge/item/index', NULL, 'N', 'Y', 'C', '0', '0', 'property:chargeItem:list', 'list', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '项目级收费项目管理'),
(2609252000000001801, 2609252000000000501, '收费项目查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeItem:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001802, 2609252000000000501, '收费项目新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeItem:add', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001803, 2609252000000000501, '收费项目修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeItem:edit', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001804, 2609252000000000501, '收费项目删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeItem:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001805, 2609252000000000501, '收费项目导出', 5, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeItem:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000000502, 2609252000000000005, '计费规则', 20, 'rule', 'property/charge/rule/index', NULL, 'N', 'Y', 'C', '0', '0', 'property:chargeRule:list', 'list', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '项目级周期性计费规则管理'),
(2609252000000001811, 2609252000000000502, '计费规则查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeRule:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001812, 2609252000000000502, '计费规则新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeRule:add', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001813, 2609252000000000502, '计费规则修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeRule:edit', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001814, 2609252000000000502, '计费规则删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeRule:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001815, 2609252000000000502, '计费规则导出', 5, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeRule:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001821, 2609252000000000005, '房屋收费设置', 25, 'binding', 'property/charge/binding/index', NULL, 'N', 'Y', 'C', '0', '0', 'property:chargeBinding:list', 'house', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '收费管理下独立房屋收费设置页面'),
(2609252000000001822, 2609252000000001821, '房屋收费设置查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeBinding:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '房屋收费设置详情'),
(2609252000000001823, 2609252000000001821, '房屋收费设置新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeBinding:add', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '新增房屋收费设置'),
(2609252000000001824, 2609252000000001821, '房屋收费设置修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeBinding:edit', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '终止、启停及维护房屋收费设置'),
(2609252000000001825, 2609252000000001821, '房屋收费设置删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeBinding:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '删除未产生应收的房屋收费设置')
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `menu_name` = VALUES(`menu_name`),
    `order_num` = VALUES(`order_num`),
    `path`      = VALUES(`path`),
    `component` = VALUES(`component`),
    `perms`     = VALUES(`perms`),
    `icon`      = VALUES(`icon`),
    `remark`    = VALUES(`remark`);

-- Receivable management: financial records are cancelled, not deleted.
INSERT INTO `sys_menu`
(`menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`,
 `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`,
 `perms`, `icon`, `active_menu`, `ext`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609252000000000503, 2609252000000000005, '应收管理', 30, 'receivable', 'property/charge/receivable/index', NULL, 'N', 'Y', 'C', '0', '0', 'property:receivable:list', 'money', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '项目级应收与生成批次管理'),
(2609252000000001831, 2609252000000000503, '应收查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:receivable:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001832, 2609252000000000503, '应收生成', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:receivable:generate', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001833, 2609252000000000503, '应收作废', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:receivable:cancel', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001834, 2609252000000000503, '应收导出', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:receivable:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`), `menu_name` = VALUES(`menu_name`), `order_num` = VALUES(`order_num`),
    `path` = VALUES(`path`), `component` = VALUES(`component`), `perms` = VALUES(`perms`),
    `icon` = VALUES(`icon`), `remark` = VALUES(`remark`);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`menu_name`,`order_num`,`path`,`component`,`query_param`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`active_menu`,`ext`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
(2609252000000000612,2609252000000000006,'出库管理',40,'outbound','warehouse/outbound/index',NULL,'N','Y','C','0','0','warehouse:outbound:list','upload',NULL,NULL,NULL,1,NOW(),NULL,NULL,'出库业务闭环'),
(2609252000000002281,2609252000000000612,'出库查询',1,'',NULL,NULL,'N','N','F','0','0','warehouse:outbound:query','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002282,2609252000000000612,'出库新增',2,'',NULL,NULL,'N','N','F','0','0','warehouse:outbound:add','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002283,2609252000000000612,'出库修改',3,'',NULL,NULL,'N','N','F','0','0','warehouse:outbound:edit','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002284,2609252000000000612,'出库删除',4,'',NULL,NULL,'N','N','F','0','0','warehouse:outbound:remove','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002285,2609252000000000612,'出库确认',5,'',NULL,NULL,'N','N','F','0','0','warehouse:outbound:confirm','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002286,2609252000000000612,'完成出库',6,'',NULL,NULL,'N','N','F','0','0','warehouse:outbound:complete','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002287,2609252000000000612,'取消出库',7,'',NULL,NULL,'N','N','F','0','0','warehouse:outbound:cancel','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002288,2609252000000000612,'出库导出',8,'',NULL,NULL,'N','N','F','0','0','warehouse:outbound:export','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL) ON DUPLICATE KEY UPDATE `parent_id`=VALUES(`parent_id`),`menu_name`=VALUES(`menu_name`),`component`=VALUES(`component`),`perms`=VALUES(`perms`);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`menu_name`,`order_num`,`path`,`component`,`query_param`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`active_menu`,`ext`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
(2609252000000000611,2609252000000000006,'入库管理',30,'inbound','warehouse/inbound/index',NULL,'N','Y','C','0','0','warehouse:inbound:list','download',NULL,NULL,NULL,1,NOW(),NULL,NULL,'入库业务闭环'),
(2609252000000002273,2609252000000000611,'入库查询',1,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:query','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002274,2609252000000000611,'入库新增',2,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:add','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002275,2609252000000000611,'入库修改',3,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:edit','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002276,2609252000000000611,'入库删除',4,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:remove','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002277,2609252000000000611,'入库确认',5,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:confirm','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002278,2609252000000000611,'完成入库',6,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:complete','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002279,2609252000000000611,'取消入库',7,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:cancel','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002280,2609252000000000611,'入库导出',8,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:export','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL)
ON DUPLICATE KEY UPDATE `parent_id`=VALUES(`parent_id`),`menu_name`=VALUES(`menu_name`),`component`=VALUES(`component`),`perms`=VALUES(`perms`);
-- Warehouse management.
INSERT INTO `sys_menu`
(`menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`,
 `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`,
 `perms`, `icon`, `active_menu`, `ext`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609252000000000006, 0, '仓库管理', 60, 'warehouse', NULL, NULL, 'N', 'N', 'M', '0', '0', NULL, 'box', NULL, NULL, NULL, 1, NOW(), NULL, NULL, 'Warehouse Phase 1'),
(2609252000000000601, 2609252000000000006, '基础资料', 10, 'base', NULL, NULL, 'N', 'N', 'M', '0', '0', NULL, 'list', NULL, NULL, NULL, 1, NOW(), NULL, NULL, 'Warehouse 基础资料'),
(2609252000000000602, 2609252000000000601, '仓库管理', 10, 'warehouse', 'warehouse/warehouse/index', NULL, 'N', 'Y', 'C', '0', '0', 'warehouse:warehouse:list', 'box', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '仓库主数据'),
(2609252000000000603, 2609252000000000601, '库区管理', 20, 'zone', 'warehouse/zone/index', NULL, 'N', 'Y', 'C', '0', '0', 'warehouse:zone:list', 'grid', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '仓库库区'),
(2609252000000000604, 2609252000000000601, '库位管理', 30, 'location', 'warehouse/location/index', NULL, 'N', 'Y', 'C', '0', '0', 'warehouse:location:list', 'location', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '仓库库位'),
(2609252000000002201, 2609252000000000602, '仓库查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:warehouse:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002202, 2609252000000000602, '仓库新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:warehouse:add', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002203, 2609252000000000602, '仓库修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:warehouse:edit', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002204, 2609252000000000602, '仓库删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:warehouse:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002211, 2609252000000000603, '库区查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:zone:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002212, 2609252000000000603, '库区新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:zone:add', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002213, 2609252000000000603, '库区修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:zone:edit', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002214, 2609252000000000603, '库区删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:zone:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002221, 2609252000000000604, '库位查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:location:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002222, 2609252000000000604, '库位新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:location:add', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002223, 2609252000000000604, '库位修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:location:edit', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002224, 2609252000000000604, '库位删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:location:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000000605, 2609252000000000601, '物料分类', 40, 'materialCategory', 'warehouse/materialCategory/index', NULL, 'N', 'Y', 'C', '0', '0', 'warehouse:materialCategory:list', 'tree', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '公司级物料分类'),
(2609252000000000606, 2609252000000000601, '计量单位', 50, 'unit', 'warehouse/unit/index', NULL, 'N', 'Y', 'C', '0', '0', 'warehouse:unit:list', 'component', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '平台共享计量单位'),
(2609252000000000607, 2609252000000000601, '物料管理', 60, 'material', 'warehouse/material/index', NULL, 'N', 'Y', 'C', '0', '0', 'warehouse:material:list', 'shopping', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '公司级物料主数据'),
(2609252000000002231, 2609252000000000605, '分类查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:materialCategory:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002232, 2609252000000000605, '分类新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:materialCategory:add', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002233, 2609252000000000605, '分类修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:materialCategory:edit', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002234, 2609252000000000605, '分类删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:materialCategory:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002241, 2609252000000000606, '单位查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:unit:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002242, 2609252000000000606, '单位新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:unit:add', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002243, 2609252000000000606, '单位修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:unit:edit', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002244, 2609252000000000606, '单位删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:unit:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002251, 2609252000000000607, '物料查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:material:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002252, 2609252000000000607, '物料新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:material:add', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002253, 2609252000000000607, '物料修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:material:edit', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002254, 2609252000000000607, '物料删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:material:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000000608, 2609252000000000006, '库存管理', 20, 'inventory', NULL, NULL, 'N', 'N', 'M', '0', '0', NULL, 'database', NULL, NULL, NULL, 1, NOW(), NULL, NULL, 'Warehouse 库存管理'),
(2609252000000000609, 2609252000000000608, '实时库存', 10, 'realtime', 'warehouse/inventory/index', NULL, 'N', 'Y', 'C', '0', '0', 'warehouse:inventory:list', 'list', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '只读库位库存余额'),
(2609252000000002261, 2609252000000000609, '库存详情', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:inventory:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002262, 2609252000000000609, '库存导出', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:inventory:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000000610, 2609252000000000608, '库存流水', 20, 'transaction', 'warehouse/inventoryTransaction/index', NULL, 'N', 'Y', 'C', '0', '0', 'warehouse:inventoryTransaction:list', 'time', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '不可变库存流水查询'),
(2609252000000002271, 2609252000000000610, '流水查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:inventoryTransaction:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000002272, 2609252000000000610, '流水导出', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:inventoryTransaction:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`), `menu_name` = VALUES(`menu_name`), `order_num` = VALUES(`order_num`),
    `path` = VALUES(`path`), `component` = VALUES(`component`), `perms` = VALUES(`perms`),
    `icon` = VALUES(`icon`), `remark` = VALUES(`remark`);


-- Payment management: completed financial records have no ordinary edit/remove permission.
INSERT INTO `sys_menu`
(`menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`,
 `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`,
 `perms`, `icon`, `active_menu`, `ext`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609252000000000504, 2609252000000000005, '收款管理', 40, 'payment', 'property/charge/payment/index', NULL, 'N', 'Y', 'C', '0', '0', 'property:payment:list', 'money', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '项目级收款与核销管理'),
(2609252000000001841, 2609252000000000504, '收款查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:payment:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001842, 2609252000000000504, '收款新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:payment:add', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001843, 2609252000000000504, '收款核销', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:payment:allocate', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001844, 2609252000000000504, '收款导出', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:payment:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001845, 2609252000000000504, '收款冲销', 5, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:payment:reverse', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '整笔收款冲销与全部核销回退')
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`), `menu_name` = VALUES(`menu_name`), `order_num` = VALUES(`order_num`),
    `path` = VALUES(`path`), `component` = VALUES(`component`), `perms` = VALUES(`perms`),
    `icon` = VALUES(`icon`), `remark` = VALUES(`remark`);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`menu_name`,`order_num`,`path`,`component`,`query_param`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`active_menu`,`ext`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES (2609252000000000613,2609252000000000006,'调拨管理',50,'transfer','warehouse/transfer/index',NULL,'N','Y','C','0','0','warehouse:transfer:list','switch',NULL,NULL,NULL,1,NOW(),NULL,NULL,'调拨业务闭环'),(2609252000000002289,2609252000000000613,'调拨查询',1,'',NULL,NULL,'N','N','F','0','0','warehouse:transfer:query','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002290,2609252000000000613,'调拨新增',2,'',NULL,NULL,'N','N','F','0','0','warehouse:transfer:add','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002291,2609252000000000613,'调拨修改',3,'',NULL,NULL,'N','N','F','0','0','warehouse:transfer:edit','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002292,2609252000000000613,'调拨删除',4,'',NULL,NULL,'N','N','F','0','0','warehouse:transfer:remove','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002293,2609252000000000613,'调拨确认',5,'',NULL,NULL,'N','N','F','0','0','warehouse:transfer:confirm','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002294,2609252000000000613,'完成调拨',6,'',NULL,NULL,'N','N','F','0','0','warehouse:transfer:complete','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002295,2609252000000000613,'取消调拨',7,'',NULL,NULL,'N','N','F','0','0','warehouse:transfer:cancel','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002296,2609252000000000613,'调拨导出',8,'',NULL,NULL,'N','N','F','0','0','warehouse:transfer:export','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL) ON DUPLICATE KEY UPDATE `parent_id`=VALUES(`parent_id`),`menu_name`=VALUES(`menu_name`),`component`=VALUES(`component`),`perms`=VALUES(`perms`);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`menu_name`,`order_num`,`path`,`component`,`query_param`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`active_menu`,`ext`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES (2609252000000000614,2609252000000000006,'盘点管理',60,'stocktake','warehouse/stocktake/index',NULL,'N','Y','C','0','0','warehouse:stocktake:list','clipboard',NULL,NULL,NULL,1,NOW(),NULL,NULL,'库存盘点闭环'),(2609252000000002297,2609252000000000614,'盘点查询',1,'',NULL,NULL,'N','N','F','0','0','warehouse:stocktake:query','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002298,2609252000000000614,'盘点新增',2,'',NULL,NULL,'N','N','F','0','0','warehouse:stocktake:add','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002299,2609252000000000614,'盘点修改',3,'',NULL,NULL,'N','N','F','0','0','warehouse:stocktake:edit','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002300,2609252000000000614,'盘点删除',4,'',NULL,NULL,'N','N','F','0','0','warehouse:stocktake:remove','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002301,2609252000000000614,'盘点确认',5,'',NULL,NULL,'N','N','F','0','0','warehouse:stocktake:confirm','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002302,2609252000000000614,'完成盘点',6,'',NULL,NULL,'N','N','F','0','0','warehouse:stocktake:complete','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002303,2609252000000000614,'取消盘点',7,'',NULL,NULL,'N','N','F','0','0','warehouse:stocktake:cancel','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002304,2609252000000000614,'盘点导出',8,'',NULL,NULL,'N','N','F','0','0','warehouse:stocktake:export','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL) ON DUPLICATE KEY UPDATE `parent_id`=VALUES(`parent_id`),`component`=VALUES(`component`),`perms`=VALUES(`perms`);
