/* TASK-019A：补齐计价/到期日规则，并新增应收生成批次与应收模型。 */
SET NAMES utf8mb4;

ALTER TABLE `pm_charge_rule`
    ADD COLUMN `price_cycle` VARCHAR(16) NOT NULL DEFAULT 'MONTH' COMMENT '计价周期（单价/固定金额对应周期）' AFTER `billing_cycle`,
    ADD COLUMN `due_date_mode` VARCHAR(32) NOT NULL DEFAULT 'AFTER_PERIOD_START' COMMENT '到期日模式（周期开始日前/后）' AFTER `cycle_mode`,
    ADD COLUMN `due_date_offset_days` TINYINT NOT NULL DEFAULT 0 COMMENT '到期日相对周期开始日偏移天数（0-7）' AFTER `due_date_mode`,
    ADD CONSTRAINT `chk_pm_charge_rule_price_cycle` CHECK (`price_cycle` IN ('MONTH','BIMONTHLY','QUARTER','HALF_YEAR','YEAR')),
    ADD CONSTRAINT `chk_pm_charge_rule_due_date` CHECK (`due_date_mode` IN ('BEFORE_PERIOD_START','AFTER_PERIOD_START') AND `due_date_offset_days` BETWEEN 0 AND 7);

CREATE TABLE `pm_receivable_batch`
(
    `id` BIGINT NOT NULL COMMENT '应收生成批次ID',
    `project_id` BIGINT NOT NULL COMMENT '所属物业项目ID',
    `batch_no` VARCHAR(64) NOT NULL COMMENT '批次业务编号',
    `generate_month` DATE NOT NULL COMMENT '生成月份（统一保存当月1日）',
    `status` VARCHAR(32) NOT NULL COMMENT '批次状态',
    `scanned_count` INT NOT NULL DEFAULT 0 COMMENT '扫描收费绑定数量',
    `generated_count` INT NOT NULL DEFAULT 0 COMMENT '成功生成应收数量',
    `skipped_existing_count` INT NOT NULL DEFAULT 0 COMMENT '已有应收跳过数量',
    `skipped_not_due_count` INT NOT NULL DEFAULT 0 COMMENT '本月无应生成周期跳过数量',
    `failed_count` INT NOT NULL DEFAULT 0 COMMENT '生成失败数量',
    `started_at` DATETIME DEFAULT NULL COMMENT '开始时间',
    `finished_at` DATETIME DEFAULT NULL COMMENT '结束时间',
    `error_message` VARCHAR(1000) DEFAULT NULL COMMENT '批次级异常摘要',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除；业务不提供删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_receivable_batch_no` (`batch_no`),
    KEY `idx_pm_receivable_batch_project_month` (`project_id`,`generate_month`,`create_time`),
    CONSTRAINT `chk_pm_receivable_batch_month` CHECK (DAYOFMONTH(`generate_month`)=1),
    CONSTRAINT `chk_pm_receivable_batch_status` CHECK (`status` IN ('RUNNING','SUCCESS','PARTIAL_SUCCESS','FAILED')),
    CONSTRAINT `chk_pm_receivable_batch_counts` CHECK (`scanned_count`>=0 AND `generated_count`>=0 AND `skipped_existing_count`>=0 AND `skipped_not_due_count`>=0 AND `failed_count`>=0),
    CONSTRAINT `chk_pm_receivable_batch_time` CHECK (`finished_at` IS NULL OR `started_at` IS NULL OR `finished_at`>=`started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业应收生成批次表';

CREATE TABLE `pm_receivable`
(
    `id` BIGINT NOT NULL COMMENT '应收ID',
    `project_id` BIGINT NOT NULL COMMENT '所属物业项目ID',
    `receivable_no` VARCHAR(64) NOT NULL COMMENT '应收业务编号',
    `batch_id` BIGINT NOT NULL COMMENT '首次生成批次ID',
    `binding_id` BIGINT NOT NULL COMMENT '收费载体绑定ID',
    `room_id` BIGINT NOT NULL COMMENT '房屋ID（等于ROOM绑定的object_id）',
    `charge_item_id` BIGINT NOT NULL COMMENT '收费项目ID',
    `charge_rule_id` BIGINT NOT NULL COMMENT '计费规则ID',
    `generate_month` DATE NOT NULL COMMENT '业务生成月份（统一保存当月1日）',
    `period_start_date` DATE NOT NULL COMMENT '实际收费周期开始日期',
    `period_end_date` DATE NOT NULL COMMENT '实际收费周期结束日期',
    `theoretical_start_date` DATE NOT NULL COMMENT '完整理论周期开始日期',
    `theoretical_end_date` DATE NOT NULL COMMENT '完整理论周期结束日期',
    `due_date` DATE NOT NULL COMMENT '缴费到期日',
    `responsible_customer_id` BIGINT DEFAULT NULL COMMENT '责任客户ID快照来源',
    `responsible_customer_name_snapshot` VARCHAR(128) DEFAULT NULL COMMENT '责任客户名称快照',
    `responsible_customer_type_snapshot` VARCHAR(32) DEFAULT NULL COMMENT '责任客户类型快照',
    `room_full_name_snapshot` VARCHAR(255) NOT NULL COMMENT '房屋完整显示名称快照',
    `charge_item_code_snapshot` VARCHAR(64) NOT NULL COMMENT '收费项目编码快照',
    `charge_item_name_snapshot` VARCHAR(128) NOT NULL COMMENT '收费项目名称快照',
    `charge_rule_code_snapshot` VARCHAR(64) NOT NULL COMMENT '计费规则编码快照',
    `charge_rule_name_snapshot` VARCHAR(128) NOT NULL COMMENT '计费规则名称快照',
    `billing_method_snapshot` VARCHAR(16) NOT NULL COMMENT '计费方式快照',
    `billing_cycle_snapshot` VARCHAR(16) NOT NULL COMMENT '账单周期快照',
    `price_cycle_snapshot` VARCHAR(16) NOT NULL COMMENT '计价周期快照',
    `cycle_mode_snapshot` VARCHAR(16) NOT NULL COMMENT '周期模式快照',
    `due_date_mode_snapshot` VARCHAR(32) NOT NULL COMMENT '到期日模式快照',
    `due_date_offset_days_snapshot` TINYINT NOT NULL COMMENT '到期日偏移天数快照',
    `area_snapshot` DECIMAL(18,2) DEFAULT NULL COMMENT '房屋使用面积快照（平方米）',
    `unit_price_snapshot` DECIMAL(18,6) DEFAULT NULL COMMENT '面积单价快照（元/平方米/计价周期）',
    `fixed_amount_snapshot` DECIMAL(18,2) DEFAULT NULL COMMENT '固定金额快照（元/计价周期）',
    `theoretical_days` INT NOT NULL COMMENT '理论周期天数（含首尾日）',
    `actual_days` INT NOT NULL COMMENT '实际计费天数（含首尾日）',
    `receivable_amount` DECIMAL(18,2) NOT NULL COMMENT '应收金额',
    `paid_amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '累计已核销金额',
    `payment_status` VARCHAR(16) NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态',
    `cancel_reason` VARCHAR(500) DEFAULT NULL COMMENT '作废原因',
    `cancelled_by` BIGINT DEFAULT NULL COMMENT '作废人',
    `cancelled_at` DATETIME DEFAULT NULL COMMENT '作废时间',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除；业务不提供删除）',
    `active_period_key` VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN `del_flag`='0' AND `payment_status`<>'CANCELLED' THEN CONCAT(`binding_id`,'#',DATE_FORMAT(`period_start_date`,'%Y%m%d'),'#',DATE_FORMAT(`period_end_date`,'%Y%m%d')) ELSE NULL END) STORED COMMENT '未作废有效应收周期键（用于周期防重）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_receivable_no` (`receivable_no`),
    UNIQUE KEY `uk_pm_receivable_active_period` (`active_period_key`),
    KEY `idx_pm_receivable_project_month` (`project_id`,`generate_month`),
    KEY `idx_pm_receivable_project_room` (`project_id`,`room_id`,`payment_status`),
    KEY `idx_pm_receivable_project_customer` (`project_id`,`responsible_customer_id`,`payment_status`),
    KEY `idx_pm_receivable_binding` (`binding_id`,`period_start_date`),
    KEY `idx_pm_receivable_batch` (`batch_id`),
    KEY `idx_pm_receivable_project_due` (`project_id`,`payment_status`,`due_date`),
    CONSTRAINT `chk_pm_receivable_month` CHECK (DAYOFMONTH(`generate_month`)=1),
    CONSTRAINT `chk_pm_receivable_period` CHECK (`period_end_date`>=`period_start_date` AND `theoretical_end_date`>=`theoretical_start_date` AND `period_start_date`>=`theoretical_start_date` AND `period_end_date`<=`theoretical_end_date`),
    CONSTRAINT `chk_pm_receivable_days` CHECK (`theoretical_days`>0 AND `actual_days`>0 AND `actual_days`<=`theoretical_days`),
    CONSTRAINT `chk_pm_receivable_snapshot_method` CHECK ((`billing_method_snapshot`='AREA' AND `area_snapshot` IS NOT NULL AND `area_snapshot`>=0 AND `unit_price_snapshot` IS NOT NULL AND `unit_price_snapshot`>=0 AND `fixed_amount_snapshot` IS NULL) OR (`billing_method_snapshot`='FIXED' AND `fixed_amount_snapshot` IS NOT NULL AND `fixed_amount_snapshot`>=0 AND `area_snapshot` IS NULL AND `unit_price_snapshot` IS NULL)),
    CONSTRAINT `chk_pm_receivable_cycle` CHECK (`billing_cycle_snapshot` IN ('MONTH','BIMONTHLY','QUARTER','HALF_YEAR','YEAR') AND `price_cycle_snapshot` IN ('MONTH','BIMONTHLY','QUARTER','HALF_YEAR','YEAR') AND `cycle_mode_snapshot` IN ('NATURAL','ROLLING')),
    CONSTRAINT `chk_pm_receivable_due_mode` CHECK (`due_date_mode_snapshot` IN ('BEFORE_PERIOD_START','AFTER_PERIOD_START') AND `due_date_offset_days_snapshot` BETWEEN 0 AND 7),
    CONSTRAINT `chk_pm_receivable_amount` CHECK (`receivable_amount`>=0 AND `paid_amount`>=0 AND `paid_amount`<=`receivable_amount`),
    CONSTRAINT `chk_pm_receivable_payment_status` CHECK (`payment_status` IN ('UNPAID','PARTIAL','PAID','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业应收表';

INSERT INTO `sys_dict_type`
(`dict_id`,`dict_name`,`dict_type`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
VALUES
(2609251000000000042,'物业-到期日模式','pm_due_date_mode',NULL,1,NOW(),NULL,NULL,'Property Receivable'),
(2609251000000000043,'物业-应收批次状态','pm_receivable_batch_status',NULL,1,NOW(),NULL,NULL,'Property Receivable'),
(2609251000000000044,'物业-支付状态','pm_payment_status',NULL,1,NOW(),NULL,NULL,'Property Receivable')
ON DUPLICATE KEY UPDATE `dict_name`=VALUES(`dict_name`),`remark`=VALUES(`remark`);

INSERT INTO `sys_dict_data`
(`dict_code`,`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
VALUES
(2609251100000000199,1,'周期开始日前','BEFORE_PERIOD_START','pm_due_date_mode',NULL,'warning','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000200,2,'周期开始日后','AFTER_PERIOD_START','pm_due_date_mode',NULL,'primary','Y',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000201,1,'执行中','RUNNING','pm_receivable_batch_status',NULL,'primary','Y',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000202,2,'成功','SUCCESS','pm_receivable_batch_status',NULL,'success','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000203,3,'部分成功','PARTIAL_SUCCESS','pm_receivable_batch_status',NULL,'warning','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000204,4,'失败','FAILED','pm_receivable_batch_status',NULL,'danger','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000205,1,'未支付','UNPAID','pm_payment_status',NULL,'danger','Y',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000206,2,'部分支付','PARTIAL','pm_payment_status',NULL,'warning','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000207,3,'已支付','PAID','pm_payment_status',NULL,'success','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000208,4,'已作废','CANCELLED','pm_payment_status',NULL,'info','N',NULL,1,NOW(),NULL,NULL,NULL)
ON DUPLICATE KEY UPDATE `dict_sort`=VALUES(`dict_sort`),`dict_label`=VALUES(`dict_label`),`dict_value`=VALUES(`dict_value`),`dict_type`=VALUES(`dict_type`),`list_class`=VALUES(`list_class`),`is_default`=VALUES(`is_default`);

INSERT INTO `sys_menu`
(`menu_id`,`parent_id`,`menu_name`,`order_num`,`path`,`component`,`query_param`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`active_menu`,`ext`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
VALUES
(2609252000000000503,2609252000000000005,'应收管理',30,'receivable','property/charge/receivable/index',NULL,'N','Y','C','0','0','property:receivable:list','money',NULL,NULL,NULL,1,NOW(),NULL,NULL,'项目级应收与生成批次管理'),
(2609252000000001831,2609252000000000503,'应收查询',1,'',NULL,NULL,'N','N','F','0','0','property:receivable:query','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000001832,2609252000000000503,'应收生成',2,'',NULL,NULL,'N','N','F','0','0','property:receivable:generate','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000001833,2609252000000000503,'应收作废',3,'',NULL,NULL,'N','N','F','0','0','property:receivable:cancel','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000001834,2609252000000000503,'应收导出',4,'',NULL,NULL,'N','N','F','0','0','property:receivable:export','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL)
ON DUPLICATE KEY UPDATE `parent_id`=VALUES(`parent_id`),`menu_name`=VALUES(`menu_name`),`order_num`=VALUES(`order_num`),`path`=VALUES(`path`),`component`=VALUES(`component`),`perms`=VALUES(`perms`),`icon`=VALUES(`icon`),`remark`=VALUES(`remark`);
