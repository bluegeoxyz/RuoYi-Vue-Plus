DELIMITER $$
CREATE PROCEDURE zbm_check_pm_room_customer_rel_effective_duplicates()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pm_room_customer_rel
        WHERE del_flag = '0'
          AND status = '0'
          AND relation_status IN ('PENDING', 'ACTIVE')
        GROUP BY room_id, customer_id, relation_type
        HAVING COUNT(*) > 1
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'pm_room_customer_rel contains duplicate effective room/customer/type relations';
    END IF;
END$$
DELIMITER ;

CALL zbm_check_pm_room_customer_rel_effective_duplicates();
DROP PROCEDURE zbm_check_pm_room_customer_rel_effective_duplicates;

ALTER TABLE pm_room_customer_rel
    ADD COLUMN effective_relation_key TINYINT
        GENERATED ALWAYS AS (
            CASE
                WHEN del_flag = '0' AND status = '0' AND relation_status IN ('PENDING', 'ACTIVE') THEN 1
                ELSE NULL
            END
        ) STORED,
    ADD UNIQUE KEY uk_pm_room_customer_rel_effective
        (room_id, customer_id, relation_type, effective_relation_key),
    ADD CONSTRAINT chk_pm_room_customer_rel_type
        CHECK (relation_type IN ('OWNER', 'TENANT', 'FAMILY', 'RESIDENT', 'CONTACT', 'OTHER')),
    ADD CONSTRAINT chk_pm_room_customer_rel_status
        CHECK (relation_status IN ('PENDING', 'ACTIVE', 'ENDED')),
    ADD CONSTRAINT chk_pm_room_customer_rel_primary
        CHECK (is_primary IN ('0', '1')),
    ADD CONSTRAINT chk_pm_room_customer_rel_dates
        CHECK (end_date IS NULL OR end_date >= start_date),
    ADD CONSTRAINT chk_pm_room_customer_rel_move_dates
        CHECK (move_out_date IS NULL OR move_in_date IS NULL OR move_out_date >= move_in_date);
