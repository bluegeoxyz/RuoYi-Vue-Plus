/*
 * TASK-AUTH-010B development-only seed.
 *
 * Never add this file to baseline/99_init.sql or the production migration chain.
 * Required session variables (set by the local execution wrapper):
 *   @auth010b_allow_development_seed = 'AUTH010B_LOCAL_DEV'
 *   @auth010b_password_hash          = BCrypt hash; plaintext is never stored here
 */

SET NAMES utf8mb4;

DROP TEMPORARY TABLE IF EXISTS `tmp_auth010b_guard`;
CREATE TEMPORARY TABLE `tmp_auth010b_guard` (`guard_id` int NOT NULL PRIMARY KEY) ENGINE=InnoDB;
INSERT INTO `tmp_auth010b_guard` (`guard_id`) VALUES (1), (2), (3), (4), (5);

-- Explicit opt-in and password hash are mandatory.
INSERT INTO `tmp_auth010b_guard` (`guard_id`)
SELECT 1
WHERE DATABASE() <> 'zbm-data'
   OR COALESCE(@auth010b_allow_development_seed, '') <> 'AUTH010B_LOCAL_DEV';

INSERT INTO `tmp_auth010b_guard` (`guard_id`)
SELECT 2
WHERE @auth010b_password_hash IS NULL
   OR CHAR_LENGTH(@auth010b_password_hash) <> 60
   OR @auth010b_password_hash NOT LIKE '$2%';

-- Seven standard roles must be unique and retain the V2.0.34 permission counts.
INSERT INTO `tmp_auth010b_guard` (`guard_id`)
SELECT 3
FROM (
    SELECT expected.`role_key`, expected.`permission_count`,
           COUNT(DISTINCT role.`role_id`) AS `role_count`,
           COUNT(DISTINCT CASE WHEN menu.`perms` IS NOT NULL AND menu.`perms` <> '' THEN menu.`perms` END) AS `actual_count`
    FROM (
        SELECT 'property_customer_service' AS `role_key`, 6 AS `permission_count`
        UNION ALL SELECT 'property_operation_manager', 38
        UNION ALL SELECT 'property_cashier', 6
        UNION ALL SELECT 'property_finance_manager', 28
        UNION ALL SELECT 'property_warehouse_keeper', 20
        UNION ALL SELECT 'property_warehouse_manager', 72
        UNION ALL SELECT 'property_project_manager', 48
    ) expected
    LEFT JOIN `sys_role` role ON role.`role_key` = expected.`role_key` AND role.`del_flag` = '0'
    LEFT JOIN `sys_role_menu` role_menu ON role_menu.`role_id` = role.`role_id`
    LEFT JOIN `sys_menu` menu ON menu.`menu_id` = role_menu.`menu_id`
    GROUP BY expected.`role_key`, expected.`permission_count`
    HAVING `role_count` <> 1 OR `actual_count` <> expected.`permission_count`
) invalid_role
LIMIT 1;

-- Fail on username/employee/project namespace collisions outside this seed.
INSERT INTO `tmp_auth010b_guard` (`guard_id`)
SELECT 4
WHERE EXISTS (
    SELECT 1 FROM `sys_user`
    WHERE `user_name` IN (
        'test_customer', 'test_operation', 'test_cashier', 'test_finance', 'test_warehouse',
        'test_wh_manager', 'test_pm_a', 'test_pm_b', 'test_multi_role', 'test_no_project'
    )
      AND (`user_id` NOT BETWEEN 2199011000000002001 AND 2199011000000002010
           OR COALESCE(`remark`, '') <> 'TEST-AUTH-010B')
)
OR EXISTS (
    SELECT 1 FROM `hr_employee`
    WHERE `employee_no` LIKE 'TEST-AUTH-%'
      AND (`id` NOT BETWEEN 2199011000000003001 AND 2199011000000003010
           OR COALESCE(`remark`, '') <> 'TEST-AUTH-010B')
)
OR EXISTS (
    SELECT 1 FROM `pm_project`
    WHERE `project_code` IN ('TEST_AUTH_A', 'TEST_AUTH_B')
      AND (`id` NOT IN (2199011000000000101, 2199011000000000102)
           OR COALESCE(`remark`, '') <> 'TEST-AUTH-010B')
);

-- Fail if any reserved ID already belongs to unrelated data.
INSERT INTO `tmp_auth010b_guard` (`guard_id`)
SELECT 5
WHERE EXISTS (SELECT 1 FROM `sys_dept` WHERE `dept_id` BETWEEN 2199011000000000001 AND 2199011000000000003 AND `dept_name` NOT LIKE 'TEST-%')
   OR EXISTS (SELECT 1 FROM `sys_post` WHERE `post_id` = 2199011000000000010 AND `post_code` <> 'TEST_PROPERTY_STAFF')
   OR EXISTS (SELECT 1 FROM `sys_user` WHERE `user_id` BETWEEN 2199011000000002001 AND 2199011000000002010 AND `user_name` NOT LIKE 'test_%')
   OR EXISTS (SELECT 1 FROM `hr_employee` WHERE `id` BETWEEN 2199011000000003001 AND 2199011000000003010 AND `employee_no` NOT LIKE 'TEST-AUTH-%')
   OR EXISTS (SELECT 1 FROM `pm_project` WHERE `id` IN (2199011000000000101, 2199011000000000102) AND `project_code` NOT LIKE 'TEST_AUTH_%')
   OR EXISTS (SELECT 1 FROM `wm_warehouse` WHERE `id` BETWEEN 2199011000000001201 AND 2199011000000001203 AND `warehouse_code` NOT LIKE 'TEST-%');

START TRANSACTION;

INSERT IGNORE INTO `sys_dept`
(`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `status`, `del_flag`, `create_by`, `create_time`)
VALUES
(2199011000000000001, 1761000000000000100, '0,1761000000000000100', 'TEST-物业测试公司', 900, '0', '0', 1, NOW()),
(2199011000000000002, 2199011000000000001, '0,1761000000000000100,2199011000000000001', 'TEST-A项目组', 1, '0', '0', 1, NOW()),
(2199011000000000003, 2199011000000000001, '0,1761000000000000100,2199011000000000001', 'TEST-B项目组', 2, '0', '0', 1, NOW());

INSERT IGNORE INTO `sys_post`
(`post_id`, `dept_id`, `post_code`, `post_name`, `post_sort`, `status`, `del_flag`, `create_dept`, `create_by`, `create_time`, `remark`)
VALUES
(2199011000000000010, 2199011000000000001, 'TEST_PROPERTY_STAFF', 'TEST-物业业务测试岗', 900, '0', '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B');

INSERT IGNORE INTO `pm_project`
(`id`, `project_code`, `project_name`, `project_short_name`, `project_type`, `address`, `sort`, `status`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000000101, 'TEST_AUTH_A', 'TEST-A物业项目', 'TEST-A', 'RESIDENTIAL', 'TEST-A地址', 900, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000000102, 'TEST_AUTH_B', 'TEST-B物业项目', 'TEST-B', 'RESIDENTIAL', 'TEST-B地址', 901, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `pm_building`
(`id`, `project_id`, `building_code`, `building_name`, `building_type`, `above_ground_floors`, `sort`, `status`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000000201, 2199011000000000101, 'TEST-A-B01', 'TEST-A一号楼', 'RESIDENTIAL', 10, 1, '0', 2199011000000000002, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000000202, 2199011000000000102, 'TEST-B-B01', 'TEST-B一号楼', 'RESIDENTIAL', 10, 1, '0', 2199011000000000003, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `pm_unit`
(`id`, `project_id`, `building_id`, `unit_code`, `unit_name`, `floor_count`, `sort`, `status`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000000301, 2199011000000000101, 2199011000000000201, 'TEST-A-U01', '一单元', 10, 1, '0', 2199011000000000002, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000000302, 2199011000000000102, 2199011000000000202, 'TEST-B-U01', '一单元', 10, 1, '0', 2199011000000000003, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `pm_room`
(`id`, `project_id`, `building_id`, `unit_id`, `room_code`, `room_no`, `floor_no`, `room_type`, `property_type`, `building_area`, `usable_area`, `delivery_status`, `occupancy_status`, `sort`, `status`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000000401, 2199011000000000101, 2199011000000000201, 2199011000000000301, 'TEST-A-R101', '101', 1, 'RESIDENTIAL', 'PRIVATE', 100.00, 80.00, 'DELIVERED', 'OCCUPIED', 1, '0', 2199011000000000002, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000000402, 2199011000000000102, 2199011000000000202, 2199011000000000302, 'TEST-B-R101', '101', 1, 'RESIDENTIAL', 'PRIVATE', 100.00, 80.00, 'DELIVERED', 'OCCUPIED', 1, '0', 2199011000000000003, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `pm_customer`
(`id`, `customer_no`, `customer_type`, `customer_name`, `status`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`, `lifecycle_status`, `version`)
VALUES
(2199011000000000501, 'TEST-A-CUST-001', 'PERSON', 'TEST-A客户', '0', 2199011000000000002, 1, NOW(), 'TEST-AUTH-010B', '0', 'ACTIVE', 0),
(2199011000000000502, 'TEST-B-CUST-001', 'PERSON', 'TEST-B客户', '0', 2199011000000000003, 1, NOW(), 'TEST-AUTH-010B', '0', 'ACTIVE', 0);

INSERT IGNORE INTO `pm_room_customer_rel`
(`id`, `project_id`, `room_id`, `customer_id`, `relation_type`, `is_primary`, `start_date`, `move_in_date`, `relation_status`, `status`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000000601, 2199011000000000101, 2199011000000000401, 2199011000000000501, 'OWNER', '1', '2026-01-01', '2026-01-01', 'ACTIVE', '0', 2199011000000000002, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000000602, 2199011000000000102, 2199011000000000402, 2199011000000000502, 'OWNER', '1', '2026-01-01', '2026-01-01', 'ACTIVE', '0', 2199011000000000003, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `pm_charge_item`
(`id`, `project_id`, `item_code`, `item_name`, `charge_type`, `charge_category`, `charge_object_type`, `status`, `sort`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000000701, 2199011000000000101, 'TEST-A-PROPERTY', 'TEST-A物业费', 'PERIODIC', 'PROPERTY_FEE', 'ROOM', '0', 1, 2199011000000000002, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000000702, 2199011000000000102, 'TEST-B-PROPERTY', 'TEST-B物业费', 'PERIODIC', 'PROPERTY_FEE', 'ROOM', '0', 1, 2199011000000000003, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `pm_charge_rule`
(`id`, `project_id`, `charge_item_id`, `rule_code`, `rule_name`, `billing_method`, `billing_cycle`, `price_cycle`, `cycle_mode`, `due_date_mode`, `due_date_offset_days`, `fixed_amount`, `effective_start_date`, `status`, `sort`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000000801, 2199011000000000101, 2199011000000000701, 'TEST-A-RULE', 'TEST-A固定月费', 'FIXED', 'MONTH', 'MONTH', 'NATURAL', 'AFTER_PERIOD_START', 5, 100.00, '2026-01-01', '0', 1, 2199011000000000002, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000000802, 2199011000000000102, 2199011000000000702, 'TEST-B-RULE', 'TEST-B固定月费', 'FIXED', 'MONTH', 'MONTH', 'NATURAL', 'AFTER_PERIOD_START', 5, 120.00, '2026-01-01', '0', 1, 2199011000000000003, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `pm_charge_binding`
(`id`, `project_id`, `object_type`, `object_id`, `charge_item_id`, `charge_rule_id`, `effective_start_date`, `status`, `sort`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000000901, 2199011000000000101, 'ROOM', 2199011000000000401, 2199011000000000701, 2199011000000000801, '2026-01-01', '0', 1, 2199011000000000002, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000000902, 2199011000000000102, 'ROOM', 2199011000000000402, 2199011000000000702, 2199011000000000802, '2026-01-01', '0', 1, 2199011000000000003, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `pm_receivable_batch`
(`id`, `project_id`, `batch_no`, `generate_month`, `status`, `scanned_count`, `generated_count`, `started_at`, `finished_at`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000001001, 2199011000000000101, 'TEST-A-BATCH-202610', '2026-10-01', 'SUCCESS', 1, 1, NOW(), NOW(), 2199011000000000002, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000001002, 2199011000000000102, 'TEST-B-BATCH-202610', '2026-10-01', 'SUCCESS', 1, 1, NOW(), NOW(), 2199011000000000003, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `pm_receivable`
(`id`, `project_id`, `receivable_no`, `batch_id`, `binding_id`, `room_id`, `charge_item_id`, `charge_rule_id`, `generate_month`, `period_start_date`, `period_end_date`, `theoretical_start_date`, `theoretical_end_date`, `due_date`, `responsible_customer_id`, `responsible_customer_name_snapshot`, `responsible_customer_type_snapshot`, `room_full_name_snapshot`, `charge_item_code_snapshot`, `charge_item_name_snapshot`, `charge_rule_code_snapshot`, `charge_rule_name_snapshot`, `billing_method_snapshot`, `billing_cycle_snapshot`, `price_cycle_snapshot`, `cycle_mode_snapshot`, `due_date_mode_snapshot`, `due_date_offset_days_snapshot`, `fixed_amount_snapshot`, `theoretical_days`, `actual_days`, `receivable_amount`, `paid_amount`, `payment_status`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000001101, 2199011000000000101, 'TEST-A-REC-202610', 2199011000000001001, 2199011000000000901, 2199011000000000401, 2199011000000000701, 2199011000000000801, '2026-10-01', '2026-10-01', '2026-10-31', '2026-10-01', '2026-10-31', '2026-10-06', 2199011000000000501, 'TEST-A客户', 'PERSON', 'TEST-A一号楼/一单元/101', 'TEST-A-PROPERTY', 'TEST-A物业费', 'TEST-A-RULE', 'TEST-A固定月费', 'FIXED', 'MONTH', 'MONTH', 'NATURAL', 'AFTER_PERIOD_START', 5, 100.00, 31, 31, 100.00, 0.00, 'UNPAID', 2199011000000000002, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000001102, 2199011000000000102, 'TEST-B-REC-202610', 2199011000000001002, 2199011000000000902, 2199011000000000402, 2199011000000000702, 2199011000000000802, '2026-10-01', '2026-10-01', '2026-10-31', '2026-10-01', '2026-10-31', '2026-10-06', 2199011000000000502, 'TEST-B客户', 'PERSON', 'TEST-B一号楼/一单元/101', 'TEST-B-PROPERTY', 'TEST-B物业费', 'TEST-B-RULE', 'TEST-B固定月费', 'FIXED', 'MONTH', 'MONTH', 'NATURAL', 'AFTER_PERIOD_START', 5, 120.00, 31, 31, 120.00, 0.00, 'UNPAID', 2199011000000000003, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `sys_user`
(`user_id`, `dept_id`, `user_name`, `nick_name`, `user_type`, `email`, `phone_number`, `gender`, `password`, `status`, `del_flag`, `create_dept`, `create_by`, `create_time`, `remark`)
VALUES
(2199011000000002001, 2199011000000000002, 'test_customer', 'TEST-物业客服', 'sys_user', '', '', '2', @auth010b_password_hash, '0', '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B'),
(2199011000000002002, 2199011000000000002, 'test_operation', 'TEST-物业运营主管', 'sys_user', '', '', '2', @auth010b_password_hash, '0', '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B'),
(2199011000000002003, 2199011000000000002, 'test_cashier', 'TEST-收费员', 'sys_user', '', '', '2', @auth010b_password_hash, '0', '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B'),
(2199011000000002004, 2199011000000000001, 'test_finance', 'TEST-财务主管', 'sys_user', '', '', '2', @auth010b_password_hash, '0', '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B'),
(2199011000000002005, 2199011000000000002, 'test_warehouse', 'TEST-仓库管理员', 'sys_user', '', '', '2', @auth010b_password_hash, '0', '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B'),
(2199011000000002006, 2199011000000000001, 'test_wh_manager', 'TEST-仓库主管', 'sys_user', '', '', '2', @auth010b_password_hash, '0', '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B'),
(2199011000000002007, 2199011000000000002, 'test_pm_a', 'TEST-A项目经理', 'sys_user', '', '', '2', @auth010b_password_hash, '0', '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B'),
(2199011000000002008, 2199011000000000003, 'test_pm_b', 'TEST-B项目经理', 'sys_user', '', '', '2', @auth010b_password_hash, '0', '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B'),
(2199011000000002009, 2199011000000000002, 'test_multi_role', 'TEST-客服收费复合角色', 'sys_user', '', '', '2', @auth010b_password_hash, '0', '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B'),
(2199011000000002010, 2199011000000000002, 'test_no_project', 'TEST-无项目客服', 'sys_user', '', '', '2', @auth010b_password_hash, '0', '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B');

INSERT IGNORE INTO `hr_employee`
(`id`, `employee_no`, `employee_name`, `dept_id`, `post_id`, `user_id`, `entry_date`, `employment_type`, `employment_status`, `sort`, `status`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000003001, 'TEST-AUTH-001', 'TEST-物业客服', 2199011000000000002, 2199011000000000010, 2199011000000002001, '2026-10-09', 'FORMAL', 'ACTIVE', 1, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000003002, 'TEST-AUTH-002', 'TEST-物业运营主管', 2199011000000000002, 2199011000000000010, 2199011000000002002, '2026-10-09', 'FORMAL', 'ACTIVE', 2, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000003003, 'TEST-AUTH-003', 'TEST-收费员', 2199011000000000002, 2199011000000000010, 2199011000000002003, '2026-10-09', 'FORMAL', 'ACTIVE', 3, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000003004, 'TEST-AUTH-004', 'TEST-财务主管', 2199011000000000001, 2199011000000000010, 2199011000000002004, '2026-10-09', 'FORMAL', 'ACTIVE', 4, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000003005, 'TEST-AUTH-005', 'TEST-仓库管理员', 2199011000000000002, 2199011000000000010, 2199011000000002005, '2026-10-09', 'FORMAL', 'ACTIVE', 5, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000003006, 'TEST-AUTH-006', 'TEST-仓库主管', 2199011000000000001, 2199011000000000010, 2199011000000002006, '2026-10-09', 'FORMAL', 'ACTIVE', 6, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000003007, 'TEST-AUTH-007', 'TEST-A项目经理', 2199011000000000002, 2199011000000000010, 2199011000000002007, '2026-10-09', 'FORMAL', 'ACTIVE', 7, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000003008, 'TEST-AUTH-008', 'TEST-B项目经理', 2199011000000000003, 2199011000000000010, 2199011000000002008, '2026-10-09', 'FORMAL', 'ACTIVE', 8, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000003009, 'TEST-AUTH-009', 'TEST-客服收费复合角色', 2199011000000000002, 2199011000000000010, 2199011000000002009, '2026-10-09', 'FORMAL', 'ACTIVE', 9, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000003010, 'TEST-AUTH-010', 'TEST-无项目客服', 2199011000000000002, 2199011000000000010, 2199011000000002010, '2026-10-09', 'FORMAL', 'ACTIVE', 10, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `sys_user_post` (`user_id`, `post_id`)
SELECT `user_id`, 2199011000000000010 FROM `sys_user`
WHERE `user_id` BETWEEN 2199011000000002001 AND 2199011000000002010;

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT 2199011000000002001, `role_id` FROM `sys_role` WHERE `role_key` = 'property_customer_service' AND `del_flag` = '0'
UNION ALL SELECT 2199011000000002002, `role_id` FROM `sys_role` WHERE `role_key` = 'property_operation_manager' AND `del_flag` = '0'
UNION ALL SELECT 2199011000000002003, `role_id` FROM `sys_role` WHERE `role_key` = 'property_cashier' AND `del_flag` = '0'
UNION ALL SELECT 2199011000000002004, `role_id` FROM `sys_role` WHERE `role_key` = 'property_finance_manager' AND `del_flag` = '0'
UNION ALL SELECT 2199011000000002005, `role_id` FROM `sys_role` WHERE `role_key` = 'property_warehouse_keeper' AND `del_flag` = '0'
UNION ALL SELECT 2199011000000002006, `role_id` FROM `sys_role` WHERE `role_key` = 'property_warehouse_manager' AND `del_flag` = '0'
UNION ALL SELECT 2199011000000002007, `role_id` FROM `sys_role` WHERE `role_key` = 'property_project_manager' AND `del_flag` = '0'
UNION ALL SELECT 2199011000000002008, `role_id` FROM `sys_role` WHERE `role_key` = 'property_project_manager' AND `del_flag` = '0'
UNION ALL SELECT 2199011000000002009, `role_id` FROM `sys_role` WHERE `role_key` = 'property_customer_service' AND `del_flag` = '0'
UNION ALL SELECT 2199011000000002009, `role_id` FROM `sys_role` WHERE `role_key` = 'property_cashier' AND `del_flag` = '0'
UNION ALL SELECT 2199011000000002010, `role_id` FROM `sys_role` WHERE `role_key` = 'property_customer_service' AND `del_flag` = '0';

INSERT IGNORE INTO `pm_project_member`
(`id`, `project_id`, `employee_id`, `member_role`, `is_manager`, `start_date`, `member_status`, `sort`, `status`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000004001, 2199011000000000101, 2199011000000003001, 'MEMBER', '0', '2026-10-09', 'ACTIVE', 1, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000004002, 2199011000000000101, 2199011000000003002, 'MEMBER', '0', '2026-10-09', 'ACTIVE', 2, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000004003, 2199011000000000101, 2199011000000003003, 'FEE_MANAGER', '0', '2026-10-09', 'ACTIVE', 3, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000004004, 2199011000000000101, 2199011000000003004, 'FEE_MANAGER', '0', '2026-10-09', 'ACTIVE', 4, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000004005, 2199011000000000102, 2199011000000003004, 'FEE_MANAGER', '0', '2026-10-09', 'ACTIVE', 5, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000004006, 2199011000000000101, 2199011000000003005, 'MEMBER', '0', '2026-10-09', 'ACTIVE', 6, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000004007, 2199011000000000101, 2199011000000003006, 'MEMBER', '0', '2026-10-09', 'ACTIVE', 7, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000004008, 2199011000000000102, 2199011000000003006, 'MEMBER', '0', '2026-10-09', 'ACTIVE', 8, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000004009, 2199011000000000101, 2199011000000003007, 'PROJECT_MANAGER', '1', '2026-10-09', 'ACTIVE', 9, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000004010, 2199011000000000102, 2199011000000003008, 'PROJECT_MANAGER', '1', '2026-10-09', 'ACTIVE', 10, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000004011, 2199011000000000101, 2199011000000003009, 'MEMBER', '0', '2026-10-09', 'ACTIVE', 11, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0');

UPDATE `pm_project` SET `manager_employee_id` = 2199011000000003007
WHERE `id` = 2199011000000000101 AND `remark` = 'TEST-AUTH-010B';
UPDATE `pm_project` SET `manager_employee_id` = 2199011000000003008
WHERE `id` = 2199011000000000102 AND `remark` = 'TEST-AUTH-010B';

INSERT IGNORE INTO `pm_project_management_grant`
(`id`, `principal_type`, `principal_id`, `scope_type`, `project_id`, `status`, `version`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000005001, 'USER', 2199011000000002007, 'PROJECT', 2199011000000000101, '0', 0, 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000005002, 'USER', 2199011000000002008, 'PROJECT', 2199011000000000102, '0', 0, 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `wm_warehouse`
(`id`, `company_id`, `project_id`, `warehouse_scope`, `warehouse_code`, `warehouse_name`, `manager_id`, `status`, `sort`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000001201, 2199011000000000001, 2199011000000000101, 'PROJECT', 'TEST-A-WH', 'TEST-A项目仓', 2199011000000002005, '0', 1, 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000001202, 2199011000000000001, 2199011000000000102, 'PROJECT', 'TEST-B-WH', 'TEST-B项目仓', 2199011000000002006, '0', 2, 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000001203, 2199011000000000001, NULL, 'COMPANY', 'TEST-COMPANY-WH', 'TEST公司仓', 2199011000000002006, '0', 3, 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `wm_warehouse_zone`
(`id`, `warehouse_id`, `zone_code`, `zone_name`, `status`, `sort`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000001301, 2199011000000001201, 'TEST-A-Z01', 'TEST-A默认库区', '0', 1, 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000001302, 2199011000000001202, 'TEST-B-Z01', 'TEST-B默认库区', '0', 1, 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000001303, 2199011000000001203, 'TEST-C-Z01', 'TEST公司默认库区', '0', 1, 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `wm_warehouse_location`
(`id`, `warehouse_id`, `zone_id`, `location_code`, `location_name`, `status`, `sort`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000001401, 2199011000000001201, 2199011000000001301, 'TEST-A-L01', 'TEST-A默认库位', '0', 1, 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000001402, 2199011000000001202, 2199011000000001302, 'TEST-B-L01', 'TEST-B默认库位', '0', 1, 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0'),
(2199011000000001403, 2199011000000001203, 2199011000000001303, 'TEST-C-L01', 'TEST公司默认库位', '0', 1, 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `wm_material_category`
(`id`, `company_id`, `parent_id`, `ancestors`, `category_code`, `category_name`, `sort`, `status`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000001501, 2199011000000000001, 0, '0', 'TEST-MATERIAL', 'TEST物料分类', 1, '0', 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `wm_unit`
(`id`, `unit_code`, `unit_name`, `symbol`, `precision_scale`, `status`, `sort`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000001601, 'TEST-PCS', 'TEST件', '件', 0, '0', 1, 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `wm_material`
(`id`, `company_id`, `material_code`, `material_name`, `category_id`, `base_unit_id`, `specification`, `safety_stock`, `status`, `sort`, `create_dept`, `create_by`, `create_time`, `remark`, `del_flag`)
VALUES
(2199011000000001701, 2199011000000000001, 'TEST-CLEAN-001', 'TEST清洁耗材', 2199011000000001501, 2199011000000001601, 'AUTH-010B', 5.000000, '0', 1, 2199011000000000001, 1, NOW(), 'TEST-AUTH-010B', '0');

INSERT IGNORE INTO `wm_inventory`
(`id`, `warehouse_id`, `location_id`, `material_id`, `quantity`, `locked_quantity`, `version`, `create_dept`, `create_by`, `create_time`)
VALUES
(2199011000000001801, 2199011000000001201, 2199011000000001401, 2199011000000001701, 100.000000, 0.000000, 0, 2199011000000000001, 1, NOW()),
(2199011000000001802, 2199011000000001202, 2199011000000001402, 2199011000000001701, 200.000000, 0.000000, 0, 2199011000000000001, 1, NOW());

COMMIT;

DROP TEMPORARY TABLE IF EXISTS `tmp_auth010b_guard`;

SELECT `user_id`, `user_name`, `nick_name`, `dept_id`, `status`
FROM `sys_user`
WHERE `user_id` BETWEEN 2199011000000002001 AND 2199011000000002010
ORDER BY `user_id`;
