/* Phase 1 Property 字典补齐：数据库保存英文 code，业务端展示中文 label。 */
SET NAMES utf8mb4;

INSERT INTO `sys_dict_type`
(`dict_id`, `dict_name`, `dict_type`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609251000000000033, '物业-客户生命周期', 'pm_customer_lifecycle', NULL, 1, NOW(), NULL, NULL, 'Phase 1 Property 中文展示'),
(2609251000000000034, '物业-管理授权主体类型', 'pm_management_principal_type', NULL, 1, NOW(), NULL, NULL, 'Phase 1 Property 中文展示'),
(2609251000000000035, '物业-管理授权范围类型', 'pm_management_scope_type', NULL, 1, NOW(), NULL, NULL, 'Phase 1 Property 中文展示')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`), `remark` = VALUES(`remark`);

INSERT INTO `sys_dict_data`
(`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`,
 `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2609251100000000140, 1, '正常',   'ACTIVE',    'pm_customer_lifecycle', NULL, 'success', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000141, 2, '停用',   'INACTIVE',  'pm_customer_lifecycle', NULL, 'warning', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000142, 3, '注销',   'CANCELLED', 'pm_customer_lifecycle', NULL, 'danger',  'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000143, 4, '已合并', 'MERGED',    'pm_customer_lifecycle', NULL, 'info',    'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000150, 1, '用户',   'USER',      'pm_management_principal_type', NULL, 'primary', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000151, 2, '角色',   'ROLE',      'pm_management_principal_type', NULL, 'success', 'N', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000152, 1, '全部项目','ALL',       'pm_management_scope_type', NULL, 'warning', 'Y', NULL, 1, NOW(), NULL, NULL, NULL),
(2609251100000000153, 2, '指定项目','PROJECT',   'pm_management_scope_type', NULL, 'primary', 'N', NULL, 1, NOW(), NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
    `dict_sort` = VALUES(`dict_sort`), `dict_label` = VALUES(`dict_label`), `dict_value` = VALUES(`dict_value`),
    `dict_type` = VALUES(`dict_type`), `list_class` = VALUES(`list_class`), `is_default` = VALUES(`is_default`);
