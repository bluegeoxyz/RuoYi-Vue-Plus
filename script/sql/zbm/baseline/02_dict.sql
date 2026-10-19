/*
 * ============================================================
 * ZBM Property Management - Current Baseline
 * File    : 02_dict.sql
 * Version : V2.0.33
 * Purpose : Business dictionaries
 *
 * Assumption:
 *   RuoYi-Vue-Plus 6.X sys_dict_type / sys_dict_data tables already exist.
 * ============================================================
 */

SET NAMES utf8mb4;

-- Fixed baseline ID ranges
-- Dict Type: 2609251000000000001 ~
-- Dict Data: 2609251100000000001 ~

-- ============================================================
-- 1. Dictionary Types
-- ============================================================

INSERT INTO `sys_dict_type`
(`dict_id`, `dict_name`, `dict_type`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609251000000000001, 'HR-用工类型',       'hr_employment_type',   NULL, 1, NOW(), NULL, NULL, 'V2 Phase1'),
(2609251000000000002, 'HR-员工状态',       'hr_employment_status', NULL, 1, NOW(), NULL, NULL, 'V2 Phase1'),
(2609251000000000003, '通用-证件类型',      'zbm_id_type',          NULL, 1, NOW(), NULL, NULL, 'V2 Phase1'),

(2609251000000000010, '物业-项目类型',      'pm_project_type',      NULL, 1, NOW(), NULL, NULL, 'V2 Phase1'),
(2609251000000000011, '物业-项目成员角色',   'pm_member_role',       NULL, 1, NOW(), NULL, NULL, 'V2 Phase1'),
(2609251000000000012, '物业-项目成员状态',   'pm_member_status',     NULL, 1, NOW(), NULL, NULL, 'V2 Phase1'),

(2609251000000000020, '物业-楼栋类型',      'pm_building_type',     NULL, 1, NOW(), NULL, NULL, 'V2 Phase1'),
(2609251000000000021, '物业-房屋类型',      'pm_room_type',         NULL, 1, NOW(), NULL, NULL, 'V2 Phase1'),
(2609251000000000022, '物业-产权资源属性',   'pm_property_type',     NULL, 1, NOW(), NULL, NULL, 'V2 Phase1'),
(2609251000000000023, '物业-房屋交付状态',   'pm_delivery_status',   NULL, 1, NOW(), NULL, NULL, 'V2 Phase1'),
(2609251000000000024, '物业-房屋入住状态',   'pm_occupancy_status',  NULL, 1, NOW(), NULL, NULL, 'V2 Phase1'),

(2609251000000000030, '物业-客户类型',      'pm_customer_type',     NULL, 1, NOW(), NULL, NULL, 'V2 Phase1'),
(2609251000000000031, '物业-房屋客户关系',   'pm_relation_type',     NULL, 1, NOW(), NULL, NULL, 'V2 Phase1'),
(2609251000000000032, '物业-房屋关系状态',   'pm_relation_status',   NULL, 1, NOW(), NULL, NULL, 'Property Baseline'),
(2609251000000000033, '物业-客户生命周期',   'pm_customer_lifecycle', NULL, 1, NOW(), NULL, NULL, 'Property Baseline'),
(2609251000000000034, '物业-管理授权主体类型', 'pm_management_principal_type', NULL, 1, NOW(), NULL, NULL, 'Property Baseline'),
(2609251000000000035, '物业-管理授权范围类型', 'pm_management_scope_type', NULL, 1, NOW(), NULL, NULL, 'Property Baseline'),
(2609251000000000036, '物业-收费类型',       'pm_charge_type', NULL, 1, NOW(), NULL, NULL, 'Property Baseline'),
(2609251000000000037, '物业-收费分类',       'pm_charge_category', NULL, 1, NOW(), NULL, NULL, 'Property Baseline'),
(2609251000000000038, '物业-收费载体类型',   'pm_charge_object_type', NULL, 1, NOW(), NULL, NULL, 'Property Baseline'),
(2609251000000000039, '物业-计费方式',       'pm_billing_method', NULL, 1, NOW(), NULL, NULL, 'Property Baseline'),
(2609251000000000040, '物业-计费周期',       'pm_billing_cycle', NULL, 1, NOW(), NULL, NULL, 'Property Baseline'),
(2609251000000000041, '物业-周期模式',       'pm_cycle_mode', NULL, 1, NOW(), NULL, NULL, 'Property Baseline')
ON DUPLICATE KEY UPDATE
    `dict_name` = VALUES(`dict_name`),
    `remark` = VALUES(`remark`);


-- ============================================================
-- 2. Dictionary Data
-- Columns follow RuoYi-Vue-Plus 6.X SysDictData.
-- ============================================================

INSERT INTO `sys_dict_data`
(`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`,
 `css_class`, `list_class`, `is_default`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES

-- hr_employment_type
(2609251100000000001, 1, '正式员工', 'FORMAL',    'hr_employment_type', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000002, 2, '试用员工', 'PROBATION', 'hr_employment_type', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000003, 3, '合同人员', 'CONTRACT',  'hr_employment_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000004, 4, '外包人员', 'OUTSOURCE', 'hr_employment_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000005, 5, '实习人员', 'INTERN',    'hr_employment_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000006, 6, '临时人员', 'TEMPORARY', 'hr_employment_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000007, 7, '其他',     'OTHER',     'hr_employment_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- hr_employment_status
(2609251100000000010, 1, '待入职', 'PENDING',   'hr_employment_status', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000011, 2, '在职',   'ACTIVE',    'hr_employment_status', NULL, 'success', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000012, 3, '停职',   'SUSPENDED', 'hr_employment_status', NULL, 'danger',  'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000013, 4, '离职',   'LEFT',      'hr_employment_status', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- zbm_id_type
(2609251100000000020, 1, '居民身份证', 'ID_CARD',  'zbm_id_type', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000021, 2, '护照',       'PASSPORT', 'zbm_id_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000022, 3, '其他',       'OTHER',    'zbm_id_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_project_type
(2609251100000000030, 1, '住宅',   'RESIDENTIAL', 'pm_project_type', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000031, 2, '商业',   'COMMERCIAL',  'pm_project_type', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000032, 3, '写字楼', 'OFFICE',      'pm_project_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000033, 4, '园区',   'PARK',        'pm_project_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000034, 5, '产业园', 'INDUSTRIAL',  'pm_project_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000035, 6, '综合体', 'MIXED',       'pm_project_type', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000036, 7, '其他',   'OTHER',       'pm_project_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_member_role
(2609251100000000040, 1, '项目负责人', 'PROJECT_MANAGER',          'pm_member_role', NULL, 'danger',  'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000041, 2, '客服负责人', 'CUSTOMER_SERVICE_MANAGER', 'pm_member_role', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000042, 3, '工程负责人', 'ENGINEERING_MANAGER',      'pm_member_role', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000043, 4, '收费负责人', 'FEE_MANAGER',              'pm_member_role', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000044, 5, '普通成员',   'MEMBER',                   'pm_member_role', NULL, 'info',    'Y', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_member_status
(2609251100000000050, 1, '有效', 'ACTIVE',   'pm_member_status', NULL, 'success', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000051, 2, '已结束', 'INACTIVE','pm_member_status', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_building_type
(2609251100000000060, 1, '住宅',   'RESIDENTIAL', 'pm_building_type', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000061, 2, '商业',   'COMMERCIAL',  'pm_building_type', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000062, 3, '写字楼', 'OFFICE',      'pm_building_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000063, 4, '公共建筑','PUBLIC',     'pm_building_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000064, 5, '停车建筑','PARKING',    'pm_building_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000065, 6, '其他',   'OTHER',       'pm_building_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_room_type
(2609251100000000070, 1, '住宅',   'RESIDENCE', 'pm_room_type', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000071, 2, '商铺',   'SHOP',      'pm_room_type', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000072, 3, '办公室', 'OFFICE',    'pm_room_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000073, 4, '仓储',   'STORAGE',   'pm_room_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000074, 5, '其他',   'OTHER',     'pm_room_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_property_type
(2609251100000000080, 1, '私有产权', 'PRIVATE',      'pm_property_type', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000081, 2, '公共产权', 'PUBLIC',       'pm_property_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000082, 3, '商业产权', 'COMMERCIAL',   'pm_property_type', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000083, 4, '物业用房', 'PROPERTY_USE', 'pm_property_type', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000084, 5, '其他',     'OTHER',        'pm_property_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_delivery_status
(2609251100000000090, 1, '未交付', 'UNDELIVERED', 'pm_delivery_status', NULL, 'warning', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000091, 2, '已交付', 'DELIVERED',   'pm_delivery_status', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_occupancy_status
(2609251100000000100, 1, '空置', 'VACANT',   'pm_occupancy_status', NULL, 'info',    'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000101, 2, '已入住', 'OCCUPIED','pm_occupancy_status', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_customer_type
(2609251100000000110, 1, '个人', 'PERSON',  'pm_customer_type', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000111, 2, '企业', 'COMPANY', 'pm_customer_type', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_relation_type
(2609251100000000120, 1, '产权人/业主', 'OWNER',    'pm_relation_type', NULL, 'danger',  'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000121, 2, '租户',        'TENANT',   'pm_relation_type', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000122, 3, '家庭成员',    'FAMILY',   'pm_relation_type', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000123, 4, '实际居住人',  'RESIDENT', 'pm_relation_type', NULL, 'primary', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000124, 5, '联系人',      'CONTACT',  'pm_relation_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000125, 6, '其他',        'OTHER',    'pm_relation_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_relation_status
(2609251100000000130, 1, '待生效', 'PENDING', 'pm_relation_status', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000131, 2, '有效',   'ACTIVE',  'pm_relation_status', NULL, 'success', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000132, 3, '已结束', 'ENDED',   'pm_relation_status', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_customer_lifecycle / management grant
(2609251100000000140, 1, '正常', 'ACTIVE', 'pm_customer_lifecycle', NULL, 'success', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000141, 2, '停用', 'INACTIVE', 'pm_customer_lifecycle', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000142, 3, '注销', 'CANCELLED', 'pm_customer_lifecycle', NULL, 'danger', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000143, 4, '已合并', 'MERGED', 'pm_customer_lifecycle', NULL, 'info', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000150, 1, '用户', 'USER', 'pm_management_principal_type', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000151, 2, '角色', 'ROLE', 'pm_management_principal_type', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000152, 1, '全部项目', 'ALL', 'pm_management_scope_type', NULL, 'warning', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000153, 2, '指定项目', 'PROJECT', 'pm_management_scope_type', NULL, 'primary', 'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_charge_type
(2609251100000000160, 1, '周期性', 'PERIODIC', 'pm_charge_type', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, '周期生成费用'),
(2609251100000000161, 2, '仪表类', 'METER', 'pm_charge_type', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, '按抄表用量生成费用'),
(2609251100000000162, 3, '押金类', 'DEPOSIT', 'pm_charge_type', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, '产生押金台账'),
(2609251100000000163, 4, '临时类', 'TEMPORARY', 'pm_charge_type', NULL, 'info', 'N', NULL, 1, NOW(), NULL, NULL, '临时单次费用'),
(2609251100000000164, 5, '预收类', 'PREPAYMENT', 'pm_charge_type', NULL, 'primary', 'N', NULL, 1, NOW(), NULL, NULL, '预存后抵扣正常应收'),
(2609251100000000165, 6, '储值类', 'STORED_VALUE', 'pm_charge_type', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, '充值余额直接消费'),
(2609251100000000166, 7, '订金类', 'RESERVATION', 'pm_charge_type', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, '订金或保证金'),

-- pm_charge_category
(2609251100000000170, 1, '物业费', 'PROPERTY_FEE', 'pm_charge_category', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000171, 2, '停车费', 'PARKING_FEE', 'pm_charge_category', NULL, 'info', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000172, 3, '水费', 'WATER_FEE', 'pm_charge_category', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000173, 4, '电费', 'ELECTRICITY_FEE', 'pm_charge_category', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000174, 5, '租金', 'RENT', 'pm_charge_category', NULL, 'danger', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000175, 6, '押金', 'DEPOSIT', 'pm_charge_category', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000176, 7, '服务费', 'SERVICE_FEE', 'pm_charge_category', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000177, 8, '其他', 'OTHER', 'pm_charge_category', NULL, 'info', 'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_charge_object_type
(2609251100000000180, 1, '房屋', 'ROOM', 'pm_charge_object_type', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000181, 2, '车位', 'PARKING_SPACE', 'pm_charge_object_type', NULL, 'info', 'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_billing_method
(2609251100000000190, 1, '按面积', 'AREA', 'pm_billing_method', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000191, 2, '固定金额', 'FIXED', 'pm_billing_method', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_billing_cycle
(2609251100000000192, 1, '每月', 'MONTH', 'pm_billing_cycle', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000193, 2, '双月', 'BIMONTHLY', 'pm_billing_cycle', NULL, 'info', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000194, 3, '季度', 'QUARTER', 'pm_billing_cycle', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000195, 4, '半年', 'HALF_YEAR', 'pm_billing_cycle', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000196, 5, '每年', 'YEAR', 'pm_billing_cycle', NULL, 'danger', 'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_cycle_mode
(2609251100000000197, 1, '自然周期', 'NATURAL', 'pm_cycle_mode', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000198, 2, '循环周期', 'ROLLING', 'pm_cycle_mode', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL)

ON DUPLICATE KEY UPDATE
    `dict_sort`  = VALUES(`dict_sort`),
    `dict_label` = VALUES(`dict_label`),
    `dict_value` = VALUES(`dict_value`),
    `dict_type`  = VALUES(`dict_type`),
    `css_class`  = VALUES(`css_class`),
    `list_class` = VALUES(`list_class`),
    `is_default` = VALUES(`is_default`),
    `remark`     = VALUES(`remark`);

-- Receivable dictionaries. price_cycle reuses pm_billing_cycle.
INSERT INTO `sys_dict_type`
(`dict_id`, `dict_name`, `dict_type`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609251000000000042, '物业-到期日模式', 'pm_due_date_mode', NULL, 1, NOW(), NULL, NULL, 'Property Receivable'),
(2609251000000000043, '物业-应收批次状态', 'pm_receivable_batch_status', NULL, 1, NOW(), NULL, NULL, 'Property Receivable'),
(2609251000000000044, '物业-支付状态', 'pm_payment_status', NULL, 1, NOW(), NULL, NULL, 'Property Receivable')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`), `remark` = VALUES(`remark`);

INSERT INTO `sys_dict_data`
(`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609251100000000199, 1, '周期开始日前', 'BEFORE_PERIOD_START', 'pm_due_date_mode', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000200, 2, '周期开始日后', 'AFTER_PERIOD_START', 'pm_due_date_mode', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000201, 1, '执行中', 'RUNNING', 'pm_receivable_batch_status', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000202, 2, '成功', 'SUCCESS', 'pm_receivable_batch_status', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000203, 3, '部分成功', 'PARTIAL_SUCCESS', 'pm_receivable_batch_status', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000204, 4, '失败', 'FAILED', 'pm_receivable_batch_status', NULL, 'danger', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000205, 1, '未支付', 'UNPAID', 'pm_payment_status', NULL, 'danger', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000206, 2, '部分支付', 'PARTIAL', 'pm_payment_status', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000207, 3, '已支付', 'PAID', 'pm_payment_status', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000208, 4, '已作废', 'CANCELLED', 'pm_payment_status', NULL, 'info', 'N', NULL, 1, NOW(), NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
    `dict_sort` = VALUES(`dict_sort`), `dict_label` = VALUES(`dict_label`), `dict_value` = VALUES(`dict_value`),
    `dict_type` = VALUES(`dict_type`), `css_class` = VALUES(`css_class`), `list_class` = VALUES(`list_class`),
    `is_default` = VALUES(`is_default`), `remark` = VALUES(`remark`);

INSERT INTO `sys_dict_type` (`dict_id`,`dict_name`,`dict_type`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
(2609251000000000054,'仓库-出库单状态','wm_outbound_status',NULL,1,NOW(),NULL,NULL,'Warehouse Phase 1'),(2609251000000000055,'仓库-出库类型','wm_outbound_type',NULL,1,NOW(),NULL,NULL,'Warehouse Phase 1') ON DUPLICATE KEY UPDATE `dict_name`=VALUES(`dict_name`);
INSERT INTO `sys_dict_data` (`dict_code`,`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
(2609251100000000244,1,'草稿','DRAFT','wm_outbound_status',NULL,'info','Y',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000245,2,'已确认','CONFIRMED','wm_outbound_status',NULL,'warning','N',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000246,3,'已完成','COMPLETED','wm_outbound_status',NULL,'success','N',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000247,4,'已取消','CANCELLED','wm_outbound_status',NULL,'danger','N',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000248,1,'领料出库','MATERIAL_USE','wm_outbound_type',NULL,'primary','Y',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000249,2,'其他出库','OTHER','wm_outbound_type',NULL,'info','N',NULL,1,NOW(),NULL,NULL,NULL) ON DUPLICATE KEY UPDATE `dict_label`=VALUES(`dict_label`),`list_class`=VALUES(`list_class`);

INSERT INTO `sys_dict_type` (`dict_id`,`dict_name`,`dict_type`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
(2609251000000000052,'仓库-入库单状态','wm_inbound_status',NULL,1,NOW(),NULL,NULL,'Warehouse Phase 1'),
(2609251000000000053,'仓库-入库类型','wm_inbound_type',NULL,1,NOW(),NULL,NULL,'Warehouse Phase 1')
ON DUPLICATE KEY UPDATE `dict_name`=VALUES(`dict_name`),`remark`=VALUES(`remark`);
INSERT INTO `sys_dict_data` (`dict_code`,`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
(2609251100000000238,1,'草稿','DRAFT','wm_inbound_status',NULL,'info','Y',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000239,2,'已确认','CONFIRMED','wm_inbound_status',NULL,'warning','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000240,3,'已完成','COMPLETED','wm_inbound_status',NULL,'success','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000241,4,'已取消','CANCELLED','wm_inbound_status',NULL,'danger','N',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000242,1,'采购入库','PURCHASE','wm_inbound_type',NULL,'primary','Y',NULL,1,NOW(),NULL,NULL,NULL),
(2609251100000000243,2,'其他入库','OTHER','wm_inbound_type',NULL,'info','N',NULL,1,NOW(),NULL,NULL,NULL)
ON DUPLICATE KEY UPDATE `dict_label`=VALUES(`dict_label`),`dict_value`=VALUES(`dict_value`),`list_class`=VALUES(`list_class`);

-- Warehouse scope.
INSERT INTO `sys_dict_type`
(`dict_id`, `dict_name`, `dict_type`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609251000000000050, '仓库-归属类型', 'wm_warehouse_scope', NULL, 1, NOW(), NULL, NULL, 'Warehouse Phase 1')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`), `remark` = VALUES(`remark`);

INSERT INTO `sys_dict_data`
(`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609251100000000230, 1, '公司/组织仓', 'COMPANY', 'wm_warehouse_scope', NULL, 'primary', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000231, 2, '项目仓', 'PROJECT', 'wm_warehouse_scope', NULL, 'success', 'Y', NULL, 1, NOW(), NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
    `dict_sort` = VALUES(`dict_sort`), `dict_label` = VALUES(`dict_label`), `dict_value` = VALUES(`dict_value`),
    `dict_type` = VALUES(`dict_type`), `css_class` = VALUES(`css_class`), `list_class` = VALUES(`list_class`),
    `is_default` = VALUES(`is_default`), `remark` = VALUES(`remark`);

-- Warehouse inventory transaction type.
INSERT INTO `sys_dict_type`
(`dict_id`, `dict_name`, `dict_type`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609251000000000051, '仓库-库存流水类型', 'wm_inventory_transaction_type', NULL, 1, NOW(), NULL, NULL, 'Warehouse Phase 1')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`), `remark` = VALUES(`remark`);

INSERT INTO `sys_dict_data`
(`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609251100000000232, 1, '入库', 'INBOUND', 'wm_inventory_transaction_type', NULL, 'success', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000233, 2, '出库', 'OUTBOUND', 'wm_inventory_transaction_type', NULL, 'danger', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000234, 3, '调拨入', 'TRANSFER_IN', 'wm_inventory_transaction_type', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000235, 4, '调拨出', 'TRANSFER_OUT', 'wm_inventory_transaction_type', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000236, 5, '盘盈', 'STOCKTAKE_GAIN', 'wm_inventory_transaction_type', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000237, 6, '盘亏', 'STOCKTAKE_LOSS', 'wm_inventory_transaction_type', NULL, 'danger', 'N', NULL, 1, NOW(), NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE `dict_sort` = VALUES(`dict_sort`), `dict_label` = VALUES(`dict_label`),
    `dict_value` = VALUES(`dict_value`), `dict_type` = VALUES(`dict_type`), `list_class` = VALUES(`list_class`);

-- Payment dictionaries.
INSERT INTO `sys_dict_type`
(`dict_id`, `dict_name`, `dict_type`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609251000000000045, '物业-收款方式', 'pm_payment_method', NULL, 1, NOW(), NULL, NULL, 'Property Payment'),
(2609251000000000046, '物业-收款记录状态', 'pm_payment_record_status', NULL, 1, NOW(), NULL, NULL, 'Property Payment')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`), `remark` = VALUES(`remark`);

INSERT INTO `sys_dict_data`
(`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609251100000000209, 1, '现金', 'CASH', 'pm_payment_method', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000210, 2, '微信', 'WECHAT', 'pm_payment_method', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000211, 3, '支付宝', 'ALIPAY', 'pm_payment_method', NULL, 'primary', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000212, 4, '银行转账', 'BANK_TRANSFER', 'pm_payment_method', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000213, 5, 'POS', 'POS', 'pm_payment_method', NULL, 'info', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000214, 6, '其他', 'OTHER', 'pm_payment_method', NULL, 'info', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000215, 1, '有效', 'VALID', 'pm_payment_record_status', NULL, 'success', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000216, 2, '已冲销', 'REVERSED', 'pm_payment_record_status', NULL, 'danger', 'N', NULL, 1, NOW(), NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
    `dict_sort` = VALUES(`dict_sort`), `dict_label` = VALUES(`dict_label`), `dict_value` = VALUES(`dict_value`),
    `dict_type` = VALUES(`dict_type`), `css_class` = VALUES(`css_class`), `list_class` = VALUES(`list_class`),
    `is_default` = VALUES(`is_default`), `remark` = VALUES(`remark`);
INSERT INTO `sys_dict_type` (`dict_id`,`dict_name`,`dict_type`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES (2609251000000000056,'仓库-调拨单状态','wm_transfer_status',NULL,1,NOW(),NULL,NULL,'Warehouse Phase 1'),(2609251000000000057,'仓库-调拨类型','wm_transfer_type',NULL,1,NOW(),NULL,NULL,'Warehouse Phase 1') ON DUPLICATE KEY UPDATE `dict_name`=VALUES(`dict_name`);
INSERT INTO `sys_dict_data` (`dict_code`,`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES (2609251100000000250,1,'草稿','DRAFT','wm_transfer_status',NULL,'info','Y',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000251,2,'已确认','CONFIRMED','wm_transfer_status',NULL,'warning','N',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000252,3,'已完成','COMPLETED','wm_transfer_status',NULL,'success','N',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000253,4,'已取消','CANCELLED','wm_transfer_status',NULL,'danger','N',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000254,1,'普通调拨','NORMAL','wm_transfer_type',NULL,'primary','Y',NULL,1,NOW(),NULL,NULL,NULL) ON DUPLICATE KEY UPDATE `dict_label`=VALUES(`dict_label`),`list_class`=VALUES(`list_class`);
INSERT INTO `sys_dict_type` (`dict_id`,`dict_name`,`dict_type`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES (2609251000000000058,'仓库-盘点单状态','wm_stocktake_status',NULL,1,NOW(),NULL,NULL,'Warehouse Phase 1'),(2609251000000000059,'仓库-盘点类型','wm_stocktake_type',NULL,1,NOW(),NULL,NULL,'Warehouse Phase 1') ON DUPLICATE KEY UPDATE `dict_name`=VALUES(`dict_name`);
INSERT INTO `sys_dict_data` (`dict_code`,`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES (2609251100000000255,1,'草稿','DRAFT','wm_stocktake_status',NULL,'info','Y',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000256,2,'已确认','CONFIRMED','wm_stocktake_status',NULL,'warning','N',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000257,3,'已完成','COMPLETED','wm_stocktake_status',NULL,'success','N',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000258,4,'已取消','CANCELLED','wm_stocktake_status',NULL,'danger','N',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000259,1,'整仓盘点','FULL','wm_stocktake_type',NULL,'primary','Y',NULL,1,NOW(),NULL,NULL,NULL),(2609251100000000260,2,'部分盘点','PARTIAL','wm_stocktake_type',NULL,'info','N',NULL,1,NOW(),NULL,NULL,NULL) ON DUPLICATE KEY UPDATE `dict_label`=VALUES(`dict_label`),`list_class`=VALUES(`list_class`);
