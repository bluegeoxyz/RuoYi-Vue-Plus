/* TASK-017A：周期性计费规则表、业务字典与菜单权限。 */
SET NAMES utf8mb4;

-- ROLLING 周期按收费载体起算日滚动；目标月份无对应日时取月末。
-- 具体收费载体起算日属于后续绑定模型，不存入本表。
CREATE TABLE `pm_charge_rule`
(
    `id`                   BIGINT        NOT NULL COMMENT '计费规则ID',
    `project_id`           BIGINT        NOT NULL COMMENT '所属物业项目ID',
    `charge_item_id`       BIGINT        NOT NULL COMMENT '收费项目ID（仅允许PERIODIC类型）',
    `rule_code`            VARCHAR(64)   NOT NULL COMMENT '计费规则编码',
    `rule_name`            VARCHAR(128)  NOT NULL COMMENT '计费规则名称',
    `billing_method`       VARCHAR(16)   NOT NULL COMMENT '计费方式（AREA按面积 FIXED固定金额）',
    `billing_cycle`        VARCHAR(16)   NOT NULL COMMENT '计费周期',
    `cycle_mode`           VARCHAR(16)   NOT NULL COMMENT '周期模式（NATURAL自然周期 ROLLING循环周期）',
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
    CONSTRAINT `chk_pm_charge_rule_method` CHECK (`billing_method` IN ('AREA', 'FIXED')),
    CONSTRAINT `chk_pm_charge_rule_cycle` CHECK (`billing_cycle` IN ('MONTH', 'BIMONTHLY', 'QUARTER', 'HALF_YEAR', 'YEAR')),
    CONSTRAINT `chk_pm_charge_rule_cycle_mode` CHECK (`cycle_mode` IN ('NATURAL', 'ROLLING')),
    CONSTRAINT `chk_pm_charge_rule_amount`
        CHECK ((`billing_method` = 'AREA' AND `unit_price` IS NOT NULL AND `unit_price` >= 0 AND `fixed_amount` IS NULL)
            OR (`billing_method` = 'FIXED' AND `fixed_amount` IS NOT NULL AND `fixed_amount` >= 0 AND `unit_price` IS NULL)),
    CONSTRAINT `chk_pm_charge_rule_effective_date`
        CHECK (`effective_end_date` IS NULL OR `effective_end_date` >= `effective_start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业周期性计费规则表';

INSERT INTO `sys_dict_type`
(`dict_id`, `dict_name`, `dict_type`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609251000000000039, '物业-计费方式', 'pm_billing_method', NULL, 1, NOW(), NULL, NULL, 'Property Baseline'),
(2609251000000000040, '物业-计费周期', 'pm_billing_cycle', NULL, 1, NOW(), NULL, NULL, 'Property Baseline'),
(2609251000000000041, '物业-周期模式', 'pm_cycle_mode', NULL, 1, NOW(), NULL, NULL, 'Property Baseline')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`), `remark` = VALUES(`remark`);

INSERT INTO `sys_dict_data`
(`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609251100000000190, 1, '按面积', 'AREA', 'pm_billing_method', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000191, 2, '固定金额', 'FIXED', 'pm_billing_method', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000192, 1, '每月', 'MONTH', 'pm_billing_cycle', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000193, 2, '双月', 'BIMONTHLY', 'pm_billing_cycle', NULL, 'info', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000194, 3, '季度', 'QUARTER', 'pm_billing_cycle', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000195, 4, '半年', 'HALF_YEAR', 'pm_billing_cycle', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000196, 5, '每年', 'YEAR', 'pm_billing_cycle', NULL, 'danger', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000197, 1, '自然周期', 'NATURAL', 'pm_cycle_mode', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000198, 2, '循环周期', 'ROLLING', 'pm_cycle_mode', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
    `dict_sort` = VALUES(`dict_sort`), `dict_label` = VALUES(`dict_label`), `dict_value` = VALUES(`dict_value`),
    `dict_type` = VALUES(`dict_type`), `css_class` = VALUES(`css_class`), `list_class` = VALUES(`list_class`),
    `is_default` = VALUES(`is_default`), `remark` = VALUES(`remark`);

INSERT INTO `sys_menu`
(`menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`,
 `menu_type`, `visible`, `status`, `perms`, `icon`, `active_menu`, `ext`, `create_dept`, `create_by`, `create_time`,
 `update_by`, `update_time`, `remark`)
VALUES
(2609252000000000502, 2609252000000000005, '计费规则', 20, 'rule', 'property/charge/rule/index', NULL, 'N', 'Y', 'C', '0', '0', 'property:chargeRule:list', 'list', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '项目级周期性计费规则管理'),
(2609252000000001811, 2609252000000000502, '计费规则查询', 1, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeRule:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001812, 2609252000000000502, '计费规则新增', 2, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeRule:add', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001813, 2609252000000000502, '计费规则修改', 3, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeRule:edit', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001814, 2609252000000000502, '计费规则删除', 4, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeRule:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL),
(2609252000000001815, 2609252000000000502, '计费规则导出', 5, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeRule:export', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`), `menu_name` = VALUES(`menu_name`), `order_num` = VALUES(`order_num`),
    `path` = VALUES(`path`), `component` = VALUES(`component`), `perms` = VALUES(`perms`),
    `icon` = VALUES(`icon`), `remark` = VALUES(`remark`);
