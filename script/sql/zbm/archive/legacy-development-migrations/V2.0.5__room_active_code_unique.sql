DELIMITER $$
CREATE PROCEDURE zbm_check_pm_room_active_code_duplicates()
BEGIN
 IF EXISTS (SELECT 1 FROM pm_room WHERE del_flag='0' GROUP BY project_id,room_code HAVING COUNT(*)>1) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='pm_room contains duplicate active (project_id, room_code) values';
 END IF;
END$$
DELIMITER ;
CALL zbm_check_pm_room_active_code_duplicates();
DROP PROCEDURE zbm_check_pm_room_active_code_duplicates;
ALTER TABLE pm_room
 DROP INDEX uk_pm_room_code,
 ADD COLUMN active_room_code VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN del_flag='0' THEN room_code ELSE NULL END) STORED,
 ADD UNIQUE KEY uk_pm_room_active_code(project_id,active_room_code);
