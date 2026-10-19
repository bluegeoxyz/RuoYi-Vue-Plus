-- TASK-012 B2-2: allow a terminal FAILED record when an authorized sensitive export
-- cannot be generated. No PII is stored in this audit table.
ALTER TABLE `pm_customer_sensitive_access_audit`
    DROP CHECK `ck_pm_customer_sensitive_decision`,
    ADD CONSTRAINT `ck_pm_customer_sensitive_decision`
        CHECK (`decision` IN ('ALLOW', 'DENY', 'FAILED'));
