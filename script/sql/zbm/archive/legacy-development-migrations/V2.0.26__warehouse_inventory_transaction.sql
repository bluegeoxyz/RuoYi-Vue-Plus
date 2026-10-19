/* Warehouse Phase 1 immutable inventory transaction. Previous version: V2.0.25 */
SET NAMES utf8mb4;

CREATE TABLE `wm_inventory_transaction` (
    `id` BIGINT NOT NULL COMMENT '库存流水ID', `transaction_no` VARCHAR(64) NOT NULL COMMENT '业务流水号',
    `transaction_type` VARCHAR(32) NOT NULL COMMENT '库存流水类型', `company_id` BIGINT NOT NULL COMMENT '发生时公司快照ID',
    `project_id` BIGINT DEFAULT NULL COMMENT '发生时项目快照ID', `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `location_id` BIGINT NOT NULL COMMENT '库位ID', `material_id` BIGINT NOT NULL COMMENT '物料ID',
    `quantity_before` DECIMAL(19,6) NOT NULL COMMENT '变更前实际库存', `quantity_change` DECIMAL(19,6) NOT NULL COMMENT '实际库存变化量',
    `quantity_after` DECIMAL(19,6) NOT NULL COMMENT '变更后实际库存', `locked_before` DECIMAL(19,6) NOT NULL DEFAULT 0 COMMENT '变更前锁定数量',
    `locked_change` DECIMAL(19,6) NOT NULL DEFAULT 0 COMMENT '锁定数量变化量', `locked_after` DECIMAL(19,6) NOT NULL DEFAULT 0 COMMENT '变更后锁定数量',
    `source_type` VARCHAR(32) DEFAULT NULL COMMENT '来源业务类型', `source_id` BIGINT DEFAULT NULL COMMENT '来源业务单据ID', `source_no` VARCHAR(64) DEFAULT NULL COMMENT '来源业务单号',
    `occurred_at` DATETIME NOT NULL COMMENT '库存业务实际生效时间', `operator_id` BIGINT NOT NULL COMMENT '业务操作人用户ID',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门', `create_by` BIGINT DEFAULT NULL COMMENT '创建者', `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新者', `update_time` DATETIME DEFAULT NULL COMMENT '更新时间', `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`), UNIQUE KEY `uk_wm_inventory_transaction_no` (`transaction_no`),
    KEY `idx_wm_transaction_warehouse_time` (`warehouse_id`, `occurred_at`, `id`), KEY `idx_wm_transaction_location_time` (`location_id`, `occurred_at`),
    KEY `idx_wm_transaction_material_time` (`material_id`, `occurred_at`), KEY `idx_wm_transaction_source` (`source_type`, `source_id`),
    CONSTRAINT `chk_wm_transaction_quantity` CHECK (`quantity_before` >= 0 AND `quantity_after` >= 0 AND `quantity_change` <> 0),
    CONSTRAINT `chk_wm_transaction_quantity_math` CHECK (`quantity_after` = `quantity_before` + `quantity_change`),
    CONSTRAINT `chk_wm_transaction_locked` CHECK (`locked_before` >= 0 AND `locked_after` >= 0),
    CONSTRAINT `chk_wm_transaction_locked_math` CHECK (`locked_after` = `locked_before` + `locked_change`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不可变库存流水事实表';

INSERT INTO `sys_dict_type` (`dict_id`,`dict_name`,`dict_type`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
(2609251000000000051,'仓库-库存流水类型','wm_inventory_transaction_type',NULL,1,NOW(),NULL,NULL,'Warehouse Phase 1')
ON DUPLICATE KEY UPDATE `dict_name`=VALUES(`dict_name`),`remark`=VALUES(`remark`);

INSERT INTO `sys_dict_data` (`dict_code`,`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
(2609251100000000232,1,'入库','INBOUND','wm_inventory_transaction_type',NULL,'success','Y',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000233,2,'出库','OUTBOUND','wm_inventory_transaction_type',NULL,'danger','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000234,3,'调拨入','TRANSFER_IN','wm_inventory_transaction_type',NULL,'success','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000235,4,'调拨出','TRANSFER_OUT','wm_inventory_transaction_type',NULL,'warning','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000236,5,'盘盈','STOCKTAKE_GAIN','wm_inventory_transaction_type',NULL,'success','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000237,6,'盘亏','STOCKTAKE_LOSS','wm_inventory_transaction_type',NULL,'danger','N',NULL,1,NOW(),NULL,NULL,NULL)
ON DUPLICATE KEY UPDATE `dict_label`=VALUES(`dict_label`),`dict_value`=VALUES(`dict_value`),`list_class`=VALUES(`list_class`);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`menu_name`,`order_num`,`path`,`component`,`query_param`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`active_menu`,`ext`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
(2609252000000000610,2609252000000000608,'库存流水',20,'transaction','warehouse/inventoryTransaction/index',NULL,'N','Y','C','0','0','warehouse:inventoryTransaction:list','time',NULL,NULL,NULL,1,NOW(),NULL,NULL,'不可变库存流水查询'),
(2609252000000002271,2609252000000000610,'流水查询',1,'',NULL,NULL,'N','N','F','0','0','warehouse:inventoryTransaction:query','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000002272,2609252000000000610,'流水导出',2,'',NULL,NULL,'N','N','F','0','0','warehouse:inventoryTransaction:export','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL)
ON DUPLICATE KEY UPDATE `parent_id`=VALUES(`parent_id`),`menu_name`=VALUES(`menu_name`),`component`=VALUES(`component`),`perms`=VALUES(`perms`);
