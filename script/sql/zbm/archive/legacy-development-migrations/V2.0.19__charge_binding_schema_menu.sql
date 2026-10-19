/* TASK-018A：收费载体绑定表与房屋收费设置权限。 */
SET NAMES utf8mb4;

-- 第一阶段 Service 仅允许 ROOM；数据库预留 PARKING_SPACE。
-- 日期区间按闭区间处理，完整重叠校验由 TASK-018B Service 在事务内完成。
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
    CONSTRAINT `chk_pm_charge_binding_object_type` CHECK (`object_type` IN ('ROOM', 'PARKING_SPACE')),
    CONSTRAINT `chk_pm_charge_binding_status` CHECK (`status` IN ('0', '1')),
    CONSTRAINT `chk_pm_charge_binding_effective_date`
        CHECK (`effective_end_date` IS NULL OR `effective_end_date` >= `effective_start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业收费载体绑定表';

INSERT INTO `sys_menu`
(`menu_id`, `parent_id`, `menu_name`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`,
 `menu_type`, `visible`, `status`, `perms`, `icon`, `active_menu`, `ext`, `create_dept`, `create_by`, `create_time`,
 `update_by`, `update_time`, `remark`)
VALUES
(2609252000000001821, 2609252000000000303, '房屋收费设置列表', 21, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeBinding:list', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '房屋详情内收费设置列表'),
(2609252000000001822, 2609252000000000303, '房屋收费设置查询', 22, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeBinding:query', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '房屋收费设置详情'),
(2609252000000001823, 2609252000000000303, '房屋收费设置新增', 23, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeBinding:add', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '新增房屋收费设置'),
(2609252000000001824, 2609252000000000303, '房屋收费设置修改', 24, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeBinding:edit', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '终止、启停及维护房屋收费设置'),
(2609252000000001825, 2609252000000000303, '房屋收费设置删除', 25, '', NULL, NULL, 'N', 'N', 'F', '0', '0', 'property:chargeBinding:remove', '#', NULL, NULL, NULL, 1, NOW(), NULL, NULL, '删除未产生应收的房屋收费设置')
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`), `menu_name` = VALUES(`menu_name`), `order_num` = VALUES(`order_num`),
    `perms` = VALUES(`perms`), `icon` = VALUES(`icon`), `remark` = VALUES(`remark`);
