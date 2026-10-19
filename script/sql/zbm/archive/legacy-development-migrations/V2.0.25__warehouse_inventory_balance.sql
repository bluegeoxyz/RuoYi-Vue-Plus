/* Warehouse Phase 1 inventory balance. Previous version: V2.0.24 */
SET NAMES utf8mb4;

CREATE TABLE `wm_inventory` (
    `id` BIGINT NOT NULL COMMENT '库存余额ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID', `location_id` BIGINT NOT NULL COMMENT '库位ID', `material_id` BIGINT NOT NULL COMMENT '物料ID',
    `quantity` DECIMAL(19,6) NOT NULL DEFAULT 0 COMMENT '当前实际库存数量',
    `locked_quantity` DECIMAL(19,6) NOT NULL DEFAULT 0 COMMENT '锁定数量',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门', `create_by` BIGINT DEFAULT NULL COMMENT '创建者', `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新者', `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wm_inventory_balance` (`warehouse_id`, `location_id`, `material_id`),
    KEY `idx_wm_inventory_warehouse_material` (`warehouse_id`, `material_id`),
    KEY `idx_wm_inventory_location` (`location_id`), KEY `idx_wm_inventory_material` (`material_id`),
    CONSTRAINT `chk_wm_inventory_quantity` CHECK (`quantity` >= 0),
    CONSTRAINT `chk_wm_inventory_locked_quantity` CHECK (`locked_quantity` >= 0 AND `locked_quantity` <= `quantity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库位级当前库存余额表';

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `active_menu`, `ext`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES
(2609252000000000608,2609252000000000006,'库存管理',20,'inventory',NULL,NULL,'N','N','M','0','0',NULL,'database',NULL,NULL,NULL,1,NOW(),NULL,NULL,'Warehouse 库存管理'),
(2609252000000000609,2609252000000000608,'实时库存',10,'realtime','warehouse/inventory/index',NULL,'N','Y','C','0','0','warehouse:inventory:list','list',NULL,NULL,NULL,1,NOW(),NULL,NULL,'只读库位库存余额'),
(2609252000000002261,2609252000000000609,'库存详情',1,'',NULL,NULL,'N','N','F','0','0','warehouse:inventory:query','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002262,2609252000000000609,'库存导出',2,'',NULL,NULL,'N','N','F','0','0','warehouse:inventory:export','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL)
ON DUPLICATE KEY UPDATE `parent_id`=VALUES(`parent_id`),`menu_name`=VALUES(`menu_name`),`component`=VALUES(`component`),`perms`=VALUES(`perms`);
