/* TASK-020A：收款单、核销明细、字典与菜单权限。 */
SET NAMES utf8mb4;

CREATE TABLE `pm_payment`
(
    `id`                      BIGINT         NOT NULL COMMENT '收款ID',
    `project_id`              BIGINT         NOT NULL COMMENT '所属物业项目ID',
    `payment_no`              VARCHAR(64)    NOT NULL COMMENT '收款业务编号',
    `room_id`                 BIGINT         NOT NULL COMMENT '本次收款所属房屋ID',
    `room_full_name_snapshot` VARCHAR(255)   NOT NULL COMMENT '收款时房屋完整名称快照',
    `payer_customer_id`       BIGINT         DEFAULT NULL COMMENT '付款客户ID（允许代缴或非系统客户）',
    `payer_name_snapshot`     VARCHAR(128)   NOT NULL COMMENT '付款人名称快照',
    `payer_type_snapshot`     VARCHAR(32)    DEFAULT NULL COMMENT '付款人类型快照',
    `payment_amount`          DECIMAL(18,2)  NOT NULL COMMENT '实际收款金额',
    `allocated_amount`        DECIMAL(18,2)  NOT NULL DEFAULT 0.00 COMMENT '累计已核销金额',
    `payment_method`          VARCHAR(32)    NOT NULL COMMENT '收款方式',
    `transaction_no`          VARCHAR(128)   DEFAULT NULL COMMENT '支付渠道交易流水号',
    `payment_time`            DATETIME       NOT NULL COMMENT '实际收款时间',
    `status`                  VARCHAR(16)    NOT NULL DEFAULT 'VALID' COMMENT '收款状态（VALID有效 REVERSED已冲销）',
    `create_dept`             BIGINT         DEFAULT NULL COMMENT '创建部门',
    `create_by`               BIGINT         DEFAULT NULL COMMENT '创建者',
    `create_time`             DATETIME       DEFAULT NULL COMMENT '创建时间',
    `update_by`               BIGINT         DEFAULT NULL COMMENT '更新者',
    `update_time`             DATETIME       DEFAULT NULL COMMENT '更新时间',
    `remark`                  VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    `del_flag`                CHAR(1)        NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除；业务不提供删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_payment_no` (`payment_no`),
    KEY `idx_pm_payment_project_time` (`project_id`, `payment_time`),
    KEY `idx_pm_payment_project_room_time` (`project_id`, `room_id`, `payment_time`),
    KEY `idx_pm_payment_project_payer` (`project_id`, `payer_customer_id`),
    CONSTRAINT `chk_pm_payment_amount`
        CHECK (`payment_amount` > 0 AND `allocated_amount` >= 0 AND `allocated_amount` <= `payment_amount`),
    CONSTRAINT `chk_pm_payment_method`
        CHECK (`payment_method` IN ('CASH','WECHAT','ALIPAY','BANK_TRANSFER','POS','OTHER')),
    CONSTRAINT `chk_pm_payment_status`
        CHECK (`status` IN ('VALID','REVERSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业收款单表';

CREATE TABLE `pm_payment_allocation`
(
    `id`                BIGINT        NOT NULL COMMENT '核销明细ID',
    `project_id`        BIGINT        NOT NULL COMMENT '所属物业项目ID',
    `payment_id`        BIGINT        NOT NULL COMMENT '收款ID',
    `receivable_id`     BIGINT        NOT NULL COMMENT '应收ID',
    `allocation_amount` DECIMAL(18,2) NOT NULL COMMENT '本次核销金额',
    `allocation_time`   DATETIME      NOT NULL COMMENT '核销时间',
    `create_dept`       BIGINT        DEFAULT NULL COMMENT '创建部门',
    `create_by`         BIGINT        DEFAULT NULL COMMENT '创建者',
    `create_time`       DATETIME      DEFAULT NULL COMMENT '创建时间',
    `update_by`         BIGINT        DEFAULT NULL COMMENT '更新者',
    `update_time`       DATETIME      DEFAULT NULL COMMENT '更新时间',
    `remark`            VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    `del_flag`          CHAR(1)       NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除；业务不提供删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_payment_allocation_payment_receivable` (`payment_id`, `receivable_id`),
    KEY `idx_pm_payment_allocation_receivable` (`receivable_id`),
    KEY `idx_pm_payment_allocation_project_payment` (`project_id`, `payment_id`),
    CONSTRAINT `chk_pm_payment_allocation_amount` CHECK (`allocation_amount` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业收款核销明细表';

INSERT INTO `sys_dict_type`
(`dict_id`,`dict_name`,`dict_type`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
VALUES
(2609251000000000045,'物业-收款方式','pm_payment_method',NULL,1,NOW(),NULL,NULL,'Property Payment'),
(2609251000000000046,'物业-收款记录状态','pm_payment_record_status',NULL,1,NOW(),NULL,NULL,'Property Payment')
ON DUPLICATE KEY UPDATE `dict_name`=VALUES(`dict_name`),`remark`=VALUES(`remark`);

INSERT INTO `sys_dict_data`
(`dict_code`,`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
VALUES
(2609251100000000209,1,'现金','CASH','pm_payment_method',NULL,'primary','Y',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000210,2,'微信','WECHAT','pm_payment_method',NULL,'success','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000211,3,'支付宝','ALIPAY','pm_payment_method',NULL,'primary','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000212,4,'银行转账','BANK_TRANSFER','pm_payment_method',NULL,'warning','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000213,5,'POS','POS','pm_payment_method',NULL,'info','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000214,6,'其他','OTHER','pm_payment_method',NULL,'info','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000215,1,'有效','VALID','pm_payment_record_status',NULL,'success','Y',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000216,2,'已冲销','REVERSED','pm_payment_record_status',NULL,'danger','N',NULL,1,NOW(),NULL,NULL,NULL)
ON DUPLICATE KEY UPDATE `dict_sort`=VALUES(`dict_sort`),`dict_label`=VALUES(`dict_label`),`dict_value`=VALUES(`dict_value`),`dict_type`=VALUES(`dict_type`),`list_class`=VALUES(`list_class`),`is_default`=VALUES(`is_default`);

INSERT INTO `sys_menu`
(`menu_id`,`parent_id`,`menu_name`,`order_num`,`path`,`component`,`query_param`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`active_menu`,`ext`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
VALUES
(2609252000000000504,2609252000000000005,'收款管理',40,'payment','property/charge/payment/index',NULL,'N','Y','C','0','0','property:payment:list','money',NULL,NULL,NULL,1,NOW(),NULL,NULL,'项目级收款与核销管理'),
(2609252000000001841,2609252000000000504,'收款查询',1,'',NULL,NULL,'N','N','F','0','0','property:payment:query','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000001842,2609252000000000504,'收款新增',2,'',NULL,NULL,'N','N','F','0','0','property:payment:add','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000001843,2609252000000000504,'收款核销',3,'',NULL,NULL,'N','N','F','0','0','property:payment:allocate','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL),
(2609252000000001844,2609252000000000504,'收款导出',4,'',NULL,NULL,'N','N','F','0','0','property:payment:export','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,NULL)
ON DUPLICATE KEY UPDATE `parent_id`=VALUES(`parent_id`),`menu_name`=VALUES(`menu_name`),`order_num`=VALUES(`order_num`),`path`=VALUES(`path`),`component`=VALUES(`component`),`perms`=VALUES(`perms`),`icon`=VALUES(`icon`),`remark`=VALUES(`remark`);
