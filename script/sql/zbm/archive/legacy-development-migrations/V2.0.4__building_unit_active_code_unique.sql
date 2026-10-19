-- Abort before schema changes when active unit codes are already duplicated.
DELIMITER $$
CREATE PROCEDURE zbm_check_pm_unit_active_code_duplicates()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pm_unit
        WHERE del_flag = '0'
        GROUP BY building_id, unit_code
        HAVING COUNT(*) > 1
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'pm_unit contains duplicate active (building_id, unit_code) values';
    END IF;
END$$
DELIMITER ;

CALL zbm_check_pm_unit_active_code_duplicates();
DROP PROCEDURE zbm_check_pm_unit_active_code_duplicates;

ALTER TABLE pm_unit
    ADD COLUMN active_unit_code VARCHAR(32)
        GENERATED ALWAYS AS (CASE WHEN del_flag = '0' THEN unit_code ELSE NULL END) STORED,
    ADD UNIQUE KEY uk_pm_unit_active_code (building_id, active_unit_code);
