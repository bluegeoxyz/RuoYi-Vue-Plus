/*
 * Warehouse Phase 1: warehouse / zone / location schema, dictionary and permissions.
 * Previous version: V2.0.22
 */
SET NAMES utf8mb4;

CREATE TABLE `wm_warehouse`
(
    `id` BIGINT NOT NULL COMMENT '仓库ID',
    `company_id` BIGINT NOT NULL COMMENT '所属公司/组织部门ID',
    `project_id` BIGINT DEFAULT NULL COMMENT '所属物业项目ID',
    `warehouse_scope` VARCHAR(16) NOT NULL COMMENT '归属类型（COMPANY/PROJECT）',
    `warehouse_code` VARCHAR(64) NOT NULL COMMENT '仓库编码',
    `warehouse_name` VARCHAR(128) NOT NULL COMMENT '仓库名称',
    `manager_id` BIGINT DEFAULT NULL COMMENT '负责人用户ID',
    `contact_phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '仓库地址',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门', `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间', `update_by` BIGINT DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间', `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_code_key` VARCHAR(160) GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN CONCAT(`company_id`, '#', UPPER(`warehouse_code`)) ELSE NULL END) STORED COMMENT '未删除仓库编码唯一键',
    PRIMARY KEY (`id`), UNIQUE KEY `uk_wm_warehouse_active_code` (`active_code_key`),
    KEY `idx_wm_warehouse_company` (`company_id`, `status`, `del_flag`), KEY `idx_wm_warehouse_project` (`project_id`, `status`, `del_flag`),
    CONSTRAINT `chk_wm_warehouse_scope` CHECK ((`warehouse_scope` = 'COMPANY' AND `project_id` IS NULL) OR (`warehouse_scope` = 'PROJECT' AND `project_id` IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库主数据表';

CREATE TABLE `wm_warehouse_zone`
(
    `id` BIGINT NOT NULL COMMENT '库区ID', `warehouse_id` BIGINT NOT NULL COMMENT '所属仓库ID',
    `zone_code` VARCHAR(64) NOT NULL COMMENT '库区编码', `zone_name` VARCHAR(128) NOT NULL COMMENT '库区名称',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）', `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门', `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间', `update_by` BIGINT DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间', `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_code_key` VARCHAR(160) GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN CONCAT(`warehouse_id`, '#', UPPER(`zone_code`)) ELSE NULL END) STORED COMMENT '未删除库区编码唯一键',
    PRIMARY KEY (`id`), UNIQUE KEY `uk_wm_zone_active_code` (`active_code_key`),
    KEY `idx_wm_zone_warehouse` (`warehouse_id`, `status`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库库区表';

CREATE TABLE `wm_warehouse_location`
(
    `id` BIGINT NOT NULL COMMENT '库位ID', `warehouse_id` BIGINT NOT NULL COMMENT '所属仓库ID', `zone_id` BIGINT NOT NULL COMMENT '所属库区ID',
    `location_code` VARCHAR(64) NOT NULL COMMENT '库位编码', `location_name` VARCHAR(128) NOT NULL COMMENT '库位名称',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）', `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门', `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间', `update_by` BIGINT DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间', `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_code_key` VARCHAR(160) GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN CONCAT(`warehouse_id`, '#', UPPER(`location_code`)) ELSE NULL END) STORED COMMENT '未删除库位编码唯一键',
    PRIMARY KEY (`id`), UNIQUE KEY `uk_wm_location_active_code` (`active_code_key`),
    KEY `idx_wm_location_warehouse` (`warehouse_id`, `status`, `del_flag`), KEY `idx_wm_location_zone` (`zone_id`, `status`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库库位表';

INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (2609251000000000050, '仓库-归属类型', 'wm_warehouse_scope', NULL, 1, NOW(), NULL, NULL, 'Warehouse Phase 1')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`), `remark` = VALUES(`remark`);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609251100000000230, 1, '公司/组织仓', 'COMPANY', 'wm_warehouse_scope', NULL, 'primary', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000231, 2, '项目仓', 'PROJECT', 'wm_warehouse_scope', NULL, 'success', 'Y', NULL, 1, NOW(), NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_sort` = VALUES(`dict_sort`), `dict_label` = VALUES(`dict_label`), `list_class` = VALUES(`list_class`);

INSERT INTO `sys_menu`
(`menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `active_menu`, `ext`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
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
(2609252000000002224, 2609252000000000604, '库位删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'warehouse:location:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE `parent_id` = VALUES(`parent_id`), `menu_name` = VALUES(`menu_name`), `component` = VALUES(`component`), `perms` = VALUES(`perms`);

