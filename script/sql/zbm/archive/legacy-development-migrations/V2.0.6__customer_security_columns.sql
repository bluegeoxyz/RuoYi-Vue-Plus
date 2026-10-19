-- TASK-012 Phase B1-1: Customer encryption metadata and active identity uniqueness.
-- This migration never contains encryption or HMAC keys.
-- Run in a maintenance window. Existing identity data must be backfilled by the application
-- immediately after this DDL and verified before Customer writes are enabled.

DELIMITER $$
CREATE PROCEDURE zbm_check_pm_customer_identity_duplicates()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pm_customer
        WHERE del_flag = '0'
          AND id_type IS NOT NULL AND TRIM(id_type) <> ''
          AND id_no IS NOT NULL AND TRIM(id_no) <> ''
          AND id_no NOT LIKE 'ENC\\_%' ESCAPE '\\'
        GROUP BY UPPER(TRIM(id_type)),
                 UPPER(REGEXP_REPLACE(TRIM(id_no), '[[:space:]]+', ''))
        HAVING COUNT(*) > 1
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'pm_customer contains duplicate active plaintext identities';
    END IF;
END$$
DELIMITER ;

CALL zbm_check_pm_customer_identity_duplicates();
DROP PROCEDURE zbm_check_pm_customer_identity_duplicates;

ALTER TABLE pm_customer
    MODIFY COLUMN id_no VARCHAR(512) DEFAULT NULL COMMENT '证件号码密文（ENC_ 前缀）',
    ADD COLUMN identity_fingerprint BINARY(32) DEFAULT NULL COMMENT '规范化证件身份 HMAC-SHA256',
    ADD COLUMN active_identity_key BINARY(32)
        GENERATED ALWAYS AS (
            CASE WHEN del_flag = '0' AND identity_fingerprint IS NOT NULL
                 THEN identity_fingerprint ELSE NULL END
        ) STORED COMMENT '有效身份唯一键',
    ADD COLUMN pii_key_version VARCHAR(32) DEFAULT NULL COMMENT 'PII 字段加密密钥版本',
    ADD COLUMN lifecycle_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '生命周期状态',
    ADD COLUMN merged_into_customer_id BIGINT DEFAULT NULL COMMENT '合并后的主客户ID',
    ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    ADD CONSTRAINT chk_pm_customer_lifecycle_status
        CHECK (lifecycle_status IN ('ACTIVE', 'INACTIVE', 'CANCELLED', 'MERGED')),
    ADD UNIQUE KEY uk_pm_customer_active_identity (id_type, active_identity_key);

UPDATE pm_customer
SET lifecycle_status = CASE WHEN status = '0' THEN 'ACTIVE' ELSE 'INACTIVE' END
WHERE lifecycle_status = 'ACTIVE';
