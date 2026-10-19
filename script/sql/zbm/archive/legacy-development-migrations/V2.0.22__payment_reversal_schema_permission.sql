/* TASK-022A：整笔收款冲销审计表与独立冲销权限。 */
SET NAMES utf8mb4;

CREATE TABLE `pm_payment_reversal`
(
    `id`              BIGINT        NOT NULL COMMENT '收款冲销ID',
    `project_id`      BIGINT        NOT NULL COMMENT '所属物业项目ID',
    `reversal_no`     VARCHAR(64)   NOT NULL COMMENT '冲销业务编号',
    `payment_id`      BIGINT        NOT NULL COMMENT '原收款ID',
    `reversal_amount` DECIMAL(18,2) NOT NULL COMMENT '冲销金额（第一阶段等于原收款金额）',
    `reversal_time`   DATETIME      NOT NULL COMMENT '正式执行冲销时间',
    `reason`          VARCHAR(500)  NOT NULL COMMENT '冲销原因',
    `create_dept`     BIGINT        DEFAULT NULL COMMENT '创建部门',
    `create_by`       BIGINT        DEFAULT NULL COMMENT '冲销操作人',
    `create_time`     DATETIME      DEFAULT NULL COMMENT '创建时间',
    `update_by`       BIGINT        DEFAULT NULL COMMENT '更新者',
    `update_time`     DATETIME      DEFAULT NULL COMMENT '更新时间',
    `remark`          VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    `del_flag`        CHAR(1)       NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除；业务不提供删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_payment_reversal_no` (`reversal_no`),
    UNIQUE KEY `uk_pm_payment_reversal_payment` (`payment_id`),
    KEY `idx_pm_payment_reversal_project_time` (`project_id`, `reversal_time`),
    CONSTRAINT `chk_pm_payment_reversal_amount` CHECK (`reversal_amount` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业收款冲销审计表';

INSERT INTO `sys_menu`
(`menu_id`,`parent_id`,`menu_name`,`order_num`,`path`,`component`,`query_param`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`active_menu`,`ext`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
VALUES
(2609252000000001845,2609252000000000504,'收款冲销',5,'',NULL,NULL,'N','N','F','0','0','property:payment:reverse','#',NULL,NULL,NULL,1,NOW(),NULL,NULL,'整笔收款冲销与全部核销回退')
ON DUPLICATE KEY UPDATE `parent_id`=VALUES(`parent_id`),`menu_name`=VALUES(`menu_name`),`order_num`=VALUES(`order_num`),`perms`=VALUES(`perms`),`remark`=VALUES(`remark`);
