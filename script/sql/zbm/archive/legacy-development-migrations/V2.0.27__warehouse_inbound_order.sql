/* Warehouse Phase 1 inbound order. Previous version: V2.0.26 */
SET NAMES utf8mb4;

CREATE TABLE `wm_inbound_order` (
    `id` BIGINT NOT NULL, `inbound_no` VARCHAR(64) NOT NULL, `company_id` BIGINT NOT NULL, `project_id` BIGINT DEFAULT NULL,
    `warehouse_id` BIGINT NOT NULL, `inbound_type` VARCHAR(32) NOT NULL, `status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    `source_type` VARCHAR(32) DEFAULT NULL, `source_id` BIGINT DEFAULT NULL, `source_no` VARCHAR(64) DEFAULT NULL,
    `inbound_at` DATETIME NOT NULL, `completed_at` DATETIME DEFAULT NULL, `operator_id` BIGINT DEFAULT NULL,
    `create_dept` BIGINT DEFAULT NULL, `create_by` BIGINT DEFAULT NULL, `create_time` DATETIME DEFAULT NULL,
    `update_by` BIGINT DEFAULT NULL, `update_time` DATETIME DEFAULT NULL, `remark` VARCHAR(500) DEFAULT NULL,
    `del_flag` CHAR(1) NOT NULL DEFAULT '0', PRIMARY KEY (`id`), UNIQUE KEY `uk_wm_inbound_no` (`inbound_no`),
    KEY `idx_wm_inbound_warehouse_status` (`warehouse_id`,`status`,`inbound_at`), KEY `idx_wm_inbound_source` (`source_type`,`source_id`),
    CONSTRAINT `chk_wm_inbound_status` CHECK (`status` IN ('DRAFT','CONFIRMED','COMPLETED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单';
CREATE TABLE `wm_inbound_order_item` (
    `id` BIGINT NOT NULL, `inbound_order_id` BIGINT NOT NULL, `material_id` BIGINT NOT NULL, `location_id` BIGINT NOT NULL,
    `quantity` DECIMAL(19,6) NOT NULL, `unit_id` BIGINT NOT NULL, `create_dept` BIGINT DEFAULT NULL,
    `create_by` BIGINT DEFAULT NULL, `create_time` DATETIME DEFAULT NULL, `update_by` BIGINT DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL, `remark` VARCHAR(500) DEFAULT NULL, PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wm_inbound_item` (`inbound_order_id`,`material_id`,`location_id`),
    KEY `idx_wm_inbound_item_order` (`inbound_order_id`), KEY `idx_wm_inbound_item_material` (`material_id`),
    KEY `idx_wm_inbound_item_location` (`location_id`), CONSTRAINT `chk_wm_inbound_item_qty` CHECK (`quantity` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单明细';

INSERT INTO `sys_dict_type` (`dict_id`,`dict_name`,`dict_type`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
(2609251000000000052,'仓库-入库单状态','wm_inbound_status',NULL,1,NOW(),NULL,NULL,'Warehouse Phase 1'),
(2609251000000000053,'仓库-入库类型','wm_inbound_type',NULL,1,NOW(),NULL,NULL,'Warehouse Phase 1')
ON DUPLICATE KEY UPDATE `dict_name`=VALUES(`dict_name`),`remark`=VALUES(`remark`);
INSERT INTO `sys_dict_data` (`dict_code`,`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
(2609251100000000238,1,'草稿','DRAFT','wm_inbound_status',NULL,'info','Y',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000239,2,'已确认','CONFIRMED','wm_inbound_status',NULL,'warning','N',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000240,3,'已完成','COMPLETED','wm_inbound_status',NULL,'success','N',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000241,4,'已取消','CANCELLED','wm_inbound_status',NULL,'danger','N',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000242,1,'采购入库','PURCHASE','wm_inbound_type',NULL,'primary','Y',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000243,2,'其他入库','OTHER','wm_inbound_type',NULL,'info','N',NULL,1,NOW(),NULL,NULL,NULL)
ON DUPLICATE KEY UPDATE `dict_label`=VALUES(`dict_label`),`dict_value`=VALUES(`dict_value`),`list_class`=VALUES(`list_class`);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`menu_name`,`order_num`,`path`,`component`,`query_param`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`active_menu`,`ext`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
(2609252000000000611,2609252000000000600,'入库管理',30,'inbound','warehouse/inbound/index',NULL,'N','Y','C','0','0','warehouse:inbound:list','download',NULL,NULL,NULL,1,NOW(),NULL,NULL,'入库业务闭环'),
(2609252000000002273,2609252000000000611,'入库查询',1,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:query','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002274,2609252000000000611,'入库新增',2,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:add','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002275,2609252000000000611,'入库修改',3,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:edit','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002276,2609252000000000611,'入库删除',4,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:remove','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002277,2609252000000000611,'入库确认',5,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:confirm','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002278,2609252000000000611,'完成入库',6,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:complete','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002279,2609252000000000611,'取消入库',7,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:cancel','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),(2609252000000002280,2609252000000000611,'入库导出',8,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:export','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL)
ON DUPLICATE KEY UPDATE `parent_id`=VALUES(`parent_id`),`menu_name`=VALUES(`menu_name`),`component`=VALUES(`component`),`perms`=VALUES(`perms`);
