/* Warehouse Phase 1 material master data. Previous version: V2.0.23 */
SET NAMES utf8mb4;

CREATE TABLE `wm_material_category` (
    `id` BIGINT NOT NULL COMMENT '分类ID', `company_id` BIGINT NOT NULL COMMENT '所属公司/组织部门ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID', `ancestors` VARCHAR(500) NOT NULL DEFAULT '0' COMMENT '祖级列表',
    `category_code` VARCHAR(64) NOT NULL COMMENT '分类编码', `category_name` VARCHAR(128) NOT NULL COMMENT '分类名称',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序', `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门', `create_by` BIGINT DEFAULT NULL COMMENT '创建者', `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新者', `update_time` DATETIME DEFAULT NULL COMMENT '更新时间', `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_code_key` VARCHAR(160) GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN CONCAT(`company_id`, '#', UPPER(`category_code`)) ELSE NULL END) STORED COMMENT '未删除分类编码唯一键',
    PRIMARY KEY (`id`), UNIQUE KEY `uk_wm_material_category_active_code` (`active_code_key`), KEY `idx_wm_material_category_company_parent` (`company_id`, `parent_id`, `status`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料分类表';

CREATE TABLE `wm_unit` (
    `id` BIGINT NOT NULL COMMENT '单位ID', `unit_code` VARCHAR(32) NOT NULL COMMENT '单位编码', `unit_name` VARCHAR(64) NOT NULL COMMENT '单位名称',
    `symbol` VARCHAR(32) DEFAULT NULL COMMENT '单位符号', `precision_scale` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '推荐小数精度（0-6）',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）', `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门', `create_by` BIGINT DEFAULT NULL COMMENT '创建者', `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新者', `update_time` DATETIME DEFAULT NULL COMMENT '更新时间', `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_code_key` VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN UPPER(`unit_code`) ELSE NULL END) STORED COMMENT '未删除单位编码唯一键',
    PRIMARY KEY (`id`), UNIQUE KEY `uk_wm_unit_active_code` (`active_code_key`), KEY `idx_wm_unit_status` (`status`, `del_flag`),
    CONSTRAINT `chk_wm_unit_precision` CHECK (`precision_scale` BETWEEN 0 AND 6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计量单位表';

CREATE TABLE `wm_material` (
    `id` BIGINT NOT NULL COMMENT '物料ID', `company_id` BIGINT NOT NULL COMMENT '所属公司/组织部门ID', `material_code` VARCHAR(64) NOT NULL COMMENT '物料编码',
    `material_name` VARCHAR(128) NOT NULL COMMENT '物料名称', `category_id` BIGINT NOT NULL COMMENT '物料分类ID', `base_unit_id` BIGINT NOT NULL COMMENT '基础计量单位ID',
    `specification` VARCHAR(255) DEFAULT NULL COMMENT '规格描述', `model` VARCHAR(128) DEFAULT NULL COMMENT '型号', `barcode` VARCHAR(128) DEFAULT NULL COMMENT '条码',
    `safety_stock` DECIMAL(19,6) NOT NULL DEFAULT 0 COMMENT '安全库存', `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）', `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门', `create_by` BIGINT DEFAULT NULL COMMENT '创建者', `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新者', `update_time` DATETIME DEFAULT NULL COMMENT '更新时间', `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_code_key` VARCHAR(160) GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN CONCAT(`company_id`, '#', UPPER(`material_code`)) ELSE NULL END) STORED COMMENT '未删除物料编码唯一键',
    `active_barcode_key` VARCHAR(256) GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' AND `barcode` IS NOT NULL AND `barcode` <> '' THEN CONCAT(`company_id`, '#', `barcode`) ELSE NULL END) STORED COMMENT '未删除非空条码唯一键',
    PRIMARY KEY (`id`), UNIQUE KEY `uk_wm_material_active_code` (`active_code_key`), UNIQUE KEY `uk_wm_material_active_barcode` (`active_barcode_key`),
    KEY `idx_wm_material_company_category` (`company_id`, `category_id`, `status`, `del_flag`), KEY `idx_wm_material_unit` (`base_unit_id`, `del_flag`),
    CONSTRAINT `chk_wm_material_safety_stock` CHECK (`safety_stock` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料主数据表';

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `active_menu`, `ext`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES
(2609252000000000605,2609252000000000601,'物料分类',40,'materialCategory','warehouse/materialCategory/index',NULL,'N','Y','C','0','0','warehouse:materialCategory:list','tree',NULL,NULL,NULL,1,NOW(),NULL,NULL,'公司级物料分类'),
(2609252000000000606,2609252000000000601,'计量单位',50,'unit','warehouse/unit/index',NULL,'N','Y','C','0','0','warehouse:unit:list','component',NULL,NULL,NULL,1,NOW(),NULL,NULL,'平台共享计量单位'),
(2609252000000000607,2609252000000000601,'物料管理',60,'material','warehouse/material/index',NULL,'N','Y','C','0','0','warehouse:material:list','shopping',NULL,NULL,NULL,1,NOW(),NULL,NULL,'公司级物料主数据'),
(2609252000000002231,2609252000000000605,'分类查询',1,'',NULL,NULL,'N','N','F','0','0','warehouse:materialCategory:query','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002232,2609252000000000605,'分类新增',2,'',NULL,NULL,'N','N','F','0','0','warehouse:materialCategory:add','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002233,2609252000000000605,'分类修改',3,'',NULL,NULL,'N','N','F','0','0','warehouse:materialCategory:edit','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002234,2609252000000000605,'分类删除',4,'',NULL,NULL,'N','N','F','0','0','warehouse:materialCategory:remove','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002241,2609252000000000606,'单位查询',1,'',NULL,NULL,'N','N','F','0','0','warehouse:unit:query','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002242,2609252000000000606,'单位新增',2,'',NULL,NULL,'N','N','F','0','0','warehouse:unit:add','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002243,2609252000000000606,'单位修改',3,'',NULL,NULL,'N','N','F','0','0','warehouse:unit:edit','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002244,2609252000000000606,'单位删除',4,'',NULL,NULL,'N','N','F','0','0','warehouse:unit:remove','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002251,2609252000000000607,'物料查询',1,'',NULL,NULL,'N','N','F','0','0','warehouse:material:query','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002252,2609252000000000607,'物料新增',2,'',NULL,NULL,'N','N','F','0','0','warehouse:material:add','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002253,2609252000000000607,'物料修改',3,'',NULL,NULL,'N','N','F','0','0','warehouse:material:edit','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002254,2609252000000000607,'物料删除',4,'',NULL,NULL,'N','N','F','0','0','warehouse:material:remove','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL)
ON DUPLICATE KEY UPDATE `parent_id`=VALUES(`parent_id`),`menu_name`=VALUES(`menu_name`),`component`=VALUES(`component`),`perms`=VALUES(`perms`);
