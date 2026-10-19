-- Zero Boundary Property Management V1 database baseline.
-- Generated from the read-only zbm-data structure by script/audit/New-ZbmV1Baseline.ps1.
-- Authoritative Flyway migration for new empty MySQL 8.0 databases.
-- Do not execute against an existing non-empty database and never edit after release.
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gen_table` (
  `table_id` bigint NOT NULL COMMENT '编号',
  `data_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '数据源名称',
  `table_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '表描述',
  `class_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `frontend_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'vue' COMMENT '前端模板类型，对应 vm 下的模板目录',
  `package_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '其它生成选项',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代码生成业务表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gen_table_column` (
  `column_id` bigint NOT NULL COMMENT '编号',
  `table_id` bigint DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '字典类型',
  `sort` int DEFAULT NULL COMMENT '排序',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代码生成业务表字段';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_employee` (
  `id` bigint NOT NULL COMMENT '员工ID',
  `employee_no` varchar(32) NOT NULL COMMENT '员工编号',
  `employee_name` varchar(64) NOT NULL COMMENT '员工姓名',
  `dept_id` bigint NOT NULL COMMENT '所属部门ID',
  `post_id` bigint DEFAULT NULL COMMENT '主岗位ID',
  `user_id` bigint DEFAULT NULL COMMENT '绑定系统用户ID',
  `gender` char(1) DEFAULT NULL COMMENT '性别',
  `mobile` varchar(32) DEFAULT NULL COMMENT '手机号码',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `id_type` varchar(32) DEFAULT NULL COMMENT '证件类型',
  `id_no` varchar(64) DEFAULT NULL COMMENT '证件号码',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `entry_date` date DEFAULT NULL COMMENT '入职日期',
  `leave_date` date DEFAULT NULL COMMENT '离职日期',
  `employment_type` varchar(32) DEFAULT NULL COMMENT '用工类型',
  `employment_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '员工状态',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_employee_no` (`employee_no`),
  KEY `idx_hr_employee_user_id` (`user_id`),
  KEY `idx_hr_employee_dept_id` (`dept_id`),
  KEY `idx_hr_employee_post_id` (`post_id`),
  KEY `idx_hr_employee_name` (`employee_name`),
  KEY `idx_hr_employee_mobile` (`mobile`),
  KEY `idx_hr_employee_status` (`employment_status`,`status`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工档案表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_building` (
  `id` bigint NOT NULL COMMENT '楼栋ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `building_code` varchar(32) NOT NULL COMMENT '楼栋编码',
  `building_name` varchar(64) NOT NULL COMMENT '楼栋名称',
  `building_type` varchar(32) DEFAULT NULL COMMENT '楼栋类型',
  `above_ground_floors` int DEFAULT NULL COMMENT '地上楼层数',
  `underground_floors` int DEFAULT NULL COMMENT '地下楼层数',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `active_building_code` varchar(32) GENERATED ALWAYS AS ((case when (`del_flag` = _utf8mb4'0') then `building_code` else NULL end)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_building_active_code` (`project_id`,`active_building_code`),
  KEY `idx_pm_building_project` (`project_id`,`status`,`del_flag`),
  KEY `idx_pm_building_code` (`project_id`,`building_code`,`del_flag`),
  KEY `idx_pm_building_name` (`project_id`,`building_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业楼栋表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_charge_binding` (
  `id` bigint NOT NULL COMMENT '收费载体绑定ID',
  `project_id` bigint NOT NULL COMMENT '所属物业项目ID',
  `object_type` varchar(32) NOT NULL COMMENT '收费载体类型（ROOM房屋 PARKING_SPACE车位）',
  `object_id` bigint NOT NULL COMMENT '收费载体ID（ROOM时为pm_room.id）',
  `charge_item_id` bigint NOT NULL COMMENT '收费项目ID',
  `charge_rule_id` bigint NOT NULL COMMENT '计费规则ID',
  `effective_start_date` date NOT NULL COMMENT '载体实际开始收费日期（ROLLING周期锚点）',
  `effective_end_date` date DEFAULT NULL COMMENT '载体收费失效日期（含当日，空表示长期有效）',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `active_open_charge_item_id` bigint GENERATED ALWAYS AS ((case when ((`del_flag` = _utf8mb4'0') and (`effective_end_date` is null)) then `charge_item_id` else NULL end)) STORED COMMENT '未删除长期收费项目ID（用于限制同载体仅一条长期设置）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_charge_binding_active_open` (`object_type`,`object_id`,`active_open_charge_item_id`),
  KEY `idx_pm_charge_binding_project` (`project_id`,`status`,`del_flag`),
  KEY `idx_pm_charge_binding_object` (`object_type`,`object_id`,`charge_item_id`,`del_flag`),
  KEY `idx_pm_charge_binding_item` (`charge_item_id`,`del_flag`),
  KEY `idx_pm_charge_binding_rule` (`charge_rule_id`,`del_flag`),
  CONSTRAINT `chk_pm_charge_binding_effective_date` CHECK (((`effective_end_date` is null) or (`effective_end_date` >= `effective_start_date`))),
  CONSTRAINT `chk_pm_charge_binding_object_type` CHECK ((`object_type` in (_utf8mb4'ROOM',_utf8mb4'PARKING_SPACE'))),
  CONSTRAINT `chk_pm_charge_binding_status` CHECK ((`status` in (_utf8mb4'0',_utf8mb4'1')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业收费载体绑定表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_charge_item` (
  `id` bigint NOT NULL COMMENT '收费项目ID',
  `project_id` bigint NOT NULL COMMENT '所属物业项目ID',
  `item_code` varchar(64) NOT NULL COMMENT '收费项目编码',
  `item_name` varchar(128) NOT NULL COMMENT '收费项目名称',
  `charge_type` varchar(32) NOT NULL COMMENT '收费类型',
  `charge_category` varchar(32) NOT NULL COMMENT '收费分类',
  `charge_object_type` varchar(32) NOT NULL COMMENT '收费载体类型',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `active_item_code` varchar(64) GENERATED ALWAYS AS ((case when (`del_flag` = _utf8mb4'0') then `item_code` else NULL end)) STORED COMMENT '未删除收费项目编码（用于逻辑删除唯一约束）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_charge_item_active_code` (`project_id`,`active_item_code`),
  KEY `idx_pm_charge_item_project` (`project_id`,`status`,`del_flag`),
  KEY `idx_pm_charge_item_type` (`project_id`,`charge_type`,`status`,`del_flag`),
  KEY `idx_pm_charge_item_category` (`project_id`,`charge_category`,`status`,`del_flag`),
  CONSTRAINT `chk_pm_charge_item_category` CHECK ((`charge_category` in (_utf8mb4'PROPERTY_FEE',_utf8mb4'PARKING_FEE',_utf8mb4'WATER_FEE',_utf8mb4'ELECTRICITY_FEE',_utf8mb4'RENT',_utf8mb4'DEPOSIT',_utf8mb4'SERVICE_FEE',_utf8mb4'OTHER'))),
  CONSTRAINT `chk_pm_charge_item_object_type` CHECK ((`charge_object_type` in (_utf8mb4'ROOM',_utf8mb4'PARKING_SPACE'))),
  CONSTRAINT `chk_pm_charge_item_type` CHECK ((`charge_type` in (_utf8mb4'PERIODIC',_utf8mb4'METER',_utf8mb4'DEPOSIT',_utf8mb4'TEMPORARY',_utf8mb4'PREPAYMENT',_utf8mb4'STORED_VALUE',_utf8mb4'RESERVATION')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业收费项目表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_charge_rule` (
  `id` bigint NOT NULL COMMENT '计费规则ID',
  `project_id` bigint NOT NULL COMMENT '所属物业项目ID',
  `charge_item_id` bigint NOT NULL COMMENT '收费项目ID（仅允许PERIODIC类型）',
  `rule_code` varchar(64) NOT NULL COMMENT '计费规则编码',
  `rule_name` varchar(128) NOT NULL COMMENT '计费规则名称',
  `billing_method` varchar(16) NOT NULL COMMENT '计费方式（AREA按面积 FIXED固定金额）',
  `billing_cycle` varchar(16) NOT NULL COMMENT '计费周期',
  `price_cycle` varchar(16) NOT NULL DEFAULT 'MONTH' COMMENT '计价周期（单价/固定金额对应周期）',
  `cycle_mode` varchar(16) NOT NULL COMMENT '周期模式（NATURAL自然周期 ROLLING循环周期）',
  `due_date_mode` varchar(32) NOT NULL DEFAULT 'AFTER_PERIOD_START' COMMENT '到期日模式（周期开始日前/后）',
  `due_date_offset_days` tinyint NOT NULL DEFAULT '0' COMMENT '到期日相对周期开始日偏移天数（0-7）',
  `unit_price` decimal(18,6) DEFAULT NULL COMMENT '面积单价（元/平方米/计费周期）',
  `fixed_amount` decimal(18,2) DEFAULT NULL COMMENT '每计费周期固定金额（元）',
  `effective_start_date` date NOT NULL COMMENT '规则生效日期',
  `effective_end_date` date DEFAULT NULL COMMENT '规则失效日期（空表示长期有效）',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `active_rule_code` varchar(64) GENERATED ALWAYS AS ((case when (`del_flag` = _utf8mb4'0') then `rule_code` else NULL end)) STORED COMMENT '未删除计费规则编码（用于逻辑删除唯一约束）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_charge_rule_active_code` (`project_id`,`active_rule_code`),
  KEY `idx_pm_charge_rule_project` (`project_id`,`status`,`del_flag`),
  KEY `idx_pm_charge_rule_item` (`charge_item_id`,`status`,`del_flag`),
  CONSTRAINT `chk_pm_charge_rule_amount` CHECK ((((`billing_method` = _utf8mb4'AREA') and (`unit_price` is not null) and (`unit_price` >= 0) and (`fixed_amount` is null)) or ((`billing_method` = _utf8mb4'FIXED') and (`fixed_amount` is not null) and (`fixed_amount` >= 0) and (`unit_price` is null)))),
  CONSTRAINT `chk_pm_charge_rule_cycle` CHECK ((`billing_cycle` in (_utf8mb4'MONTH',_utf8mb4'BIMONTHLY',_utf8mb4'QUARTER',_utf8mb4'HALF_YEAR',_utf8mb4'YEAR'))),
  CONSTRAINT `chk_pm_charge_rule_cycle_mode` CHECK ((`cycle_mode` in (_utf8mb4'NATURAL',_utf8mb4'ROLLING'))),
  CONSTRAINT `chk_pm_charge_rule_due_date` CHECK (((`due_date_mode` in (_utf8mb4'BEFORE_PERIOD_START',_utf8mb4'AFTER_PERIOD_START')) and (`due_date_offset_days` between 0 and 7))),
  CONSTRAINT `chk_pm_charge_rule_effective_date` CHECK (((`effective_end_date` is null) or (`effective_end_date` >= `effective_start_date`))),
  CONSTRAINT `chk_pm_charge_rule_method` CHECK ((`billing_method` in (_utf8mb4'AREA',_utf8mb4'FIXED'))),
  CONSTRAINT `chk_pm_charge_rule_price_cycle` CHECK ((`price_cycle` in (_utf8mb4'MONTH',_utf8mb4'BIMONTHLY',_utf8mb4'QUARTER',_utf8mb4'HALF_YEAR',_utf8mb4'YEAR')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业周期性计费规则表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_customer` (
  `id` bigint NOT NULL COMMENT '客户ID',
  `customer_no` varchar(32) NOT NULL COMMENT '客户编号',
  `customer_type` varchar(32) NOT NULL DEFAULT 'PERSON' COMMENT '客户类型',
  `customer_name` varchar(128) NOT NULL COMMENT '客户名称',
  `gender` char(1) DEFAULT NULL COMMENT '性别',
  `mobile` varchar(32) DEFAULT NULL COMMENT '手机号码',
  `phone` varchar(32) DEFAULT NULL COMMENT '固定电话',
  `email` varchar(128) DEFAULT NULL COMMENT '电子邮箱',
  `id_type` varchar(32) DEFAULT NULL COMMENT '证件类型',
  `id_no` varchar(512) DEFAULT NULL COMMENT '证件号码密文（ENC_ 前缀）',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `identity_fingerprint` binary(32) DEFAULT NULL COMMENT '规范化证件身份 HMAC-SHA256',
  `active_identity_key` binary(32) GENERATED ALWAYS AS ((case when ((`del_flag` = _utf8mb4'0') and (`identity_fingerprint` is not null)) then `identity_fingerprint` else NULL end)) STORED COMMENT '有效身份唯一键',
  `pii_key_version` varchar(32) DEFAULT NULL COMMENT 'PII 字段加密密钥版本',
  `lifecycle_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '生命周期状态',
  `merged_into_customer_id` bigint DEFAULT NULL COMMENT '合并后的主客户ID',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_customer_no` (`customer_no`),
  UNIQUE KEY `uk_pm_customer_active_identity` (`id_type`,`active_identity_key`),
  KEY `idx_pm_customer_name` (`customer_name`),
  KEY `idx_pm_customer_mobile` (`mobile`),
  KEY `idx_pm_customer_id_no` (`id_type`,`id_no`),
  KEY `idx_pm_customer_status` (`customer_type`,`status`,`del_flag`),
  CONSTRAINT `chk_pm_customer_lifecycle_status` CHECK ((`lifecycle_status` in (_utf8mb4'ACTIVE',_utf8mb4'INACTIVE',_utf8mb4'CANCELLED',_utf8mb4'MERGED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业客户主数据表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_customer_management_grant` (
  `id` bigint NOT NULL COMMENT '授权ID',
  `principal_type` varchar(16) NOT NULL COMMENT '主体类型 USER/ROLE',
  `principal_id` bigint NOT NULL COMMENT '主体ID',
  `scope_type` varchar(16) NOT NULL DEFAULT 'ENTERPRISE' COMMENT '范围，V2仅ENTERPRISE',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态 0正常 1停用',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  `create_dept` bigint DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
  `active_scope_key` varchar(16) GENERATED ALWAYS AS ((case when (`del_flag` = _utf8mb4'0') then `scope_type` else NULL end)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_customer_grant_active` (`principal_type`,`principal_id`,`active_scope_key`),
  KEY `idx_pm_customer_grant_principal` (`principal_type`,`principal_id`,`status`,`del_flag`),
  CONSTRAINT `ck_pm_customer_grant_principal` CHECK ((`principal_type` in (_utf8mb4'USER',_utf8mb4'ROLE'))),
  CONSTRAINT `ck_pm_customer_grant_scope` CHECK ((`scope_type` = _utf8mb4'ENTERPRISE')),
  CONSTRAINT `ck_pm_customer_grant_status` CHECK ((`status` in (_utf8mb4'0',_utf8mb4'1')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业客户主档授权';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_customer_management_grant_audit` (
  `id` bigint NOT NULL COMMENT '审计ID',
  `grant_id` bigint DEFAULT NULL COMMENT '授权ID，失败操作可为空',
  `operator_id` bigint NOT NULL COMMENT '操作人',
  `action` varchar(16) NOT NULL COMMENT 'GRANT/ENABLE/DISABLE/REVOKE',
  `principal_type` varchar(16) NOT NULL,
  `principal_id` bigint NOT NULL,
  `scope_type` varchar(16) NOT NULL,
  `before_status` char(1) DEFAULT NULL,
  `after_status` char(1) DEFAULT NULL,
  `result` char(1) NOT NULL COMMENT '0成功 1失败',
  `trace_id` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL COMMENT '仅允许非敏感失败原因',
  PRIMARY KEY (`id`),
  KEY `idx_pm_customer_grant_audit_grant` (`grant_id`,`create_time`),
  KEY `idx_pm_customer_grant_audit_principal` (`principal_type`,`principal_id`,`create_time`),
  KEY `idx_pm_customer_grant_audit_operator` (`operator_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户主档授权安全审计';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_customer_sensitive_access_audit` (
  `id` bigint NOT NULL COMMENT '审计ID',
  `operator_id` bigint NOT NULL COMMENT '操作人',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `access_action` varchar(16) NOT NULL COMMENT 'PII_VIEW/PII_EXPORT',
  `access_purpose` varchar(64) NOT NULL COMMENT '接口或业务用途编码',
  `field_level` varchar(32) NOT NULL COMMENT '敏感字段级别',
  `decision` varchar(16) NOT NULL COMMENT 'ALLOW/DENY',
  `trace_id` varchar(64) NOT NULL COMMENT '请求追踪ID',
  `project_id` bigint DEFAULT NULL COMMENT '可选项目上下文',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL COMMENT '非敏感原因编码',
  PRIMARY KEY (`id`),
  KEY `idx_pm_customer_sensitive_customer` (`customer_id`,`create_time`),
  KEY `idx_pm_customer_sensitive_operator` (`operator_id`,`create_time`),
  KEY `idx_pm_customer_sensitive_trace` (`trace_id`),
  CONSTRAINT `ck_pm_customer_sensitive_action` CHECK ((`access_action` in (_utf8mb4'PII_VIEW',_utf8mb4'PII_EXPORT'))),
  CONSTRAINT `ck_pm_customer_sensitive_decision` CHECK ((`decision` in (_utf8mb4'ALLOW',_utf8mb4'DENY',_utf8mb4'FAILED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户敏感资料访问审计';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_payment` (
  `id` bigint NOT NULL COMMENT '收款ID',
  `project_id` bigint NOT NULL COMMENT '所属物业项目ID',
  `payment_no` varchar(64) NOT NULL COMMENT '收款业务编号',
  `room_id` bigint NOT NULL COMMENT '本次收款所属房屋ID',
  `room_full_name_snapshot` varchar(255) NOT NULL COMMENT '收款时房屋完整名称快照',
  `payer_customer_id` bigint DEFAULT NULL COMMENT '付款客户ID（允许代缴或非系统客户）',
  `payer_name_snapshot` varchar(128) NOT NULL COMMENT '付款人名称快照',
  `payer_type_snapshot` varchar(32) DEFAULT NULL COMMENT '付款人类型快照',
  `payment_amount` decimal(18,2) NOT NULL COMMENT '实际收款金额',
  `allocated_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '累计已核销金额',
  `payment_method` varchar(32) NOT NULL COMMENT '收款方式',
  `transaction_no` varchar(128) DEFAULT NULL COMMENT '支付渠道交易流水号',
  `payment_time` datetime NOT NULL COMMENT '实际收款时间',
  `status` varchar(16) NOT NULL DEFAULT 'VALID' COMMENT '收款状态（VALID有效 REVERSED已冲销）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除；业务不提供删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_payment_no` (`payment_no`),
  KEY `idx_pm_payment_project_time` (`project_id`,`payment_time`),
  KEY `idx_pm_payment_project_room_time` (`project_id`,`room_id`,`payment_time`),
  KEY `idx_pm_payment_project_payer` (`project_id`,`payer_customer_id`),
  CONSTRAINT `chk_pm_payment_amount` CHECK (((`payment_amount` > 0) and (`allocated_amount` >= 0) and (`allocated_amount` <= `payment_amount`))),
  CONSTRAINT `chk_pm_payment_method` CHECK ((`payment_method` in (_utf8mb4'CASH',_utf8mb4'WECHAT',_utf8mb4'ALIPAY',_utf8mb4'BANK_TRANSFER',_utf8mb4'POS',_utf8mb4'OTHER'))),
  CONSTRAINT `chk_pm_payment_status` CHECK ((`status` in (_utf8mb4'VALID',_utf8mb4'REVERSED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业收款单表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_payment_allocation` (
  `id` bigint NOT NULL COMMENT '核销明细ID',
  `project_id` bigint NOT NULL COMMENT '所属物业项目ID',
  `payment_id` bigint NOT NULL COMMENT '收款ID',
  `receivable_id` bigint NOT NULL COMMENT '应收ID',
  `allocation_amount` decimal(18,2) NOT NULL COMMENT '本次核销金额',
  `allocation_time` datetime NOT NULL COMMENT '核销时间',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除；业务不提供删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_payment_allocation_payment_receivable` (`payment_id`,`receivable_id`),
  KEY `idx_pm_payment_allocation_receivable` (`receivable_id`),
  KEY `idx_pm_payment_allocation_project_payment` (`project_id`,`payment_id`),
  CONSTRAINT `chk_pm_payment_allocation_amount` CHECK ((`allocation_amount` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业收款核销明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_payment_reversal` (
  `id` bigint NOT NULL COMMENT '收款冲销ID',
  `project_id` bigint NOT NULL COMMENT '所属物业项目ID',
  `reversal_no` varchar(64) NOT NULL COMMENT '冲销业务编号',
  `payment_id` bigint NOT NULL COMMENT '原收款ID',
  `reversal_amount` decimal(18,2) NOT NULL COMMENT '冲销金额（第一阶段等于原收款金额）',
  `reversal_time` datetime NOT NULL COMMENT '正式执行冲销时间',
  `reason` varchar(500) NOT NULL COMMENT '冲销原因',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '冲销操作人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除；业务不提供删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_payment_reversal_no` (`reversal_no`),
  UNIQUE KEY `uk_pm_payment_reversal_payment` (`payment_id`),
  KEY `idx_pm_payment_reversal_project_time` (`project_id`,`reversal_time`),
  CONSTRAINT `chk_pm_payment_reversal_amount` CHECK ((`reversal_amount` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业收款冲销审计表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_project` (
  `id` bigint NOT NULL COMMENT '项目ID',
  `project_code` varchar(32) NOT NULL COMMENT '项目编码',
  `project_name` varchar(128) NOT NULL COMMENT '项目名称',
  `project_short_name` varchar(64) DEFAULT NULL COMMENT '项目简称',
  `project_type` varchar(32) DEFAULT NULL COMMENT '项目类型',
  `province_code` varchar(32) DEFAULT NULL COMMENT '省编码',
  `city_code` varchar(32) DEFAULT NULL COMMENT '市编码',
  `district_code` varchar(32) DEFAULT NULL COMMENT '区县编码',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `service_phone` varchar(32) DEFAULT NULL COMMENT '服务电话',
  `manager_employee_id` bigint DEFAULT NULL COMMENT '项目负责人员工ID',
  `delivery_date` date DEFAULT NULL COMMENT '项目交付日期',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_project_code` (`project_code`),
  KEY `idx_pm_project_name` (`project_name`),
  KEY `idx_pm_project_manager` (`manager_employee_id`),
  KEY `idx_pm_project_type` (`project_type`),
  KEY `idx_pm_project_status` (`status`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业项目表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_project_management_grant` (
  `id` bigint NOT NULL COMMENT '授权ID',
  `principal_type` varchar(16) NOT NULL COMMENT '主体类型 USER/ROLE',
  `principal_id` bigint NOT NULL COMMENT '主体ID',
  `scope_type` varchar(16) NOT NULL COMMENT '范围 ALL/PROJECT',
  `project_id` bigint NOT NULL DEFAULT '0' COMMENT '项目ID，ALL固定0',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态 0正常 1停用',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  `create_dept` bigint DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
  `active_project_id` bigint GENERATED ALWAYS AS ((case when (`del_flag` = _utf8mb4'0') then `project_id` else NULL end)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_project_management_grant_active` (`principal_type`,`principal_id`,`scope_type`,`active_project_id`),
  KEY `idx_pm_project_management_grant_principal` (`principal_type`,`principal_id`,`status`,`del_flag`),
  KEY `idx_pm_project_management_grant_project` (`project_id`,`status`,`del_flag`),
  CONSTRAINT `ck_pm_project_management_grant_principal` CHECK ((`principal_type` in (_utf8mb4'USER',_utf8mb4'ROLE'))),
  CONSTRAINT `ck_pm_project_management_grant_scope` CHECK ((((`scope_type` = _utf8mb4'ALL') and (`project_id` = 0)) or ((`scope_type` = _utf8mb4'PROJECT') and (`project_id` > 0)))),
  CONSTRAINT `ck_pm_project_management_grant_scope_type` CHECK ((`scope_type` in (_utf8mb4'ALL',_utf8mb4'PROJECT')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业项目管理范围授权表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_project_management_grant_audit` (
  `id` bigint NOT NULL COMMENT '审计ID',
  `grant_id` bigint DEFAULT NULL COMMENT '授权ID，可为空以记录失败操作',
  `operator_id` bigint NOT NULL COMMENT '操作人',
  `action` varchar(16) NOT NULL COMMENT 'GRANT/DISABLE/ENABLE/REVOKE',
  `principal_type` varchar(16) NOT NULL,
  `principal_id` bigint NOT NULL,
  `scope_type` varchar(16) NOT NULL,
  `project_id` bigint NOT NULL DEFAULT '0',
  `before_status` char(1) DEFAULT NULL,
  `after_status` char(1) DEFAULT NULL,
  `result` char(1) NOT NULL COMMENT '0成功 1失败',
  `trace_id` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pm_project_grant_audit_grant` (`grant_id`,`create_time`),
  KEY `idx_pm_project_grant_audit_principal` (`principal_type`,`principal_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目管理授权安全审计';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_project_member` (
  `id` bigint NOT NULL COMMENT '项目成员关系ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `member_role` varchar(32) NOT NULL DEFAULT 'MEMBER' COMMENT '项目角色',
  `is_manager` char(1) NOT NULL DEFAULT '0' COMMENT '是否项目负责人（0否 1是）',
  `start_date` date NOT NULL COMMENT '任职开始日期',
  `end_date` date DEFAULT NULL COMMENT '任职结束日期',
  `member_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '成员关系状态',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_pm_project_member_project` (`project_id`,`member_status`,`status`,`del_flag`),
  KEY `idx_pm_project_member_employee` (`employee_id`,`member_status`,`status`,`del_flag`),
  KEY `idx_pm_project_member_project_employee` (`project_id`,`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业项目成员表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_receivable` (
  `id` bigint NOT NULL COMMENT '应收ID',
  `project_id` bigint NOT NULL COMMENT '所属物业项目ID',
  `receivable_no` varchar(64) NOT NULL COMMENT '应收业务编号',
  `batch_id` bigint NOT NULL COMMENT '首次生成批次ID',
  `binding_id` bigint NOT NULL COMMENT '收费载体绑定ID',
  `room_id` bigint NOT NULL COMMENT '房屋ID（等于ROOM绑定的object_id）',
  `charge_item_id` bigint NOT NULL COMMENT '收费项目ID',
  `charge_rule_id` bigint NOT NULL COMMENT '计费规则ID',
  `generate_month` date NOT NULL COMMENT '业务生成月份（统一保存当月1日）',
  `period_start_date` date NOT NULL COMMENT '实际收费周期开始日期',
  `period_end_date` date NOT NULL COMMENT '实际收费周期结束日期',
  `theoretical_start_date` date NOT NULL COMMENT '完整理论周期开始日期',
  `theoretical_end_date` date NOT NULL COMMENT '完整理论周期结束日期',
  `due_date` date NOT NULL COMMENT '缴费到期日',
  `responsible_customer_id` bigint DEFAULT NULL COMMENT '责任客户ID快照来源',
  `responsible_customer_name_snapshot` varchar(128) DEFAULT NULL COMMENT '责任客户名称快照',
  `responsible_customer_type_snapshot` varchar(32) DEFAULT NULL COMMENT '责任客户类型快照',
  `room_full_name_snapshot` varchar(255) NOT NULL COMMENT '房屋完整显示名称快照',
  `charge_item_code_snapshot` varchar(64) NOT NULL COMMENT '收费项目编码快照',
  `charge_item_name_snapshot` varchar(128) NOT NULL COMMENT '收费项目名称快照',
  `charge_rule_code_snapshot` varchar(64) NOT NULL COMMENT '计费规则编码快照',
  `charge_rule_name_snapshot` varchar(128) NOT NULL COMMENT '计费规则名称快照',
  `billing_method_snapshot` varchar(16) NOT NULL COMMENT '计费方式快照',
  `billing_cycle_snapshot` varchar(16) NOT NULL COMMENT '账单周期快照',
  `price_cycle_snapshot` varchar(16) NOT NULL COMMENT '计价周期快照',
  `cycle_mode_snapshot` varchar(16) NOT NULL COMMENT '周期模式快照',
  `due_date_mode_snapshot` varchar(32) NOT NULL COMMENT '到期日模式快照',
  `due_date_offset_days_snapshot` tinyint NOT NULL COMMENT '到期日偏移天数快照',
  `area_snapshot` decimal(18,2) DEFAULT NULL COMMENT '房屋使用面积快照（平方米）',
  `unit_price_snapshot` decimal(18,6) DEFAULT NULL COMMENT '面积单价快照（元/平方米/计价周期）',
  `fixed_amount_snapshot` decimal(18,2) DEFAULT NULL COMMENT '固定金额快照（元/计价周期）',
  `theoretical_days` int NOT NULL COMMENT '理论周期天数（含首尾日）',
  `actual_days` int NOT NULL COMMENT '实际计费天数（含首尾日）',
  `receivable_amount` decimal(18,2) NOT NULL COMMENT '应收金额',
  `paid_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '累计已核销金额',
  `payment_status` varchar(16) NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态',
  `cancel_reason` varchar(500) DEFAULT NULL COMMENT '作废原因',
  `cancelled_by` bigint DEFAULT NULL COMMENT '作废人',
  `cancelled_at` datetime DEFAULT NULL COMMENT '作废时间',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除；业务不提供删除）',
  `active_period_key` varchar(64) GENERATED ALWAYS AS ((case when ((`del_flag` = _utf8mb4'0') and (`payment_status` <> _utf8mb4'CANCELLED')) then concat(`binding_id`,_utf8mb4'#',date_format(`period_start_date`,_utf8mb4'%Y%m%d'),_utf8mb4'#',date_format(`period_end_date`,_utf8mb4'%Y%m%d')) else NULL end)) STORED COMMENT '未作废有效应收周期键（用于周期防重）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_receivable_no` (`receivable_no`),
  UNIQUE KEY `uk_pm_receivable_active_period` (`active_period_key`),
  KEY `idx_pm_receivable_project_month` (`project_id`,`generate_month`),
  KEY `idx_pm_receivable_project_room` (`project_id`,`room_id`,`payment_status`),
  KEY `idx_pm_receivable_project_customer` (`project_id`,`responsible_customer_id`,`payment_status`),
  KEY `idx_pm_receivable_binding` (`binding_id`,`period_start_date`),
  KEY `idx_pm_receivable_batch` (`batch_id`),
  KEY `idx_pm_receivable_project_due` (`project_id`,`payment_status`,`due_date`),
  CONSTRAINT `chk_pm_receivable_amount` CHECK (((`receivable_amount` >= 0) and (`paid_amount` >= 0) and (`paid_amount` <= `receivable_amount`))),
  CONSTRAINT `chk_pm_receivable_cycle` CHECK (((`billing_cycle_snapshot` in (_utf8mb4'MONTH',_utf8mb4'BIMONTHLY',_utf8mb4'QUARTER',_utf8mb4'HALF_YEAR',_utf8mb4'YEAR')) and (`price_cycle_snapshot` in (_utf8mb4'MONTH',_utf8mb4'BIMONTHLY',_utf8mb4'QUARTER',_utf8mb4'HALF_YEAR',_utf8mb4'YEAR')) and (`cycle_mode_snapshot` in (_utf8mb4'NATURAL',_utf8mb4'ROLLING')))),
  CONSTRAINT `chk_pm_receivable_days` CHECK (((`theoretical_days` > 0) and (`actual_days` > 0) and (`actual_days` <= `theoretical_days`))),
  CONSTRAINT `chk_pm_receivable_due_mode` CHECK (((`due_date_mode_snapshot` in (_utf8mb4'BEFORE_PERIOD_START',_utf8mb4'AFTER_PERIOD_START')) and (`due_date_offset_days_snapshot` between 0 and 7))),
  CONSTRAINT `chk_pm_receivable_month` CHECK ((dayofmonth(`generate_month`) = 1)),
  CONSTRAINT `chk_pm_receivable_payment_status` CHECK ((`payment_status` in (_utf8mb4'UNPAID',_utf8mb4'PARTIAL',_utf8mb4'PAID',_utf8mb4'CANCELLED'))),
  CONSTRAINT `chk_pm_receivable_period` CHECK (((`period_end_date` >= `period_start_date`) and (`theoretical_end_date` >= `theoretical_start_date`) and (`period_start_date` >= `theoretical_start_date`) and (`period_end_date` <= `theoretical_end_date`))),
  CONSTRAINT `chk_pm_receivable_snapshot_method` CHECK ((((`billing_method_snapshot` = _utf8mb4'AREA') and (`area_snapshot` is not null) and (`area_snapshot` >= 0) and (`unit_price_snapshot` is not null) and (`unit_price_snapshot` >= 0) and (`fixed_amount_snapshot` is null)) or ((`billing_method_snapshot` = _utf8mb4'FIXED') and (`fixed_amount_snapshot` is not null) and (`fixed_amount_snapshot` >= 0) and (`area_snapshot` is null) and (`unit_price_snapshot` is null))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业应收表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_receivable_batch` (
  `id` bigint NOT NULL COMMENT '应收生成批次ID',
  `project_id` bigint NOT NULL COMMENT '所属物业项目ID',
  `batch_no` varchar(64) NOT NULL COMMENT '批次业务编号',
  `generate_month` date NOT NULL COMMENT '生成月份（统一保存当月1日）',
  `status` varchar(32) NOT NULL COMMENT '批次状态',
  `scanned_count` int NOT NULL DEFAULT '0' COMMENT '扫描收费绑定数量',
  `generated_count` int NOT NULL DEFAULT '0' COMMENT '成功生成应收数量',
  `skipped_existing_count` int NOT NULL DEFAULT '0' COMMENT '已有应收跳过数量',
  `skipped_not_due_count` int NOT NULL DEFAULT '0' COMMENT '本月无应生成周期跳过数量',
  `failed_count` int NOT NULL DEFAULT '0' COMMENT '生成失败数量',
  `started_at` datetime DEFAULT NULL COMMENT '开始时间',
  `finished_at` datetime DEFAULT NULL COMMENT '结束时间',
  `error_message` varchar(1000) DEFAULT NULL COMMENT '批次级异常摘要',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除；业务不提供删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_receivable_batch_no` (`batch_no`),
  KEY `idx_pm_receivable_batch_project_month` (`project_id`,`generate_month`,`create_time`),
  CONSTRAINT `chk_pm_receivable_batch_counts` CHECK (((`scanned_count` >= 0) and (`generated_count` >= 0) and (`skipped_existing_count` >= 0) and (`skipped_not_due_count` >= 0) and (`failed_count` >= 0))),
  CONSTRAINT `chk_pm_receivable_batch_month` CHECK ((dayofmonth(`generate_month`) = 1)),
  CONSTRAINT `chk_pm_receivable_batch_status` CHECK ((`status` in (_utf8mb4'RUNNING',_utf8mb4'SUCCESS',_utf8mb4'PARTIAL_SUCCESS',_utf8mb4'FAILED'))),
  CONSTRAINT `chk_pm_receivable_batch_time` CHECK (((`finished_at` is null) or (`started_at` is null) or (`finished_at` >= `started_at`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业应收生成批次表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_room` (
  `id` bigint NOT NULL COMMENT '房屋ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `building_id` bigint NOT NULL COMMENT '楼栋ID',
  `unit_id` bigint NOT NULL COMMENT '单元ID',
  `room_code` varchar(64) NOT NULL COMMENT '房屋编码',
  `room_no` varchar(32) NOT NULL COMMENT '房号',
  `floor_no` int DEFAULT NULL COMMENT '所在楼层',
  `room_type` varchar(32) DEFAULT NULL COMMENT '房屋类型',
  `property_type` varchar(32) DEFAULT NULL COMMENT '产权/资源属性',
  `building_area` decimal(12,2) DEFAULT NULL COMMENT '建筑面积',
  `usable_area` decimal(12,2) DEFAULT NULL COMMENT '使用面积',
  `delivery_status` varchar(32) NOT NULL DEFAULT 'UNDELIVERED' COMMENT '交付状态',
  `occupancy_status` varchar(32) NOT NULL DEFAULT 'VACANT' COMMENT '入住状态',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `active_room_code` varchar(64) GENERATED ALWAYS AS ((case when (`del_flag` = _utf8mb4'0') then `room_code` else NULL end)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_room_active_code` (`project_id`,`active_room_code`),
  KEY `idx_pm_room_project` (`project_id`,`status`,`del_flag`),
  KEY `idx_pm_room_building` (`building_id`,`status`,`del_flag`),
  KEY `idx_pm_room_unit` (`unit_id`,`status`,`del_flag`),
  KEY `idx_pm_room_no` (`unit_id`,`room_no`,`del_flag`),
  KEY `idx_pm_room_occupancy` (`project_id`,`occupancy_status`,`status`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业房屋表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_room_customer_rel` (
  `id` bigint NOT NULL COMMENT '房屋客户关系ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `room_id` bigint NOT NULL COMMENT '房屋ID',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `relation_type` varchar(32) NOT NULL COMMENT '关系类型',
  `is_primary` char(1) NOT NULL DEFAULT '0' COMMENT '是否主要关系人（0否 1是）',
  `start_date` date NOT NULL COMMENT '关系开始日期',
  `end_date` date DEFAULT NULL COMMENT '关系结束日期',
  `move_in_date` date DEFAULT NULL COMMENT '实际迁入日期',
  `move_out_date` date DEFAULT NULL COMMENT '实际迁出日期',
  `relation_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '关系状态',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `effective_relation_key` tinyint GENERATED ALWAYS AS ((case when ((`del_flag` = _utf8mb4'0') and (`status` = _utf8mb4'0') and (`relation_status` in (_utf8mb4'PENDING',_utf8mb4'ACTIVE'))) then 1 else NULL end)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_room_customer_rel_effective` (`room_id`,`customer_id`,`relation_type`,`effective_relation_key`),
  KEY `idx_pm_room_customer_project` (`project_id`,`relation_status`,`status`,`del_flag`),
  KEY `idx_pm_room_customer_room` (`room_id`,`relation_status`,`status`,`del_flag`),
  KEY `idx_pm_room_customer_customer` (`customer_id`,`relation_status`,`status`,`del_flag`),
  KEY `idx_pm_room_customer_relation` (`room_id`,`customer_id`,`relation_type`),
  KEY `idx_pm_room_customer_project_customer` (`project_id`,`customer_id`),
  CONSTRAINT `chk_pm_room_customer_rel_dates` CHECK (((`end_date` is null) or (`end_date` >= `start_date`))),
  CONSTRAINT `chk_pm_room_customer_rel_move_dates` CHECK (((`move_out_date` is null) or (`move_in_date` is null) or (`move_out_date` >= `move_in_date`))),
  CONSTRAINT `chk_pm_room_customer_rel_primary` CHECK ((`is_primary` in (_utf8mb4'0',_utf8mb4'1'))),
  CONSTRAINT `chk_pm_room_customer_rel_status` CHECK ((`relation_status` in (_utf8mb4'PENDING',_utf8mb4'ACTIVE',_utf8mb4'ENDED'))),
  CONSTRAINT `chk_pm_room_customer_rel_type` CHECK ((`relation_type` in (_utf8mb4'OWNER',_utf8mb4'TENANT',_utf8mb4'FAMILY',_utf8mb4'RESIDENT',_utf8mb4'CONTACT',_utf8mb4'OTHER')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房屋客户关系表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pm_unit` (
  `id` bigint NOT NULL COMMENT '单元ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `building_id` bigint NOT NULL COMMENT '楼栋ID',
  `unit_code` varchar(32) NOT NULL COMMENT '单元编码',
  `unit_name` varchar(64) NOT NULL COMMENT '单元名称',
  `floor_count` int DEFAULT NULL COMMENT '楼层数',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `active_unit_code` varchar(32) GENERATED ALWAYS AS ((case when (`del_flag` = _utf8mb4'0') then `unit_code` else NULL end)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_unit_active_code` (`building_id`,`active_unit_code`),
  KEY `idx_pm_unit_project` (`project_id`,`status`,`del_flag`),
  KEY `idx_pm_unit_building` (`building_id`,`status`,`del_flag`),
  KEY `idx_pm_unit_code` (`building_id`,`unit_code`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业楼栋单元表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_client` (
  `id` bigint NOT NULL COMMENT 'id',
  `client_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户端id',
  `client_key` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户端key',
  `client_secret` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户端秘钥',
  `grant_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '授权类型',
  `device_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备类型',
  `access_path` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '允许访问路径',
  `ip_whitelist` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP白名单',
  `active_timeout` int DEFAULT '1800' COMMENT 'token活跃超时时间',
  `timeout` int DEFAULT '604800' COMMENT 'token固定超时',
  `status` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统授权表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
  `config_id` bigint NOT NULL COMMENT '参数主键',
  `config_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) COLLATE utf8mb4_unicode_ci DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='参数配置表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dept` (
  `dept_id` bigint NOT NULL COMMENT '部门id',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门id',
  `ancestors` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '部门名称',
  `dept_category` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '部门类别编码',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `leader` bigint DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `status` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`),
  KEY `idx_sys_dept_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_data` (
  `dict_code` bigint NOT NULL COMMENT '字典编码',
  `dict_sort` int DEFAULT '0' COMMENT '字典排序',
  `dict_label` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) COLLATE utf8mb4_unicode_ci DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`),
  KEY `idx_sys_dict_data_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典数据表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_type` (
  `dict_id` bigint NOT NULL COMMENT '字典主键',
  `dict_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '字典类型',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_login_info` (
  `info_id` bigint NOT NULL COMMENT '访问ID',
  `user_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户账号',
  `client_key` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '客户端',
  `device_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '设备类型',
  `ipaddr` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '操作系统',
  `status` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '登录状态（0正常 1异常）',
  `msg` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '提示消息',
  `login_time` datetime DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`),
  KEY `idx_sys_login_info_s` (`status`),
  KEY `idx_sys_login_info_lt` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统访问记录';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu` (
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `menu_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `path` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组件路径',
  `query_param` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '路由参数',
  `is_frame` char(1) COLLATE utf8mb4_unicode_ci DEFAULT 'N' COMMENT '是否为外链（Y是 N否）',
  `is_cache` char(1) COLLATE utf8mb4_unicode_ci DEFAULT 'Y' COMMENT '是否缓存（Y缓存 N不缓存）',
  `menu_type` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '显示状态（0显示 1隐藏）',
  `status` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '#' COMMENT '菜单图标',
  `active_menu` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '激活菜单路径',
  `ext` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '扩展字段',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单权限表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_message` (
  `message_id` bigint NOT NULL COMMENT '消息ID',
  `category` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息分组(system/notice/workflow)',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息类型',
  `source` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息来源',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '标题',
  `message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '摘要消息',
  `content` longtext COLLATE utf8mb4_unicode_ci COMMENT '详细内容',
  `data_json` longtext COLLATE utf8mb4_unicode_ci COMMENT '扩展数据JSON',
  `path` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '前端跳转路径',
  `send_user_ids` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '目标用户ID串，0表示全局',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`message_id`),
  KEY `idx_sys_message_category_time` (`category`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice` (
  `notice_id` bigint NOT NULL COMMENT '公告ID',
  `notice_title` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob COMMENT '公告内容',
  `status` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知公告表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_oper_log` (
  `oper_id` bigint NOT NULL COMMENT '日志主键',
  `title` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '模块标题',
  `business_type` int DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '请求方式',
  `operator_type` int DEFAULT '0' COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '操作人员',
  `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
  `dept_id` bigint DEFAULT NULL COMMENT '操作部门ID',
  `dept_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '部门名称',
  `client_key` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '客户端',
  `device_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '设备类型',
  `browser` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '操作系统',
  `oper_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(4000) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(4000) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '返回参数',
  `status` int DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(4000) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint DEFAULT '0' COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`),
  KEY `idx_sys_oper_log_bt` (`business_type`),
  KEY `idx_sys_oper_log_uid` (`user_id`),
  KEY `idx_sys_oper_log_s` (`status`),
  KEY `idx_sys_oper_log_ot` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志记录';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_oss` (
  `oss_id` bigint NOT NULL COMMENT '对象存储主键',
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '文件名',
  `original_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '原名',
  `file_suffix` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '文件后缀名',
  `url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'URL地址',
  `ext1` text COLLATE utf8mb4_unicode_ci COMMENT '扩展字段',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '上传人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `service` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'minio' COMMENT '服务商',
  PRIMARY KEY (`oss_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OSS对象存储表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_oss_config` (
  `oss_config_id` bigint NOT NULL COMMENT '主键',
  `config_key` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '配置key',
  `access_key` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT 'accessKey',
  `secret_key` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '秘钥',
  `bucket_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '桶名称',
  `prefix` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '前缀',
  `endpoint` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '访问站点',
  `domain_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '自定义域名',
  `is_https` char(1) COLLATE utf8mb4_unicode_ci DEFAULT 'N' COMMENT '是否https（Y=是,N=否）',
  `region` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '域',
  `access_policy` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1' COMMENT '桶权限类型(0=private 1=public 2=custom)',
  `status` char(1) COLLATE utf8mb4_unicode_ci DEFAULT 'N' COMMENT '是否默认（Y=是,N=否）',
  `ext1` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '扩展字段',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`oss_config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对象存储配置表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_post` (
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  `dept_id` bigint NOT NULL COMMENT '部门id',
  `post_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '岗位编码',
  `post_category` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '岗位类别编码',
  `post_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`),
  KEY `idx_sys_post_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位信息表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `role_name` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限 6：部门及以下或本人数据权限）',
  `menu_check_strictly` tinyint(1) DEFAULT '1' COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) DEFAULT '1' COMMENT '部门树选择项是否关联显示',
  `status` char(1) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`),
  KEY `idx_sys_role_create_dept` (`create_dept`),
  KEY `idx_sys_role_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色信息表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_dept` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色和部门关联表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色和菜单关联表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_social` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `auth_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台+平台唯一id',
  `source` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户来源',
  `open_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台编号唯一id',
  `user_name` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号',
  `nick_name` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户昵称',
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户邮箱',
  `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '头像地址',
  `access_token` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户的授权令牌',
  `expire_in` int DEFAULT NULL COMMENT '用户的授权令牌的有效期，部分平台可能没有',
  `refresh_token` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '刷新令牌，部分平台可能没有',
  `access_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台的授权信息，部分平台可能没有',
  `union_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户的 unionid',
  `scope` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '授予的权限，部分平台可能没有',
  `token_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '个别平台的授权信息，部分平台可能没有',
  `id_token` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'id token，部分平台可能没有',
  `mac_algorithm` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '小米平台用户的附带属性，部分平台可能没有',
  `mac_key` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '小米平台用户的附带属性，部分平台可能没有',
  `code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户的授权code，部分平台可能没有',
  `oauth_token` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Twitter平台用户的附带属性，部分平台可能没有',
  `oauth_token_secret` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Twitter平台用户的附带属性，部分平台可能没有',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社会化关系表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户昵称',
  `user_type` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT 'sys_user' COMMENT '用户类型（sys_user系统用户）',
  `email` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户邮箱',
  `phone_number` varchar(11) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '手机号码',
  `gender` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` bigint DEFAULT NULL COMMENT '头像地址',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '密码',
  `status` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `del_flag` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `login_ip` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`),
  KEY `idx_sys_user_dept_id` (`dept_id`),
  KEY `idx_sys_user_create_by` (`create_by`),
  KEY `idx_sys_user_user_name` (`user_name`),
  KEY `idx_sys_user_phone` (`phone_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_post` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`,`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户与岗位关联表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `idx_sys_user_role_rid` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户和角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wm_inbound_order` (
  `id` bigint NOT NULL,
  `inbound_no` varchar(64) NOT NULL,
  `company_id` bigint NOT NULL,
  `project_id` bigint DEFAULT NULL,
  `warehouse_id` bigint NOT NULL,
  `inbound_type` varchar(32) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'DRAFT',
  `source_type` varchar(32) DEFAULT NULL,
  `source_id` bigint DEFAULT NULL,
  `source_no` varchar(64) DEFAULT NULL,
  `inbound_at` datetime NOT NULL,
  `completed_at` datetime DEFAULT NULL,
  `operator_id` bigint DEFAULT NULL,
  `create_dept` bigint DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `del_flag` char(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wm_inbound_no` (`inbound_no`),
  KEY `idx_wm_inbound_warehouse_status` (`warehouse_id`,`status`,`inbound_at`),
  KEY `idx_wm_inbound_source` (`source_type`,`source_id`),
  CONSTRAINT `chk_wm_inbound_status` CHECK ((`status` in (_utf8mb4'DRAFT',_utf8mb4'CONFIRMED',_utf8mb4'COMPLETED',_utf8mb4'CANCELLED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入库单';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wm_inbound_order_item` (
  `id` bigint NOT NULL,
  `inbound_order_id` bigint NOT NULL,
  `material_id` bigint NOT NULL,
  `location_id` bigint NOT NULL,
  `quantity` decimal(19,6) NOT NULL,
  `unit_id` bigint NOT NULL,
  `create_dept` bigint DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wm_inbound_item` (`inbound_order_id`,`material_id`,`location_id`),
  KEY `idx_wm_inbound_item_order` (`inbound_order_id`),
  KEY `idx_wm_inbound_item_material` (`material_id`),
  KEY `idx_wm_inbound_item_location` (`location_id`),
  CONSTRAINT `chk_wm_inbound_item_qty` CHECK ((`quantity` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入库单明细';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wm_inventory` (
  `id` bigint NOT NULL COMMENT '库存余额ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `quantity` decimal(19,6) NOT NULL DEFAULT '0.000000' COMMENT '当前实际库存数量',
  `locked_quantity` decimal(19,6) NOT NULL DEFAULT '0.000000' COMMENT '锁定数量',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wm_inventory_balance` (`warehouse_id`,`location_id`,`material_id`),
  KEY `idx_wm_inventory_warehouse_material` (`warehouse_id`,`material_id`),
  KEY `idx_wm_inventory_location` (`location_id`),
  KEY `idx_wm_inventory_material` (`material_id`),
  CONSTRAINT `chk_wm_inventory_locked_quantity` CHECK (((`locked_quantity` >= 0) and (`locked_quantity` <= `quantity`))),
  CONSTRAINT `chk_wm_inventory_quantity` CHECK ((`quantity` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库位级当前库存余额表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wm_inventory_transaction` (
  `id` bigint NOT NULL COMMENT '库存流水ID',
  `transaction_no` varchar(64) NOT NULL COMMENT '业务流水号',
  `transaction_type` varchar(32) NOT NULL COMMENT '库存流水类型',
  `company_id` bigint NOT NULL COMMENT '发生时公司快照ID',
  `project_id` bigint DEFAULT NULL COMMENT '发生时项目快照ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `quantity_before` decimal(19,6) NOT NULL COMMENT '变更前实际库存',
  `quantity_change` decimal(19,6) NOT NULL COMMENT '实际库存变化量',
  `quantity_after` decimal(19,6) NOT NULL COMMENT '变更后实际库存',
  `locked_before` decimal(19,6) NOT NULL DEFAULT '0.000000' COMMENT '变更前锁定数量',
  `locked_change` decimal(19,6) NOT NULL DEFAULT '0.000000' COMMENT '锁定数量变化量',
  `locked_after` decimal(19,6) NOT NULL DEFAULT '0.000000' COMMENT '变更后锁定数量',
  `source_type` varchar(32) DEFAULT NULL COMMENT '来源业务类型',
  `source_id` bigint DEFAULT NULL COMMENT '来源业务单据ID',
  `source_no` varchar(64) DEFAULT NULL COMMENT '来源业务单号',
  `occurred_at` datetime NOT NULL COMMENT '库存业务实际生效时间',
  `operator_id` bigint NOT NULL COMMENT '业务操作人用户ID',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wm_inventory_transaction_no` (`transaction_no`),
  KEY `idx_wm_transaction_warehouse_time` (`warehouse_id`,`occurred_at`,`id`),
  KEY `idx_wm_transaction_location_time` (`location_id`,`occurred_at`),
  KEY `idx_wm_transaction_material_time` (`material_id`,`occurred_at`),
  KEY `idx_wm_transaction_source` (`source_type`,`source_id`),
  CONSTRAINT `chk_wm_transaction_locked` CHECK (((`locked_before` >= 0) and (`locked_after` >= 0))),
  CONSTRAINT `chk_wm_transaction_locked_math` CHECK ((`locked_after` = (`locked_before` + `locked_change`))),
  CONSTRAINT `chk_wm_transaction_quantity` CHECK (((`quantity_before` >= 0) and (`quantity_after` >= 0) and (`quantity_change` <> 0))),
  CONSTRAINT `chk_wm_transaction_quantity_math` CHECK ((`quantity_after` = (`quantity_before` + `quantity_change`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='不可变库存流水事实表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wm_material` (
  `id` bigint NOT NULL COMMENT '物料ID',
  `company_id` bigint NOT NULL COMMENT '所属公司/组织部门ID',
  `material_code` varchar(64) NOT NULL COMMENT '物料编码',
  `material_name` varchar(128) NOT NULL COMMENT '物料名称',
  `category_id` bigint NOT NULL COMMENT '物料分类ID',
  `base_unit_id` bigint NOT NULL COMMENT '基础计量单位ID',
  `specification` varchar(255) DEFAULT NULL COMMENT '规格描述',
  `model` varchar(128) DEFAULT NULL COMMENT '型号',
  `barcode` varchar(128) DEFAULT NULL COMMENT '条码',
  `safety_stock` decimal(19,6) NOT NULL DEFAULT '0.000000' COMMENT '安全库存',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `active_code_key` varchar(160) GENERATED ALWAYS AS ((case when (`del_flag` = _utf8mb4'0') then concat(`company_id`,_utf8mb4'#',upper(`material_code`)) else NULL end)) STORED COMMENT '未删除物料编码唯一键',
  `active_barcode_key` varchar(256) GENERATED ALWAYS AS ((case when ((`del_flag` = _utf8mb4'0') and (`barcode` is not null) and (`barcode` <> _utf8mb4'')) then concat(`company_id`,_utf8mb4'#',`barcode`) else NULL end)) STORED COMMENT '未删除非空条码唯一键',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wm_material_active_code` (`active_code_key`),
  UNIQUE KEY `uk_wm_material_active_barcode` (`active_barcode_key`),
  KEY `idx_wm_material_company_category` (`company_id`,`category_id`,`status`,`del_flag`),
  KEY `idx_wm_material_unit` (`base_unit_id`,`del_flag`),
  CONSTRAINT `chk_wm_material_safety_stock` CHECK ((`safety_stock` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物料主数据表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wm_material_category` (
  `id` bigint NOT NULL COMMENT '分类ID',
  `company_id` bigint NOT NULL COMMENT '所属公司/组织部门ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父分类ID',
  `ancestors` varchar(500) NOT NULL DEFAULT '0' COMMENT '祖级列表',
  `category_code` varchar(64) NOT NULL COMMENT '分类编码',
  `category_name` varchar(128) NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `active_code_key` varchar(160) GENERATED ALWAYS AS ((case when (`del_flag` = _utf8mb4'0') then concat(`company_id`,_utf8mb4'#',upper(`category_code`)) else NULL end)) STORED COMMENT '未删除分类编码唯一键',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wm_material_category_active_code` (`active_code_key`),
  KEY `idx_wm_material_category_company_parent` (`company_id`,`parent_id`,`status`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物料分类表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wm_outbound_order` (
  `id` bigint NOT NULL,
  `outbound_no` varchar(64) NOT NULL,
  `company_id` bigint NOT NULL,
  `project_id` bigint DEFAULT NULL,
  `warehouse_id` bigint NOT NULL,
  `outbound_type` varchar(32) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'DRAFT',
  `outbound_at` datetime NOT NULL,
  `confirmed_at` datetime DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  `cancelled_at` datetime DEFAULT NULL,
  `operator_id` bigint DEFAULT NULL,
  `source_type` varchar(32) DEFAULT NULL,
  `source_id` bigint DEFAULT NULL,
  `source_no` varchar(64) DEFAULT NULL,
  `create_dept` bigint DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `del_flag` char(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wm_outbound_no` (`outbound_no`),
  KEY `idx_wm_outbound_warehouse_status` (`warehouse_id`,`status`,`outbound_at`),
  KEY `idx_wm_outbound_source` (`source_type`,`source_id`),
  CONSTRAINT `chk_wm_outbound_status` CHECK ((`status` in (_utf8mb4'DRAFT',_utf8mb4'CONFIRMED',_utf8mb4'COMPLETED',_utf8mb4'CANCELLED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出库单';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wm_outbound_order_item` (
  `id` bigint NOT NULL,
  `outbound_order_id` bigint NOT NULL,
  `material_id` bigint NOT NULL,
  `location_id` bigint NOT NULL,
  `unit_id` bigint NOT NULL,
  `quantity` decimal(19,6) NOT NULL,
  `create_dept` bigint DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wm_outbound_item` (`outbound_order_id`,`material_id`,`location_id`),
  KEY `idx_wm_outbound_item_order` (`outbound_order_id`),
  KEY `idx_wm_outbound_item_material` (`material_id`),
  KEY `idx_wm_outbound_item_location` (`location_id`),
  CONSTRAINT `chk_wm_outbound_item_qty` CHECK ((`quantity` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出库单明细';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wm_stocktake_order` (
  `id` bigint NOT NULL,
  `stocktake_no` varchar(64) NOT NULL,
  `company_id` bigint NOT NULL,
  `project_id` bigint DEFAULT NULL,
  `warehouse_id` bigint NOT NULL,
  `stocktake_type` varchar(16) NOT NULL,
  `scope_type` varchar(16) NOT NULL,
  `zone_id` bigint DEFAULT NULL,
  `location_id` bigint DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'DRAFT',
  `stocktake_at` datetime NOT NULL,
  `confirmed_at` datetime DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  `cancelled_at` datetime DEFAULT NULL,
  `operator_id` bigint DEFAULT NULL,
  `create_dept` bigint DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `del_flag` char(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wm_stocktake_no` (`stocktake_no`),
  KEY `idx_wm_stocktake_warehouse_status` (`warehouse_id`,`status`,`stocktake_at`),
  CONSTRAINT `chk_wm_stocktake_scope` CHECK ((`scope_type` in (_utf8mb4'WAREHOUSE',_utf8mb4'ZONE',_utf8mb4'LOCATION'))),
  CONSTRAINT `chk_wm_stocktake_status` CHECK ((`status` in (_utf8mb4'DRAFT',_utf8mb4'CONFIRMED',_utf8mb4'COMPLETED',_utf8mb4'CANCELLED'))),
  CONSTRAINT `chk_wm_stocktake_type` CHECK ((`stocktake_type` in (_utf8mb4'FULL',_utf8mb4'PARTIAL')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='盘点单';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wm_stocktake_order_item` (
  `id` bigint NOT NULL,
  `stocktake_order_id` bigint NOT NULL,
  `material_id` bigint NOT NULL,
  `location_id` bigint NOT NULL,
  `unit_id` bigint NOT NULL,
  `book_quantity` decimal(19,6) NOT NULL,
  `actual_quantity` decimal(19,6) DEFAULT NULL,
  `difference_quantity` decimal(19,6) DEFAULT NULL,
  `create_dept` bigint DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wm_stocktake_item` (`stocktake_order_id`,`material_id`,`location_id`),
  KEY `idx_wm_stocktake_item_order` (`stocktake_order_id`),
  KEY `idx_wm_stocktake_item_material_location` (`material_id`,`location_id`),
  CONSTRAINT `chk_wm_stocktake_actual` CHECK (((`actual_quantity` is null) or (`actual_quantity` >= 0))),
  CONSTRAINT `chk_wm_stocktake_book` CHECK ((`book_quantity` >= 0)),
  CONSTRAINT `chk_wm_stocktake_difference` CHECK (((`difference_quantity` is null) or (`difference_quantity` = (`actual_quantity` - `book_quantity`))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='盘点单明细';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wm_transfer_order` (
  `id` bigint NOT NULL,
  `transfer_no` varchar(64) NOT NULL,
  `source_warehouse_id` bigint NOT NULL,
  `target_warehouse_id` bigint NOT NULL,
  `source_company_id` bigint NOT NULL,
  `source_project_id` bigint DEFAULT NULL,
  `target_company_id` bigint NOT NULL,
  `target_project_id` bigint DEFAULT NULL,
  `transfer_type` varchar(32) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'DRAFT',
  `transfer_at` datetime NOT NULL,
  `confirmed_at` datetime DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  `cancelled_at` datetime DEFAULT NULL,
  `operator_id` bigint DEFAULT NULL,
  `create_dept` bigint DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `del_flag` char(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wm_transfer_no` (`transfer_no`),
  KEY `idx_wm_transfer_source_status` (`source_warehouse_id`,`status`,`transfer_at`),
  KEY `idx_wm_transfer_target_status` (`target_warehouse_id`,`status`,`transfer_at`),
  CONSTRAINT `chk_wm_transfer_status` CHECK ((`status` in (_utf8mb4'DRAFT',_utf8mb4'CONFIRMED',_utf8mb4'COMPLETED',_utf8mb4'CANCELLED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调拨单';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wm_transfer_order_item` (
  `id` bigint NOT NULL,
  `transfer_order_id` bigint NOT NULL,
  `material_id` bigint NOT NULL,
  `source_location_id` bigint NOT NULL,
  `target_location_id` bigint NOT NULL,
  `unit_id` bigint NOT NULL,
  `quantity` decimal(19,6) NOT NULL,
  `create_dept` bigint DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wm_transfer_item` (`transfer_order_id`,`material_id`,`source_location_id`,`target_location_id`),
  KEY `idx_wm_transfer_item_order` (`transfer_order_id`),
  KEY `idx_wm_transfer_item_material` (`material_id`),
  KEY `idx_wm_transfer_item_source_location` (`source_location_id`),
  KEY `idx_wm_transfer_item_target_location` (`target_location_id`),
  CONSTRAINT `chk_wm_transfer_item_qty` CHECK ((`quantity` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调拨单明细';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wm_unit` (
  `id` bigint NOT NULL COMMENT '单位ID',
  `unit_code` varchar(32) NOT NULL COMMENT '单位编码',
  `unit_name` varchar(64) NOT NULL COMMENT '单位名称',
  `symbol` varchar(32) DEFAULT NULL COMMENT '单位符号',
  `precision_scale` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '推荐小数精度（0-6）',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `active_code_key` varchar(64) GENERATED ALWAYS AS ((case when (`del_flag` = _utf8mb4'0') then upper(`unit_code`) else NULL end)) STORED COMMENT '未删除单位编码唯一键',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wm_unit_active_code` (`active_code_key`),
  KEY `idx_wm_unit_status` (`status`,`del_flag`),
  CONSTRAINT `chk_wm_unit_precision` CHECK ((`precision_scale` between 0 and 6))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='计量单位表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wm_warehouse` (
  `id` bigint NOT NULL COMMENT '仓库ID',
  `company_id` bigint NOT NULL COMMENT '所属公司/组织部门ID',
  `project_id` bigint DEFAULT NULL COMMENT '所属物业项目ID',
  `warehouse_scope` varchar(16) NOT NULL COMMENT '归属类型（COMPANY/PROJECT）',
  `warehouse_code` varchar(64) NOT NULL COMMENT '仓库编码',
  `warehouse_name` varchar(128) NOT NULL COMMENT '仓库名称',
  `manager_id` bigint DEFAULT NULL COMMENT '负责人用户ID',
  `contact_phone` varchar(32) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) DEFAULT NULL COMMENT '仓库地址',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `active_code_key` varchar(160) GENERATED ALWAYS AS ((case when (`del_flag` = _utf8mb4'0') then concat(`company_id`,_utf8mb4'#',upper(`warehouse_code`)) else NULL end)) STORED COMMENT '未删除仓库编码唯一键',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wm_warehouse_active_code` (`active_code_key`),
  KEY `idx_wm_warehouse_company` (`company_id`,`status`,`del_flag`),
  KEY `idx_wm_warehouse_project` (`project_id`,`status`,`del_flag`),
  CONSTRAINT `chk_wm_warehouse_scope` CHECK ((((`warehouse_scope` = _utf8mb4'COMPANY') and (`project_id` is null)) or ((`warehouse_scope` = _utf8mb4'PROJECT') and (`project_id` is not null))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓库主数据表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wm_warehouse_location` (
  `id` bigint NOT NULL COMMENT '库位ID',
  `warehouse_id` bigint NOT NULL COMMENT '所属仓库ID',
  `zone_id` bigint NOT NULL COMMENT '所属库区ID',
  `location_code` varchar(64) NOT NULL COMMENT '库位编码',
  `location_name` varchar(128) NOT NULL COMMENT '库位名称',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `active_code_key` varchar(160) GENERATED ALWAYS AS ((case when (`del_flag` = _utf8mb4'0') then concat(`warehouse_id`,_utf8mb4'#',upper(`location_code`)) else NULL end)) STORED COMMENT '未删除库位编码唯一键',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wm_location_active_code` (`active_code_key`),
  KEY `idx_wm_location_warehouse` (`warehouse_id`,`status`,`del_flag`),
  KEY `idx_wm_location_zone` (`zone_id`,`status`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓库库位表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wm_warehouse_zone` (
  `id` bigint NOT NULL COMMENT '库区ID',
  `warehouse_id` bigint NOT NULL COMMENT '所属仓库ID',
  `zone_code` varchar(64) NOT NULL COMMENT '库区编码',
  `zone_name` varchar(128) NOT NULL COMMENT '库区名称',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `active_code_key` varchar(160) GENERATED ALWAYS AS ((case when (`del_flag` = _utf8mb4'0') then concat(`warehouse_id`,_utf8mb4'#',upper(`zone_code`)) else NULL end)) STORED COMMENT '未删除库区编码唯一键',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wm_zone_active_code` (`active_code_key`),
  KEY `idx_wm_zone_warehouse` (`warehouse_id`,`status`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓库库区表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Required bootstrap data only; no development business transactions.
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `dept_category`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1761000000000000100,0,'0','栾川县景道物业管理有限公司',NULL,0,NULL,'15888888888','xxx@qq.com','0','0',1761000000000000103,1761100000000000001,'2026-10-01 15:54:22',1761100000000000001,'2026-10-01 17:19:48'),(1761000000000000101,1761000000000000100,'0,1761000000000000100','君山怡景',NULL,1,NULL,'15888888888','xxx@qq.com','0','0',1761000000000000103,1761100000000000001,'2026-10-01 15:54:22',1761100000000000001,'2026-10-11 12:13:55'),(1761000000000000103,1761000000000000101,'0,1761000000000000100,1761000000000000101','研发部',NULL,1,1761100000000000001,'15888888888','xxx@qq.com','0','0',1761000000000000103,1761100000000000001,'2026-10-01 15:54:22',1761100000000000001,'2026-10-08 22:43:18');
INSERT INTO `sys_post` (`post_id`, `dept_id`, `post_code`, `post_category`, `post_name`, `post_sort`, `status`, `del_flag`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1761200000000000001,1761000000000000103,'ceo',NULL,'董事长',1,'0','0',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761200000000000002,1761000000000000100,'se',NULL,'项目经理',2,'0','0',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761200000000000003,1761000000000000100,'hr',NULL,'人力资源',3,'0','0',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761200000000000004,1761000000000000100,'user',NULL,'普通员工',4,'0','0',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'');
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (102113167622012935,'物业运营主管','property_operation_manager',100,'5',1,1,'0','0',1761000000000000103,1761100000000000001,'2026-10-09 10:21:20',1761100000000000001,'2026-10-09 10:21:29','物业日常运营管理；项目范围由物业项目授权控制'),(102113167622012936,'物业客服','property_customer_service',110,'5',1,1,'0','0',1761000000000000103,1761100000000000001,'2026-10-09 10:21:20',1761100000000000001,'2026-10-10 17:06:53','住户、房屋查询及入住迁出业务；项目范围由物业项目授权控制'),(102113167622012937,'物业收费员','property_cashier',120,'5',1,1,'0','0',1761000000000000103,1761100000000000001,'2026-10-09 10:21:20',1761100000000000001,'2026-10-09 10:21:29','应收查询、收款及核销，不含收款冲销；项目范围由物业项目授权控制'),(102113167622012938,'物业财务主管','property_finance_manager',130,'5',1,1,'0','0',1761000000000000103,1761100000000000001,'2026-10-09 10:21:20',1761100000000000001,'2026-10-09 10:21:29','收费配置、应收及收款管理，包含授权冲销；项目范围由物业项目授权控制'),(102113167622012939,'仓库管理员','property_warehouse_keeper',140,'5',1,1,'0','0',1761000000000000103,1761100000000000001,'2026-10-09 10:21:20',1761100000000000001,'2026-10-09 10:21:29','库存、物料查询及出入库制单，不含确认和完成操作'),(102113167622012940,'仓库主管','property_warehouse_manager',150,'5',1,1,'0','0',1761000000000000103,1761100000000000001,'2026-10-09 10:21:20',1761100000000000001,'2026-10-10 23:24:53','仓库全业务管理，包含当前已实现的确认和完成操作'),(102113167622012941,'物业项目经理','property_project_manager',160,'5',1,1,'0','0',1761000000000000103,1761100000000000001,'2026-10-09 10:21:20',1761100000000000001,'2026-10-09 10:21:29','项目运营业务管理，不含系统管理和企业级项目授权；项目范围由物业项目授权控制'),(1761300000000000001,'超级管理员','superadmin',1,'1',1,1,'0','0',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'超级管理员');
INSERT INTO `sys_user` (`user_id`, `dept_id`, `user_name`, `nick_name`, `user_type`, `email`, `phone_number`, `gender`, `avatar`, `password`, `status`, `del_flag`, `login_ip`, `login_date`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1761100000000000001,1761000000000000103,'admin','Shadow','sys_user','yfmapleforest@outlook.com','19838590923','0',NULL,'$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0','0:0:0:0:0:0:0:1','2026-10-12 19:55:33',1761000000000000103,1761100000000000001,'2026-10-01 15:54:22',-1,'2026-10-12 19:55:33','管理员');
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1761100000000000001,1761300000000000001);
INSERT INTO `sys_user_post` (`user_id`, `post_id`) VALUES (1761100000000000001,1761200000000000001);
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1761700000000000001,'用户管理-账号初始密码','sys.user.initPassword','123456','Y',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'初始化密码 123456'),(1761700000000000002,'账号自助-是否开启用户注册功能','sys.account.registerUser','false','Y',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'是否开启注册用户功能（true开启，false关闭）'),(1761700000000000003,'OSS预览列表资源开关','sys.oss.previewListResource','true','Y',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'true:开启, false:关闭');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1761500000000000001,'用户性别','sys_user_gender',1761000000000000103,1761100000000000001,'2026-10-01 15:54:26',NULL,NULL,'用户性别列表'),(1761500000000000002,'菜单状态','sys_show_hide',1761000000000000103,1761100000000000001,'2026-10-01 15:54:26',NULL,NULL,'菜单状态列表'),(1761500000000000003,'系统开关','sys_normal_disable',1761000000000000103,1761100000000000001,'2026-10-01 15:54:26',NULL,NULL,'系统开关列表'),(1761500000000000006,'系统是否','sys_yes_no',1761000000000000103,1761100000000000001,'2026-10-01 15:54:26',NULL,NULL,'系统是否列表'),(1761500000000000007,'通知类型','sys_notice_type',1761000000000000103,1761100000000000001,'2026-10-01 15:54:26',NULL,NULL,'通知类型列表'),(1761500000000000008,'通知状态','sys_notice_status',1761000000000000103,1761100000000000001,'2026-10-01 15:54:26',NULL,NULL,'通知状态列表'),(1761500000000000009,'操作类型','sys_oper_type',1761000000000000103,1761100000000000001,'2026-10-01 15:54:26',NULL,NULL,'操作类型列表'),(1761500000000000010,'系统状态','sys_common_status',1761000000000000103,1761100000000000001,'2026-10-01 15:54:26',NULL,NULL,'登录状态列表'),(1761500000000000011,'授权类型','sys_grant_type',1761000000000000103,1761100000000000001,'2026-10-01 15:54:26',NULL,NULL,'认证授权类型'),(1761500000000000012,'设备类型','sys_device_type',1761000000000000103,1761100000000000001,'2026-10-01 15:54:26',NULL,NULL,'客户端设备类型'),(2609251000000000001,'HR-用工类型','hr_employment_type',NULL,1,'2026-10-01 15:59:21',NULL,NULL,'V2 Phase1'),(2609251000000000002,'HR-员工状态','hr_employment_status',NULL,1,'2026-10-01 15:59:21',NULL,NULL,'V2 Phase1'),(2609251000000000003,'通用-证件类型','zbm_id_type',NULL,1,'2026-10-01 15:59:21',NULL,NULL,'V2 Phase1'),(2609251000000000010,'物业-项目类型','pm_project_type',NULL,1,'2026-10-01 15:59:21',NULL,NULL,'V2 Phase1'),(2609251000000000011,'物业-项目成员角色','pm_member_role',NULL,1,'2026-10-01 15:59:21',NULL,NULL,'V2 Phase1'),(2609251000000000012,'物业-项目成员状态','pm_member_status',NULL,1,'2026-10-01 15:59:21',NULL,NULL,'V2 Phase1'),(2609251000000000020,'物业-楼栋类型','pm_building_type',NULL,1,'2026-10-01 15:59:21',NULL,NULL,'V2 Phase1'),(2609251000000000021,'物业-房屋类型','pm_room_type',NULL,1,'2026-10-01 15:59:21',NULL,NULL,'V2 Phase1'),(2609251000000000022,'物业-产权资源属性','pm_property_type',NULL,1,'2026-10-01 15:59:21',NULL,NULL,'V2 Phase1'),(2609251000000000023,'物业-房屋交付状态','pm_delivery_status',NULL,1,'2026-10-01 15:59:21',NULL,NULL,'V2 Phase1'),(2609251000000000024,'物业-房屋入住状态','pm_occupancy_status',NULL,1,'2026-10-01 15:59:21',NULL,NULL,'V2 Phase1'),(2609251000000000030,'物业-客户类型','pm_customer_type',NULL,1,'2026-10-01 15:59:21',NULL,NULL,'V2 Phase1'),(2609251000000000031,'物业-房屋客户关系','pm_relation_type',NULL,1,'2026-10-01 15:59:21',NULL,NULL,'V2 Phase1'),(2609251000000000032,'物业-房屋关系状态','pm_relation_status',NULL,1,'2026-10-01 15:59:21',NULL,NULL,'V2 Phase1'),(2609251000000000033,'物业-客户生命周期','pm_customer_lifecycle',NULL,1,'2026-10-04 17:12:28',NULL,NULL,'Phase 1 Property 中文展示'),(2609251000000000034,'物业-管理授权主体类型','pm_management_principal_type',NULL,1,'2026-10-04 17:12:28',NULL,NULL,'Phase 1 Property 中文展示'),(2609251000000000035,'物业-管理授权范围类型','pm_management_scope_type',NULL,1,'2026-10-04 17:12:28',NULL,NULL,'Phase 1 Property 中文展示'),(2609251000000000036,'物业-收费类型','pm_charge_type',NULL,1,'2026-10-06 11:13:25',NULL,NULL,'TASK-016A 收费项目'),(2609251000000000037,'物业-收费分类','pm_charge_category',NULL,1,'2026-10-06 11:13:25',NULL,NULL,'TASK-016A 收费项目'),(2609251000000000038,'物业-收费载体类型','pm_charge_object_type',NULL,1,'2026-10-06 11:13:25',NULL,NULL,'TASK-016 收费载体类型'),(2609251000000000039,'物业-计费方式','pm_billing_method',NULL,1,'2026-10-06 16:20:22',NULL,NULL,'Property Baseline'),(2609251000000000040,'物业-计费周期','pm_billing_cycle',NULL,1,'2026-10-06 16:20:22',NULL,NULL,'Property Baseline'),(2609251000000000041,'物业-周期模式','pm_cycle_mode',NULL,1,'2026-10-06 16:20:22',NULL,NULL,'Property Baseline'),(2609251000000000042,'物业-到期日模式','pm_due_date_mode',NULL,1,'2026-10-07 09:53:31',NULL,NULL,'Property Receivable'),(2609251000000000043,'物业-应收批次状态','pm_receivable_batch_status',NULL,1,'2026-10-07 09:53:31',NULL,NULL,'Property Receivable'),(2609251000000000044,'物业-支付状态','pm_payment_status',NULL,1,'2026-10-07 09:53:31',NULL,NULL,'Property Receivable'),(2609251000000000045,'物业-收款方式','pm_payment_method',NULL,1,'2026-10-07 14:53:04',NULL,NULL,'Property Payment'),(2609251000000000046,'物业-收款记录状态','pm_payment_record_status',NULL,1,'2026-10-07 14:53:04',NULL,NULL,'Property Payment'),(2609251000000000050,'仓库-归属类型','wm_warehouse_scope',NULL,1,'2026-10-08 10:54:02',NULL,NULL,'Warehouse Phase 1'),(2609251000000000051,'仓库-库存流水类型','wm_inventory_transaction_type',NULL,1,'2026-10-08 10:54:03',NULL,NULL,'Warehouse Phase 1'),(2609251000000000052,'仓库-入库单状态','wm_inbound_status',NULL,1,'2026-10-08 10:54:04',NULL,NULL,'Warehouse Phase 1'),(2609251000000000053,'仓库-入库类型','wm_inbound_type',NULL,1,'2026-10-08 10:54:04',NULL,NULL,'Warehouse Phase 1'),(2609251000000000054,'仓库-出库单状态','wm_outbound_status',NULL,1,'2026-10-08 17:38:26',NULL,NULL,'Warehouse Phase 1'),(2609251000000000055,'仓库-出库类型','wm_outbound_type',NULL,1,'2026-10-08 17:38:26',NULL,NULL,'Warehouse Phase 1'),(2609251000000000056,'仓库-调拨单状态','wm_transfer_status',NULL,1,'2026-10-08 17:38:26',NULL,NULL,'Warehouse Phase 1'),(2609251000000000057,'仓库-调拨类型','wm_transfer_type',NULL,1,'2026-10-08 17:38:26',NULL,NULL,'Warehouse Phase 1'),(2609251000000000058,'仓库-盘点单状态','wm_stocktake_status',NULL,1,'2026-10-08 17:38:27',NULL,NULL,'Warehouse Phase 1'),(2609251000000000059,'仓库-盘点类型','wm_stocktake_type',NULL,1,'2026-10-08 17:38:27',NULL,NULL,'Warehouse Phase 1');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1761600000000000001,1,'男','0','sys_user_gender','','','Y',1761000000000000103,1761100000000000001,'2026-10-01 15:54:26',NULL,NULL,'性别男'),(1761600000000000002,2,'女','1','sys_user_gender','','','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:26',NULL,NULL,'性别女'),(1761600000000000003,3,'未知','2','sys_user_gender','','','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:26',NULL,NULL,'性别未知'),(1761600000000000004,1,'显示','0','sys_show_hide','','primary','Y',1761000000000000103,1761100000000000001,'2026-10-01 15:54:26',NULL,NULL,'显示菜单'),(1761600000000000005,2,'隐藏','1','sys_show_hide','','danger','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:26',NULL,NULL,'隐藏菜单'),(1761600000000000006,1,'正常','0','sys_normal_disable','','primary','Y',1761000000000000103,1761100000000000001,'2026-10-01 15:54:26',NULL,NULL,'正常状态'),(1761600000000000007,2,'停用','1','sys_normal_disable','','danger','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'停用状态'),(1761600000000000012,1,'是','Y','sys_yes_no','','primary','Y',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'系统默认是'),(1761600000000000013,2,'否','N','sys_yes_no','','danger','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'系统默认否'),(1761600000000000014,1,'通知','1','sys_notice_type','','warning','Y',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'通知'),(1761600000000000015,2,'公告','2','sys_notice_type','','success','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'公告'),(1761600000000000016,1,'正常','0','sys_notice_status','','primary','Y',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'正常状态'),(1761600000000000017,2,'关闭','1','sys_notice_status','','danger','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'关闭状态'),(1761600000000000018,1,'新增','1','sys_oper_type','','info','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'新增操作'),(1761600000000000019,2,'修改','2','sys_oper_type','','info','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'修改操作'),(1761600000000000020,3,'删除','3','sys_oper_type','','danger','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'删除操作'),(1761600000000000021,4,'授权','4','sys_oper_type','','primary','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'授权操作'),(1761600000000000022,5,'导出','5','sys_oper_type','','warning','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'导出操作'),(1761600000000000023,6,'导入','6','sys_oper_type','','warning','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'导入操作'),(1761600000000000024,7,'强退','7','sys_oper_type','','danger','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'强退操作'),(1761600000000000025,8,'生成代码','8','sys_oper_type','','warning','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'生成操作'),(1761600000000000026,9,'清空数据','9','sys_oper_type','','danger','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'清空操作'),(1761600000000000027,1,'成功','0','sys_common_status','','primary','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'正常状态'),(1761600000000000028,2,'失败','1','sys_common_status','','danger','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'停用状态'),(1761600000000000029,99,'其他','0','sys_oper_type','','info','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'其他操作'),(1761600000000000030,0,'密码认证','password','sys_grant_type','el-check-tag','default','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'密码认证'),(1761600000000000031,0,'短信认证','sms','sys_grant_type','el-check-tag','default','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'短信认证'),(1761600000000000032,0,'邮件认证','email','sys_grant_type','el-check-tag','default','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'邮件认证'),(1761600000000000033,0,'小程序认证','xcx','sys_grant_type','el-check-tag','default','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'小程序认证'),(1761600000000000034,0,'三方登录认证','social','sys_grant_type','el-check-tag','default','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'三方登录认证'),(1761600000000000035,0,'PC','pc','sys_device_type','','default','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'PC'),(1761600000000000036,0,'安卓','android','sys_device_type','','default','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'安卓'),(1761600000000000037,0,'iOS','ios','sys_device_type','','default','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'iOS'),(1761600000000000038,0,'小程序','xcx','sys_device_type','','default','N',1761000000000000103,1761100000000000001,'2026-10-01 15:54:27',NULL,NULL,'小程序'),(2609251100000000001,1,'正式员工','FORMAL','hr_employment_type',NULL,'primary','Y',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000002,2,'试用员工','PROBATION','hr_employment_type',NULL,'warning','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000003,3,'合同人员','CONTRACT','hr_employment_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000004,4,'外包人员','OUTSOURCE','hr_employment_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000005,5,'实习人员','INTERN','hr_employment_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000006,6,'临时人员','TEMPORARY','hr_employment_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000007,7,'其他','OTHER','hr_employment_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000010,1,'待入职','PENDING','hr_employment_status',NULL,'warning','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000011,2,'在职','ACTIVE','hr_employment_status',NULL,'success','Y',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000012,3,'停职','SUSPENDED','hr_employment_status',NULL,'danger','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000013,4,'离职','LEFT','hr_employment_status',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000020,1,'居民身份证','ID_CARD','zbm_id_type',NULL,'primary','Y',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000021,2,'护照','PASSPORT','zbm_id_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000022,3,'其他','OTHER','zbm_id_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000030,1,'住宅','RESIDENTIAL','pm_project_type',NULL,'primary','Y',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000031,2,'商业','COMMERCIAL','pm_project_type',NULL,'success','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000032,3,'写字楼','OFFICE','pm_project_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000033,4,'园区','PARK','pm_project_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000034,5,'产业园','INDUSTRIAL','pm_project_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000035,6,'综合体','MIXED','pm_project_type',NULL,'warning','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000036,7,'其他','OTHER','pm_project_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000040,1,'项目负责人','PROJECT_MANAGER','pm_member_role',NULL,'danger','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000041,2,'客服负责人','CUSTOMER_SERVICE_MANAGER','pm_member_role',NULL,'warning','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000042,3,'工程负责人','ENGINEERING_MANAGER','pm_member_role',NULL,'warning','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000043,4,'收费负责人','FEE_MANAGER','pm_member_role',NULL,'warning','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000044,5,'普通成员','MEMBER','pm_member_role',NULL,'info','Y',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000050,1,'有效','ACTIVE','pm_member_status',NULL,'success','Y',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000051,2,'已结束','INACTIVE','pm_member_status',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000060,1,'住宅','RESIDENTIAL','pm_building_type',NULL,'primary','Y',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000061,2,'商业','COMMERCIAL','pm_building_type',NULL,'success','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000062,3,'写字楼','OFFICE','pm_building_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000063,4,'公共建筑','PUBLIC','pm_building_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000064,5,'停车建筑','PARKING','pm_building_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000065,6,'其他','OTHER','pm_building_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000070,1,'住宅','RESIDENCE','pm_room_type',NULL,'primary','Y',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000071,2,'商铺','SHOP','pm_room_type',NULL,'success','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000072,3,'办公室','OFFICE','pm_room_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000073,4,'仓储','STORAGE','pm_room_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000074,5,'其他','OTHER','pm_room_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000080,1,'私有产权','PRIVATE','pm_property_type',NULL,'primary','Y',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000081,2,'公共产权','PUBLIC','pm_property_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000082,3,'商业产权','COMMERCIAL','pm_property_type',NULL,'success','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000083,4,'物业用房','PROPERTY_USE','pm_property_type',NULL,'warning','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000084,5,'其他','OTHER','pm_property_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000090,1,'未交付','UNDELIVERED','pm_delivery_status',NULL,'warning','Y',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000091,2,'已交付','DELIVERED','pm_delivery_status',NULL,'success','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000100,1,'空置','VACANT','pm_occupancy_status',NULL,'info','Y',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000101,2,'已入住','OCCUPIED','pm_occupancy_status',NULL,'success','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000110,1,'个人','PERSON','pm_customer_type',NULL,'primary','Y',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000111,2,'企业','COMPANY','pm_customer_type',NULL,'success','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000120,1,'产权人/业主','OWNER','pm_relation_type',NULL,'danger','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000121,2,'租户','TENANT','pm_relation_type',NULL,'warning','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000122,3,'家庭成员','FAMILY','pm_relation_type',NULL,'success','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000123,4,'实际居住人','RESIDENT','pm_relation_type',NULL,'primary','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000124,5,'联系人','CONTACT','pm_relation_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000125,6,'其他','OTHER','pm_relation_type',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000130,1,'待生效','PENDING','pm_relation_status',NULL,'warning','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000131,2,'有效','ACTIVE','pm_relation_status',NULL,'success','Y',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000132,3,'已结束','ENDED','pm_relation_status',NULL,'info','N',NULL,1,'2026-10-01 15:59:21',NULL,NULL,NULL),(2609251100000000140,1,'正常','ACTIVE','pm_customer_lifecycle',NULL,'success','Y',NULL,1,'2026-10-04 17:12:28',NULL,NULL,NULL),(2609251100000000141,2,'停用','INACTIVE','pm_customer_lifecycle',NULL,'warning','N',NULL,1,'2026-10-04 17:12:28',NULL,NULL,NULL),(2609251100000000142,3,'注销','CANCELLED','pm_customer_lifecycle',NULL,'danger','N',NULL,1,'2026-10-04 17:12:28',NULL,NULL,NULL),(2609251100000000143,4,'已合并','MERGED','pm_customer_lifecycle',NULL,'info','N',NULL,1,'2026-10-04 17:12:28',NULL,NULL,NULL),(2609251100000000150,1,'用户','USER','pm_management_principal_type',NULL,'primary','Y',NULL,1,'2026-10-04 17:12:28',NULL,NULL,NULL),(2609251100000000151,2,'角色','ROLE','pm_management_principal_type',NULL,'success','N',NULL,1,'2026-10-04 17:12:28',NULL,NULL,NULL),(2609251100000000152,1,'全部项目','ALL','pm_management_scope_type',NULL,'warning','Y',NULL,1,'2026-10-04 17:12:28',NULL,NULL,NULL),(2609251100000000153,2,'指定项目','PROJECT','pm_management_scope_type',NULL,'primary','N',NULL,1,'2026-10-04 17:12:28',NULL,NULL,NULL),(2609251100000000160,1,'周期性','PERIODIC','pm_charge_type',NULL,'primary','Y',NULL,1,'2026-10-06 11:13:25',NULL,NULL,'周期生成费用'),(2609251100000000161,2,'仪表类','METER','pm_charge_type',NULL,'success','N',NULL,1,'2026-10-06 11:13:25',NULL,NULL,'按抄表用量生成费用'),(2609251100000000162,3,'押金类','DEPOSIT','pm_charge_type',NULL,'warning','N',NULL,1,'2026-10-06 11:13:25',NULL,NULL,'产生押金台账'),(2609251100000000163,4,'临时类','TEMPORARY','pm_charge_type',NULL,'info','N',NULL,1,'2026-10-06 11:13:25',NULL,NULL,'临时单次费用'),(2609251100000000164,5,'预收类','PREPAYMENT','pm_charge_type',NULL,'primary','N',NULL,1,'2026-10-06 11:13:25',NULL,NULL,'预存后抵扣正常应收'),(2609251100000000165,6,'储值类','STORED_VALUE','pm_charge_type',NULL,'success','N',NULL,1,'2026-10-06 11:13:25',NULL,NULL,'充值余额直接消费'),(2609251100000000166,7,'订金类','RESERVATION','pm_charge_type',NULL,'warning','N',NULL,1,'2026-10-06 11:13:25',NULL,NULL,'订金或保证金'),(2609251100000000170,1,'物业费','PROPERTY_FEE','pm_charge_category',NULL,'primary','Y',NULL,1,'2026-10-06 11:13:25',NULL,NULL,NULL),(2609251100000000171,2,'停车费','PARKING_FEE','pm_charge_category',NULL,'info','N',NULL,1,'2026-10-06 11:13:25',NULL,NULL,NULL),(2609251100000000172,3,'水费','WATER_FEE','pm_charge_category',NULL,'success','N',NULL,1,'2026-10-06 11:13:25',NULL,NULL,NULL),(2609251100000000173,4,'电费','ELECTRICITY_FEE','pm_charge_category',NULL,'warning','N',NULL,1,'2026-10-06 11:13:25',NULL,NULL,NULL),(2609251100000000174,5,'租金','RENT','pm_charge_category',NULL,'danger','N',NULL,1,'2026-10-06 11:13:25',NULL,NULL,NULL),(2609251100000000175,6,'押金','DEPOSIT','pm_charge_category',NULL,'warning','N',NULL,1,'2026-10-06 11:13:25',NULL,NULL,NULL),(2609251100000000176,7,'服务费','SERVICE_FEE','pm_charge_category',NULL,'success','N',NULL,1,'2026-10-06 11:13:25',NULL,NULL,NULL),(2609251100000000177,8,'其他','OTHER','pm_charge_category',NULL,'info','N',NULL,1,'2026-10-06 11:13:25',NULL,NULL,NULL),(2609251100000000180,1,'房屋','ROOM','pm_charge_object_type',NULL,'primary','Y',NULL,1,'2026-10-06 11:13:25',NULL,NULL,NULL),(2609251100000000181,2,'车位','PARKING_SPACE','pm_charge_object_type',NULL,'info','N',NULL,1,'2026-10-06 11:13:25',NULL,NULL,NULL),(2609251100000000190,1,'按面积','AREA','pm_billing_method',NULL,'primary','Y',NULL,1,'2026-10-06 16:20:22',NULL,NULL,NULL),(2609251100000000191,2,'固定金额','FIXED','pm_billing_method',NULL,'success','N',NULL,1,'2026-10-06 16:20:22',NULL,NULL,NULL),(2609251100000000192,1,'每月','MONTH','pm_billing_cycle',NULL,'primary','Y',NULL,1,'2026-10-06 16:20:22',NULL,NULL,NULL),(2609251100000000193,2,'双月','BIMONTHLY','pm_billing_cycle',NULL,'info','N',NULL,1,'2026-10-06 16:20:22',NULL,NULL,NULL),(2609251100000000194,3,'季度','QUARTER','pm_billing_cycle',NULL,'success','N',NULL,1,'2026-10-06 16:20:22',NULL,NULL,NULL),(2609251100000000195,4,'半年','HALF_YEAR','pm_billing_cycle',NULL,'warning','N',NULL,1,'2026-10-06 16:20:22',NULL,NULL,NULL),(2609251100000000196,5,'每年','YEAR','pm_billing_cycle',NULL,'danger','N',NULL,1,'2026-10-06 16:20:22',NULL,NULL,NULL),(2609251100000000197,1,'自然周期','NATURAL','pm_cycle_mode',NULL,'primary','Y',NULL,1,'2026-10-06 16:20:22',NULL,NULL,NULL),(2609251100000000198,2,'循环周期','ROLLING','pm_cycle_mode',NULL,'success','N',NULL,1,'2026-10-06 16:20:22',NULL,NULL,NULL),(2609251100000000199,1,'周期开始日前','BEFORE_PERIOD_START','pm_due_date_mode',NULL,'warning','N',NULL,1,'2026-10-07 09:53:31',NULL,NULL,NULL),(2609251100000000200,2,'周期开始日后','AFTER_PERIOD_START','pm_due_date_mode',NULL,'primary','Y',NULL,1,'2026-10-07 09:53:31',NULL,NULL,NULL),(2609251100000000201,1,'执行中','RUNNING','pm_receivable_batch_status',NULL,'primary','Y',NULL,1,'2026-10-07 09:53:31',NULL,NULL,NULL),(2609251100000000202,2,'成功','SUCCESS','pm_receivable_batch_status',NULL,'success','N',NULL,1,'2026-10-07 09:53:31',NULL,NULL,NULL),(2609251100000000203,3,'部分成功','PARTIAL_SUCCESS','pm_receivable_batch_status',NULL,'warning','N',NULL,1,'2026-10-07 09:53:31',NULL,NULL,NULL),(2609251100000000204,4,'失败','FAILED','pm_receivable_batch_status',NULL,'danger','N',NULL,1,'2026-10-07 09:53:31',NULL,NULL,NULL),(2609251100000000205,1,'未支付','UNPAID','pm_payment_status',NULL,'danger','Y',NULL,1,'2026-10-07 09:53:31',NULL,NULL,NULL),(2609251100000000206,2,'部分支付','PARTIAL','pm_payment_status',NULL,'warning','N',NULL,1,'2026-10-07 09:53:31',NULL,NULL,NULL),(2609251100000000207,3,'已支付','PAID','pm_payment_status',NULL,'success','N',NULL,1,'2026-10-07 09:53:31',NULL,NULL,NULL),(2609251100000000208,4,'已作废','CANCELLED','pm_payment_status',NULL,'info','N',NULL,1,'2026-10-07 09:53:31',NULL,NULL,NULL),(2609251100000000209,1,'现金','CASH','pm_payment_method',NULL,'primary','Y',NULL,1,'2026-10-07 14:53:04',NULL,NULL,NULL),(2609251100000000210,2,'微信','WECHAT','pm_payment_method',NULL,'success','N',NULL,1,'2026-10-07 14:53:04',NULL,NULL,NULL),(2609251100000000211,3,'支付宝','ALIPAY','pm_payment_method',NULL,'primary','N',NULL,1,'2026-10-07 14:53:04',NULL,NULL,NULL),(2609251100000000212,4,'银行转账','BANK_TRANSFER','pm_payment_method',NULL,'warning','N',NULL,1,'2026-10-07 14:53:04',NULL,NULL,NULL),(2609251100000000213,5,'POS','POS','pm_payment_method',NULL,'info','N',NULL,1,'2026-10-07 14:53:04',NULL,NULL,NULL),(2609251100000000214,6,'其他','OTHER','pm_payment_method',NULL,'info','N',NULL,1,'2026-10-07 14:53:04',NULL,NULL,NULL),(2609251100000000215,1,'有效','VALID','pm_payment_record_status',NULL,'success','Y',NULL,1,'2026-10-07 14:53:04',NULL,NULL,NULL),(2609251100000000216,2,'已冲销','REVERSED','pm_payment_record_status',NULL,'danger','N',NULL,1,'2026-10-07 14:53:04',NULL,NULL,NULL),(2609251100000000230,1,'公司/组织仓','COMPANY','wm_warehouse_scope',NULL,'primary','N',NULL,1,'2026-10-08 10:54:02',NULL,NULL,NULL),(2609251100000000231,2,'项目仓','PROJECT','wm_warehouse_scope',NULL,'success','Y',NULL,1,'2026-10-08 10:54:02',NULL,NULL,NULL),(2609251100000000232,1,'入库','INBOUND','wm_inventory_transaction_type',NULL,'success','Y',NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609251100000000233,2,'出库','OUTBOUND','wm_inventory_transaction_type',NULL,'danger','N',NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609251100000000234,3,'调拨入','TRANSFER_IN','wm_inventory_transaction_type',NULL,'success','N',NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609251100000000235,4,'调拨出','TRANSFER_OUT','wm_inventory_transaction_type',NULL,'warning','N',NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609251100000000236,5,'盘盈','STOCKTAKE_GAIN','wm_inventory_transaction_type',NULL,'success','N',NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609251100000000237,6,'盘亏','STOCKTAKE_LOSS','wm_inventory_transaction_type',NULL,'danger','N',NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609251100000000238,1,'草稿','DRAFT','wm_inbound_status',NULL,'info','Y',NULL,1,'2026-10-08 10:54:04',NULL,NULL,NULL),(2609251100000000239,2,'已确认','CONFIRMED','wm_inbound_status',NULL,'warning','N',NULL,1,'2026-10-08 10:54:04',NULL,NULL,NULL),(2609251100000000240,3,'已完成','COMPLETED','wm_inbound_status',NULL,'success','N',NULL,1,'2026-10-08 10:54:04',NULL,NULL,NULL),(2609251100000000241,4,'已取消','CANCELLED','wm_inbound_status',NULL,'danger','N',NULL,1,'2026-10-08 10:54:04',NULL,NULL,NULL),(2609251100000000242,1,'采购入库','PURCHASE','wm_inbound_type',NULL,'primary','Y',NULL,1,'2026-10-08 10:54:04',NULL,NULL,NULL),(2609251100000000243,2,'其他入库','OTHER','wm_inbound_type',NULL,'info','N',NULL,1,'2026-10-08 10:54:04',NULL,NULL,NULL),(2609251100000000244,1,'草稿','DRAFT','wm_outbound_status',NULL,'info','Y',NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609251100000000245,2,'已确认','CONFIRMED','wm_outbound_status',NULL,'warning','N',NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609251100000000246,3,'已完成','COMPLETED','wm_outbound_status',NULL,'success','N',NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609251100000000247,4,'已取消','CANCELLED','wm_outbound_status',NULL,'danger','N',NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609251100000000248,1,'领料出库','MATERIAL_USE','wm_outbound_type',NULL,'primary','Y',NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609251100000000249,2,'其他出库','OTHER','wm_outbound_type',NULL,'info','N',NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609251100000000250,1,'草稿','DRAFT','wm_transfer_status',NULL,'info','Y',NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609251100000000251,2,'已确认','CONFIRMED','wm_transfer_status',NULL,'warning','N',NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609251100000000252,3,'已完成','COMPLETED','wm_transfer_status',NULL,'success','N',NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609251100000000253,4,'已取消','CANCELLED','wm_transfer_status',NULL,'danger','N',NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609251100000000254,1,'普通调拨','NORMAL','wm_transfer_type',NULL,'primary','Y',NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609251100000000255,1,'草稿','DRAFT','wm_stocktake_status',NULL,'info','Y',NULL,1,'2026-10-08 17:38:27',NULL,NULL,NULL),(2609251100000000256,2,'已确认','CONFIRMED','wm_stocktake_status',NULL,'warning','N',NULL,1,'2026-10-08 17:38:27',NULL,NULL,NULL),(2609251100000000257,3,'已完成','COMPLETED','wm_stocktake_status',NULL,'success','N',NULL,1,'2026-10-08 17:38:27',NULL,NULL,NULL),(2609251100000000258,4,'已取消','CANCELLED','wm_stocktake_status',NULL,'danger','N',NULL,1,'2026-10-08 17:38:27',NULL,NULL,NULL),(2609251100000000259,1,'整仓盘点','FULL','wm_stocktake_type',NULL,'primary','Y',NULL,1,'2026-10-08 17:38:27',NULL,NULL,NULL),(2609251100000000260,2,'部分盘点','PARTIAL','wm_stocktake_type',NULL,'info','N',NULL,1,'2026-10-08 17:38:27',NULL,NULL,NULL);
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `active_menu`, `ext`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1761400000000000001,'系统管理',0,1,'system',NULL,'','N','Y','M','0','0','','system','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'系统管理目录'),(1761400000000000002,'系统监控',0,3,'monitor',NULL,'','N','Y','M','0','0','','monitor','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'系统监控目录'),(1761400000000000003,'系统工具',0,4,'tool',NULL,'','N','Y','M','0','0','','tool','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'系统工具目录'),(1761400000000000004,'PLUS官网',0,9,'https://gitee.com/dromara/RuoYi-Vue-Plus',NULL,'','Y','Y','M','1','1','','guide','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',1761100000000000001,'2026-10-12 17:33:48','RuoYi-Vue-Plus官网地址'),(1761400000000000100,'用户管理',1761400000000000001,1,'user','system/user/index','','N','Y','C','0','0','system:user:list','user','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'用户管理菜单'),(1761400000000000101,'角色管理',1761400000000000001,2,'role','system/role/index','','N','Y','C','0','0','system:role:list','peoples','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'角色管理菜单'),(1761400000000000102,'菜单管理',1761400000000000001,3,'menu','system/menu/index','','N','Y','C','0','0','system:menu:list','tree-table','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'菜单管理菜单'),(1761400000000000103,'部门管理',1761400000000000001,4,'dept','system/dept/index','','N','Y','C','0','0','system:dept:list','tree','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'部门管理菜单'),(1761400000000000104,'岗位管理',1761400000000000001,5,'post','system/post/index','','N','Y','C','0','0','system:post:list','post','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'岗位管理菜单'),(1761400000000000105,'字典管理',1761400000000000001,6,'dict','system/dict/index','','N','Y','C','0','0','system:dict:list','dict','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'字典管理菜单'),(1761400000000000106,'参数设置',1761400000000000001,7,'config','system/config/index','','N','Y','C','0','0','system:config:list','edit','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'参数设置菜单'),(1761400000000000107,'通知公告',1761400000000000001,8,'notice','system/notice/index','','N','Y','C','0','0','system:notice:list','message','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'通知公告菜单'),(1761400000000000108,'日志管理',1761400000000000001,9,'log','','','N','Y','M','0','0','','log','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'日志管理菜单'),(1761400000000000109,'在线用户',1761400000000000002,1,'online','monitor/online/index','','N','Y','C','0','0','monitor:online:list','online','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'在线用户菜单'),(1761400000000000113,'缓存监控',1761400000000000002,5,'cache','monitor/cache/index','','N','Y','C','0','0','monitor:cache:list','redis','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'缓存监控菜单'),(1761400000000000115,'代码生成',1761400000000000003,2,'gen','tool/gen/index','','N','Y','C','0','0','tool:gen:list','code','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'代码生成菜单'),(1761400000000000116,'修改生成配置',1761400000000000003,2,'gen-edit/index/:tableId','tool/gen/editTable','','N','N','C','1','0','tool:gen:edit','#','/tool/gen','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000000117,'Admin监控',1761400000000000002,5,'Admin','monitor/admin/index','','N','Y','C','0','0','monitor:admin:list','dashboard','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'Admin监控菜单'),(1761400000000000118,'文件管理',1761400000000000001,10,'oss','system/oss/index','','N','Y','C','0','0','system:oss:list','upload','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'文件管理菜单'),(1761400000000000123,'客户端管理',1761400000000000001,11,'client','system/client/index','','N','Y','C','0','0','system:client:list','international','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'客户端管理菜单'),(1761400000000000130,'分配用户',1761400000000000001,2,'role-auth/user/:roleId','system/role/authUser','','N','N','C','1','0','system:role:edit','#','/system/role','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000000131,'分配角色',1761400000000000001,1,'user-auth/role/:userId','system/user/authRole','','N','N','C','1','0','system:user:edit','#','/system/user','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000000133,'文件配置管理',1761400000000000001,10,'oss-config/index','system/oss/config','','N','N','C','1','0','system:ossConfig:list','#','/system/oss','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000000500,'操作日志',1761400000000000108,1,'operlog','monitor/operlog/index','','N','Y','C','0','0','monitor:operlog:list','form','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'操作日志菜单'),(1761400000000000501,'登录日志',1761400000000000108,2,'logininfo','monitor/logininfo/index','','N','Y','C','0','0','monitor:logininfo:list','logininfo','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,'登录日志菜单'),(1761400000000001001,'用户查询',1761400000000000100,1,'','','','N','Y','F','0','0','system:user:query','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001002,'用户新增',1761400000000000100,2,'','','','N','Y','F','0','0','system:user:add','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001003,'用户修改',1761400000000000100,3,'','','','N','Y','F','0','0','system:user:edit','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001004,'用户删除',1761400000000000100,4,'','','','N','Y','F','0','0','system:user:remove','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001005,'用户导出',1761400000000000100,5,'','','','N','Y','F','0','0','system:user:export','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001006,'用户导入',1761400000000000100,6,'','','','N','Y','F','0','0','system:user:import','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001007,'重置密码',1761400000000000100,7,'','','','N','Y','F','0','0','system:user:resetPwd','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001008,'角色查询',1761400000000000101,1,'','','','N','Y','F','0','0','system:role:query','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001009,'角色新增',1761400000000000101,2,'','','','N','Y','F','0','0','system:role:add','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001010,'角色修改',1761400000000000101,3,'','','','N','Y','F','0','0','system:role:edit','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001011,'角色删除',1761400000000000101,4,'','','','N','Y','F','0','0','system:role:remove','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001012,'角色导出',1761400000000000101,5,'','','','N','Y','F','0','0','system:role:export','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001013,'菜单查询',1761400000000000102,1,'','','','N','Y','F','0','0','system:menu:query','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001014,'菜单新增',1761400000000000102,2,'','','','N','Y','F','0','0','system:menu:add','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001015,'菜单修改',1761400000000000102,3,'','','','N','Y','F','0','0','system:menu:edit','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001016,'菜单删除',1761400000000000102,4,'','','','N','Y','F','0','0','system:menu:remove','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001017,'部门查询',1761400000000000103,1,'','','','N','Y','F','0','0','system:dept:query','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001018,'部门新增',1761400000000000103,2,'','','','N','Y','F','0','0','system:dept:add','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001019,'部门修改',1761400000000000103,3,'','','','N','Y','F','0','0','system:dept:edit','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001020,'部门删除',1761400000000000103,4,'','','','N','Y','F','0','0','system:dept:remove','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001021,'岗位查询',1761400000000000104,1,'','','','N','Y','F','0','0','system:post:query','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001022,'岗位新增',1761400000000000104,2,'','','','N','Y','F','0','0','system:post:add','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001023,'岗位修改',1761400000000000104,3,'','','','N','Y','F','0','0','system:post:edit','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001024,'岗位删除',1761400000000000104,4,'','','','N','Y','F','0','0','system:post:remove','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001025,'岗位导出',1761400000000000104,5,'','','','N','Y','F','0','0','system:post:export','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001026,'字典查询',1761400000000000105,1,'#','','','N','Y','F','0','0','system:dict:query','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001027,'字典新增',1761400000000000105,2,'#','','','N','Y','F','0','0','system:dict:add','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001028,'字典修改',1761400000000000105,3,'#','','','N','Y','F','0','0','system:dict:edit','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001029,'字典删除',1761400000000000105,4,'#','','','N','Y','F','0','0','system:dict:remove','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001030,'字典导出',1761400000000000105,5,'#','','','N','Y','F','0','0','system:dict:export','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001031,'参数查询',1761400000000000106,1,'#','','','N','Y','F','0','0','system:config:query','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:23',NULL,NULL,''),(1761400000000001032,'参数新增',1761400000000000106,2,'#','','','N','Y','F','0','0','system:config:add','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001033,'参数修改',1761400000000000106,3,'#','','','N','Y','F','0','0','system:config:edit','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001034,'参数删除',1761400000000000106,4,'#','','','N','Y','F','0','0','system:config:remove','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001035,'参数导出',1761400000000000106,5,'#','','','N','Y','F','0','0','system:config:export','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001036,'公告查询',1761400000000000107,1,'#','','','N','Y','F','0','0','system:notice:query','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001037,'公告新增',1761400000000000107,2,'#','','','N','Y','F','0','0','system:notice:add','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001038,'公告修改',1761400000000000107,3,'#','','','N','Y','F','0','0','system:notice:edit','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001039,'公告删除',1761400000000000107,4,'#','','','N','Y','F','0','0','system:notice:remove','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001040,'操作查询',1761400000000000500,1,'#','','','N','Y','F','0','0','monitor:operlog:query','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001041,'操作删除',1761400000000000500,2,'#','','','N','Y','F','0','0','monitor:operlog:remove','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001042,'日志导出',1761400000000000500,4,'#','','','N','Y','F','0','0','monitor:operlog:export','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001043,'登录查询',1761400000000000501,1,'#','','','N','Y','F','0','0','monitor:logininfo:query','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001044,'登录删除',1761400000000000501,2,'#','','','N','Y','F','0','0','monitor:logininfo:remove','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001045,'日志导出',1761400000000000501,3,'#','','','N','Y','F','0','0','monitor:logininfo:export','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001046,'在线查询',1761400000000000109,1,'#','','','N','Y','F','0','0','monitor:online:query','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001047,'批量强退',1761400000000000109,2,'#','','','N','Y','F','0','0','monitor:online:batchLogout','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001048,'单条强退',1761400000000000109,3,'#','','','N','Y','F','0','0','monitor:online:forceLogout','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001050,'账户解锁',1761400000000000501,4,'#','','','N','Y','F','0','0','monitor:logininfo:unlock','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001055,'生成查询',1761400000000000115,1,'#','','','N','Y','F','0','0','tool:gen:query','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001056,'生成修改',1761400000000000115,2,'#','','','N','Y','F','0','0','tool:gen:edit','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001057,'生成删除',1761400000000000115,3,'#','','','N','Y','F','0','0','tool:gen:remove','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001058,'导入代码',1761400000000000115,2,'#','','','N','Y','F','0','0','tool:gen:import','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001059,'预览代码',1761400000000000115,4,'#','','','N','Y','F','0','0','tool:gen:preview','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001060,'生成代码',1761400000000000115,5,'#','','','N','Y','F','0','0','tool:gen:code','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001061,'客户端管理查询',1761400000000000123,1,'#','','','N','Y','F','0','0','system:client:query','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001062,'客户端管理新增',1761400000000000123,2,'#','','','N','Y','F','0','0','system:client:add','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001063,'客户端管理修改',1761400000000000123,3,'#','','','N','Y','F','0','0','system:client:edit','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001064,'客户端管理删除',1761400000000000123,4,'#','','','N','Y','F','0','0','system:client:remove','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001065,'客户端管理导出',1761400000000000123,5,'#','','','N','Y','F','0','0','system:client:export','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001600,'文件查询',1761400000000000118,1,'#','','','N','Y','F','0','0','system:oss:query','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001601,'文件上传',1761400000000000118,2,'#','','','N','Y','F','0','0','system:oss:upload','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001602,'文件下载',1761400000000000118,3,'#','','','N','Y','F','0','0','system:oss:download','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001603,'文件删除',1761400000000000118,4,'#','','','N','Y','F','0','0','system:oss:remove','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001620,'配置列表',1761400000000000118,5,'#','','','N','Y','F','0','0','system:ossConfig:list','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001621,'配置添加',1761400000000000118,6,'#','','','N','Y','F','0','0','system:ossConfig:add','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001622,'配置编辑',1761400000000000118,6,'#','','','N','Y','F','0','0','system:ossConfig:edit','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(1761400000000001623,'配置删除',1761400000000000118,6,'#','','','N','Y','F','0','0','system:ossConfig:remove','#','','',1761000000000000103,1761100000000000001,'2026-10-01 15:54:24',NULL,NULL,''),(2609251200000000011,'房屋资源导入',2609252000000000301,90,'',NULL,NULL,'N','N','F','0','0','property:resource:treeImport','#',NULL,NULL,NULL,1,'2026-10-09 21:10:03',NULL,NULL,'项目房屋树形原子导入'),(2609251200000000012,'客户批量导入',2609252000000000401,6,'',NULL,NULL,'N','N','F','0','0','property:customer:import','#',NULL,NULL,NULL,1,'2026-10-09 21:10:03',NULL,NULL,'Customer Excel 原子批量导入'),(2609251200000000014,'房屋客户关系批量导入',2609252000000000402,10,'',NULL,NULL,'N','N','F','0','0','property:customer-relation:import','#',NULL,NULL,NULL,1,'2026-10-09 21:10:03',NULL,NULL,'RoomCustomerRelation Excel 原子批量导入'),(2609251200000000015,'住户管理',2609252000000000004,30,'resident','property/resident/index',NULL,'N','Y','C','0','0','property:resident:list','user',NULL,NULL,NULL,1,'2026-10-06 09:42:28',NULL,NULL,'项目级 Owner / Resident 查询视图'),(2609251200000000016,'住户详情',2609251200000000015,1,'',NULL,NULL,'N','N','F','0','0','property:resident:query','#',NULL,NULL,NULL,1,'2026-10-06 09:42:28',NULL,NULL,'住户聚合详情'),(2609251200000000017,'住户导出',2609251200000000015,2,'',NULL,NULL,'N','N','F','0','0','property:resident:export','#',NULL,NULL,NULL,1,'2026-10-06 09:42:28',NULL,NULL,'住户项目视图脱敏导出'),(2609252000000000001,'企业中心',0,10,'enterprise',NULL,NULL,'N','N','M','0','0',NULL,'user-filled',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,'V2 企业 HR 域'),(2609252000000000002,'项目中心',0,20,'project',NULL,NULL,'N','N','M','0','0',NULL,'office-building',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,'V2 物业项目域'),(2609252000000000003,'资源中心',0,30,'resource',NULL,NULL,'N','N','M','0','0',NULL,'house',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,'V2 物业资源域'),(2609252000000000004,'客户中心',0,40,'customer',NULL,NULL,'N','N','M','0','0',NULL,'user',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,'V2 物业客户域'),(2609252000000000005,'收费管理',0,50,'charge',NULL,NULL,'N','N','M','0','0',NULL,'money',NULL,NULL,NULL,1,'2026-10-06 11:13:25',NULL,NULL,'物业收费业务域'),(2609252000000000006,'仓库管理',0,60,'warehouse',NULL,NULL,'N','N','M','0','0',NULL,'box',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,'Warehouse Phase 1'),(2609252000000000101,'员工管理',2609252000000000001,10,'employee','hr/employee/index',NULL,'N','Y','C','0','0','hr:employee:list','peoples',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,'员工档案'),(2609252000000000201,'项目管理',2609252000000000002,10,'project','property/project/index',NULL,'N','Y','C','0','0','property:project:list','office-building',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,'物业项目管理'),(2609252000000000202,'项目成员',2609252000000000002,20,'member','property/project/member/index',NULL,'N','Y','C','0','0','property:projectMember:list','user-filled',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,'物业项目成员管理'),(2609252000000000203,'项目管理授权',2609252000000000002,30,'admin-scope','property/project/admin-scope/index',NULL,'N','Y','C','0','0','property:projectAdminScope:list','lock','','',NULL,1,'2026-10-01 16:00:31',NULL,NULL,'企业项目管理范围授权'),(2609252000000000301,'楼栋管理',2609252000000000003,10,'building','property/resource/building/index',NULL,'N','Y','C','0','0','property:building:list','office-building',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,'楼栋管理'),(2609252000000000302,'单元管理',2609252000000000003,20,'unit','property/resource/unit/index',NULL,'N','Y','C','0','0','property:unit:list','connection',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,'楼栋单元管理；Java Entity = BuildingUnit'),(2609252000000000303,'房屋管理',2609252000000000003,30,'room','property/resource/room/index',NULL,'N','Y','C','0','0','property:room:list','house',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,'房屋管理'),(2609252000000000401,'客户档案',2609252000000000004,10,'customer','property/customer/index',NULL,'N','Y','C','0','0','property:customer:list','user',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,'企业级客户主数据'),(2609252000000000402,'房客关系',2609252000000000004,20,'relation','property/customer/relation/index',NULL,'N','Y','C','0','0','property:customerRelation:list','link',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,'房屋客户关系 / 迁入迁出'),(2609252000000000501,'收费项目',2609252000000000005,10,'item','property/charge/item/index',NULL,'N','Y','C','0','0','property:chargeItem:list','list',NULL,NULL,NULL,1,'2026-10-06 11:13:25',NULL,NULL,'项目级收费项目管理'),(2609252000000000502,'计费规则',2609252000000000005,20,'rule','property/charge/rule/index',NULL,'N','Y','C','0','0','property:chargeRule:list','list',NULL,NULL,NULL,1,'2026-10-06 16:20:22',NULL,NULL,'项目级周期性计费规则管理'),(2609252000000000503,'应收管理',2609252000000000005,30,'receivable','property/charge/receivable/index',NULL,'N','Y','C','0','0','property:receivable:list','money',NULL,NULL,NULL,1,'2026-10-07 09:53:31',NULL,NULL,'项目级应收与生成批次管理'),(2609252000000000504,'收款管理',2609252000000000005,40,'payment','property/charge/payment/index',NULL,'N','Y','C','0','0','property:payment:list','money',NULL,NULL,NULL,1,'2026-10-07 14:53:04',NULL,NULL,'项目级收款与核销管理'),(2609252000000000601,'基础资料',2609252000000000006,10,'base',NULL,NULL,'N','N','M','0','0',NULL,'list',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,'Warehouse 基础资料'),(2609252000000000602,'仓库管理',2609252000000000601,10,'warehouse','warehouse/warehouse/index',NULL,'N','Y','C','0','0','warehouse:warehouse:list','box',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,'仓库主数据'),(2609252000000000603,'库区管理',2609252000000000601,20,'zone','warehouse/zone/index',NULL,'N','Y','C','0','0','warehouse:zone:list','grid',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,'仓库库区'),(2609252000000000604,'库位管理',2609252000000000601,30,'location','warehouse/location/index',NULL,'N','Y','C','0','0','warehouse:location:list','location',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,'仓库库位'),(2609252000000000605,'物料分类',2609252000000000601,40,'materialCategory','warehouse/materialCategory/index',NULL,'N','Y','C','0','0','warehouse:materialCategory:list','tree',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,'公司级物料分类'),(2609252000000000606,'计量单位',2609252000000000601,50,'unit','warehouse/unit/index',NULL,'N','Y','C','0','0','warehouse:unit:list','component',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,'平台共享计量单位'),(2609252000000000607,'物料管理',2609252000000000601,60,'material','warehouse/material/index',NULL,'N','Y','C','0','0','warehouse:material:list','shopping',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,'公司级物料主数据'),(2609252000000000608,'库存管理',2609252000000000006,20,'inventory',NULL,NULL,'N','N','M','0','0',NULL,'database',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,'Warehouse 库存管理'),(2609252000000000609,'实时库存',2609252000000000608,10,'realtime','warehouse/inventory/index',NULL,'N','Y','C','0','0','warehouse:inventory:list','list',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,'只读库位库存余额'),(2609252000000000610,'库存流水',2609252000000000608,20,'transaction','warehouse/inventoryTransaction/index',NULL,'N','Y','C','0','0','warehouse:inventoryTransaction:list','time',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,'不可变库存流水查询'),(2609252000000000611,'入库管理',2609252000000000006,30,'inbound','warehouse/inbound/index',NULL,'N','Y','C','0','0','warehouse:inbound:list','download',NULL,NULL,NULL,1,'2026-10-08 10:54:04',1,'2026-10-11 16:24:55','入库业务闭环'),(2609252000000000612,'出库管理',2609252000000000006,40,'outbound','warehouse/outbound/index',NULL,'N','Y','C','0','0','warehouse:outbound:list','upload',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,'出库业务闭环'),(2609252000000000613,'调拨管理',2609252000000000006,50,'transfer','warehouse/transfer/index',NULL,'N','Y','C','0','0','warehouse:transfer:list','switch',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,'调拨业务闭环'),(2609252000000000614,'盘点管理',2609252000000000006,60,'stocktake','warehouse/stocktake/index',NULL,'N','Y','C','0','0','warehouse:stocktake:list','clipboard',NULL,NULL,NULL,1,'2026-10-08 17:38:27',NULL,NULL,'库存盘点闭环'),(2609252000000001001,'员工查询',2609252000000000101,1,'',NULL,NULL,'N','N','F','0','0','hr:employee:query','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001002,'员工新增',2609252000000000101,2,'',NULL,NULL,'N','N','F','0','0','hr:employee:add','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001003,'员工修改',2609252000000000101,3,'',NULL,NULL,'N','N','F','0','0','hr:employee:edit','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001004,'员工删除',2609252000000000101,4,'',NULL,NULL,'N','N','F','0','0','hr:employee:remove','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001005,'员工导出',2609252000000000101,5,'',NULL,NULL,'N','N','F','0','0','hr:employee:export','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001006,'账号绑定',2609252000000000101,6,'',NULL,NULL,'N','N','F','0','0','hr:employee:bindUser','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001101,'项目查询',2609252000000000201,1,'',NULL,NULL,'N','N','F','0','0','property:project:query','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001102,'项目新增',2609252000000000201,2,'',NULL,NULL,'N','N','F','0','0','property:project:add','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001103,'项目修改',2609252000000000201,3,'',NULL,NULL,'N','N','F','0','0','property:project:edit','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001104,'项目删除',2609252000000000201,4,'',NULL,NULL,'N','N','F','0','0','property:project:remove','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001105,'项目导出',2609252000000000201,5,'',NULL,NULL,'N','N','F','0','0','property:project:export','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001201,'成员查询',2609252000000000202,1,'',NULL,NULL,'N','N','F','0','0','property:projectMember:query','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001202,'成员新增',2609252000000000202,2,'',NULL,NULL,'N','N','F','0','0','property:projectMember:add','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001203,'成员修改',2609252000000000202,3,'',NULL,NULL,'N','N','F','0','0','property:projectMember:edit','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001204,'成员移除',2609252000000000202,4,'',NULL,NULL,'N','N','F','0','0','property:projectMember:remove','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001205,'成员导出',2609252000000000202,5,'',NULL,NULL,'N','N','F','0','0','property:projectMember:export','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001206,'授权查询',2609252000000000203,1,'',NULL,NULL,'N','N','F','0','0','property:projectAdminScope:query','#','','',NULL,1,'2026-10-01 16:00:31',NULL,NULL,NULL),(2609252000000001207,'授权新增',2609252000000000203,2,'',NULL,NULL,'N','N','F','0','0','property:projectAdminScope:add','#','','',NULL,1,'2026-10-01 16:00:31',NULL,NULL,NULL),(2609252000000001208,'授权修改',2609252000000000203,3,'',NULL,NULL,'N','N','F','0','0','property:projectAdminScope:edit','#','','',NULL,1,'2026-10-01 16:00:31',NULL,NULL,NULL),(2609252000000001209,'授权撤销',2609252000000000203,4,'',NULL,NULL,'N','N','F','0','0','property:projectAdminScope:remove','#','','',NULL,1,'2026-10-01 16:00:31',NULL,NULL,NULL),(2609252000000001301,'楼栋查询',2609252000000000301,1,'',NULL,NULL,'N','N','F','0','0','property:building:query','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001302,'楼栋新增',2609252000000000301,2,'',NULL,NULL,'N','N','F','0','0','property:building:add','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001303,'楼栋修改',2609252000000000301,3,'',NULL,NULL,'N','N','F','0','0','property:building:edit','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001304,'楼栋删除',2609252000000000301,4,'',NULL,NULL,'N','N','F','0','0','property:building:remove','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001305,'楼栋导出',2609252000000000301,5,'',NULL,NULL,'N','N','F','0','0','property:building:export','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001401,'单元查询',2609252000000000302,1,'',NULL,NULL,'N','N','F','0','0','property:unit:query','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001402,'单元新增',2609252000000000302,2,'',NULL,NULL,'N','N','F','0','0','property:unit:add','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001403,'单元修改',2609252000000000302,3,'',NULL,NULL,'N','N','F','0','0','property:unit:edit','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001404,'单元删除',2609252000000000302,4,'',NULL,NULL,'N','N','F','0','0','property:unit:remove','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001405,'单元导出',2609252000000000302,5,'',NULL,NULL,'N','N','F','0','0','property:unit:export','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001501,'房屋查询',2609252000000000303,1,'',NULL,NULL,'N','N','F','0','0','property:room:query','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001502,'房屋新增',2609252000000000303,2,'',NULL,NULL,'N','N','F','0','0','property:room:add','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001503,'房屋修改',2609252000000000303,3,'',NULL,NULL,'N','N','F','0','0','property:room:edit','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001504,'房屋删除',2609252000000000303,4,'',NULL,NULL,'N','N','F','0','0','property:room:remove','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001505,'房屋导出',2609252000000000303,5,'',NULL,NULL,'N','N','F','0','0','property:room:export','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001601,'客户查询',2609252000000000401,1,'',NULL,NULL,'N','N','F','0','0','property:customer:query','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001602,'客户新增',2609252000000000401,2,'',NULL,NULL,'N','N','F','0','0','property:customer:add','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001603,'客户修改',2609252000000000401,3,'',NULL,NULL,'N','N','F','0','0','property:customer:edit','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001604,'客户删除',2609252000000000401,4,'',NULL,NULL,'N','N','F','0','0','property:customer:remove','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001605,'客户导出',2609252000000000401,5,'',NULL,NULL,'N','N','F','0','0','property:customer:export','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001606,'敏感资料查看',2609252000000000401,6,'',NULL,NULL,'N','N','F','0','0','property:customer:pii:view','#','','',NULL,1,'2026-10-01 16:00:33',NULL,NULL,'完整客户敏感字段查看'),(2609252000000001607,'敏感资料导出',2609252000000000401,7,'',NULL,NULL,'N','N','F','0','0','property:customer:pii:export','#','','',NULL,1,'2026-10-01 16:00:33',NULL,NULL,'客户敏感字段导出'),(2609252000000001611,'客户授权列表',2609252000000000401,11,'',NULL,NULL,'N','N','F','0','0','property:customerGrant:list','#','','',NULL,1,'2026-10-01 16:00:33',NULL,NULL,'企业客户主档授权列表'),(2609252000000001612,'客户授权查询',2609252000000000401,12,'',NULL,NULL,'N','N','F','0','0','property:customerGrant:query','#','','',NULL,1,'2026-10-01 16:00:33',NULL,NULL,'企业客户主档授权历史'),(2609252000000001613,'客户授权新增',2609252000000000401,13,'',NULL,NULL,'N','N','F','0','0','property:customerGrant:add','#','','',NULL,1,'2026-10-01 16:00:33',NULL,NULL,'企业客户主档授权新增'),(2609252000000001614,'客户授权修改',2609252000000000401,14,'',NULL,NULL,'N','N','F','0','0','property:customerGrant:edit','#','','',NULL,1,'2026-10-01 16:00:33',NULL,NULL,'企业客户主档授权启停'),(2609252000000001615,'客户授权撤销',2609252000000000401,15,'',NULL,NULL,'N','N','F','0','0','property:customerGrant:remove','#','','',NULL,1,'2026-10-01 16:00:33',NULL,NULL,'企业客户主档授权撤销'),(2609252000000001701,'关系查询',2609252000000000402,1,'',NULL,NULL,'N','N','F','0','0','property:customerRelation:query','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001702,'关系新增',2609252000000000402,2,'',NULL,NULL,'N','N','F','0','0','property:customerRelation:add','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001703,'关系修改',2609252000000000402,3,'',NULL,NULL,'N','N','F','0','0','property:customerRelation:edit','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001704,'关系删除',2609252000000000402,4,'',NULL,NULL,'N','N','F','0','0','property:customerRelation:remove','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001705,'关系导出',2609252000000000402,5,'',NULL,NULL,'N','N','F','0','0','property:customerRelation:export','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001706,'客户迁入',2609252000000000402,6,'',NULL,NULL,'N','N','F','0','0','property:customerRelation:moveIn','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001707,'客户迁出',2609252000000000402,7,'',NULL,NULL,'N','N','F','0','0','property:customerRelation:moveOut','#',NULL,NULL,NULL,1,'2026-10-01 15:59:31',NULL,NULL,NULL),(2609252000000001801,'收费项目查询',2609252000000000501,1,'',NULL,NULL,'N','N','F','0','0','property:chargeItem:query','#',NULL,NULL,NULL,1,'2026-10-06 11:13:25',NULL,NULL,NULL),(2609252000000001802,'收费项目新增',2609252000000000501,2,'',NULL,NULL,'N','N','F','0','0','property:chargeItem:add','#',NULL,NULL,NULL,1,'2026-10-06 11:13:25',NULL,NULL,NULL),(2609252000000001803,'收费项目修改',2609252000000000501,3,'',NULL,NULL,'N','N','F','0','0','property:chargeItem:edit','#',NULL,NULL,NULL,1,'2026-10-06 11:13:25',NULL,NULL,NULL),(2609252000000001804,'收费项目删除',2609252000000000501,4,'',NULL,NULL,'N','N','F','0','0','property:chargeItem:remove','#',NULL,NULL,NULL,1,'2026-10-06 11:13:25',NULL,NULL,NULL),(2609252000000001805,'收费项目导出',2609252000000000501,5,'',NULL,NULL,'N','N','F','0','0','property:chargeItem:export','#',NULL,NULL,NULL,1,'2026-10-06 11:13:25',NULL,NULL,NULL),(2609252000000001811,'计费规则查询',2609252000000000502,1,'',NULL,NULL,'N','N','F','0','0','property:chargeRule:query','#',NULL,NULL,NULL,1,'2026-10-06 16:20:22',NULL,NULL,NULL),(2609252000000001812,'计费规则新增',2609252000000000502,2,'',NULL,NULL,'N','N','F','0','0','property:chargeRule:add','#',NULL,NULL,NULL,1,'2026-10-06 16:20:22',NULL,NULL,NULL),(2609252000000001813,'计费规则修改',2609252000000000502,3,'',NULL,NULL,'N','N','F','0','0','property:chargeRule:edit','#',NULL,NULL,NULL,1,'2026-10-06 16:20:22',NULL,NULL,NULL),(2609252000000001814,'计费规则删除',2609252000000000502,4,'',NULL,NULL,'N','N','F','0','0','property:chargeRule:remove','#',NULL,NULL,NULL,1,'2026-10-06 16:20:22',NULL,NULL,NULL),(2609252000000001815,'计费规则导出',2609252000000000502,5,'',NULL,NULL,'N','N','F','0','0','property:chargeRule:export','#',NULL,NULL,NULL,1,'2026-10-06 16:20:22',NULL,NULL,NULL),(2609252000000001821,'房屋收费设置',2609252000000000005,25,'binding','property/charge/binding/index',NULL,'N','Y','C','0','0','property:chargeBinding:list','house',NULL,NULL,NULL,1,'2026-10-07 09:53:19',NULL,'2026-10-09 21:10:03','收费管理下独立房屋收费设置页面'),(2609252000000001822,'房屋收费设置查询',2609252000000001821,22,'',NULL,NULL,'N','N','F','0','0','property:chargeBinding:query','#',NULL,NULL,NULL,1,'2026-10-07 09:53:19',NULL,'2026-10-09 21:10:03','房屋收费设置详情'),(2609252000000001823,'房屋收费设置新增',2609252000000001821,23,'',NULL,NULL,'N','N','F','0','0','property:chargeBinding:add','#',NULL,NULL,NULL,1,'2026-10-07 09:53:19',NULL,'2026-10-09 21:10:03','新增房屋收费设置'),(2609252000000001824,'房屋收费设置修改',2609252000000001821,24,'',NULL,NULL,'N','N','F','0','0','property:chargeBinding:edit','#',NULL,NULL,NULL,1,'2026-10-07 09:53:19',NULL,'2026-10-09 21:10:03','终止、启停及维护房屋收费设置'),(2609252000000001825,'房屋收费设置删除',2609252000000001821,25,'',NULL,NULL,'N','N','F','0','0','property:chargeBinding:remove','#',NULL,NULL,NULL,1,'2026-10-07 09:53:19',NULL,'2026-10-09 21:10:03','删除未产生应收的房屋收费设置'),(2609252000000001831,'应收查询',2609252000000000503,1,'',NULL,NULL,'N','N','F','0','0','property:receivable:query','#',NULL,NULL,NULL,1,'2026-10-07 09:53:31',NULL,NULL,NULL),(2609252000000001832,'应收生成',2609252000000000503,2,'',NULL,NULL,'N','N','F','0','0','property:receivable:generate','#',NULL,NULL,NULL,1,'2026-10-07 09:53:31',NULL,NULL,NULL),(2609252000000001833,'应收作废',2609252000000000503,3,'',NULL,NULL,'N','N','F','0','0','property:receivable:cancel','#',NULL,NULL,NULL,1,'2026-10-07 09:53:31',NULL,NULL,NULL),(2609252000000001834,'应收导出',2609252000000000503,4,'',NULL,NULL,'N','N','F','0','0','property:receivable:export','#',NULL,NULL,NULL,1,'2026-10-07 09:53:31',NULL,NULL,NULL),(2609252000000001841,'收款查询',2609252000000000504,1,'',NULL,NULL,'N','N','F','0','0','property:payment:query','#',NULL,NULL,NULL,1,'2026-10-07 14:53:04',NULL,NULL,NULL),(2609252000000001842,'收款新增',2609252000000000504,2,'',NULL,NULL,'N','N','F','0','0','property:payment:add','#',NULL,NULL,NULL,1,'2026-10-07 14:53:04',NULL,NULL,NULL),(2609252000000001843,'收款核销',2609252000000000504,3,'',NULL,NULL,'N','N','F','0','0','property:payment:allocate','#',NULL,NULL,NULL,1,'2026-10-07 14:53:04',NULL,NULL,NULL),(2609252000000001844,'收款导出',2609252000000000504,4,'',NULL,NULL,'N','N','F','0','0','property:payment:export','#',NULL,NULL,NULL,1,'2026-10-07 14:53:04',NULL,NULL,NULL),(2609252000000001845,'收款冲销',2609252000000000504,5,'',NULL,NULL,'N','N','F','0','0','property:payment:reverse','#',NULL,NULL,NULL,1,'2026-10-08 00:01:21',NULL,NULL,'整笔收款冲销与全部核销回退'),(2609252000000002201,'仓库查询',2609252000000000602,1,'',NULL,NULL,'N','N','F','0','0','warehouse:warehouse:query','#',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,NULL),(2609252000000002202,'仓库新增',2609252000000000602,2,'',NULL,NULL,'N','N','F','0','0','warehouse:warehouse:add','#',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,NULL),(2609252000000002203,'仓库修改',2609252000000000602,3,'',NULL,NULL,'N','N','F','0','0','warehouse:warehouse:edit','#',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,NULL),(2609252000000002204,'仓库删除',2609252000000000602,4,'',NULL,NULL,'N','N','F','0','0','warehouse:warehouse:remove','#',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,NULL),(2609252000000002211,'库区查询',2609252000000000603,1,'',NULL,NULL,'N','N','F','0','0','warehouse:zone:query','#',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,NULL),(2609252000000002212,'库区新增',2609252000000000603,2,'',NULL,NULL,'N','N','F','0','0','warehouse:zone:add','#',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,NULL),(2609252000000002213,'库区修改',2609252000000000603,3,'',NULL,NULL,'N','N','F','0','0','warehouse:zone:edit','#',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,NULL),(2609252000000002214,'库区删除',2609252000000000603,4,'',NULL,NULL,'N','N','F','0','0','warehouse:zone:remove','#',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,NULL),(2609252000000002221,'库位查询',2609252000000000604,1,'',NULL,NULL,'N','N','F','0','0','warehouse:location:query','#',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,NULL),(2609252000000002222,'库位新增',2609252000000000604,2,'',NULL,NULL,'N','N','F','0','0','warehouse:location:add','#',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,NULL),(2609252000000002223,'库位修改',2609252000000000604,3,'',NULL,NULL,'N','N','F','0','0','warehouse:location:edit','#',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,NULL),(2609252000000002224,'库位删除',2609252000000000604,4,'',NULL,NULL,'N','N','F','0','0','warehouse:location:remove','#',NULL,NULL,NULL,1,'2026-10-08 10:54:02',NULL,NULL,NULL),(2609252000000002231,'分类查询',2609252000000000605,1,'',NULL,NULL,'N','N','F','0','0','warehouse:materialCategory:query','#',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609252000000002232,'分类新增',2609252000000000605,2,'',NULL,NULL,'N','N','F','0','0','warehouse:materialCategory:add','#',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609252000000002233,'分类修改',2609252000000000605,3,'',NULL,NULL,'N','N','F','0','0','warehouse:materialCategory:edit','#',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609252000000002234,'分类删除',2609252000000000605,4,'',NULL,NULL,'N','N','F','0','0','warehouse:materialCategory:remove','#',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609252000000002241,'单位查询',2609252000000000606,1,'',NULL,NULL,'N','N','F','0','0','warehouse:unit:query','#',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609252000000002242,'单位新增',2609252000000000606,2,'',NULL,NULL,'N','N','F','0','0','warehouse:unit:add','#',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609252000000002243,'单位修改',2609252000000000606,3,'',NULL,NULL,'N','N','F','0','0','warehouse:unit:edit','#',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609252000000002244,'单位删除',2609252000000000606,4,'',NULL,NULL,'N','N','F','0','0','warehouse:unit:remove','#',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609252000000002251,'物料查询',2609252000000000607,1,'',NULL,NULL,'N','N','F','0','0','warehouse:material:query','#',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609252000000002252,'物料新增',2609252000000000607,2,'',NULL,NULL,'N','N','F','0','0','warehouse:material:add','#',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609252000000002253,'物料修改',2609252000000000607,3,'',NULL,NULL,'N','N','F','0','0','warehouse:material:edit','#',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609252000000002254,'物料删除',2609252000000000607,4,'',NULL,NULL,'N','N','F','0','0','warehouse:material:remove','#',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609252000000002261,'库存详情',2609252000000000609,1,'',NULL,NULL,'N','N','F','0','0','warehouse:inventory:query','#',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609252000000002262,'库存导出',2609252000000000609,2,'',NULL,NULL,'N','N','F','0','0','warehouse:inventory:export','#',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609252000000002271,'流水查询',2609252000000000610,1,'',NULL,NULL,'N','N','F','0','0','warehouse:inventoryTransaction:query','#',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609252000000002272,'流水导出',2609252000000000610,2,'',NULL,NULL,'N','N','F','0','0','warehouse:inventoryTransaction:export','#',NULL,NULL,NULL,1,'2026-10-08 10:54:03',NULL,NULL,NULL),(2609252000000002273,'入库查询',2609252000000000611,1,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:query','#',NULL,NULL,NULL,1,'2026-10-08 10:54:04',NULL,NULL,NULL),(2609252000000002274,'入库新增',2609252000000000611,2,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:add','#',NULL,NULL,NULL,1,'2026-10-08 10:54:04',NULL,NULL,NULL),(2609252000000002275,'入库修改',2609252000000000611,3,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:edit','#',NULL,NULL,NULL,1,'2026-10-08 10:54:04',NULL,NULL,NULL),(2609252000000002276,'入库删除',2609252000000000611,4,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:remove','#',NULL,NULL,NULL,1,'2026-10-08 10:54:04',NULL,NULL,NULL),(2609252000000002277,'入库确认',2609252000000000611,5,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:confirm','#',NULL,NULL,NULL,1,'2026-10-08 10:54:04',NULL,NULL,NULL),(2609252000000002278,'完成入库',2609252000000000611,6,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:complete','#',NULL,NULL,NULL,1,'2026-10-08 10:54:04',NULL,NULL,NULL),(2609252000000002279,'取消入库',2609252000000000611,7,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:cancel','#',NULL,NULL,NULL,1,'2026-10-08 10:54:04',NULL,NULL,NULL),(2609252000000002280,'入库导出',2609252000000000611,8,'',NULL,NULL,'N','N','F','0','0','warehouse:inbound:export','#',NULL,NULL,NULL,1,'2026-10-08 10:54:04',NULL,NULL,NULL),(2609252000000002281,'出库查询',2609252000000000612,1,'',NULL,NULL,'N','N','F','0','0','warehouse:outbound:query','#',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609252000000002282,'出库新增',2609252000000000612,2,'',NULL,NULL,'N','N','F','0','0','warehouse:outbound:add','#',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609252000000002283,'出库修改',2609252000000000612,3,'',NULL,NULL,'N','N','F','0','0','warehouse:outbound:edit','#',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609252000000002284,'出库删除',2609252000000000612,4,'',NULL,NULL,'N','N','F','0','0','warehouse:outbound:remove','#',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609252000000002285,'出库确认',2609252000000000612,5,'',NULL,NULL,'N','N','F','0','0','warehouse:outbound:confirm','#',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609252000000002286,'完成出库',2609252000000000612,6,'',NULL,NULL,'N','N','F','0','0','warehouse:outbound:complete','#',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609252000000002287,'取消出库',2609252000000000612,7,'',NULL,NULL,'N','N','F','0','0','warehouse:outbound:cancel','#',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609252000000002288,'出库导出',2609252000000000612,8,'',NULL,NULL,'N','N','F','0','0','warehouse:outbound:export','#',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609252000000002289,'调拨查询',2609252000000000613,1,'',NULL,NULL,'N','N','F','0','0','warehouse:transfer:query','#',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609252000000002290,'调拨新增',2609252000000000613,2,'',NULL,NULL,'N','N','F','0','0','warehouse:transfer:add','#',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609252000000002291,'调拨修改',2609252000000000613,3,'',NULL,NULL,'N','N','F','0','0','warehouse:transfer:edit','#',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609252000000002292,'调拨删除',2609252000000000613,4,'',NULL,NULL,'N','N','F','0','0','warehouse:transfer:remove','#',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609252000000002293,'调拨确认',2609252000000000613,5,'',NULL,NULL,'N','N','F','0','0','warehouse:transfer:confirm','#',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609252000000002294,'完成调拨',2609252000000000613,6,'',NULL,NULL,'N','N','F','0','0','warehouse:transfer:complete','#',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609252000000002295,'取消调拨',2609252000000000613,7,'',NULL,NULL,'N','N','F','0','0','warehouse:transfer:cancel','#',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609252000000002296,'调拨导出',2609252000000000613,8,'',NULL,NULL,'N','N','F','0','0','warehouse:transfer:export','#',NULL,NULL,NULL,1,'2026-10-08 17:38:26',NULL,NULL,NULL),(2609252000000002297,'盘点查询',2609252000000000614,1,'',NULL,NULL,'N','N','F','0','0','warehouse:stocktake:query','#',NULL,NULL,NULL,1,'2026-10-08 17:38:27',NULL,NULL,NULL),(2609252000000002298,'盘点新增',2609252000000000614,2,'',NULL,NULL,'N','N','F','0','0','warehouse:stocktake:add','#',NULL,NULL,NULL,1,'2026-10-08 17:38:27',NULL,NULL,NULL),(2609252000000002299,'盘点修改',2609252000000000614,3,'',NULL,NULL,'N','N','F','0','0','warehouse:stocktake:edit','#',NULL,NULL,NULL,1,'2026-10-08 17:38:27',NULL,NULL,NULL),(2609252000000002300,'盘点删除',2609252000000000614,4,'',NULL,NULL,'N','N','F','0','0','warehouse:stocktake:remove','#',NULL,NULL,NULL,1,'2026-10-08 17:38:27',NULL,NULL,NULL),(2609252000000002301,'盘点确认',2609252000000000614,5,'',NULL,NULL,'N','N','F','0','0','warehouse:stocktake:confirm','#',NULL,NULL,NULL,1,'2026-10-08 17:38:27',NULL,NULL,NULL),(2609252000000002302,'完成盘点',2609252000000000614,6,'',NULL,NULL,'N','N','F','0','0','warehouse:stocktake:complete','#',NULL,NULL,NULL,1,'2026-10-08 17:38:27',NULL,NULL,NULL),(2609252000000002303,'取消盘点',2609252000000000614,7,'',NULL,NULL,'N','N','F','0','0','warehouse:stocktake:cancel','#',NULL,NULL,NULL,1,'2026-10-08 17:38:27',NULL,NULL,NULL),(2609252000000002304,'盘点导出',2609252000000000614,8,'',NULL,NULL,'N','N','F','0','0','warehouse:stocktake:export','#',NULL,NULL,NULL,1,'2026-10-08 17:38:27',NULL,NULL,NULL);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (102113167622012935,2609251200000000011),(102113167622012935,2609251200000000015),(102113167622012935,2609251200000000016),(102113167622012935,2609251200000000017),(102113167622012935,2609252000000000002),(102113167622012935,2609252000000000003),(102113167622012935,2609252000000000004),(102113167622012935,2609252000000000201),(102113167622012935,2609252000000000301),(102113167622012935,2609252000000000302),(102113167622012935,2609252000000000303),(102113167622012935,2609252000000000401),(102113167622012935,2609252000000000402),(102113167622012935,2609252000000001101),(102113167622012935,2609252000000001301),(102113167622012935,2609252000000001302),(102113167622012935,2609252000000001303),(102113167622012935,2609252000000001304),(102113167622012935,2609252000000001305),(102113167622012935,2609252000000001401),(102113167622012935,2609252000000001402),(102113167622012935,2609252000000001403),(102113167622012935,2609252000000001404),(102113167622012935,2609252000000001405),(102113167622012935,2609252000000001501),(102113167622012935,2609252000000001502),(102113167622012935,2609252000000001503),(102113167622012935,2609252000000001504),(102113167622012935,2609252000000001505),(102113167622012935,2609252000000001601),(102113167622012935,2609252000000001602),(102113167622012935,2609252000000001603),(102113167622012935,2609252000000001604),(102113167622012935,2609252000000001605),(102113167622012935,2609252000000001701),(102113167622012935,2609252000000001702),(102113167622012935,2609252000000001703),(102113167622012935,2609252000000001704),(102113167622012935,2609252000000001705),(102113167622012935,2609252000000001706),(102113167622012935,2609252000000001707),(102113167622012936,2609252000000000004),(102113167622012936,2609252000000000005),(102113167622012936,2609252000000000401),(102113167622012936,2609252000000000402),(102113167622012936,2609252000000000503),(102113167622012936,2609252000000001601),(102113167622012936,2609252000000001701),(102113167622012936,2609252000000001702),(102113167622012936,2609252000000001703),(102113167622012936,2609252000000001706),(102113167622012936,2609252000000001707),(102113167622012936,2609252000000001831),(102113167622012937,2609252000000000005),(102113167622012937,2609252000000000503),(102113167622012937,2609252000000000504),(102113167622012937,2609252000000001831),(102113167622012937,2609252000000001841),(102113167622012937,2609252000000001842),(102113167622012937,2609252000000001843),(102113167622012938,2609252000000000005),(102113167622012938,2609252000000000501),(102113167622012938,2609252000000000502),(102113167622012938,2609252000000000503),(102113167622012938,2609252000000000504),(102113167622012938,2609252000000001801),(102113167622012938,2609252000000001802),(102113167622012938,2609252000000001803),(102113167622012938,2609252000000001804),(102113167622012938,2609252000000001805),(102113167622012938,2609252000000001811),(102113167622012938,2609252000000001812),(102113167622012938,2609252000000001813),(102113167622012938,2609252000000001814),(102113167622012938,2609252000000001815),(102113167622012938,2609252000000001821),(102113167622012938,2609252000000001822),(102113167622012938,2609252000000001823),(102113167622012938,2609252000000001824),(102113167622012938,2609252000000001825),(102113167622012938,2609252000000001831),(102113167622012938,2609252000000001832),(102113167622012938,2609252000000001833),(102113167622012938,2609252000000001834),(102113167622012938,2609252000000001841),(102113167622012938,2609252000000001842),(102113167622012938,2609252000000001843),(102113167622012938,2609252000000001844),(102113167622012938,2609252000000001845),(102113167622012939,2609252000000000006),(102113167622012939,2609252000000000608),(102113167622012939,2609252000000000609),(102113167622012939,2609252000000000610),(102113167622012939,2609252000000000611),(102113167622012939,2609252000000000612),(102113167622012939,2609252000000002261),(102113167622012939,2609252000000002262),(102113167622012939,2609252000000002271),(102113167622012939,2609252000000002272),(102113167622012939,2609252000000002273),(102113167622012939,2609252000000002274),(102113167622012939,2609252000000002275),(102113167622012939,2609252000000002276),(102113167622012939,2609252000000002279),(102113167622012939,2609252000000002280),(102113167622012939,2609252000000002281),(102113167622012939,2609252000000002282),(102113167622012939,2609252000000002283),(102113167622012939,2609252000000002284),(102113167622012939,2609252000000002287),(102113167622012939,2609252000000002288),(102113167622012940,2609252000000000006),(102113167622012940,2609252000000000601),(102113167622012940,2609252000000000602),(102113167622012940,2609252000000000603),(102113167622012940,2609252000000000604),(102113167622012940,2609252000000000605),(102113167622012940,2609252000000000606),(102113167622012940,2609252000000000607),(102113167622012940,2609252000000000608),(102113167622012940,2609252000000000609),(102113167622012940,2609252000000000610),(102113167622012940,2609252000000000611),(102113167622012940,2609252000000000612),(102113167622012940,2609252000000000613),(102113167622012940,2609252000000000614),(102113167622012940,2609252000000002201),(102113167622012940,2609252000000002202),(102113167622012940,2609252000000002203),(102113167622012940,2609252000000002204),(102113167622012940,2609252000000002211),(102113167622012940,2609252000000002212),(102113167622012940,2609252000000002213),(102113167622012940,2609252000000002214),(102113167622012940,2609252000000002221),(102113167622012940,2609252000000002222),(102113167622012940,2609252000000002223),(102113167622012940,2609252000000002224),(102113167622012940,2609252000000002231),(102113167622012940,2609252000000002232),(102113167622012940,2609252000000002233),(102113167622012940,2609252000000002234),(102113167622012940,2609252000000002241),(102113167622012940,2609252000000002242),(102113167622012940,2609252000000002243),(102113167622012940,2609252000000002244),(102113167622012940,2609252000000002251),(102113167622012940,2609252000000002252),(102113167622012940,2609252000000002253),(102113167622012940,2609252000000002254),(102113167622012940,2609252000000002261),(102113167622012940,2609252000000002262),(102113167622012940,2609252000000002271),(102113167622012940,2609252000000002272),(102113167622012940,2609252000000002273),(102113167622012940,2609252000000002274),(102113167622012940,2609252000000002275),(102113167622012940,2609252000000002276),(102113167622012940,2609252000000002277),(102113167622012940,2609252000000002278),(102113167622012940,2609252000000002279),(102113167622012940,2609252000000002280),(102113167622012940,2609252000000002281),(102113167622012940,2609252000000002282),(102113167622012940,2609252000000002283),(102113167622012940,2609252000000002284),(102113167622012940,2609252000000002285),(102113167622012940,2609252000000002286),(102113167622012940,2609252000000002287),(102113167622012940,2609252000000002288),(102113167622012940,2609252000000002289),(102113167622012940,2609252000000002290),(102113167622012940,2609252000000002291),(102113167622012940,2609252000000002292),(102113167622012940,2609252000000002293),(102113167622012940,2609252000000002294),(102113167622012940,2609252000000002295),(102113167622012940,2609252000000002296),(102113167622012940,2609252000000002297),(102113167622012940,2609252000000002298),(102113167622012940,2609252000000002299),(102113167622012940,2609252000000002300),(102113167622012940,2609252000000002301),(102113167622012940,2609252000000002302),(102113167622012940,2609252000000002303),(102113167622012940,2609252000000002304),(102113167622012941,2609251200000000011),(102113167622012941,2609251200000000015),(102113167622012941,2609251200000000016),(102113167622012941,2609251200000000017),(102113167622012941,2609252000000000002),(102113167622012941,2609252000000000003),(102113167622012941,2609252000000000004),(102113167622012941,2609252000000000005),(102113167622012941,2609252000000000201),(102113167622012941,2609252000000000202),(102113167622012941,2609252000000000301),(102113167622012941,2609252000000000302),(102113167622012941,2609252000000000303),(102113167622012941,2609252000000000401),(102113167622012941,2609252000000000402),(102113167622012941,2609252000000000503),(102113167622012941,2609252000000000504),(102113167622012941,2609252000000001101),(102113167622012941,2609252000000001201),(102113167622012941,2609252000000001202),(102113167622012941,2609252000000001203),(102113167622012941,2609252000000001204),(102113167622012941,2609252000000001205),(102113167622012941,2609252000000001301),(102113167622012941,2609252000000001302),(102113167622012941,2609252000000001303),(102113167622012941,2609252000000001304),(102113167622012941,2609252000000001305),(102113167622012941,2609252000000001401),(102113167622012941,2609252000000001402),(102113167622012941,2609252000000001403),(102113167622012941,2609252000000001404),(102113167622012941,2609252000000001405),(102113167622012941,2609252000000001501),(102113167622012941,2609252000000001502),(102113167622012941,2609252000000001503),(102113167622012941,2609252000000001504),(102113167622012941,2609252000000001505),(102113167622012941,2609252000000001601),(102113167622012941,2609252000000001602),(102113167622012941,2609252000000001603),(102113167622012941,2609252000000001604),(102113167622012941,2609252000000001605),(102113167622012941,2609252000000001701),(102113167622012941,2609252000000001702),(102113167622012941,2609252000000001703),(102113167622012941,2609252000000001704),(102113167622012941,2609252000000001705),(102113167622012941,2609252000000001706),(102113167622012941,2609252000000001707),(102113167622012941,2609252000000001831),(102113167622012941,2609252000000001841);
INSERT INTO `sys_client` (`id`, `client_id`, `client_key`, `client_secret`, `grant_type`, `device_type`, `access_path`, `ip_whitelist`, `active_timeout`, `timeout`, `status`, `del_flag`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1762000000000000001,'e5cd7e4891bf95d1d19206ce24a7b32e','pc','pc123','password,social','pc',NULL,NULL,1800,604800,'0','0',1761000000000000103,1761100000000000001,'2026-10-01 15:54:28',1761100000000000001,'2026-10-01 15:54:28'),(1762000000000000002,'428a8310cd442757ae699df5d894f051','app','app123','password,sms,social','android','/app/**',NULL,1800,604800,'0','0',1761000000000000103,1761100000000000001,'2026-10-01 15:54:28',1761100000000000001,'2026-10-01 15:54:28');
SET FOREIGN_KEY_CHECKS = 1;