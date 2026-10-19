-- Requires MySQL 8.0.16+ (enforced CHECK constraints) and stored generated columns.
CREATE TABLE `pm_project_management_grant`
(
    `id` BIGINT NOT NULL COMMENT '授权ID',
    `principal_type` VARCHAR(16) NOT NULL COMMENT '主体类型 USER/ROLE',
    `principal_id` BIGINT NOT NULL COMMENT '主体ID',
    `scope_type` VARCHAR(16) NOT NULL COMMENT '范围 ALL/PROJECT',
    `project_id` BIGINT NOT NULL DEFAULT 0 COMMENT '项目ID，ALL固定0',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态 0正常 1停用',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    `create_dept` BIGINT DEFAULT NULL, `create_by` BIGINT DEFAULT NULL,
    `create_time` DATETIME DEFAULT NULL, `update_by` BIGINT DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL, `remark` VARCHAR(500) DEFAULT NULL,
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
    `active_project_id` BIGINT GENERATED ALWAYS AS
        (CASE WHEN `del_flag` = '0' THEN `project_id` ELSE NULL END) STORED,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_project_management_grant_active`
        (`principal_type`, `principal_id`, `scope_type`, `active_project_id`),
    KEY `idx_pm_project_management_grant_principal` (`principal_type`,`principal_id`,`status`,`del_flag`),
    KEY `idx_pm_project_management_grant_project` (`project_id`,`status`,`del_flag`),
    CONSTRAINT `ck_pm_project_management_grant_scope`
        CHECK ((`scope_type` = 'ALL' AND `project_id` = 0) OR (`scope_type` = 'PROJECT' AND `project_id` > 0)),
    CONSTRAINT `ck_pm_project_management_grant_principal` CHECK (`principal_type` IN ('USER','ROLE')),
    CONSTRAINT `ck_pm_project_management_grant_scope_type` CHECK (`scope_type` IN ('ALL','PROJECT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业项目管理范围授权表';
