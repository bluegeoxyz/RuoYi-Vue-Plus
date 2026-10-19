/*
 * ============================================================
 * ZBM Property Management - Current Baseline
 * File    : 01_schema.sql
 * Version : V2.0.33
 * Purpose : Core HR + Property schema
 * ============================================================
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. HR - Employee
-- ============================================================

DROP TABLE IF EXISTS `hr_employee`;

CREATE TABLE `hr_employee`
(
    `id`                BIGINT       NOT NULL COMMENT '员工ID',
    `employee_no`       VARCHAR(32)  NOT NULL COMMENT '员工编号',
    `employee_name`     VARCHAR(64)  NOT NULL COMMENT '员工姓名',

    `dept_id`           BIGINT       NOT NULL COMMENT '所属部门ID',
    `post_id`           BIGINT       DEFAULT NULL COMMENT '主岗位ID',
    `user_id`           BIGINT       DEFAULT NULL COMMENT '绑定系统用户ID',

    `gender`            CHAR(1)      DEFAULT NULL COMMENT '性别',
    `mobile`            VARCHAR(32)  DEFAULT NULL COMMENT '手机号码',
    `email`             VARCHAR(128) DEFAULT NULL COMMENT '邮箱',

    `id_type`           VARCHAR(32)  DEFAULT NULL COMMENT '证件类型',
    `id_no`             VARCHAR(64)  DEFAULT NULL COMMENT '证件号码',
    `birth_date`        DATE         DEFAULT NULL COMMENT '出生日期',

    `entry_date`        DATE         DEFAULT NULL COMMENT '入职日期',
    `leave_date`        DATE         DEFAULT NULL COMMENT '离职日期',

    `employment_type`   VARCHAR(32)  DEFAULT NULL COMMENT '用工类型',
    `employment_status` VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE' COMMENT '员工状态',

    `sort`              INT          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `status`            CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',

    `create_dept`       BIGINT       DEFAULT NULL COMMENT '创建部门',
    `create_by`         BIGINT       DEFAULT NULL COMMENT '创建者',
    `create_time`       DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`         BIGINT       DEFAULT NULL COMMENT '更新者',
    `update_time`       DATETIME     DEFAULT NULL COMMENT '更新时间',
    `remark`            VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag`          CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hr_employee_no` (`employee_no`),
    KEY `idx_hr_employee_user_id` (`user_id`),
    KEY `idx_hr_employee_dept_id` (`dept_id`),
    KEY `idx_hr_employee_post_id` (`post_id`),
    KEY `idx_hr_employee_name` (`employee_name`),
    KEY `idx_hr_employee_mobile` (`mobile`),
    KEY `idx_hr_employee_status` (`employment_status`, `status`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工档案表';


-- ============================================================
-- 2. Property - Project
-- ============================================================

DROP TABLE IF EXISTS `pm_project`;

CREATE TABLE `pm_project`
(
    `id`                  BIGINT        NOT NULL COMMENT '项目ID',

    `project_code`        VARCHAR(32)   NOT NULL COMMENT '项目编码',
    `project_name`        VARCHAR(128)  NOT NULL COMMENT '项目名称',
    `project_short_name`  VARCHAR(64)   DEFAULT NULL COMMENT '项目简称',
    `project_type`        VARCHAR(32)   DEFAULT NULL COMMENT '项目类型',

    `province_code`       VARCHAR(32)   DEFAULT NULL COMMENT '省编码',
    `city_code`           VARCHAR(32)   DEFAULT NULL COMMENT '市编码',
    `district_code`       VARCHAR(32)   DEFAULT NULL COMMENT '区县编码',
    `address`             VARCHAR(255)  DEFAULT NULL COMMENT '详细地址',

    `service_phone`       VARCHAR(32)   DEFAULT NULL COMMENT '服务电话',
    `manager_employee_id` BIGINT        DEFAULT NULL COMMENT '项目负责人员工ID',

    `delivery_date`       DATE          DEFAULT NULL COMMENT '项目交付日期',

    `longitude`           DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
    `latitude`            DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',

    `sort`                INT           NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `status`              CHAR(1)       NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',

    `create_dept`         BIGINT        DEFAULT NULL COMMENT '创建部门',
    `create_by`           BIGINT        DEFAULT NULL COMMENT '创建者',
    `create_time`         DATETIME      DEFAULT NULL COMMENT '创建时间',
    `update_by`           BIGINT        DEFAULT NULL COMMENT '更新者',
    `update_time`         DATETIME      DEFAULT NULL COMMENT '更新时间',
    `remark`              VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    `del_flag`            CHAR(1)       NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_project_code` (`project_code`),
    KEY `idx_pm_project_name` (`project_name`),
    KEY `idx_pm_project_manager` (`manager_employee_id`),
    KEY `idx_pm_project_type` (`project_type`),
    KEY `idx_pm_project_status` (`status`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业项目表';


-- ============================================================
-- 3. Property - Project Member
-- ============================================================

DROP TABLE IF EXISTS `pm_project_member`;

CREATE TABLE `pm_project_member`
(
    `id`             BIGINT       NOT NULL COMMENT '项目成员关系ID',

    `project_id`     BIGINT       NOT NULL COMMENT '项目ID',
    `employee_id`    BIGINT       NOT NULL COMMENT '员工ID',

    `member_role`    VARCHAR(32)  NOT NULL DEFAULT 'MEMBER' COMMENT '项目角色',
    `is_manager`     CHAR(1)      NOT NULL DEFAULT '0' COMMENT '是否项目负责人（0否 1是）',

    `start_date`     DATE         NOT NULL COMMENT '任职开始日期',
    `end_date`       DATE         DEFAULT NULL COMMENT '任职结束日期',
    `member_status`  VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE' COMMENT '成员关系状态',

    `sort`           INT          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `status`         CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',

    `create_dept`    BIGINT       DEFAULT NULL COMMENT '创建部门',
    `create_by`      BIGINT       DEFAULT NULL COMMENT '创建者',
    `create_time`    DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`      BIGINT       DEFAULT NULL COMMENT '更新者',
    `update_time`    DATETIME     DEFAULT NULL COMMENT '更新时间',
    `remark`         VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag`       CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',

    PRIMARY KEY (`id`),
    KEY `idx_pm_project_member_project`
        (`project_id`, `member_status`, `status`, `del_flag`),
    KEY `idx_pm_project_member_employee`
        (`employee_id`, `member_status`, `status`, `del_flag`),
    KEY `idx_pm_project_member_project_employee`
        (`project_id`, `employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业项目成员表';


-- ============================================================
-- 4. Property Resource - Building
-- ============================================================

DROP TABLE IF EXISTS `pm_building`;

CREATE TABLE `pm_building`
(
    `id`                    BIGINT       NOT NULL COMMENT '楼栋ID',
    `project_id`            BIGINT       NOT NULL COMMENT '项目ID',

    `building_code`         VARCHAR(32)  NOT NULL COMMENT '楼栋编码',
    `building_name`         VARCHAR(64)  NOT NULL COMMENT '楼栋名称',
    `building_type`         VARCHAR(32)  DEFAULT NULL COMMENT '楼栋类型',

    `above_ground_floors`   INT          DEFAULT NULL COMMENT '地上楼层数',
    `underground_floors`    INT          DEFAULT NULL COMMENT '地下楼层数',

    `sort`                  INT          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `status`                CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',

    `create_dept`           BIGINT       DEFAULT NULL COMMENT '创建部门',
    `create_by`             BIGINT       DEFAULT NULL COMMENT '创建者',
    `create_time`           DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`             BIGINT       DEFAULT NULL COMMENT '更新者',
    `update_time`           DATETIME     DEFAULT NULL COMMENT '更新时间',
    `remark`                VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag`              CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_building_code`  VARCHAR(32)
        GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN `building_code` ELSE NULL END) STORED
        COMMENT '未删除楼栋编码（用于逻辑删除唯一约束）',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_building_active_code` (`project_id`, `active_building_code`),
    KEY `idx_pm_building_project`
        (`project_id`, `status`, `del_flag`),
    KEY `idx_pm_building_code`
        (`project_id`, `building_code`, `del_flag`),
    KEY `idx_pm_building_name`
        (`project_id`, `building_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业楼栋表';


-- ============================================================
-- 5. Property Resource - Building Unit
-- Java Entity: BuildingUnit
-- Database Table: pm_unit
-- ============================================================

DROP TABLE IF EXISTS `pm_unit`;

CREATE TABLE `pm_unit`
(
    `id`           BIGINT       NOT NULL COMMENT '单元ID',
    `project_id`   BIGINT       NOT NULL COMMENT '项目ID',
    `building_id`  BIGINT       NOT NULL COMMENT '楼栋ID',

    `unit_code`    VARCHAR(32)  NOT NULL COMMENT '单元编码',
    `unit_name`    VARCHAR(64)  NOT NULL COMMENT '单元名称',
    `floor_count`  INT          DEFAULT NULL COMMENT '楼层数',

    `sort`         INT          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `status`       CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',

    `create_dept`  BIGINT       DEFAULT NULL COMMENT '创建部门',
    `create_by`    BIGINT       DEFAULT NULL COMMENT '创建者',
    `create_time`  DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`    BIGINT       DEFAULT NULL COMMENT '更新者',
    `update_time`  DATETIME     DEFAULT NULL COMMENT '更新时间',
    `remark`       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag`     CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_unit_code` VARCHAR(32)
        GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN `unit_code` ELSE NULL END) STORED
        COMMENT '未删除单元编码（用于逻辑删除唯一约束）',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_unit_active_code` (`building_id`, `active_unit_code`),
    KEY `idx_pm_unit_project`
        (`project_id`, `status`, `del_flag`),
    KEY `idx_pm_unit_building`
        (`building_id`, `status`, `del_flag`),
    KEY `idx_pm_unit_code`
        (`building_id`, `unit_code`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业楼栋单元表';


-- ============================================================
-- 6. Property Resource - Room
-- ============================================================

DROP TABLE IF EXISTS `pm_room`;

CREATE TABLE `pm_room`
(
    `id`               BIGINT        NOT NULL COMMENT '房屋ID',

    `project_id`       BIGINT        NOT NULL COMMENT '项目ID',
    `building_id`      BIGINT        NOT NULL COMMENT '楼栋ID',
    `unit_id`          BIGINT        NOT NULL COMMENT '单元ID',

    `room_code`        VARCHAR(64)   NOT NULL COMMENT '房屋编码',
    `room_no`          VARCHAR(32)   NOT NULL COMMENT '房号',
    `floor_no`         INT           DEFAULT NULL COMMENT '所在楼层',

    `room_type`        VARCHAR(32)   DEFAULT NULL COMMENT '房屋类型',
    `property_type`    VARCHAR(32)   DEFAULT NULL COMMENT '产权/资源属性',

    `building_area`    DECIMAL(12,2) DEFAULT NULL COMMENT '建筑面积',
    `usable_area`      DECIMAL(12,2) DEFAULT NULL COMMENT '使用面积',

    `delivery_status`  VARCHAR(32)   NOT NULL DEFAULT 'UNDELIVERED' COMMENT '交付状态',
    `occupancy_status` VARCHAR(32)   NOT NULL DEFAULT 'VACANT' COMMENT '入住状态',

    `sort`             INT           NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `status`           CHAR(1)       NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',

    `create_dept`      BIGINT        DEFAULT NULL COMMENT '创建部门',
    `create_by`        BIGINT        DEFAULT NULL COMMENT '创建者',
    `create_time`      DATETIME      DEFAULT NULL COMMENT '创建时间',
    `update_by`        BIGINT        DEFAULT NULL COMMENT '更新者',
    `update_time`      DATETIME      DEFAULT NULL COMMENT '更新时间',
    `remark`           VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    `del_flag`         CHAR(1)       NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_room_code` VARCHAR(64)
        GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN `room_code` ELSE NULL END) STORED
        COMMENT '未删除房屋编码（用于逻辑删除唯一约束）',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_room_active_code`
        (`project_id`, `active_room_code`),
    KEY `idx_pm_room_project`
        (`project_id`, `status`, `del_flag`),
    KEY `idx_pm_room_building`
        (`building_id`, `status`, `del_flag`),
    KEY `idx_pm_room_unit`
        (`unit_id`, `status`, `del_flag`),
    KEY `idx_pm_room_no`
        (`unit_id`, `room_no`, `del_flag`),
    KEY `idx_pm_room_occupancy`
        (`project_id`, `occupancy_status`, `status`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业房屋表';


-- ============================================================
-- 7. Property Customer - Enterprise-level Customer Master
-- IMPORTANT: No project_id by design.
-- ============================================================

DROP TABLE IF EXISTS `pm_customer`;

CREATE TABLE `pm_customer`
(
    `id`            BIGINT       NOT NULL COMMENT '客户ID',

    `customer_no`   VARCHAR(32)  NOT NULL COMMENT '客户编号',
    `customer_type` VARCHAR(32)  NOT NULL DEFAULT 'PERSON' COMMENT '客户类型',
    `customer_name` VARCHAR(128) NOT NULL COMMENT '客户名称',

    `gender`        CHAR(1)      DEFAULT NULL COMMENT '性别',

    `mobile`        VARCHAR(32)  DEFAULT NULL COMMENT '手机号码',
    `phone`         VARCHAR(32)  DEFAULT NULL COMMENT '固定电话',
    `email`         VARCHAR(128) DEFAULT NULL COMMENT '电子邮箱',

    `id_type`       VARCHAR(32)  DEFAULT NULL COMMENT '证件类型',
    `id_no`         VARCHAR(512) DEFAULT NULL COMMENT '证件号码密文（ENC_ 前缀）',
    `birth_date`    DATE         DEFAULT NULL COMMENT '出生日期',

    `status`        CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `identity_fingerprint`    BINARY(32)  DEFAULT NULL COMMENT '规范化证件身份 HMAC-SHA256',
    `pii_key_version`         VARCHAR(32) DEFAULT NULL COMMENT 'PII 字段加密密钥版本',
    `lifecycle_status`        VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '生命周期状态',
    `merged_into_customer_id` BIGINT      DEFAULT NULL COMMENT '合并后的主客户ID',
    `version`                 INT         NOT NULL DEFAULT 0 COMMENT '乐观锁版本',

    `create_dept`   BIGINT       DEFAULT NULL COMMENT '创建部门',
    `create_by`     BIGINT       DEFAULT NULL COMMENT '创建者',
    `create_time`   DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`     BIGINT       DEFAULT NULL COMMENT '更新者',
    `update_time`   DATETIME     DEFAULT NULL COMMENT '更新时间',
    `remark`        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag`      CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_identity_key` BINARY(32)
        GENERATED ALWAYS AS (
            CASE WHEN `del_flag` = '0' AND `identity_fingerprint` IS NOT NULL
                 THEN `identity_fingerprint` ELSE NULL END
        ) STORED COMMENT '有效身份唯一键',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_customer_no` (`customer_no`),
    UNIQUE KEY `uk_pm_customer_active_identity` (`id_type`, `active_identity_key`),
    KEY `idx_pm_customer_name` (`customer_name`),
    KEY `idx_pm_customer_mobile` (`mobile`),
    KEY `idx_pm_customer_id_no` (`id_type`, `id_no`),
    KEY `idx_pm_customer_status` (`customer_type`, `status`, `del_flag`),
    CONSTRAINT `chk_pm_customer_lifecycle_status`
        CHECK (`lifecycle_status` IN ('ACTIVE', 'INACTIVE', 'CANCELLED', 'MERGED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业客户主数据表';


-- ============================================================
-- 8. Property Customer - Room Customer Relation
-- ============================================================

DROP TABLE IF EXISTS `pm_room_customer_rel`;

CREATE TABLE `pm_room_customer_rel`
(
    `id`              BIGINT       NOT NULL COMMENT '房屋客户关系ID',

    `project_id`      BIGINT       NOT NULL COMMENT '项目ID',
    `room_id`         BIGINT       NOT NULL COMMENT '房屋ID',
    `customer_id`     BIGINT       NOT NULL COMMENT '客户ID',

    `relation_type`   VARCHAR(32)  NOT NULL COMMENT '关系类型',
    `is_primary`      CHAR(1)      NOT NULL DEFAULT '0' COMMENT '是否主要关系人（0否 1是）',

    `start_date`      DATE         NOT NULL COMMENT '关系开始日期',
    `end_date`        DATE         DEFAULT NULL COMMENT '关系结束日期',
    `move_in_date`    DATE         DEFAULT NULL COMMENT '实际迁入日期',
    `move_out_date`   DATE         DEFAULT NULL COMMENT '实际迁出日期',

    `relation_status` VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE' COMMENT '关系状态',
    `status`          CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',

    `create_dept`     BIGINT       DEFAULT NULL COMMENT '创建部门',
    `create_by`       BIGINT       DEFAULT NULL COMMENT '创建者',
    `create_time`     DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`       BIGINT       DEFAULT NULL COMMENT '更新者',
    `update_time`     DATETIME     DEFAULT NULL COMMENT '更新时间',
    `remark`          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag`        CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `effective_relation_key` TINYINT
        GENERATED ALWAYS AS (
            CASE WHEN `del_flag` = '0' AND `status` = '0'
                       AND `relation_status` IN ('PENDING', 'ACTIVE')
                 THEN 1 ELSE NULL END
        ) STORED COMMENT '待生效或有效关系唯一键',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_room_customer_rel_effective`
        (`room_id`, `customer_id`, `relation_type`, `effective_relation_key`),
    KEY `idx_pm_room_customer_project`
        (`project_id`, `relation_status`, `status`, `del_flag`),
    KEY `idx_pm_room_customer_room`
        (`room_id`, `relation_status`, `status`, `del_flag`),
    KEY `idx_pm_room_customer_customer`
        (`customer_id`, `relation_status`, `status`, `del_flag`),
    KEY `idx_pm_room_customer_relation`
        (`room_id`, `customer_id`, `relation_type`),
    KEY `idx_pm_room_customer_project_customer`
        (`project_id`, `customer_id`),
    CONSTRAINT `chk_pm_room_customer_rel_type`
        CHECK (`relation_type` IN ('OWNER', 'TENANT', 'FAMILY', 'RESIDENT', 'CONTACT', 'OTHER')),
    CONSTRAINT `chk_pm_room_customer_rel_status`
        CHECK (`relation_status` IN ('PENDING', 'ACTIVE', 'ENDED')),
    CONSTRAINT `chk_pm_room_customer_rel_primary`
        CHECK (`is_primary` IN ('0', '1')),
    CONSTRAINT `chk_pm_room_customer_rel_dates`
        CHECK (`end_date` IS NULL OR `end_date` >= `start_date`),
    CONSTRAINT `chk_pm_room_customer_rel_move_dates`
        CHECK (`move_out_date` IS NULL OR `move_in_date` IS NULL OR `move_out_date` >= `move_in_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房屋客户关系表';

-- ============================================================
-- 9. Property - Project Management Grant And Audit
-- ============================================================

DROP TABLE IF EXISTS `pm_project_management_grant_audit`;
DROP TABLE IF EXISTS `pm_project_management_grant`;

CREATE TABLE `pm_project_management_grant`
(
    `id`                BIGINT       NOT NULL COMMENT '授权ID',
    `principal_type`    VARCHAR(16)  NOT NULL COMMENT '主体类型 USER/ROLE',
    `principal_id`      BIGINT       NOT NULL COMMENT '主体ID',
    `scope_type`        VARCHAR(16)  NOT NULL COMMENT '范围 ALL/PROJECT',
    `project_id`        BIGINT       NOT NULL DEFAULT 0 COMMENT '项目ID，ALL固定0',
    `status`            CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `version`           INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    `create_dept`       BIGINT       DEFAULT NULL COMMENT '创建部门',
    `create_by`         BIGINT       DEFAULT NULL COMMENT '创建者',
    `create_time`       DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`         BIGINT       DEFAULT NULL COMMENT '更新者',
    `update_time`       DATETIME     DEFAULT NULL COMMENT '更新时间',
    `remark`            VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag`          CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_project_id` BIGINT GENERATED ALWAYS AS
        (CASE WHEN `del_flag` = '0' THEN `project_id` ELSE NULL END) STORED,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_project_management_grant_active`
        (`principal_type`, `principal_id`, `scope_type`, `active_project_id`),
    KEY `idx_pm_project_management_grant_principal` (`principal_type`, `principal_id`, `status`, `del_flag`),
    KEY `idx_pm_project_management_grant_project` (`project_id`, `status`, `del_flag`),
    CONSTRAINT `ck_pm_project_management_grant_scope`
        CHECK ((`scope_type` = 'ALL' AND `project_id` = 0) OR (`scope_type` = 'PROJECT' AND `project_id` > 0)),
    CONSTRAINT `ck_pm_project_management_grant_principal` CHECK (`principal_type` IN ('USER', 'ROLE')),
    CONSTRAINT `ck_pm_project_management_grant_scope_type` CHECK (`scope_type` IN ('ALL', 'PROJECT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业项目管理范围授权表';

CREATE TABLE `pm_project_management_grant_audit`
(
    `id`             BIGINT       NOT NULL COMMENT '审计ID',
    `grant_id`       BIGINT       DEFAULT NULL COMMENT '授权ID，可为空以记录失败操作',
    `operator_id`    BIGINT       NOT NULL COMMENT '操作人',
    `action`         VARCHAR(16)  NOT NULL COMMENT 'GRANT/DISABLE/ENABLE/REVOKE',
    `principal_type` VARCHAR(16)  NOT NULL COMMENT '主体类型',
    `principal_id`   BIGINT       NOT NULL COMMENT '主体ID',
    `scope_type`     VARCHAR(16)  NOT NULL COMMENT '范围类型',
    `project_id`     BIGINT       NOT NULL DEFAULT 0 COMMENT '项目ID',
    `before_status`  CHAR(1)      DEFAULT NULL COMMENT '变更前状态',
    `after_status`   CHAR(1)      DEFAULT NULL COMMENT '变更后状态',
    `result`         CHAR(1)      NOT NULL COMMENT '结果（0成功 1失败）',
    `trace_id`       VARCHAR(64)  DEFAULT NULL COMMENT '请求追踪ID',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `remark`         VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_pm_project_grant_audit_grant` (`grant_id`, `create_time`),
    KEY `idx_pm_project_grant_audit_principal` (`principal_type`, `principal_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目管理授权安全审计';

-- ============================================================
-- 10. Property Customer - Management Grant And Security Audit
-- ============================================================

DROP TABLE IF EXISTS `pm_customer_sensitive_access_audit`;
DROP TABLE IF EXISTS `pm_customer_management_grant_audit`;
DROP TABLE IF EXISTS `pm_customer_management_grant`;

CREATE TABLE `pm_customer_management_grant`
(
    `id`               BIGINT       NOT NULL COMMENT '授权ID',
    `principal_type`   VARCHAR(16)  NOT NULL COMMENT '主体类型 USER/ROLE',
    `principal_id`     BIGINT       NOT NULL COMMENT '主体ID',
    `scope_type`       VARCHAR(16)  NOT NULL DEFAULT 'ENTERPRISE' COMMENT '范围，V2仅ENTERPRISE',
    `status`           CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `version`          INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    `create_dept`      BIGINT       DEFAULT NULL COMMENT '创建部门',
    `create_by`        BIGINT       DEFAULT NULL COMMENT '创建者',
    `create_time`      DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`        BIGINT       DEFAULT NULL COMMENT '更新者',
    `update_time`      DATETIME     DEFAULT NULL COMMENT '更新时间',
    `remark`           VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag`         CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_scope_key` VARCHAR(16) GENERATED ALWAYS AS
        (CASE WHEN `del_flag` = '0' THEN `scope_type` ELSE NULL END) STORED,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_customer_grant_active` (`principal_type`, `principal_id`, `active_scope_key`),
    KEY `idx_pm_customer_grant_principal` (`principal_type`, `principal_id`, `status`, `del_flag`),
    CONSTRAINT `ck_pm_customer_grant_principal` CHECK (`principal_type` IN ('USER', 'ROLE')),
    CONSTRAINT `ck_pm_customer_grant_scope` CHECK (`scope_type` = 'ENTERPRISE'),
    CONSTRAINT `ck_pm_customer_grant_status` CHECK (`status` IN ('0', '1'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业客户主档授权';

CREATE TABLE `pm_customer_management_grant_audit`
(
    `id`             BIGINT       NOT NULL COMMENT '审计ID',
    `grant_id`       BIGINT       DEFAULT NULL COMMENT '授权ID，失败操作可为空',
    `operator_id`    BIGINT       NOT NULL COMMENT '操作人',
    `action`         VARCHAR(16)  NOT NULL COMMENT 'GRANT/ENABLE/DISABLE/REVOKE',
    `principal_type` VARCHAR(16)  NOT NULL COMMENT '主体类型',
    `principal_id`   BIGINT       NOT NULL COMMENT '主体ID',
    `scope_type`     VARCHAR(16)  NOT NULL COMMENT '范围类型',
    `before_status`  CHAR(1)      DEFAULT NULL COMMENT '变更前状态',
    `after_status`   CHAR(1)      DEFAULT NULL COMMENT '变更后状态',
    `result`         CHAR(1)      NOT NULL COMMENT '结果（0成功 1失败）',
    `trace_id`       VARCHAR(64)  DEFAULT NULL COMMENT '请求追踪ID',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `remark`         VARCHAR(500) DEFAULT NULL COMMENT '仅允许非敏感失败原因',
    PRIMARY KEY (`id`),
    KEY `idx_pm_customer_grant_audit_grant` (`grant_id`, `create_time`),
    KEY `idx_pm_customer_grant_audit_principal` (`principal_type`, `principal_id`, `create_time`),
    KEY `idx_pm_customer_grant_audit_operator` (`operator_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户主档授权安全审计';

CREATE TABLE `pm_customer_sensitive_access_audit`
(
    `id`             BIGINT       NOT NULL COMMENT '审计ID',
    `operator_id`    BIGINT       NOT NULL COMMENT '操作人',
    `customer_id`    BIGINT       NOT NULL COMMENT '客户ID',
    `access_action`  VARCHAR(16)  NOT NULL COMMENT 'PII_VIEW/PII_EXPORT',
    `access_purpose` VARCHAR(64)  NOT NULL COMMENT '接口或业务用途编码',
    `field_level`    VARCHAR(32)  NOT NULL COMMENT '敏感字段级别',
    `decision`       VARCHAR(16)  NOT NULL COMMENT 'ALLOW/DENY/FAILED',
    `trace_id`       VARCHAR(64)  NOT NULL COMMENT '请求追踪ID',
    `project_id`     BIGINT       DEFAULT NULL COMMENT '可选项目上下文',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `remark`         VARCHAR(500) DEFAULT NULL COMMENT '非敏感原因编码',
    PRIMARY KEY (`id`),
    KEY `idx_pm_customer_sensitive_customer` (`customer_id`, `create_time`),
    KEY `idx_pm_customer_sensitive_operator` (`operator_id`, `create_time`),
    KEY `idx_pm_customer_sensitive_trace` (`trace_id`),
    CONSTRAINT `ck_pm_customer_sensitive_action` CHECK (`access_action` IN ('PII_VIEW', 'PII_EXPORT')),
    CONSTRAINT `ck_pm_customer_sensitive_decision` CHECK (`decision` IN ('ALLOW', 'DENY', 'FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户敏感资料访问审计';

-- ============================================================
-- 11. Property Finance - Charge Item
-- ============================================================

DROP TABLE IF EXISTS `pm_charge_item`;

CREATE TABLE `pm_charge_item`
(
    `id`                 BIGINT       NOT NULL COMMENT '收费项目ID',
    `project_id`         BIGINT       NOT NULL COMMENT '所属物业项目ID',
    `item_code`          VARCHAR(64)  NOT NULL COMMENT '收费项目编码',
    `item_name`          VARCHAR(128) NOT NULL COMMENT '收费项目名称',
    `charge_type`        VARCHAR(32)  NOT NULL COMMENT '收费类型',
    `charge_category`    VARCHAR(32)  NOT NULL COMMENT '收费分类',
    `charge_object_type` VARCHAR(32)  NOT NULL COMMENT '收费载体类型',
    `status`             CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `sort`               INT          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `create_dept`        BIGINT       DEFAULT NULL COMMENT '创建部门',
    `create_by`          BIGINT       DEFAULT NULL COMMENT '创建者',
    `create_time`        DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`          BIGINT       DEFAULT NULL COMMENT '更新者',
    `update_time`        DATETIME     DEFAULT NULL COMMENT '更新时间',
    `remark`             VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag`           CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_item_code`   VARCHAR(64)
        GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN `item_code` ELSE NULL END) STORED
        COMMENT '未删除收费项目编码（用于逻辑删除唯一约束）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_charge_item_active_code` (`project_id`, `active_item_code`),
    KEY `idx_pm_charge_item_project` (`project_id`, `status`, `del_flag`),
    KEY `idx_pm_charge_item_type` (`project_id`, `charge_type`, `status`, `del_flag`),
    KEY `idx_pm_charge_item_category` (`project_id`, `charge_category`, `status`, `del_flag`),
    CONSTRAINT `chk_pm_charge_item_type`
        CHECK (`charge_type` IN ('PERIODIC', 'METER', 'DEPOSIT', 'TEMPORARY', 'PREPAYMENT', 'STORED_VALUE', 'RESERVATION')),
    CONSTRAINT `chk_pm_charge_item_category`
        CHECK (`charge_category` IN ('PROPERTY_FEE', 'PARKING_FEE', 'WATER_FEE', 'ELECTRICITY_FEE', 'RENT', 'DEPOSIT', 'SERVICE_FEE', 'OTHER')),
    CONSTRAINT `chk_pm_charge_item_object_type`
        CHECK (`charge_object_type` IN ('ROOM', 'PARKING_SPACE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业收费项目表';

-- ============================================================
-- 12. Property Finance - Periodic Charge Rule
-- ROLLING 周期按收费载体起算日滚动；目标月份无对应日时取月末。
-- 具体收费载体起算日属于后续绑定模型，不存入本表。
-- ============================================================

DROP TABLE IF EXISTS `pm_charge_rule`;

CREATE TABLE `pm_charge_rule`
(
    `id`                   BIGINT        NOT NULL COMMENT '计费规则ID',
    `project_id`           BIGINT        NOT NULL COMMENT '所属物业项目ID',
    `charge_item_id`       BIGINT        NOT NULL COMMENT '收费项目ID（仅允许PERIODIC类型）',
    `rule_code`            VARCHAR(64)   NOT NULL COMMENT '计费规则编码',
    `rule_name`            VARCHAR(128)  NOT NULL COMMENT '计费规则名称',
    `billing_method`       VARCHAR(16)   NOT NULL COMMENT '计费方式（AREA按面积 FIXED固定金额）',
    `billing_cycle`        VARCHAR(16)   NOT NULL COMMENT '计费周期',
    `price_cycle`          VARCHAR(16)   NOT NULL DEFAULT 'MONTH' COMMENT '计价周期（单价/固定金额对应周期）',
    `cycle_mode`           VARCHAR(16)   NOT NULL COMMENT '周期模式（NATURAL自然周期 ROLLING循环周期）',
    `due_date_mode`        VARCHAR(32)   NOT NULL DEFAULT 'AFTER_PERIOD_START' COMMENT '到期日模式（周期开始日前/后）',
    `due_date_offset_days` TINYINT       NOT NULL DEFAULT 0 COMMENT '到期日相对周期开始日偏移天数（0-7）',
    `unit_price`           DECIMAL(18,6) DEFAULT NULL COMMENT '面积单价（元/平方米/计费周期）',
    `fixed_amount`         DECIMAL(18,2) DEFAULT NULL COMMENT '每计费周期固定金额（元）',
    `effective_start_date` DATE          NOT NULL COMMENT '规则生效日期',
    `effective_end_date`   DATE          DEFAULT NULL COMMENT '规则失效日期（空表示长期有效）',
    `status`               CHAR(1)       NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `sort`                 INT           NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `create_dept`          BIGINT        DEFAULT NULL COMMENT '创建部门',
    `create_by`            BIGINT        DEFAULT NULL COMMENT '创建者',
    `create_time`          DATETIME      DEFAULT NULL COMMENT '创建时间',
    `update_by`            BIGINT        DEFAULT NULL COMMENT '更新者',
    `update_time`          DATETIME      DEFAULT NULL COMMENT '更新时间',
    `remark`               VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    `del_flag`             CHAR(1)       NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_rule_code`     VARCHAR(64)
        GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN `rule_code` ELSE NULL END) STORED
        COMMENT '未删除计费规则编码（用于逻辑删除唯一约束）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_charge_rule_active_code` (`project_id`, `active_rule_code`),
    KEY `idx_pm_charge_rule_project` (`project_id`, `status`, `del_flag`),
    KEY `idx_pm_charge_rule_item` (`charge_item_id`, `status`, `del_flag`),
    CONSTRAINT `chk_pm_charge_rule_method`
        CHECK (`billing_method` IN ('AREA', 'FIXED')),
    CONSTRAINT `chk_pm_charge_rule_cycle`
        CHECK (`billing_cycle` IN ('MONTH', 'BIMONTHLY', 'QUARTER', 'HALF_YEAR', 'YEAR')),
    CONSTRAINT `chk_pm_charge_rule_price_cycle`
        CHECK (`price_cycle` IN ('MONTH', 'BIMONTHLY', 'QUARTER', 'HALF_YEAR', 'YEAR')),
    CONSTRAINT `chk_pm_charge_rule_cycle_mode`
        CHECK (`cycle_mode` IN ('NATURAL', 'ROLLING')),
    CONSTRAINT `chk_pm_charge_rule_due_date`
        CHECK (`due_date_mode` IN ('BEFORE_PERIOD_START', 'AFTER_PERIOD_START')
            AND `due_date_offset_days` BETWEEN 0 AND 7),
    CONSTRAINT `chk_pm_charge_rule_amount`
        CHECK ((`billing_method` = 'AREA' AND `unit_price` IS NOT NULL AND `unit_price` >= 0 AND `fixed_amount` IS NULL)
            OR (`billing_method` = 'FIXED' AND `fixed_amount` IS NOT NULL AND `fixed_amount` >= 0 AND `unit_price` IS NULL)),
    CONSTRAINT `chk_pm_charge_rule_effective_date`
        CHECK (`effective_end_date` IS NULL OR `effective_end_date` >= `effective_start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业周期性计费规则表';

-- ============================================================
-- 13. Property Finance - Charge Object Binding
-- 第一阶段业务仅开放 ROOM；表结构预留 PARKING_SPACE 扩展。
-- 日期区间为闭区间，完整重叠校验由 Service 在事务内完成。
-- ============================================================

DROP TABLE IF EXISTS `pm_charge_binding`;

CREATE TABLE `pm_charge_binding`
(
    `id`                         BIGINT       NOT NULL COMMENT '收费载体绑定ID',
    `project_id`                 BIGINT       NOT NULL COMMENT '所属物业项目ID',
    `object_type`                VARCHAR(32)  NOT NULL COMMENT '收费载体类型（ROOM房屋 PARKING_SPACE车位）',
    `object_id`                  BIGINT       NOT NULL COMMENT '收费载体ID（ROOM时为pm_room.id）',
    `charge_item_id`             BIGINT       NOT NULL COMMENT '收费项目ID',
    `charge_rule_id`             BIGINT       NOT NULL COMMENT '计费规则ID',
    `effective_start_date`       DATE         NOT NULL COMMENT '载体实际开始收费日期（ROLLING周期锚点）',
    `effective_end_date`         DATE         DEFAULT NULL COMMENT '载体收费失效日期（含当日，空表示长期有效）',
    `status`                     CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `sort`                       INT          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `create_dept`                BIGINT       DEFAULT NULL COMMENT '创建部门',
    `create_by`                  BIGINT       DEFAULT NULL COMMENT '创建者',
    `create_time`                DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`                  BIGINT       DEFAULT NULL COMMENT '更新者',
    `update_time`                DATETIME     DEFAULT NULL COMMENT '更新时间',
    `remark`                     VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag`                   CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_open_charge_item_id` BIGINT
        GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' AND `effective_end_date` IS NULL THEN `charge_item_id` ELSE NULL END) STORED
        COMMENT '未删除长期收费项目ID（用于限制同载体仅一条长期设置）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_charge_binding_active_open` (`object_type`, `object_id`, `active_open_charge_item_id`),
    KEY `idx_pm_charge_binding_project` (`project_id`, `status`, `del_flag`),
    KEY `idx_pm_charge_binding_object` (`object_type`, `object_id`, `charge_item_id`, `del_flag`),
    KEY `idx_pm_charge_binding_item` (`charge_item_id`, `del_flag`),
    KEY `idx_pm_charge_binding_rule` (`charge_rule_id`, `del_flag`),
    CONSTRAINT `chk_pm_charge_binding_object_type`
        CHECK (`object_type` IN ('ROOM', 'PARKING_SPACE')),
    CONSTRAINT `chk_pm_charge_binding_status`
        CHECK (`status` IN ('0', '1')),
    CONSTRAINT `chk_pm_charge_binding_effective_date`
        CHECK (`effective_end_date` IS NULL OR `effective_end_date` >= `effective_start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业收费载体绑定表';

-- ============================================================
-- 14. Property Finance - Receivable Generation Batch
-- 同一项目同一生成月份允许多次补生成；周期防重由 pm_receivable 保证。
-- ============================================================

DROP TABLE IF EXISTS `pm_receivable_batch`;

CREATE TABLE `pm_receivable_batch`
(
    `id`                         BIGINT        NOT NULL COMMENT '应收生成批次ID',
    `project_id`                 BIGINT        NOT NULL COMMENT '所属物业项目ID',
    `batch_no`                   VARCHAR(64)   NOT NULL COMMENT '批次业务编号',
    `generate_month`             DATE          NOT NULL COMMENT '生成月份（统一保存当月1日）',
    `status`                     VARCHAR(32)   NOT NULL COMMENT '批次状态',
    `scanned_count`              INT           NOT NULL DEFAULT 0 COMMENT '扫描收费绑定数量',
    `generated_count`            INT           NOT NULL DEFAULT 0 COMMENT '成功生成应收数量',
    `skipped_existing_count`     INT           NOT NULL DEFAULT 0 COMMENT '已有应收跳过数量',
    `skipped_not_due_count`      INT           NOT NULL DEFAULT 0 COMMENT '本月无应生成周期跳过数量',
    `failed_count`               INT           NOT NULL DEFAULT 0 COMMENT '生成失败数量',
    `started_at`                 DATETIME      DEFAULT NULL COMMENT '开始时间',
    `finished_at`                DATETIME      DEFAULT NULL COMMENT '结束时间',
    `error_message`              VARCHAR(1000) DEFAULT NULL COMMENT '批次级异常摘要',
    `create_dept`                BIGINT        DEFAULT NULL COMMENT '创建部门',
    `create_by`                  BIGINT        DEFAULT NULL COMMENT '创建者',
    `create_time`                DATETIME      DEFAULT NULL COMMENT '创建时间',
    `update_by`                  BIGINT        DEFAULT NULL COMMENT '更新者',
    `update_time`                DATETIME      DEFAULT NULL COMMENT '更新时间',
    `remark`                     VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    `del_flag`                   CHAR(1)       NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除；业务不提供删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_receivable_batch_no` (`batch_no`),
    KEY `idx_pm_receivable_batch_project_month` (`project_id`, `generate_month`, `create_time`),
    CONSTRAINT `chk_pm_receivable_batch_month` CHECK (DAYOFMONTH(`generate_month`) = 1),
    CONSTRAINT `chk_pm_receivable_batch_status`
        CHECK (`status` IN ('RUNNING', 'SUCCESS', 'PARTIAL_SUCCESS', 'FAILED')),
    CONSTRAINT `chk_pm_receivable_batch_counts`
        CHECK (`scanned_count` >= 0 AND `generated_count` >= 0 AND `skipped_existing_count` >= 0
            AND `skipped_not_due_count` >= 0 AND `failed_count` >= 0),
    CONSTRAINT `chk_pm_receivable_batch_time`
        CHECK (`finished_at` IS NULL OR `started_at` IS NULL OR `finished_at` >= `started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业应收生成批次表';

-- ============================================================
-- 15. Property Finance - Receivable
-- 每条记录代表一个收费绑定的一个独立收费周期；历史欠费不阻断后续周期。
-- ============================================================

DROP TABLE IF EXISTS `pm_receivable`;

CREATE TABLE `pm_receivable`
(
    `id`                                 BIGINT         NOT NULL COMMENT '应收ID',
    `project_id`                         BIGINT         NOT NULL COMMENT '所属物业项目ID',
    `receivable_no`                      VARCHAR(64)    NOT NULL COMMENT '应收业务编号',
    `batch_id`                           BIGINT         NOT NULL COMMENT '首次生成批次ID',
    `binding_id`                         BIGINT         NOT NULL COMMENT '收费载体绑定ID',
    `room_id`                            BIGINT         NOT NULL COMMENT '房屋ID（等于ROOM绑定的object_id）',
    `charge_item_id`                     BIGINT         NOT NULL COMMENT '收费项目ID',
    `charge_rule_id`                     BIGINT         NOT NULL COMMENT '计费规则ID',
    `generate_month`                     DATE           NOT NULL COMMENT '业务生成月份（统一保存当月1日）',
    `period_start_date`                  DATE           NOT NULL COMMENT '实际收费周期开始日期',
    `period_end_date`                    DATE           NOT NULL COMMENT '实际收费周期结束日期',
    `theoretical_start_date`             DATE           NOT NULL COMMENT '完整理论周期开始日期',
    `theoretical_end_date`               DATE           NOT NULL COMMENT '完整理论周期结束日期',
    `due_date`                           DATE           NOT NULL COMMENT '缴费到期日',
    `responsible_customer_id`            BIGINT         DEFAULT NULL COMMENT '责任客户ID快照来源',
    `responsible_customer_name_snapshot` VARCHAR(128)   DEFAULT NULL COMMENT '责任客户名称快照',
    `responsible_customer_type_snapshot` VARCHAR(32)    DEFAULT NULL COMMENT '责任客户类型快照',
    `room_full_name_snapshot`            VARCHAR(255)   NOT NULL COMMENT '房屋完整显示名称快照',
    `charge_item_code_snapshot`          VARCHAR(64)    NOT NULL COMMENT '收费项目编码快照',
    `charge_item_name_snapshot`          VARCHAR(128)   NOT NULL COMMENT '收费项目名称快照',
    `charge_rule_code_snapshot`          VARCHAR(64)    NOT NULL COMMENT '计费规则编码快照',
    `charge_rule_name_snapshot`          VARCHAR(128)   NOT NULL COMMENT '计费规则名称快照',
    `billing_method_snapshot`            VARCHAR(16)    NOT NULL COMMENT '计费方式快照',
    `billing_cycle_snapshot`             VARCHAR(16)    NOT NULL COMMENT '账单周期快照',
    `price_cycle_snapshot`               VARCHAR(16)    NOT NULL COMMENT '计价周期快照',
    `cycle_mode_snapshot`                VARCHAR(16)    NOT NULL COMMENT '周期模式快照',
    `due_date_mode_snapshot`             VARCHAR(32)    NOT NULL COMMENT '到期日模式快照',
    `due_date_offset_days_snapshot`      TINYINT        NOT NULL COMMENT '到期日偏移天数快照',
    `area_snapshot`                      DECIMAL(18,2)  DEFAULT NULL COMMENT '房屋使用面积快照（平方米）',
    `unit_price_snapshot`                DECIMAL(18,6)  DEFAULT NULL COMMENT '面积单价快照（元/平方米/计价周期）',
    `fixed_amount_snapshot`              DECIMAL(18,2)  DEFAULT NULL COMMENT '固定金额快照（元/计价周期）',
    `theoretical_days`                   INT            NOT NULL COMMENT '理论周期天数（含首尾日）',
    `actual_days`                        INT            NOT NULL COMMENT '实际计费天数（含首尾日）',
    `receivable_amount`                  DECIMAL(18,2)  NOT NULL COMMENT '应收金额',
    `paid_amount`                        DECIMAL(18,2)  NOT NULL DEFAULT 0.00 COMMENT '累计已核销金额',
    `payment_status`                     VARCHAR(16)    NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态',
    `cancel_reason`                      VARCHAR(500)   DEFAULT NULL COMMENT '作废原因',
    `cancelled_by`                       BIGINT         DEFAULT NULL COMMENT '作废人',
    `cancelled_at`                       DATETIME       DEFAULT NULL COMMENT '作废时间',
    `create_dept`                        BIGINT         DEFAULT NULL COMMENT '创建部门',
    `create_by`                          BIGINT         DEFAULT NULL COMMENT '创建者',
    `create_time`                        DATETIME       DEFAULT NULL COMMENT '创建时间',
    `update_by`                          BIGINT         DEFAULT NULL COMMENT '更新者',
    `update_time`                        DATETIME       DEFAULT NULL COMMENT '更新时间',
    `remark`                             VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    `del_flag`                           CHAR(1)        NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除；业务不提供删除）',
    `active_period_key`                  VARCHAR(64)
        GENERATED ALWAYS AS (CASE
            WHEN `del_flag` = '0' AND `payment_status` <> 'CANCELLED'
                THEN CONCAT(`binding_id`, '#', DATE_FORMAT(`period_start_date`, '%Y%m%d'), '#', DATE_FORMAT(`period_end_date`, '%Y%m%d'))
            ELSE NULL END) STORED
        COMMENT '未作废有效应收周期键（用于周期防重）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_receivable_no` (`receivable_no`),
    UNIQUE KEY `uk_pm_receivable_active_period` (`active_period_key`),
    KEY `idx_pm_receivable_project_month` (`project_id`, `generate_month`),
    KEY `idx_pm_receivable_project_room` (`project_id`, `room_id`, `payment_status`),
    KEY `idx_pm_receivable_project_customer` (`project_id`, `responsible_customer_id`, `payment_status`),
    KEY `idx_pm_receivable_binding` (`binding_id`, `period_start_date`),
    KEY `idx_pm_receivable_batch` (`batch_id`),
    KEY `idx_pm_receivable_project_due` (`project_id`, `payment_status`, `due_date`),
    CONSTRAINT `chk_pm_receivable_month` CHECK (DAYOFMONTH(`generate_month`) = 1),
    CONSTRAINT `chk_pm_receivable_period`
        CHECK (`period_end_date` >= `period_start_date`
            AND `theoretical_end_date` >= `theoretical_start_date`
            AND `period_start_date` >= `theoretical_start_date`
            AND `period_end_date` <= `theoretical_end_date`),
    CONSTRAINT `chk_pm_receivable_days`
        CHECK (`theoretical_days` > 0 AND `actual_days` > 0 AND `actual_days` <= `theoretical_days`),
    CONSTRAINT `chk_pm_receivable_snapshot_method`
        CHECK ((`billing_method_snapshot` = 'AREA' AND `area_snapshot` IS NOT NULL AND `area_snapshot` >= 0
                AND `unit_price_snapshot` IS NOT NULL AND `unit_price_snapshot` >= 0 AND `fixed_amount_snapshot` IS NULL)
            OR (`billing_method_snapshot` = 'FIXED' AND `fixed_amount_snapshot` IS NOT NULL
                AND `fixed_amount_snapshot` >= 0 AND `area_snapshot` IS NULL AND `unit_price_snapshot` IS NULL)),
    CONSTRAINT `chk_pm_receivable_cycle`
        CHECK (`billing_cycle_snapshot` IN ('MONTH', 'BIMONTHLY', 'QUARTER', 'HALF_YEAR', 'YEAR')
            AND `price_cycle_snapshot` IN ('MONTH', 'BIMONTHLY', 'QUARTER', 'HALF_YEAR', 'YEAR')
            AND `cycle_mode_snapshot` IN ('NATURAL', 'ROLLING')),
    CONSTRAINT `chk_pm_receivable_due_mode`
        CHECK (`due_date_mode_snapshot` IN ('BEFORE_PERIOD_START', 'AFTER_PERIOD_START')
            AND `due_date_offset_days_snapshot` BETWEEN 0 AND 7),
    CONSTRAINT `chk_pm_receivable_amount`
        CHECK (`receivable_amount` >= 0 AND `paid_amount` >= 0 AND `paid_amount` <= `receivable_amount`),
    CONSTRAINT `chk_pm_receivable_payment_status`
        CHECK (`payment_status` IN ('UNPAID', 'PARTIAL', 'PAID', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业应收表';

-- ============================================================
-- 16. Property Finance - Payment
-- 一笔收款归属一个房屋；付款人允许与应收责任主体不同。
-- ============================================================

DROP TABLE IF EXISTS `pm_payment`;

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
        CHECK (`payment_method` IN ('CASH', 'WECHAT', 'ALIPAY', 'BANK_TRANSFER', 'POS', 'OTHER')),
    CONSTRAINT `chk_pm_payment_status`
        CHECK (`status` IN ('VALID', 'REVERSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业收款单表';

-- ============================================================
-- 17. Property Finance - Payment Allocation
-- 核销明细是审计记录，不提供普通编辑或删除。
-- ============================================================

DROP TABLE IF EXISTS `pm_payment_allocation`;

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

-- ============================================================
-- 18. Property Finance - Payment Reversal
-- 冲销记录是不可删除、不可编辑的财务审计记录；操作人使用标准审计字段 create_by。
-- ============================================================

DROP TABLE IF EXISTS `pm_payment_reversal`;

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


-- ============================================================
-- 19. Warehouse Management - Warehouse / Zone / Location
-- ============================================================

DROP TABLE IF EXISTS `wm_warehouse_location`;
DROP TABLE IF EXISTS `wm_warehouse_zone`;
DROP TABLE IF EXISTS `wm_warehouse`;

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
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_code_key` VARCHAR(160)
        GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0'
            THEN CONCAT(`company_id`, '#', UPPER(`warehouse_code`)) ELSE NULL END) STORED
        COMMENT '未删除仓库编码唯一键',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wm_warehouse_active_code` (`active_code_key`),
    KEY `idx_wm_warehouse_company` (`company_id`, `status`, `del_flag`),
    KEY `idx_wm_warehouse_project` (`project_id`, `status`, `del_flag`),
    CONSTRAINT `chk_wm_warehouse_scope` CHECK ((`warehouse_scope` = 'COMPANY' AND `project_id` IS NULL)
        OR (`warehouse_scope` = 'PROJECT' AND `project_id` IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库主数据表';

CREATE TABLE `wm_warehouse_zone`
(
    `id` BIGINT NOT NULL COMMENT '库区ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '所属仓库ID',
    `zone_code` VARCHAR(64) NOT NULL COMMENT '库区编码',
    `zone_name` VARCHAR(128) NOT NULL COMMENT '库区名称',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_code_key` VARCHAR(160) GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0'
        THEN CONCAT(`warehouse_id`, '#', UPPER(`zone_code`)) ELSE NULL END) STORED COMMENT '未删除库区编码唯一键',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wm_zone_active_code` (`active_code_key`),
    KEY `idx_wm_zone_warehouse` (`warehouse_id`, `status`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库库区表';

CREATE TABLE `wm_warehouse_location`
(
    `id` BIGINT NOT NULL COMMENT '库位ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '所属仓库ID',
    `zone_id` BIGINT NOT NULL COMMENT '所属库区ID',
    `location_code` VARCHAR(64) NOT NULL COMMENT '库位编码',
    `location_name` VARCHAR(128) NOT NULL COMMENT '库位名称',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_code_key` VARCHAR(160) GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0'
        THEN CONCAT(`warehouse_id`, '#', UPPER(`location_code`)) ELSE NULL END) STORED COMMENT '未删除库位编码唯一键',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wm_location_active_code` (`active_code_key`),
    KEY `idx_wm_location_warehouse` (`warehouse_id`, `status`, `del_flag`),
    KEY `idx_wm_location_zone` (`zone_id`, `status`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库库位表';

-- ============================================================
-- 20. Warehouse Management - Material Master Data
-- ============================================================

DROP TABLE IF EXISTS `wm_material`;
DROP TABLE IF EXISTS `wm_unit`;
DROP TABLE IF EXISTS `wm_material_category`;

CREATE TABLE `wm_material_category`
(
    `id` BIGINT NOT NULL COMMENT '分类ID', `company_id` BIGINT NOT NULL COMMENT '所属公司/组织部门ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID', `ancestors` VARCHAR(500) NOT NULL DEFAULT '0' COMMENT '祖级列表',
    `category_code` VARCHAR(64) NOT NULL COMMENT '分类编码', `category_name` VARCHAR(128) NOT NULL COMMENT '分类名称',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序', `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门', `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间', `update_by` BIGINT DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间', `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_code_key` VARCHAR(160) GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN CONCAT(`company_id`, '#', UPPER(`category_code`)) ELSE NULL END) STORED COMMENT '未删除分类编码唯一键',
    PRIMARY KEY (`id`), UNIQUE KEY `uk_wm_material_category_active_code` (`active_code_key`),
    KEY `idx_wm_material_category_company_parent` (`company_id`, `parent_id`, `status`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料分类表';

CREATE TABLE `wm_unit`
(
    `id` BIGINT NOT NULL COMMENT '单位ID', `unit_code` VARCHAR(32) NOT NULL COMMENT '单位编码',
    `unit_name` VARCHAR(64) NOT NULL COMMENT '单位名称', `symbol` VARCHAR(32) DEFAULT NULL COMMENT '单位符号',
    `precision_scale` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '推荐小数精度（0-6）',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）', `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门', `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间', `update_by` BIGINT DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间', `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_code_key` VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN UPPER(`unit_code`) ELSE NULL END) STORED COMMENT '未删除单位编码唯一键',
    PRIMARY KEY (`id`), UNIQUE KEY `uk_wm_unit_active_code` (`active_code_key`),
    KEY `idx_wm_unit_status` (`status`, `del_flag`), CONSTRAINT `chk_wm_unit_precision` CHECK (`precision_scale` BETWEEN 0 AND 6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计量单位表';

CREATE TABLE `wm_material`
(
    `id` BIGINT NOT NULL COMMENT '物料ID', `company_id` BIGINT NOT NULL COMMENT '所属公司/组织部门ID',
    `material_code` VARCHAR(64) NOT NULL COMMENT '物料编码', `material_name` VARCHAR(128) NOT NULL COMMENT '物料名称',
    `category_id` BIGINT NOT NULL COMMENT '物料分类ID', `base_unit_id` BIGINT NOT NULL COMMENT '基础计量单位ID',
    `specification` VARCHAR(255) DEFAULT NULL COMMENT '规格描述', `model` VARCHAR(128) DEFAULT NULL COMMENT '型号',
    `barcode` VARCHAR(128) DEFAULT NULL COMMENT '条码', `safety_stock` DECIMAL(19,6) NOT NULL DEFAULT 0 COMMENT '安全库存',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）', `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门', `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间', `update_by` BIGINT DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间', `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `active_code_key` VARCHAR(160) GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN CONCAT(`company_id`, '#', UPPER(`material_code`)) ELSE NULL END) STORED COMMENT '未删除物料编码唯一键',
    `active_barcode_key` VARCHAR(256) GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' AND `barcode` IS NOT NULL AND `barcode` <> '' THEN CONCAT(`company_id`, '#', `barcode`) ELSE NULL END) STORED COMMENT '未删除非空条码唯一键',
    PRIMARY KEY (`id`), UNIQUE KEY `uk_wm_material_active_code` (`active_code_key`), UNIQUE KEY `uk_wm_material_active_barcode` (`active_barcode_key`),
    KEY `idx_wm_material_company_category` (`company_id`, `category_id`, `status`, `del_flag`), KEY `idx_wm_material_unit` (`base_unit_id`, `del_flag`),
    CONSTRAINT `chk_wm_material_safety_stock` CHECK (`safety_stock` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料主数据表';

-- ============================================================
-- 21. Warehouse Management - Inventory Balance
-- ============================================================

DROP TABLE IF EXISTS `wm_inventory`;

CREATE TABLE `wm_inventory`
(
    `id` BIGINT NOT NULL COMMENT '库存余额ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `location_id` BIGINT NOT NULL COMMENT '库位ID',
    `material_id` BIGINT NOT NULL COMMENT '物料ID',
    `quantity` DECIMAL(19,6) NOT NULL DEFAULT 0 COMMENT '当前实际库存数量',
    `locked_quantity` DECIMAL(19,6) NOT NULL DEFAULT 0 COMMENT '锁定数量',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wm_inventory_balance` (`warehouse_id`, `location_id`, `material_id`),
    KEY `idx_wm_inventory_warehouse_material` (`warehouse_id`, `material_id`),
    KEY `idx_wm_inventory_location` (`location_id`),
    KEY `idx_wm_inventory_material` (`material_id`),
    CONSTRAINT `chk_wm_inventory_quantity` CHECK (`quantity` >= 0),
    CONSTRAINT `chk_wm_inventory_locked_quantity` CHECK (`locked_quantity` >= 0 AND `locked_quantity` <= `quantity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库位级当前库存余额表';

-- ============================================================
-- 22. Warehouse Management - Immutable Inventory Transaction
-- ============================================================

DROP TABLE IF EXISTS `wm_inventory_transaction`;

CREATE TABLE `wm_inventory_transaction`
(
    `id` BIGINT NOT NULL COMMENT '库存流水ID',
    `transaction_no` VARCHAR(64) NOT NULL COMMENT '业务流水号',
    `transaction_type` VARCHAR(32) NOT NULL COMMENT '库存流水类型',
    `company_id` BIGINT NOT NULL COMMENT '发生时公司快照ID',
    `project_id` BIGINT DEFAULT NULL COMMENT '发生时项目快照ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `location_id` BIGINT NOT NULL COMMENT '库位ID',
    `material_id` BIGINT NOT NULL COMMENT '物料ID',
    `quantity_before` DECIMAL(19,6) NOT NULL COMMENT '变更前实际库存',
    `quantity_change` DECIMAL(19,6) NOT NULL COMMENT '实际库存变化量',
    `quantity_after` DECIMAL(19,6) NOT NULL COMMENT '变更后实际库存',
    `locked_before` DECIMAL(19,6) NOT NULL DEFAULT 0 COMMENT '变更前锁定数量',
    `locked_change` DECIMAL(19,6) NOT NULL DEFAULT 0 COMMENT '锁定数量变化量',
    `locked_after` DECIMAL(19,6) NOT NULL DEFAULT 0 COMMENT '变更后锁定数量',
    `source_type` VARCHAR(32) DEFAULT NULL COMMENT '来源业务类型',
    `source_id` BIGINT DEFAULT NULL COMMENT '来源业务单据ID',
    `source_no` VARCHAR(64) DEFAULT NULL COMMENT '来源业务单号',
    `occurred_at` DATETIME NOT NULL COMMENT '库存业务实际生效时间',
    `operator_id` BIGINT NOT NULL COMMENT '业务操作人用户ID',
    `create_dept` BIGINT DEFAULT NULL COMMENT '创建部门',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wm_inventory_transaction_no` (`transaction_no`),
    KEY `idx_wm_transaction_warehouse_time` (`warehouse_id`, `occurred_at`, `id`),
    KEY `idx_wm_transaction_location_time` (`location_id`, `occurred_at`),
    KEY `idx_wm_transaction_material_time` (`material_id`, `occurred_at`),
    KEY `idx_wm_transaction_source` (`source_type`, `source_id`),
    CONSTRAINT `chk_wm_transaction_quantity` CHECK (`quantity_before` >= 0 AND `quantity_after` >= 0 AND `quantity_change` <> 0),
    CONSTRAINT `chk_wm_transaction_quantity_math` CHECK (`quantity_after` = `quantity_before` + `quantity_change`),
    CONSTRAINT `chk_wm_transaction_locked` CHECK (`locked_before` >= 0 AND `locked_after` >= 0),
    CONSTRAINT `chk_wm_transaction_locked_math` CHECK (`locked_after` = `locked_before` + `locked_change`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不可变库存流水事实表';


DROP TABLE IF EXISTS `wm_inbound_order_item`;
DROP TABLE IF EXISTS `wm_inbound_order`;
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

DROP TABLE IF EXISTS `wm_outbound_order_item`;
DROP TABLE IF EXISTS `wm_outbound_order`;
CREATE TABLE `wm_outbound_order` (
 `id` BIGINT NOT NULL,`outbound_no` VARCHAR(64) NOT NULL,`company_id` BIGINT NOT NULL,`project_id` BIGINT DEFAULT NULL,`warehouse_id` BIGINT NOT NULL,`outbound_type` VARCHAR(32) NOT NULL,`status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT',`outbound_at` DATETIME NOT NULL,`confirmed_at` DATETIME DEFAULT NULL,`completed_at` DATETIME DEFAULT NULL,`cancelled_at` DATETIME DEFAULT NULL,`operator_id` BIGINT DEFAULT NULL,`source_type` VARCHAR(32) DEFAULT NULL,`source_id` BIGINT DEFAULT NULL,`source_no` VARCHAR(64) DEFAULT NULL,`create_dept` BIGINT DEFAULT NULL,`create_by` BIGINT DEFAULT NULL,`create_time` DATETIME DEFAULT NULL,`update_by` BIGINT DEFAULT NULL,`update_time` DATETIME DEFAULT NULL,`remark` VARCHAR(500) DEFAULT NULL,`del_flag` CHAR(1) NOT NULL DEFAULT '0',PRIMARY KEY(`id`),UNIQUE KEY `uk_wm_outbound_no`(`outbound_no`),KEY `idx_wm_outbound_warehouse_status`(`warehouse_id`,`status`,`outbound_at`),KEY `idx_wm_outbound_source`(`source_type`,`source_id`),CONSTRAINT `chk_wm_outbound_status` CHECK(`status` IN('DRAFT','CONFIRMED','COMPLETED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单';
CREATE TABLE `wm_outbound_order_item` (
 `id` BIGINT NOT NULL,`outbound_order_id` BIGINT NOT NULL,`material_id` BIGINT NOT NULL,`location_id` BIGINT NOT NULL,`unit_id` BIGINT NOT NULL,`quantity` DECIMAL(19,6) NOT NULL,`create_dept` BIGINT DEFAULT NULL,`create_by` BIGINT DEFAULT NULL,`create_time` DATETIME DEFAULT NULL,`update_by` BIGINT DEFAULT NULL,`update_time` DATETIME DEFAULT NULL,`remark` VARCHAR(500) DEFAULT NULL,PRIMARY KEY(`id`),UNIQUE KEY `uk_wm_outbound_item`(`outbound_order_id`,`material_id`,`location_id`),KEY `idx_wm_outbound_item_order`(`outbound_order_id`),KEY `idx_wm_outbound_item_material`(`material_id`),KEY `idx_wm_outbound_item_location`(`location_id`),CONSTRAINT `chk_wm_outbound_item_qty` CHECK(`quantity`>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单明细';

DROP TABLE IF EXISTS `wm_transfer_order_item`;
DROP TABLE IF EXISTS `wm_transfer_order`;
CREATE TABLE `wm_transfer_order` (`id` BIGINT NOT NULL,`transfer_no` VARCHAR(64) NOT NULL,`source_warehouse_id` BIGINT NOT NULL,`target_warehouse_id` BIGINT NOT NULL,`source_company_id` BIGINT NOT NULL,`source_project_id` BIGINT DEFAULT NULL,`target_company_id` BIGINT NOT NULL,`target_project_id` BIGINT DEFAULT NULL,`transfer_type` VARCHAR(32) NOT NULL,`status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT',`transfer_at` DATETIME NOT NULL,`confirmed_at` DATETIME DEFAULT NULL,`completed_at` DATETIME DEFAULT NULL,`cancelled_at` DATETIME DEFAULT NULL,`operator_id` BIGINT DEFAULT NULL,`create_dept` BIGINT DEFAULT NULL,`create_by` BIGINT DEFAULT NULL,`create_time` DATETIME DEFAULT NULL,`update_by` BIGINT DEFAULT NULL,`update_time` DATETIME DEFAULT NULL,`remark` VARCHAR(500) DEFAULT NULL,`del_flag` CHAR(1) NOT NULL DEFAULT '0',PRIMARY KEY(`id`),UNIQUE KEY `uk_wm_transfer_no`(`transfer_no`),KEY `idx_wm_transfer_source_status`(`source_warehouse_id`,`status`,`transfer_at`),KEY `idx_wm_transfer_target_status`(`target_warehouse_id`,`status`,`transfer_at`),CONSTRAINT `chk_wm_transfer_status` CHECK(`status` IN('DRAFT','CONFIRMED','COMPLETED','CANCELLED'))) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调拨单';
CREATE TABLE `wm_transfer_order_item` (`id` BIGINT NOT NULL,`transfer_order_id` BIGINT NOT NULL,`material_id` BIGINT NOT NULL,`source_location_id` BIGINT NOT NULL,`target_location_id` BIGINT NOT NULL,`unit_id` BIGINT NOT NULL,`quantity` DECIMAL(19,6) NOT NULL,`create_dept` BIGINT DEFAULT NULL,`create_by` BIGINT DEFAULT NULL,`create_time` DATETIME DEFAULT NULL,`update_by` BIGINT DEFAULT NULL,`update_time` DATETIME DEFAULT NULL,`remark` VARCHAR(500) DEFAULT NULL,PRIMARY KEY(`id`),UNIQUE KEY `uk_wm_transfer_item`(`transfer_order_id`,`material_id`,`source_location_id`,`target_location_id`),KEY `idx_wm_transfer_item_order`(`transfer_order_id`),KEY `idx_wm_transfer_item_material`(`material_id`),KEY `idx_wm_transfer_item_source_location`(`source_location_id`),KEY `idx_wm_transfer_item_target_location`(`target_location_id`),CONSTRAINT `chk_wm_transfer_item_qty` CHECK(`quantity`>0)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调拨单明细';

DROP TABLE IF EXISTS `wm_stocktake_order_item`;DROP TABLE IF EXISTS `wm_stocktake_order`;
CREATE TABLE `wm_stocktake_order` (`id` BIGINT NOT NULL,`stocktake_no` VARCHAR(64) NOT NULL,`company_id` BIGINT NOT NULL,`project_id` BIGINT DEFAULT NULL,`warehouse_id` BIGINT NOT NULL,`stocktake_type` VARCHAR(16) NOT NULL,`scope_type` VARCHAR(16) NOT NULL,`zone_id` BIGINT DEFAULT NULL,`location_id` BIGINT DEFAULT NULL,`status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT',`stocktake_at` DATETIME NOT NULL,`confirmed_at` DATETIME DEFAULT NULL,`completed_at` DATETIME DEFAULT NULL,`cancelled_at` DATETIME DEFAULT NULL,`operator_id` BIGINT DEFAULT NULL,`create_dept` BIGINT DEFAULT NULL,`create_by` BIGINT DEFAULT NULL,`create_time` DATETIME DEFAULT NULL,`update_by` BIGINT DEFAULT NULL,`update_time` DATETIME DEFAULT NULL,`remark` VARCHAR(500) DEFAULT NULL,`del_flag` CHAR(1) NOT NULL DEFAULT '0',PRIMARY KEY(`id`),UNIQUE KEY `uk_wm_stocktake_no`(`stocktake_no`),KEY `idx_wm_stocktake_warehouse_status`(`warehouse_id`,`status`,`stocktake_at`),CONSTRAINT `chk_wm_stocktake_type` CHECK(`stocktake_type` IN('FULL','PARTIAL')),CONSTRAINT `chk_wm_stocktake_scope` CHECK(`scope_type` IN('WAREHOUSE','ZONE','LOCATION')),CONSTRAINT `chk_wm_stocktake_status` CHECK(`status` IN('DRAFT','CONFIRMED','COMPLETED','CANCELLED'))) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点单';
CREATE TABLE `wm_stocktake_order_item` (`id` BIGINT NOT NULL,`stocktake_order_id` BIGINT NOT NULL,`material_id` BIGINT NOT NULL,`location_id` BIGINT NOT NULL,`unit_id` BIGINT NOT NULL,`book_quantity` DECIMAL(19,6) NOT NULL,`actual_quantity` DECIMAL(19,6) DEFAULT NULL,`difference_quantity` DECIMAL(19,6) DEFAULT NULL,`create_dept` BIGINT DEFAULT NULL,`create_by` BIGINT DEFAULT NULL,`create_time` DATETIME DEFAULT NULL,`update_by` BIGINT DEFAULT NULL,`update_time` DATETIME DEFAULT NULL,`remark` VARCHAR(500) DEFAULT NULL,PRIMARY KEY(`id`),UNIQUE KEY `uk_wm_stocktake_item`(`stocktake_order_id`,`material_id`,`location_id`),KEY `idx_wm_stocktake_item_order`(`stocktake_order_id`),KEY `idx_wm_stocktake_item_material_location`(`material_id`,`location_id`),CONSTRAINT `chk_wm_stocktake_book` CHECK(`book_quantity`>=0),CONSTRAINT `chk_wm_stocktake_actual` CHECK(`actual_quantity` IS NULL OR `actual_quantity`>=0),CONSTRAINT `chk_wm_stocktake_difference` CHECK(`difference_quantity` IS NULL OR `difference_quantity`=`actual_quantity`-`book_quantity`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点单明细';

SET FOREIGN_KEY_CHECKS = 1;
