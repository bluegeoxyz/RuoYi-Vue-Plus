/*
 * TASK-016A：收费项目基础表、业务字典与菜单权限。
 * 仅定义收费项目主数据，不包含计费规则、账单、收款或核销字段。
 */
SET NAMES utf8mb4;

-- ============================================================
-- 1. Property Finance - Charge Item
-- ============================================================

CREATE TABLE `pm_charge_item`
(
    `id`                 BIGINT       NOT NULL COMMENT '收费项目ID',
    `project_id`         BIGINT       NOT NULL COMMENT '所属物业项目ID',

    `item_code`          VARCHAR(64)  NOT NULL COMMENT '收费项目编码',
    `item_name`          VARCHAR(128) NOT NULL COMMENT '收费项目名称',

    `charge_type`        VARCHAR(32)  NOT NULL COMMENT '收费类型',
    `charge_category`    VARCHAR(32)  NOT NULL COMMENT '收费分类',
    `charge_object_type` VARCHAR(32)  NOT NULL COMMENT '默认收费对象类型',

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
        CHECK (`charge_object_type` IN ('ROOM', 'PARKING_SPACE', 'OWNER', 'HOUSEHOLD'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业收费项目表';

-- ============================================================
-- 2. Property Finance Dictionaries
-- ============================================================

INSERT INTO `sys_dict_type`
(`dict_id`, `dict_name`, `dict_type`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609251000000000036, '物业-收费类型',     'pm_charge_type',        NULL, 1, NOW(), NULL, NULL, 'TASK-016A 收费项目'),
(2609251000000000037, '物业-收费分类',     'pm_charge_category',    NULL, 1, NOW(), NULL, NULL, 'TASK-016A 收费项目'),
(2609251000000000038, '物业-默认收费对象', 'pm_charge_object_type', NULL, 1, NOW(), NULL, NULL, 'TASK-016A 收费项目')
ON DUPLICATE KEY UPDATE
    `dict_name` = VALUES(`dict_name`),
    `remark`    = VALUES(`remark`);

INSERT INTO `sys_dict_data`
(`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
-- pm_charge_type
(2609251100000000160, 1, '周期性', 'PERIODIC',     'pm_charge_type', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, '周期生成费用'),
(2609251100000000161, 2, '仪表类', 'METER',        'pm_charge_type', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, '按抄表用量生成费用'),
(2609251100000000162, 3, '押金类', 'DEPOSIT',      'pm_charge_type', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, '产生押金台账'),
(2609251100000000163, 4, '临时类', 'TEMPORARY',    'pm_charge_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, '临时单次费用'),
(2609251100000000164, 5, '预收类', 'PREPAYMENT',   'pm_charge_type', NULL, 'primary', 'N', NULL, 1, NOW(), NULL, NULL, '预存后抵扣正常应收'),
(2609251100000000165, 6, '储值类', 'STORED_VALUE', 'pm_charge_type', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, '充值余额直接消费'),
(2609251100000000166, 7, '订金类', 'RESERVATION',  'pm_charge_type', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, '订金或保证金'),

-- pm_charge_category
(2609251100000000170, 1, '物业费',   'PROPERTY_FEE',    'pm_charge_category', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000171, 2, '停车费',   'PARKING_FEE',     'pm_charge_category', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000172, 3, '水费',     'WATER_FEE',       'pm_charge_category', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000173, 4, '电费',     'ELECTRICITY_FEE', 'pm_charge_category', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000174, 5, '租金',     'RENT',            'pm_charge_category', NULL, 'danger',  'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000175, 6, '押金',     'DEPOSIT',         'pm_charge_category', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000176, 7, '服务费',   'SERVICE_FEE',     'pm_charge_category', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000177, 8, '其他',     'OTHER',           'pm_charge_category', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),

-- pm_charge_object_type
(2609251100000000180, 1, '房屋', 'ROOM',          'pm_charge_object_type', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000181, 2, '车位', 'PARKING_SPACE', 'pm_charge_object_type', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000182, 3, '业主', 'OWNER',         'pm_charge_object_type', NULL, 'danger',  'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000183, 4, '住户', 'HOUSEHOLD',     'pm_charge_object_type', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
    `dict_sort`  = VALUES(`dict_sort`),
    `dict_label` = VALUES(`dict_label`),
    `dict_value` = VALUES(`dict_value`),
    `dict_type`  = VALUES(`dict_type`),
    `css_class`  = VALUES(`css_class`),
    `list_class` = VALUES(`list_class`),
    `is_default` = VALUES(`is_default`),
    `remark`     = VALUES(`remark`);

-- ============================================================
-- 3. Charge Management Menu And Functional Permissions
-- ============================================================

INSERT INTO `sys_menu`
(`menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`,
 `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`,
 `perms`, `icon`, `active_menu`, `ext`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609252000000000005, 0, '收费管理', 50, 'charge', NULL,
 NULL, 'N', 'N', 'M', '0', '0', NULL, 'money', NULL, NULL,
 NULL, 1, NOW(), NULL, NULL, '物业收费业务域'),
(2609252000000000501, 2609252000000000005, '收费项目', 10, 'item', 'property/charge/item/index',
 NULL, 'N', 'Y', 'C', '0', '0', 'property:chargeItem:list', 'list', NULL, NULL,
 NULL, 1, NOW(), NULL, NULL, '项目级收费项目管理')
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `menu_name` = VALUES(`menu_name`),
    `order_num` = VALUES(`order_num`),
    `path`      = VALUES(`path`),
    `component` = VALUES(`component`),
    `perms`     = VALUES(`perms`),
    `icon`      = VALUES(`icon`),
    `remark`    = VALUES(`remark`);

INSERT INTO `sys_menu`
(`menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`,
 `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`,
 `perms`, `icon`, `active_menu`, `ext`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609252000000001801, 2609252000000000501, '收费项目查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeItem:query',  '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001802, 2609252000000000501, '收费项目新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeItem:add',    '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001803, 2609252000000000501, '收费项目修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeItem:edit',   '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001804, 2609252000000000501, '收费项目删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeItem:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001805, 2609252000000000501, '收费项目导出', 5, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeItem:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `menu_name` = VALUES(`menu_name`),
    `order_num` = VALUES(`order_num`),
    `perms`     = VALUES(`perms`),
    `remark`    = VALUES(`remark`);
