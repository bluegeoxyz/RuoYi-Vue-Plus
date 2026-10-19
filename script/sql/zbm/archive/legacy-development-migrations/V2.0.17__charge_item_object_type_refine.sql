/*
 * TASK-016：将 charge_object_type 收敛为收费载体类型。
 * 执行前若存在 OWNER/HOUSEHOLD 业务数据则主动失败，由业务先确定迁移策略。
 */
SET NAMES utf8mb4;

DELIMITER $$
CREATE PROCEDURE `check_pm_charge_item_object_type_refine`()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM `pm_charge_item`
        WHERE `charge_object_type` IN ('OWNER', 'HOUSEHOLD')
        LIMIT 1
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'pm_charge_item 存在 OWNER/HOUSEHOLD 数据，无法自动收敛收费载体类型';
    END IF;
END$$
DELIMITER ;

CALL `check_pm_charge_item_object_type_refine`();
DROP PROCEDURE `check_pm_charge_item_object_type_refine`;

ALTER TABLE `pm_charge_item`
    DROP CHECK `chk_pm_charge_item_object_type`,
    MODIFY COLUMN `charge_object_type` VARCHAR(32) NOT NULL COMMENT '收费载体类型',
    ADD CONSTRAINT `chk_pm_charge_item_object_type`
        CHECK (`charge_object_type` IN ('ROOM', 'PARKING_SPACE'));

UPDATE `sys_dict_type`
SET `dict_name` = '物业-收费载体类型',
    `remark` = 'TASK-016 收费载体类型'
WHERE `dict_type` = 'pm_charge_object_type';

DELETE FROM `sys_dict_data`
WHERE `dict_type` = 'pm_charge_object_type'
  AND `dict_value` IN ('OWNER', 'HOUSEHOLD');
