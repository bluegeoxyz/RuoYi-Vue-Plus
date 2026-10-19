-- TASK-012 B1-2: enterprise Customer grant and security audit.
-- This migration stores authorization metadata only; PII, fingerprints and keys are prohibited.
CREATE TABLE `pm_customer_management_grant`
(
    `id` BIGINT NOT NULL COMMENT '授权ID',
    `principal_type` VARCHAR(16) NOT NULL COMMENT '主体类型 USER/ROLE',
    `principal_id` BIGINT NOT NULL COMMENT '主体ID',
    `scope_type` VARCHAR(16) NOT NULL DEFAULT 'ENTERPRISE' COMMENT '范围，V2仅ENTERPRISE',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态 0正常 1停用',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    `create_dept` BIGINT DEFAULT NULL,
    `create_by` BIGINT DEFAULT NULL,
    `create_time` DATETIME DEFAULT NULL,
    `update_by` BIGINT DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    `remark` VARCHAR(500) DEFAULT NULL,
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
    `active_scope_key` VARCHAR(16) GENERATED ALWAYS AS
        (CASE WHEN `del_flag` = '0' THEN `scope_type` ELSE NULL END) STORED,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_customer_grant_active`
        (`principal_type`, `principal_id`, `active_scope_key`),
    KEY `idx_pm_customer_grant_principal`
        (`principal_type`, `principal_id`, `status`, `del_flag`),
    CONSTRAINT `ck_pm_customer_grant_principal`
        CHECK (`principal_type` IN ('USER', 'ROLE')),
    CONSTRAINT `ck_pm_customer_grant_scope`
        CHECK (`scope_type` = 'ENTERPRISE'),
    CONSTRAINT `ck_pm_customer_grant_status`
        CHECK (`status` IN ('0', '1'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业客户主档授权';

CREATE TABLE `pm_customer_management_grant_audit`
(
    `id` BIGINT NOT NULL COMMENT '审计ID',
    `grant_id` BIGINT DEFAULT NULL COMMENT '授权ID，失败操作可为空',
    `operator_id` BIGINT NOT NULL COMMENT '操作人',
    `action` VARCHAR(16) NOT NULL COMMENT 'GRANT/ENABLE/DISABLE/REVOKE',
    `principal_type` VARCHAR(16) NOT NULL,
    `principal_id` BIGINT NOT NULL,
    `scope_type` VARCHAR(16) NOT NULL,
    `before_status` CHAR(1) DEFAULT NULL,
    `after_status` CHAR(1) DEFAULT NULL,
    `result` CHAR(1) NOT NULL COMMENT '0成功 1失败',
    `trace_id` VARCHAR(64) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '仅允许非敏感失败原因',
    PRIMARY KEY (`id`),
    KEY `idx_pm_customer_grant_audit_grant` (`grant_id`, `create_time`),
    KEY `idx_pm_customer_grant_audit_principal`
        (`principal_type`, `principal_id`, `create_time`),
    KEY `idx_pm_customer_grant_audit_operator` (`operator_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户主档授权安全审计';

CREATE TABLE `pm_customer_sensitive_access_audit`
(
    `id` BIGINT NOT NULL COMMENT '审计ID',
    `operator_id` BIGINT NOT NULL COMMENT '操作人',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID',
    `access_action` VARCHAR(16) NOT NULL COMMENT 'PII_VIEW/PII_EXPORT',
    `access_purpose` VARCHAR(64) NOT NULL COMMENT '接口或业务用途编码',
    `field_level` VARCHAR(32) NOT NULL COMMENT '敏感字段级别',
    `decision` VARCHAR(16) NOT NULL COMMENT 'ALLOW/DENY',
    `trace_id` VARCHAR(64) NOT NULL COMMENT '请求追踪ID',
    `project_id` BIGINT DEFAULT NULL COMMENT '可选项目上下文',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '非敏感原因编码',
    PRIMARY KEY (`id`),
    KEY `idx_pm_customer_sensitive_customer` (`customer_id`, `create_time`),
    KEY `idx_pm_customer_sensitive_operator` (`operator_id`, `create_time`),
    KEY `idx_pm_customer_sensitive_trace` (`trace_id`),
    CONSTRAINT `ck_pm_customer_sensitive_action`
        CHECK (`access_action` IN ('PII_VIEW', 'PII_EXPORT')),
    CONSTRAINT `ck_pm_customer_sensitive_decision`
        CHECK (`decision` IN ('ALLOW', 'DENY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户敏感资料访问审计';
